package com.eshare.wechatairobot.infrastructure.tunnel.rest.dataobject;

import com.eshare.wechatairobot.infrastructure.common.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * 响应消息-> 图文消息
 */
@Data
public class NewsMessage extends BaseMessage {

    /**
     * 文章数量，限制为10条以内
     **/
    @JsonProperty("ArticleCount")
    @JacksonXmlProperty(localName = "ArticleCount")
    private int articleCount;

    /**
     * 文章列表默认第一个item为大图
     **/
    @JsonProperty("Articles")
    @JacksonXmlElementWrapper(localName = "Articles")
    @JacksonXmlProperty(localName = "item")
    private List<Article> articles;

    public NewsMessage() {
        super();
        setMsgType(MessageType.NEWS.getValue());
    }

    public NewsMessage(List<Article> articles) {
        this();
        Validate.notEmpty(articles, "文章列表不能为空");
        this.articles = articles;
        this.articleCount = articles.size();
    }

}
