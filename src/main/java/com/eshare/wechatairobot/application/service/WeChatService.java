package com.eshare.wechatairobot.application.service;

import com.eshare.wechatairobot.application.processor.EventMessageProcessor;
import com.eshare.wechatairobot.application.processor.TextMessageProcessor;
import com.eshare.wechatairobot.application.processor.WeChatMessageProcessor;
import com.eshare.wechatairobot.client.dto.WeChatMessageDTO;
import com.eshare.wechatairobot.infrastructure.common.enums.WeChatMsgType;
import com.eshare.wechatairobot.infrastructure.config.WechatConfig;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import com.eshare.wechatairobot.infrastructure.utils.XmlUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ø
 * 微信服务类
 */
@Slf4j
@Component
public class WeChatService {


    private final WechatConfig wechatConfig;

    private final TextMessageProcessor textMessageProcessor;

    private final EventMessageProcessor eventMessageProcessor;

    private final Map<WeChatMsgType, WeChatMessageProcessor> messageHandleMap;


    public WeChatService(WechatConfig wechatConfig, TextMessageProcessor textMessageProcessor, EventMessageProcessor eventMessageProcessor) {
        this.wechatConfig = wechatConfig;
        this.textMessageProcessor = textMessageProcessor;
        this.eventMessageProcessor = eventMessageProcessor;
        List<WeChatMessageProcessor> weChatMessageProcessorList = Lists.newLinkedList();
        weChatMessageProcessorList.add(this.textMessageProcessor);
        weChatMessageProcessorList.add(this.eventMessageProcessor);
        messageHandleMap = weChatMessageProcessorList.stream().collect(Collectors.toMap(WeChatMessageProcessor::getMsgType, t -> t));
    }

    /**
     * 验证微信消息合法性
     */
    public boolean checkSignature(String signature, String timestamp, String nonce) {
        if (signature == null || timestamp == null || nonce == null) {
            return false;
        }
        String[] arr = new String[]{wechatConfig.getToken(), timestamp, nonce};
        // 将token、timestamp、nonce三个参数进行字典序排序
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (String str : arr) {
            content.append(str);
        }
        String tmpStr;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes(StandardCharsets.UTF_8));
            tmpStr = byteToHex(digest);
            return tmpStr.equals(signature);
        } catch (Exception e) {
            log.error("校验签名异常", e);
        }
        return false;
    }

    /**
     * 处理收到的消息
     */
    public BaseMessage processReceived(String message) {
        BaseMessage resultMessage;
        WeChatMessageDTO weChatMessageDTO = XmlUtil.xmlToObj(message, WeChatMessageDTO.class);
        String fromUserName = weChatMessageDTO.getFromUserName();
        String toUserName = weChatMessageDTO.getToUserName();
        try {
            WeChatMsgType msgType = WeChatMsgType.findByValue(weChatMessageDTO.getMsgType());
            resultMessage = distributeEvent(weChatMessageDTO, fromUserName, toUserName, msgType);
        } catch (Exception e) {
            log.error("处理来至微信服务器的消息出现错误", e);
            resultMessage = new TextMessage(toUserName, fromUserName, "我竟无言以对！");
        }

        return resultMessage;
    }

    private BaseMessage distributeEvent(WeChatMessageDTO weChatMessageDTO, String fromUserName, String toUserName, WeChatMsgType msgType) {
        BaseMessage resultMessage;
        WeChatMessageProcessor weChatMessageHandle = messageHandleMap.get(msgType);
        if (weChatMessageHandle == null) {
            resultMessage = new TextMessage(toUserName, fromUserName, "你说啥我咋没懂呢[疑问]");
        } else {
            resultMessage = weChatMessageHandle.processMessage(weChatMessageDTO);
        }
        return resultMessage;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}
