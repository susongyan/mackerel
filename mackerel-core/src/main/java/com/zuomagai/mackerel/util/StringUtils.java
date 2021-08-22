package com.zuomagai.mackerel.util;

/**
 * @author S.S.Y
 **/
public class StringUtils {

    public static boolean isEmpty(String input) {
        return input == null || input.length() == 0;
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }
}
