package com.eshare.wechatairobot.application.service;


import com.eshare.wechatairobot.infrastructure.config.KeywordConfig;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.NewsMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import com.eshare.wechatairobot.infrastructure.utils.HttpUtil;
import com.eshare.wechatairobot.infrastructure.utils.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * 关键字服务类
 */
@Component
@Slf4j
public class KeywordService {


    /**
     * 当前配置的版本
     */
    private static String currentConfigVersion;

    /**
     * 关键字回复内容配置
     */
    private static Map<String, JsonNode> keywordMessageMap = Maps.newHashMap();

    private final String versionLocation;

    private final String messageLocation;


    public KeywordService(KeywordConfig keywordConfig) {
        this.versionLocation = keywordConfig.getVersionLocation();
        this.messageLocation = keywordConfig.getMessageLocation();
        //根据配置从远程加载或者从本地加载关键字库
        if (StringUtils.isNotBlank(this.versionLocation)) {
            if (versionLocation.startsWith("http")) {
                this.startReloadTask();
            } else {
                try {
                    this.loadLocalResource();
                } catch (IOException e) {
                    log.warn("加载关键字库失败", e);
                }
            }
        }
    }

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setDaemon(true);
        thread.setName("reload-keyword-task");
        return thread;
    });

    public BaseMessage getMessageByKeyword(String keyword) {
        JsonNode messageJsonNode = keywordMessageMap.get(keyword);
        if (messageJsonNode == null) {
            return null;
        }

        String type = messageJsonNode.get("type").asText();
        BaseMessage baseMessage = null;
        if ("text".equals(type)) {
            baseMessage = JsonUtil.jsonToObj(messageJsonNode.toString(), TextMessage.class);
        } else if ("news".equals(type)) {
            baseMessage = JsonUtil.jsonToObj(messageJsonNode.toString(), NewsMessage.class);
        }
        return baseMessage;
    }

    public void startReloadTask() {
        scheduledExecutorService.scheduleAtFixedRate(new ReloadKeywordTask(), 0, 1, TimeUnit.MINUTES);
    }

    private class ReloadKeywordTask extends TimerTask {

        @Override
        public void run() {
            loadRemoteResource(versionLocation, messageLocation);
        }
    }

    private synchronized void loadLocalResource() throws IOException {

        try (InputStream messageLocationIn = getClass().getClassLoader().getResourceAsStream(this.messageLocation);
             InputStream versionLocationIn = getClass().getClassLoader().getResourceAsStream(this.versionLocation);
        ) {
            String version = IOUtils.toString(versionLocationIn, Charset.defaultCharset());
            String messageConfigStr = IOUtils.toString(messageLocationIn, Charset.defaultCharset());
            replaceKeywordMessageMap(version, messageConfigStr);
        }

    }

    private synchronized void loadRemoteResource(String versionLocation, String messageLocation) {
        try {
            String newVersion = HttpUtil.get(versionLocation);

            if (Objects.equals(currentConfigVersion, newVersion)) {
                return;
            }

            String messageConfigStr = HttpUtil.get(messageLocation);
            replaceKeywordMessageMap(newVersion, messageConfigStr);

        } catch (Exception e) {
            log.info("根据Http请求获取关键字配置信息异常", e);
        }
    }

    private static void replaceKeywordMessageMap(String newVersion, String messageConfigStr) {
        Map<String, JsonNode> newKeywordMessageMap = Maps.newHashMap();
        JsonNode messageConfigJson = JsonUtil.jsonToObj(messageConfigStr, JsonNode.class);
        Iterator<Map.Entry<String, JsonNode>> keywordMessageIterator = messageConfigJson.fields();
        while (keywordMessageIterator.hasNext()) {
            Map.Entry<String, JsonNode> entry = keywordMessageIterator.next();
            String keyword = entry.getKey();
            JsonNode messageJsonNode = entry.getValue();
            newKeywordMessageMap.put(keyword, messageJsonNode);
            log.info("初始化关键字map，{0} : {1}", new Object[]{keyword, messageJsonNode.toString()});
        }
        currentConfigVersion = newVersion;
        keywordMessageMap = newKeywordMessageMap;
    }
}
