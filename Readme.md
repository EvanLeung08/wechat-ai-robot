# weChatRobot

一个基于微信公众号的智能聊天机器人项目，支持ChatGPT对话模式回复内容.

本项目是基于 ``weChatRobot``项目1.2 版本改造，去除了``Vert.x``相关模块，使用``Springboot``根据个人需要进行定制化升级改造。

``weChatRobot``项目地址：https://github.com/MartinDai/weChatRobot

![qrcode](doc/wechat.jpg "扫码关注，体验智能机器人")

## 项目介绍：

  本项目是一个微信公众号项目，需配合微信公众号使用，在微信公众号配置本项目运行的服务器域名，用户关注公众号后，向公众号发送任意信息，公众号会根据用户发送的内容自动回复。
  
## 涉及框架及技术

- [Springboot](https://github.com/spring-projects/spring-boot)
- [Jackson](https://github.com/FasterXML/jackson)
- [OkHttp](https://github.com/square/okhttp)
- [Guava](https://github.com/google/guava)
- [Openai-java](https://github.com/TheoKanning/openai-java)


## 支持的功能

+ [x] 自定义关键字回复内容
+ [x] 调用ChatGPT接口回复内容（需配置启动参数或者环境变量：`OPENAI_API_KEY`）
+ [x] 多个``OPENAI_API_KEY``切换使用

## 使用说明：

1. 使用之前需要有微信公众号的帐号，没有的请戳[微信公众号申请](https://mp.weixin.qq.com/cgi-bin/readtemplate?t=register/step1_tmpl&lang=zh_CN)
2. 如果需要使用图灵机器人的回复内容则需要[注册图灵机器人帐号](http://tuling123.com/register/email.jhtml)获取相应的ApiKey并配置在启动参数或者环境变量中
3. 如果需要使用ChatGPT的回复内容则需要[创建OpenAI的API Key](https://platform.openai.com/account/api-keys)并配置在启动参数或者环境变量中，也可以直接配置多个Key在配置文件，程序会随机使用
   ```java
   openai:
     keyList:
       - "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"
       - "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"
   ```
4. 可以通过配置启动参数或者环境变量`OPENAI_BASE_DOMAIN`更换访问OpenAI的域名
5. 可以通过配置启动参数或者环境变量`OPENAI_PROXY`使用代理服务访问OpenAI，建议参考``https://github.com/Ice-Hazymoon/openai-scf-proxy/blob/master/README.md``直接搭建自己的反向代理替换OpenAI域名
6. 内容响应来源的优先级`自定义关键 > ChatGPT
7. 在微信公众号后台配置回调URL为<https://locahost/weChat/receiveMessage>，其中`locahost`是你自己的域名，token与`application.yml`里面配置的保持一致即可

## 开发部署

### 本地启动

直接运行类`com.eshare.wechatairobot.WechatAiRobotApplication`

### jar包运行

maven编译打包

```shell
mvn clean package
```

打包完成后，在wechat-ai-robot/target目录会生成wechat-ai-robot-1.0.0.jar

启动执行

```shell
java -jar wechat-ai-robot-1.0.0.jar
```


服务器部署后台运行

```shell
nohup java -DOPENAI_BASE_DOMAIN={{你反向代理的域名}}} -jar wechat-ai-robot-1.0.0.jar > ./console.log 2>&1 &
```

在执行命令的当前目录查看console日志