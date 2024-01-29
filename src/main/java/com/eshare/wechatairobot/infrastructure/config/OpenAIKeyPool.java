package com.eshare.wechatairobot.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Order(value = 0)
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAIKeyPool {
    private List<String> keyList;


    public List<String> getKeyList() {
        return keyList;
    }

    public void setKeyList(List<String> keyList) {
        this.keyList = keyList;
    }
}
