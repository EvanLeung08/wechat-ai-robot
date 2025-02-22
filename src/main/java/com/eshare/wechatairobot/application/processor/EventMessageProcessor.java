package com.eshare.wechatairobot.application.processor;


import com.eshare.wechatairobot.client.dto.WeChatMessageDTO;
import com.eshare.wechatairobot.infrastructure.common.enums.WeChatEventType;
import com.eshare.wechatairobot.infrastructure.common.enums.WeChatMsgType;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 事件类型消息处理类
 */
@Component
@Slf4j
public class EventMessageProcessor implements WeChatMessageProcessor {


    @Override
    public WeChatMsgType getMsgType() {
        return WeChatMsgType.EVENT;
    }

    @Override
    public BaseMessage processMessage(WeChatMessageDTO weChatMessageDTO) {
        String event = weChatMessageDTO.getEvent();
        String fromUserName = weChatMessageDTO.getFromUserName();
        String toUserName = weChatMessageDTO.getToUserName();
        WeChatEventType eventType = WeChatEventType.findByValue(event);
        if (eventType == WeChatEventType.SUBSCRIBE) {
            return new TextMessage(toUserName, fromUserName, "谢谢关注！可以开始跟我聊天啦😁我是外企牛马的性感AI助手，哈哈！如果想获取英语教程，请输入 \"神救救我吧\"， 由于最近缺钱，AI功能已关闭，有问题不用问我！");
        } else if (eventType == WeChatEventType.UNSUBSCRIBE) {
            log.info("用户[" + weChatMessageDTO.getFromUserName() + "]取消了订阅");
        }

        return new TextMessage(toUserName, fromUserName, "bye!");
    }
}
