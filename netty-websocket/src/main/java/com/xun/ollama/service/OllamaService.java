package com.xun.ollama.service;

import reactor.core.publisher.Flux;

import java.util.List;

public interface OllamaService {

    Flux<String> getAskResult(String msg);

    String getAskStrResult(String msg);

    String getAskStrResultPlus(String msg);

//    String getAskStrResultPlus(List<AiChatMessage> history);

}
