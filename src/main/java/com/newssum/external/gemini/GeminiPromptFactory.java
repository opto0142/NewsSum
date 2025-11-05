package com.newssum.external.gemini;

/**
 * Gemini 프롬프트 문자열을 생성한다.
 */
public final class GeminiPromptFactory {

    private GeminiPromptFactory() {
    }

    public static String translationPrompt(final String title, final String content) {
        return """
            Translate the following news article to Korean.
            Maintain the original meaning and tone.

            Title: %s
            Content: %s
            """.stripIndent().formatted(nullToEmpty(title), nullToEmpty(content));
    }

    public static String summaryPrompt(final String title, final String content) {
        return """
            Summarize the following Korean news article in 3-5 bullet points.
            Focus on key facts and main points.

            Title: %s
            Content: %s
            """.stripIndent().formatted(nullToEmpty(title), nullToEmpty(content));
    }

    private static String nullToEmpty(final String value) {
        return value == null ? "" : value;
    }
}
