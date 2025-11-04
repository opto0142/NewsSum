package com.newssum.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Captures a crawled news article with Gemini translation and summary metadata.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document(collection = "news_articles")
public class NewsArticle {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("url_hash")
    private String urlHash;

    @Indexed
    private String url;

    @Field("source_outlet")
    private String sourceOutlet;

    @Field("original_title")
    private String originalTitle;

    @Field("original_content")
    private String originalContent;

    private String language;

    @Field("translated_title")
    private String translatedTitle;

    @Field("translated_content")
    private String translatedContent;

    @Builder.Default
    private List<String> summary = new ArrayList<>();

    @Field("published_at")
    private LocalDateTime publishedAt;

    @CreatedDate
    @Field("crawled_at")
    private LocalDateTime crawledAt;

    @Field("crawled_by")
    private String crawledBy;

    public void updateTranslation(final String translatedTitle, final String translatedContent) {
        this.translatedTitle = translatedTitle;
        this.translatedContent = translatedContent;
    }

    public void updateSummary(final List<String> summaryPoints) {
        this.summary = new ArrayList<>(summaryPoints);
    }
}
