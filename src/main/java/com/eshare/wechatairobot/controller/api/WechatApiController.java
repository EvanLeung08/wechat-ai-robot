package com.eshare.wechatairobot.controller.api;

import com.eshare.wechatairobot.application.service.WeChatService;
import com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject.BaseMessage;
import com.eshare.wechatairobot.infrastructure.utils.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class WechatApiController {

    @Autowired
    private WeChatService weChatService;

    @GetMapping("/health")
    public String health() {
        return "Running";
    }


    @GetMapping(value = "/weChat/receiveMessage")
    public ResponseEntity<String> receiveMessage(@RequestParam(value = "signature", required = false) String signature,
                                                 @RequestParam(value = "timestamp", required = false) String timestamp,
                                                 @RequestParam(value = "nonce", required = false) String nonce,
                                                 @RequestParam(value = "echostr", required = false) String echostr
    ) {

        // 验证签名是否有效
        if (weChatService.checkSignature(signature, timestamp, nonce)) {
            return new ResponseEntity<String>(echostr, HttpStatus.OK);
        } else {
            return new ResponseEntity<String>("我是Evan的人工智障助手 !", HttpStatus.OK);
        }
    }


    @PostMapping(value = "/weChat/receiveMessage",produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receiveMessage(@RequestBody String requestBody) {
        BaseMessage baseMessage = weChatService.processReceived(requestBody);
        String xmlResult = XmlUtil.objToXml(baseMessage);
        return new ResponseEntity<String>(xmlResult, HttpStatus.OK);

    }

}
