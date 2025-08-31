package com.xun.orchestrator.module;

import com.xun.sdk.annotation.AiFunction;
import com.xun.sdk.annotation.AiModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.*;
import org.osgi.framework.launch.Framework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.module
 * @Author: xun
 * @CreateTime: 2025-08-23  21:12
 * @Description: TODO 加载模块服务
 * @Version: 1.0
 */
@Service
@Slf4j
public class ModuleLoader {
    private BundleContext context;
    private final ModuleRegistry registry;

    @Autowired
    public ModuleLoader(ModuleRegistry registry) throws Exception {
        this.registry = registry;
        initOSGi();
    }


    private void initOSGi() throws Exception {
        Map<String, String> config = new HashMap<>();

        String cacheDir = "osgi-cache-" + UUID.randomUUID().toString().substring(0, 8);
        config.put(Constants.FRAMEWORK_STORAGE, cacheDir);
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "onFirstInit");

        FrameworkFactory factory = new FrameworkFactory();
        Framework framework = factory.newFramework(config);
        framework.init();

        // === 关键：注册监听器，等待 STARTED 事件 ===
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<FrameworkEvent> eventRef = new AtomicReference<>();

        framework.getBundleContext().addFrameworkListener(frameworkEvent -> {
            if (frameworkEvent.getType() == FrameworkEvent.STARTED ||
                    frameworkEvent.getType() == FrameworkEvent.ERROR) {
                eventRef.set(frameworkEvent);
                latch.countDown();
            }
        });

        // 启动框架（异步）
        framework.start();

        // 等待事件，最多 10 秒
        boolean finished = latch.await(10, TimeUnit.SECONDS);
        if (!finished) {
            throw new TimeoutException("OSGi Framework failed to start within 10 seconds");
        }

        FrameworkEvent event = eventRef.get();
        if (event.getType() == FrameworkEvent.ERROR) {
            throw new Exception("OSGi Framework start failed", event.getThrowable());
        }

        // 此时框架已完全启动
        this.context = framework.getBundleContext();
        if (this.context == null) {
            throw new IllegalStateException("BundleContext is null after framework start");
        }

