package com.xun.orchestrator.intent;

import com.xun.orchestrator.entity.MatchStrategy;
import com.xun.orchestrator.module.ModuleRegistry;
import com.xun.orchestrator.session.UserSessionManager;
import com.xun.orchestrator.entity.RecognizedIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @BelongsProject: ai-orchestrator-pro
 * @BelongsPackage: com.xun.orchestrator.intent
 * @Author: xun
 * @CreateTime: 2025-08-23  21:13
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class IntentRecognizer {

    @Autowired
    private ModuleRegistry registry;

    @Autowired
    private MatchStrategy matchStrategy;

    public RecognizedIntent recognize(String input, String userId) {
        if (input == null || input.trim().isEmpty()) {
            return RecognizedIntent.unknown(input);
        }

        double bestConfidence = 0.0;
        RecognizedIntent bestIntent = RecognizedIntent.unknown(input);

        for (var func : registry.getAllFunctions().values()) {
            for (String utter : func.getAnnotation().utterances()) {
                if (matchStrategy.matches(input, utter)) {
                    double confidence = matchStrategy.confidence(input, utter);
                    if (confidence > bestConfidence) {
                        bestIntent = new RecognizedIntent.Builder()
                                .intentId(func.getIntentId())
                                .confidence(confidence)
                                .parameters(extractParams(input, utter))
                                .originalInput(input)
                                .matchedUtterance(utter)
                                .build();
                        bestConfidence = confidence;
                    }
                }
            }
        }

        return bestIntent;
    }

    /**
     * 从用户输入中提取参数（槽位填充）
     * 示例：从“请三天假”中提取天数
     */
    private Map<String, Object> extractParams(String input, String utter) {
        Map<String, Object> params = new HashMap<>();

        // 示例规则 1：提取请假天数
        Pattern pattern1 = Pattern.compile("(?:请|休)([一二三四五六七八九十]+|[0-9]+)天假");
        Matcher matcher1 = pattern1.matcher(input);
        if (matcher1.find()) {
            String dayStr = matcher1.group(1);
            int days = convertChineseNumberToDigit(dayStr);
            params.put("days", days);
        }

        // 示例规则 2：提取会议室预定时间
        Pattern pattern2 = Pattern.compile("(今天|明天|后天|[0-9]{1,2}月[0-9]{1,2}日)下午?[0-9]{1,2}点");
        Matcher matcher2 = pattern2.matcher(input);
        if (matcher2.find()) {
            params.put("time", matcher2.group());
        }

        // 可扩展：基于 {slot} 占位符的模板匹配
        // 如：预定 {time} 的会议室 → 匹配后提取 {time: "明天下午3点"}

        return params;
    }

    /**
     * 中文数字转阿拉伯数字（简化版）
     */
    private int convertChineseNumberToDigit(String chinese) {
        return switch (chinese) {
            case "一" -> 1;
            case "二", "两" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            case "七" -> 7;
            case "八" -> 8;
            case "九" -> 9;
            case "十" -> 10;
            default -> {
                try {
                    yield Integer.parseInt(chinese);
                } catch (NumberFormatException e) {
                    yield 1;
                }
            }
        };
    }
}
