package com.eshare.wechatairobot.infrastructure.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * 关键字配置
 */
@Component
@ConfigurationProperties(prefix = "keyword")
@Getter
@Setter
public class KeywordConfig {

    private String versionLocation;

    private String messageLocation;
}
