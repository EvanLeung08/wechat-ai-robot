package com.eshare.wechatairobot.application.processor;


import com.eshare.wechatairobot.application.service.KeywordService;
import com.eshare.wechatairobot.client.dto.WeChatMessageDTO;
import com.eshare.wechatairobot.infrastructure.common.constant.OpenAIConstant;
import com.eshare.wechatairobot.infrastructure.common.enums.WeChatMsgType;
import com.eshare.wechatairobot.infrastructure.config.OpenAIKeyPool;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.OpenAiTunnel;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文本类型消息处理类
 */
@Component
@Slf4j
public class TextMessageProcessor implements WeChatMessageProcessor {

    private final KeywordService keywordService;

    private final OpenAIKeyPool openAIKeyPool;

    public TextMessageProcessor(KeywordService keywordService, OpenAIKeyPool openAIKeyPool) {
        this.keywordService = keywordService;
        this.openAIKeyPool = openAIKeyPool;
    }

    @Override
    public WeChatMsgType getMsgType() {
        return WeChatMsgType.TEXT;
    }

    @Override
    public BaseMessage processMessage(WeChatMessageDTO weChatMessageDTO) {

        log.info("收到用户文本信息{}", weChatMessageDTO);

        String fromUserName = weChatMessageDTO.getFromUserName();
        String toUserName = weChatMessageDTO.getToUserName();
        String content = weChatMessageDTO.getContent();

        //优先查找关键字配置
        BaseMessage message = keywordService.getMessageByKeyword(content);

        //再尝试从GPT获取响应
        if (message == null) {
           String requestContent = content + "? 请回复内容不要超过200字符，不要换行，选出唯一一个最优最短答案即可。";
            log.info("请求文本信息：{}", requestContent);
            boolean retry = false;
            //优先使用GPT$
            try {
                OpenAiTunnel openAITunnel = new OpenAiTunnel(this.openAIKeyPool, OpenAIConstant.MODEL_GPT4);
                message = openAITunnel.getResponse(requestContent, OpenAIConstant.MODEL_GPT4);
            } catch (Exception ex) {
                log.warn("调用openai GPT4接口异常，准备重试", ex);
                retry = true;
            }
            if (retry) {
                try {
                    OpenAiTunnel openAITunnel = new OpenAiTunnel(this.openAIKeyPool, OpenAIConstant.MODEL_GPT3);
                    message = openAITunnel.getResponse(requestContent, OpenAIConstant.MODEL_GPT3);
                } catch (Exception ex) {
                    log.error("调用openai接口异常", ex);
                    message = null;
                }
            }
        }


        //最后返回收到的文本信息作为兜底
        if (message == null) {
            message = new TextMessage(toUserName, fromUserName, content);
        } else {
            message.setFromUserName(toUserName);
            message.setToUserName(fromUserName);
            message.setCreateTime(System.currentTimeMillis());
        }

        return message;
    }
}
