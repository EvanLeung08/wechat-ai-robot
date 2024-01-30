package com.eshare.wechatairobot.controller.scheduler;

import com.eshare.wechatairobot.application.service.KeywordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class KeywordRefreshScheduler {

    @Autowired
    private final KeywordService keywordService;

    public KeywordRefreshScheduler(KeywordService keywordService) {
        this.keywordService = keywordService;
    }

    @Scheduled(fixedRate = 1,timeUnit = TimeUnit.MINUTES)
    public void refreshKeywordTask() {
        keywordService.refreshKeyword();
        log.info("Refresh keyword successfully!");
    }
}
