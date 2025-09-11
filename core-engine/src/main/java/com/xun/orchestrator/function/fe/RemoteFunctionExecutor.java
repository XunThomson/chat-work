package com.xun.orchestrator.function.fe;

import com.xun.orchestrator.function.FunctionExecutor;
import com.xun.sdk.model.AiFunctionResult;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.function.fe
 * @Author: xun
 * @CreateTime: 2025-08-31  12:00
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Qualifier("remote")
public class RemoteFunctionExecutor implements FunctionExecutor {
    @Override
    public AiFunctionResult execute(String intentId,Object... args) {
        return null;
    }
}
