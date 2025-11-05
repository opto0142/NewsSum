package com.newssum.crawler;

import java.util.Locale;

/**
 * 간단한 한글 비율 기반 언어 감지를 수행한다.
 */
public final class LanguageDetector {

    private LanguageDetector() {
    }

    public static String detectLanguage(final String text) {
        if (text == null || text.isBlank()) {
            return Locale.ROOT.getLanguage();
        }
        final long koreanCharacters = text.codePoints()
            .filter(LanguageDetector::isHangul)
            .count();
        final double ratio = (double) koreanCharacters / Math.max(text.length(), 1);
        return ratio > 0.2 ? Locale.KOREAN.getLanguage() : Locale.ENGLISH.getLanguage();
    }

    private static boolean isHangul(final int codePoint) {
        final Character.UnicodeBlock block = Character.UnicodeBlock.of(codePoint);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
            || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO
            || block == Character.UnicodeBlock.HANGUL_JAMO;
    }
}
