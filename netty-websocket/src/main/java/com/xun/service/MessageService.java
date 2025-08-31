package com.xun.service;

import com.xun.entity.MessagePayload;
import com.xun.entity.MutualMessagePayload;
import io.netty.channel.Channel;

public interface MessageService {

    void handleMessage(Channel channel, MutualMessagePayload payload);

    // 处理聊天消息：包含上下文、历史、工具等
    void handleChatMessage(Channel channel, String payload);

}
