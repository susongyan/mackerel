package com.zuomagai.mackerel.util;

/**
 * @author susongyan
 **/
public class StringUtils {

    public static boolean isEmpty(String input) {
        return input == null || input.isEmpty();
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }
}
