package com.example.server.infrastructure.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class để phát hiện và lọc các từ ngữ thô tục, xúc phạm, chửi bậy trong tin nhắn Chatbot và Live Chat.
 * Các từ vi phạm sẽ tự động được thay thế bằng chuỗi "*****".
 */
public class ProfanityFilterUtil {

    private static final String REPLACEMENT = "*******";

    // Danh sách các từ thô tục, tục tĩu Tiếng Việt và Tiếng Anh phổ biến
    private static final List<String> PROFANITY_WORDS = Arrays.asList(
            // Từ chửi bậy xúc phạm chứa từ "ngu"
            "ngu", "đồ ngu", "do ngu", "thằng ngu", "thang ngu", "con ngu", "ngu học", "ngu hoc",
            "ngu lồn", "ngu lon", "ngu cặc", "ngu cac", "ngu vkl", "ngu vcl", "ngu vl", "ngu vãi", "ngu vai",
            "ngu như chó", "ngu nhu cho", "ngu quá", "ngu qua", "ngu dốt", "ngu dot",

            // Tiếng Việt tắt / teencode / tục tĩu
            "đm", "dm", "dkm", "đkm", "đờ mờ", "do ma", "đồ má", "đố má", "đm", "đ.m", "d.k.m", "d.m",
            "vkl", "vcl", "vl", "v.k.l", "v.c.l", "v.l", "vkl",
            "cặc", "cac", "cặt", "cat", "c.ă.c", "c.a.c", "con cặc", "con cac",
            "lồn", "lon", "l.ồ.n", "l.o.n", "con lồn", "con lon", "cái lồn",
            "đụ", "du", "đụ má", "du ma", "đụ mẹ", "du me",
            "đéo", "deo", "đ.é.o", "đéo con mẹ",
            "bố láo", "bo lao", "chửi", "óc chó", "oc cho",
            "mẹ kiếp", "me kiep", "đồ chó", "do cho", "chó đẻ", "cho de",

            // Tiếng Anh
            "fuck", "f.u.c.k", "fucking", "fucker",
            "bitch", "b.i.t.c.h",
            "shit", "s.h.i.t",
            "bastard", "asshole", "dick", "pussy", "cunt", "whore", "slut"
    );

    private static final List<Pattern> PROFANITY_PATTERNS = new ArrayList<>();

    static {
        for (String word : PROFANITY_WORDS) {
            // Pattern hỗ trợ khớp từ độc lập hoặc lách luật có dấu chấm/khoảng trắng
            String escaped = Pattern.quote(word);
            // Nếu từ có chứa dấu chấm, tìm chính xác pattern đó
            if (word.contains(".")) {
                PROFANITY_PATTERNS.add(Pattern.compile(escaped, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            } else {
                // Sử dụng regex với boundary hoặc khoảng trắng xung quanh
                String regex = "(?i)(?<=\\s|^|\\p{Punct})" + escaped + "(?=\\s|$|\\p{Punct})";
                PROFANITY_PATTERNS.add(Pattern.compile(regex, Pattern.UNICODE_CHARACTER_CLASS));
            }
        }
    }

    public static String filter(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        String result = input;
        for (Pattern pattern : PROFANITY_PATTERNS) {
            result = pattern.matcher(result).replaceAll(REPLACEMENT);
        }

        return result;
    }
}
