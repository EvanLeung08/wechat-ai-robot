package com.eshare.wechatairobot.application.processor;


import com.eshare.wechatairobot.domain.WeChatMessage;
import com.eshare.wechatairobot.infrastructure.common.enums.WeChatMsgType;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;

/**
 * 处理微信消息接口类
 */
public interface WeChatMessageProcessor {

    WeChatMsgType getMsgType();

    BaseMessage processMessage(WeChatMessage weChatMessage);
}
