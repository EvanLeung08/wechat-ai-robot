package com.eshare.wechatairobot.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置类
 */
@Component
@ConfigurationProperties(prefix = "wechat")
@Getter
@Setter
public class WechatConfig {

    /**
     * 令牌
     */
    private String token;

}
