package com.eshare.wechatairobot.infrastructure.tunnel.rest;


import com.eshare.wechatairobot.infrastructure.common.constant.OpenAIConstant;
import com.eshare.wechatairobot.infrastructure.config.OpenAIKeyPool;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import com.eshare.wechatairobot.infrastructure.utils.AddressUtil;
import com.eshare.wechatairobot.infrastructure.utils.PropertyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import retrofit2.Retrofit;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.Collections;
import java.util.Random;

@Slf4j
public class OpenAiTunnel {

    private final OpenAIKeyPool openAIKeyPool;

    private static OpenAiService openAiService;


    public OpenAiTunnel(OpenAIKeyPool openAIKeyPool, String model) {
        this.openAIKeyPool = openAIKeyPool;
        //优先使用系统配置的API KEY
       String openaiApiKey = PropertyUtil.getProperty(OpenAIConstant.OPENAI_API_KEY);
        if (StringUtils.isBlank(openaiApiKey)) {
            Random random = new Random();
            if (OpenAIConstant.MODEL_GPT4.equalsIgnoreCase(model)) {
                int index = random.nextInt(this.openAIKeyPool.getGpt4KeyList().size());
                openaiApiKey = this.openAIKeyPool.getGpt4KeyList().get(index);
            } else {
                int index = random.nextInt(this.openAIKeyPool.getKeyList().size());
                openaiApiKey = this.openAIKeyPool.getKeyList().get(index);
            }
        }
        if (StringUtils.isBlank(openaiApiKey)) {
            return;
        }
        String proxyAddress = PropertyUtil.getProperty(OpenAIConstant.OPENAI_PROXY);
        OkHttpClient.Builder clientBuilder = OpenAiService.defaultClient(openaiApiKey, Duration.ofSeconds(600)).newBuilder();
        if (StringUtils.isNotBlank(proxyAddress)) {
            boolean valid = AddressUtil.validateAddress(proxyAddress);
            if (!valid) {
                throw new RuntimeException("OPENAI_PROXY is not valid, value:" + proxyAddress);
            }

            String[] parts = proxyAddress.split(":");
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], parts.length == 2 ? Integer.parseInt(parts[1]) : 80));
            clientBuilder.proxy(proxy);
        }

        OkHttpClient client = clientBuilder.build();
        ObjectMapper mapper = OpenAiService.defaultObjectMapper();
        Retrofit.Builder retrofitBuilder = OpenAiService.defaultRetrofit(client, mapper).newBuilder();

        String baseDomain = PropertyUtil.getProperty(OpenAIConstant.OPENAI_BASE_DOMAIN);
        if (StringUtils.isNotBlank(baseDomain)) {
            boolean valid = AddressUtil.validateAddress(baseDomain);
            if (!valid) {
                throw new RuntimeException("OPENAI_BASE_DOMAIN is not valid, value:" + baseDomain);
            }

            retrofitBuilder.baseUrl("https://" + baseDomain + "/");
        }

        Retrofit retrofit = retrofitBuilder.build();
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        openAiService = new OpenAiService(api);
    }

    /**
     * 获取消息响应
     */
    public BaseMessage getResponse(String content, String model) {
        if (openAiService == null) {
            return null;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setRole("user");
        chatMessage.setContent(content);
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(model)
                .messages(Collections.singletonList(chatMessage))
                .build();
       /* List<ChatCompletionChoice> choiceList = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0);

        Collections.sort(choiceList, new Comparator<ChatCompletionChoice>() {
            @Override
            public int compare(ChatCompletionChoice s1, ChatCompletionChoice s2) {
                return s1.getMessage().getContent().length() - s2.getMessage().getContent().length();
            }
        });*/
       /* choiceList.forEach(System.out::println);
        String result = choiceList.get(0).getMessage().getContent();*/
        ChatCompletionChoice choice = openAiService.createChatCompletion(chatCompletionRequest).getChoices().get(0);
        String result = choice.getMessage().getContent();
        log.info("GPT输出结果:{}", result);
        result = "[" + model + "]" + result;
        return new TextMessage(result);
    }
}
