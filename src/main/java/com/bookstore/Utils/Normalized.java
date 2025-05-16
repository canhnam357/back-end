package com.bookstore.Utils;

public class Normalized {
    public static String removeVietnameseAccents(String str) {

        String result = remove(str);

        if (result == null) return null;

        StringBuilder search_word = new StringBuilder("%");
        for (char c : result.toCharArray()) {
            search_word.append(c);
        }

        search_word.append("%");

        return search_word.toString();
    }

    public static String remove(String str) {
        if (str == null) return null;

        String[] vietnameseChars = {
                "àáảãạăằắẳẵặâầấẩẫậ",
                "èéẻẽẹêềếểễệ",
                "ìíỉĩị",
                "òóỏõọôồốổỗộơờớởỡợ",
                "ùúủũụưừứửữự",
                "ỳýỷỹỵ",
                "đ"
        };
        String[] replacements = {"a", "e", "i", "o", "u", "y", "d"};

        String result = str.toLowerCase();
        for (int i = 0; i < vietnameseChars.length; i++) {
            for (char c : vietnameseChars[i].toCharArray()) {
                result = result.replace(c, replacements[i].charAt(0));
            }
        }
        return result;
    }
}
