package com.xun.orchestrator.web;

import com.xun.orchestrator.module.ModuleLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.web
 * @Author: xun
 * @CreateTime: 2025-08-23  21:17
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/modules")
public class ModuleController {
    @Autowired
    private ModuleLoader moduleLoader;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        File tmp = File.createTempFile("mod-", ".jar");
        file.transferTo(tmp);
        try {
            moduleLoader.loadModule(tmp);
            return "模块加载成功: " + file.getOriginalFilename();
        } catch (Exception e) {
            return "加载失败: " + e.getMessage();
        }
    }

    @DeleteMapping("/unload/{id}")
    public String unload(@PathVariable String id) {
        moduleLoader.unloadModule(id);
        return "模块已卸载: " + id;
    }
}
