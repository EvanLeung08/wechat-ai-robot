package com.eshare.wechatairobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WechatAiRobotApplication {

    public static void main(String[] args) {
        SpringApplication.run(WechatAiRobotApplication.class, args);
    }

}
