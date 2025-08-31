package com.xun.orchestrator.user;

import com.xun.orchestrator.entity.ChatSession;
import com.xun.orchestrator.session.UserSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.user
 * @Author: xun
 * @CreateTime: 2025-08-23  21:16
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class UserFamiliarityService {
    private final Set<String> HIGH_FREQ = Set.of("apply_leave", "clock_in");

    @Autowired
    private UserSessionManager sessionManager;

    public void updateFamiliarity(ChatSession session, String intentId) {
        session.getKnownFunctions().add(intentId);
        int count = session.getKnownFunctions().size();

        if (count >= 3) {
            session.setFamiliarityLevel(ChatSession.UserFamiliarityLevel.REGULAR);
        }
        if (count >= 5 && session.getKnownFunctions().containsAll(HIGH_FREQ)) {
            session.setFamiliarityLevel(ChatSession.UserFamiliarityLevel.EXPERT);
        }
        sessionManager.save(session);
    }
}
