package com.xun.service;

import io.netty.channel.Channel;

public interface ChatMessagePlusService {

    void handleChatMessage(Channel channel, String payload);

}