        log.info("OSGi Framework started successfully.");
    }

    public void loadModule(File jarFile) throws Exception {
        Bundle bundle = context.installBundle(jarFile.toURI().toString());
        bundle.start();

        // 等待激活
        long start = System.currentTimeMillis();
        while (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.RESOLVED) {
            Thread.sleep(10);
            if (System.currentTimeMillis() - start > 5000) {
                throw new TimeoutException("Bundle failed to start: " + bundle.getLocation());
            }
        }

        ScanResult result = scanBundleForAnnotations(bundle);
        Class<?> entryClass = result.moduleClass;

        if (entryClass == null) {
            bundle.uninstall();
            throw new IllegalArgumentException("No @AiModule class found in " + jarFile.getName());
        }

        Object instance = newInstance(entryClass);
        if (instance == null) {
            bundle.uninstall();
            throw new IllegalArgumentException("Cannot instantiate @AiModule class: " + entryClass.getName());
        }

        AiModule moduleAnno = entryClass.getAnnotation(AiModule.class);
        ModuleInstance moduleInstance = new ModuleInstance(moduleAnno.id(), bundle, instance);
        registry.registerModule(moduleInstance);

        // 注册所有 @AiFunction（使用已扫描的类列表）
        for (Class<?> clazz : result.functionCandidateClasses) {
            Object funcInstance = newInstance(clazz);
            if (funcInstance == null) continue;

            for (Method method : clazz.getDeclaredMethods()) {
                AiFunction funcAnno = method.getAnnotation(AiFunction.class);
                if (funcAnno != null && Modifier.isPublic(method.getModifiers())) {
                    registry.registerFunction(funcAnno.intentId(), new ModuleRegistry.RegisteredFunction(
                            moduleAnno.id(), funcAnno, method, funcInstance
                    ));
                }
            }
        }
    }

    private Enumeration<URL> findClassEntries(Bundle bundle) {
        return Stream.of("", "/")
                .map(path -> bundle.findEntries(path, "*.class", true))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(
                        bundle.findEntries(".", "*.class", true) // 最后兜底 "."
                );
    }
    private Enumeration<URL> findClassEntries1(Bundle bundle) {
        Enumeration<URL> entries = bundle.findEntries(null, "*.class", true);
        if (entries != null && entries.hasMoreElements()) {
            return entries;
        }
        entries = bundle.findEntries("/", "*.class", true);
        if (entries != null && entries.hasMoreElements()) {
            return entries;
        }
        return bundle.findEntries(".", "*.class", true);
    }

    private static class ScanResult {
        Class<?> moduleClass;
        List<Class<?>> functionCandidateClasses = new ArrayList<>();
    }

    private ScanResult scanBundleForAnnotations(Bundle bundle) {
        ScanResult result = new ScanResult();
        Enumeration<URL> entries = findClassEntries(bundle);
        if (entries == null) return result;

        while (entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            String className = toClassName(entry.getPath());
            if (className == null) continue;

            try {
                Class<?> clazz = bundle.loadClass(className);
                if (result.moduleClass == null && hasAiModuleAnnotation(clazz)) {
                    result.moduleClass = clazz;
                }
                // 无论有没有 @AiModule，都记录可能含 @AiFunction 的类
                if (hasAiFunctionMethods(clazz)) {
                    result.functionCandidateClasses.add(clazz);
                }
            } catch (Exception e) {
                log.debug("Skip class during scan: {}", className);
            }
        }
        return result;
    }

    private <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException |
                 ExceptionInInitializerError e) {
            log.warn("Failed to instantiate: {}", clazz.getName(), e);
            return null;
        } catch (Throwable t) {
            log.error("Unexpected error instantiating: {}", clazz.getName(), t);
            return null;
        }
    }

    private String toClassName(String path) {
        if (!path.endsWith(".class") || path.contains("$")) {
            return null;
        }
        // 去掉开头的 / 和结尾的 .class
        return path.substring(1, path.length() - 6).replace('/', '.');
    }

    private boolean hasAiModuleAnnotation(Class<?> clazz) {
        return clazz.getAnnotation(AiModule.class) != null;
    }

    private boolean hasAiFunctionMethods(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(AiFunction.class) != null) {
                return true;
            }
        }
        return false;
    }

    public void unloadModule(String moduleId) {
        ModuleInstance instance = registry.getModule(moduleId);
        if (instance != null) {
            try {
                instance.getBundle().stop();
                instance.getBundle().uninstall();
                registry.unloadModule(moduleId);
            } catch (Exception e) {
                log.error("Failed to unload module: {}", moduleId, e);
            }
        }
    }

    public void loadModule1(File jarFile) throws Exception {
        Bundle bundle = context.installBundle(jarFile.toURI().toString());
        bundle.start();

        // 扫描主类
        JarInputStream jis = new JarInputStream(jarFile.toURL().openStream());
        String mainClass = null;
        java.util.jar.Manifest manifest = jis.getManifest();
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Main-Class");
        }
        jis.close();

        if (mainClass == null) throw new IllegalArgumentException("No Main-Class in JAR");

        Class<?> clazz = bundle.loadClass(mainClass);
        AiModule moduleAnno = clazz.getAnnotation(AiModule.class);
        if (moduleAnno == null) return;

        Object instance = clazz.getDeclaredConstructor().newInstance();
        ModuleInstance moduleInstance = new ModuleInstance(moduleAnno.id(), bundle, instance);
        registry.registerModule(moduleInstance);

        // 注册所有 @AiFunction 方法
        for (Method method : clazz.getDeclaredMethods()) {
            AiFunction funcAnno = method.getAnnotation(AiFunction.class);
            if (funcAnno != null) {
                registry.registerFunction(funcAnno.intentId(),
                        new ModuleRegistry.RegisteredFunction(moduleAnno.id(), funcAnno, method, instance));
            }
        }
    }

    public void loadModule2(File jarFile) throws Exception {
        Bundle bundle = context.installBundle(jarFile.toURI().toString());
        bundle.start();

        // 等待 Bundle 激活
        long start = System.currentTimeMillis();
        while (bundle.getState() != Bundle.ACTIVE && bundle.getState() != Bundle.RESOLVED) {
            Thread.sleep(10);
            if (System.currentTimeMillis() - start > 5000) {
                throw new TimeoutException("Bundle failed to start: " + bundle.getLocation());
            }
        }

        String mainClass = getMainClassFromManifest(jarFile);
        Class<?> candidateClass = null;

        if (null != mainClass) {
            try {
                candidateClass = bundle.loadClass(mainClass);
                if (hasAiModuleAnnotation(candidateClass)) {
                    log.info("Using Main-Class as module entry: " + mainClass);
                    loadModuleFromEntryClass(bundle, candidateClass);
                    return;
                }
            } catch (Exception e) {
                log.error("Failed to load Main-Class: " + mainClass);
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }

        Class<?> moduleClass = findFirstAiModuleClass(bundle);
        if (moduleClass != null) {
            log.info("Found @AiModule class: " + moduleClass.getName());
            loadModuleFromEntryClass(bundle, moduleClass);
            return;
        }

        bundle.uninstall(); // 清理已安装的无效 bundle
        throw new IllegalArgumentException("No valid @AiModule class found in " + jarFile.getName());
    }

    private String getMainClassFromManifest(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            java.util.jar.Manifest manifest = jar.getManifest();
            return manifest != null ? manifest.getMainAttributes().getValue("Main-Class") : null;
        }
    }

    private Class<?> findFirstAiModuleClass(Bundle bundle) {
        // 确保 Bundle 已解析
        if (bundle.getState() < Bundle.RESOLVED) {
            System.err.println("Bundle not resolved: " + bundle.getSymbolicName());
            return null;
        }

        // 尝试两种路径模式，提高兼容性
        Enumeration<URL> entries = bundle.findEntries(null, "*.class", true);
        if (entries == null || !entries.hasMoreElements()) {
            entries = bundle.findEntries(".", "*.class", true);
        }
        if (entries == null) return null;

        while (entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            String path = entry.getPath();
            String className = toClassName(path);
            if (className == null) continue;

            try {
                Class<?> clazz = bundle.loadClass(className);
                if (hasAiModuleAnnotation(clazz)) {
                    return clazz;
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // 忽略：类找不到或依赖缺失
            } catch (Throwable t) {
                System.err.println("Error loading class " + className + " from " + bundle.getSymbolicName() + ": " + t.getMessage());
            }
        }
        return null;
    }

    private void registerAiFunction(Method method, AiFunction funcAnno, Class<?> clazz, String moduleId) {
        try {
            // 确保方法是 public
            if (!Modifier.isPublic(method.getModifiers())) {
                System.err.println("Skipped @AiFunction: " + method + " - must be public");
                return;
            }

            // 实例化该类（要求有无参构造函数）
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // 构建注册函数对象
            ModuleRegistry.RegisteredFunction registeredFunction = new ModuleRegistry.RegisteredFunction(
                    moduleId,
                    funcAnno,
                    method,
                    instance
            );

            // 注册到全局 registry
            registry.registerFunction(funcAnno.intentId(), registeredFunction);

            System.out.println("Registered @AiFunction: " + funcAnno.intentId() + " -> " + method);

        } catch (NoSuchMethodException e) {
            System.err.println("No no-arg constructor for class: " + clazz.getName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            System.err.println("Cannot instantiate class for @AiFunction: " + clazz.getName() + " - " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to register @AiFunction: " + funcAnno.intentId() + " - " + e.getMessage());
        }
    }

    private void loadModuleFromEntryClass(Bundle bundle, Class<?> entryClass) throws Exception {
        AiModule moduleAnno = entryClass.getAnnotation(AiModule.class);
        Object instance = entryClass.getDeclaredConstructor().newInstance();

        ModuleInstance moduleInstance = new ModuleInstance(moduleAnno.id(), bundle, instance);
        registry.registerModule(moduleInstance);

        // 扫描整个 Bundle 注册所有 @AiFunction（无论在哪）
        registerAllAiFunctions(bundle, moduleAnno.id());
    }

    private void registerAllAiFunctions(Bundle bundle, String moduleId) {
        if (bundle.getState() < Bundle.RESOLVED) {
            log.warn("Bundle not resolved: {}", bundle.getSymbolicName());
            return;
        }

        Set<String> processedClasses = new HashSet<>(); // 避免重复处理

        Enumeration<URL> entries = bundle.findEntries("/", "*.class", true);
        if (entries == null || !entries.hasMoreElements()) {
            entries = bundle.findEntries(".", "*.class", true);
        }
        if (entries == null) return;

        while (entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            String className = toClassName(entry.getPath());
            if (className == null || processedClasses.contains(className)) continue;

            processedClasses.add(className);

            try {
                Class<?> clazz = bundle.loadClass(className);
                Object instance = createInstance(clazz); // 提取为方法
                if (instance == null) continue;

                for (Method method : clazz.getDeclaredMethods()) {
                    AiFunction funcAnno = method.getAnnotation(AiFunction.class);
                    if (funcAnno != null && Modifier.isPublic(method.getModifiers())) {
                        registry.registerFunction(funcAnno.intentId(), new ModuleRegistry.RegisteredFunction(
                                moduleId, funcAnno, method, instance
                        ));
                        log.debug("Registered @AiFunction: {} -> {}.{}", funcAnno.intentId(), className, method.getName());
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                log.debug("Class not found during scan: {}", className);
            } catch (Throwable t) {
                log.warn("Error scanning class: {}", className, t);
            }
        }
    }

    private Object createInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.warn("Cannot instantiate class: {} - {}", clazz.getName(), e.getMessage());
            return null;
        }
    }
}
