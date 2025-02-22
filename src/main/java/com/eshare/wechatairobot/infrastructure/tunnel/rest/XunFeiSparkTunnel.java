package com.eshare.wechatairobot.infrastructure.tunnel.rest;

import com.eshare.wechatairobot.infrastructure.common.constant.OpenAIConstant;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import com.eshare.wechatairobot.infrastructure.utils.PropertyUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class XunFeiSparkTunnel {

    private static final String DEFAULT_API_URL = "https://spark-api-open.xf-yun.com/v1/chat/completions";
    private static final String DEEPSEEK_API_URL = "https://maas-api.cn-huabei-1.xf-yun.com/v1/chat/completions";
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public XunFeiSparkTunnel() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public BaseMessage getResponse(String userId, String userContent, String model) {
        String aiKey = PropertyUtil.getProperty(OpenAIConstant.MODEL_DEEPSEEK.equals(model) ? OpenAIConstant.DEEPSEEK_API_KEY : OpenAIConstant.OPENAI_API_KEY);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("user", userId);
            requestBody.put("messages", new Object[]{
                    createMessage("system", "你���知识渊博的助理"),
                    createMessage("user", userContent)
            });
            requestBody.put("stream", false);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    objectMapper.writeValueAsString(requestBody)
            );

            Request request = new Request.Builder()
                    .url(OpenAIConstant.MODEL_DEEPSEEK.equals(model) ? DEEPSEEK_API_URL : DEFAULT_API_URL)
                    .addHeader("Authorization", "Bearer " + aiKey)
                    .post(body)
                    .build();

            Response response = httpClient.newCall(request).execute();
            if (response.isSuccessful() && response.body() != null) {
                JsonNode responseJson = objectMapper.readTree(response.body().string());
                log.info("XunFei Spark output: {}", responseJson);
                String result = OpenAIConstant.MODEL_DEEPSEEK.equals(model) ?
                        responseJson.get("choices").get(0).get("message").get("content").asText() :
                        responseJson.get("choices").get(0).get("text").asText();
                return new TextMessage(result);
            } else {
                log.error("Failed to get response from XunFei Spark API: {}", response.message());
            }
        } catch (IOException e) {
            log.error("Error while calling XunFei Spark API", e);
        }
        return null;
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }
}