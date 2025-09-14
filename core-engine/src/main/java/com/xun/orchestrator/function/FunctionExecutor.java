package com.xun.orchestrator.function;

import com.xun.sdk.model.AiFunctionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.function
 * @Author: xun
 * @CreateTime: 2025-08-23  21:13
 * @Description: TODO
 * @Version: 1.0
 */
public interface FunctionExecutor {

    /**
     * @description: 方法执行
     * @author: xun
     * @date: 2025/8/31 16:02
     * @param: [intentId, args]
     * @return: com.xun.sdk.model.AiFunctionResult
     **/
    public AiFunctionResult execute(String intentId,Object... args);
}
