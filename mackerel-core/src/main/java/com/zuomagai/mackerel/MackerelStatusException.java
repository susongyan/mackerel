package com.zuomagai.mackerel;

/**
 * @author susongyan
 **/
public class MackerelStatusException extends RuntimeException {

    public MackerelStatusException(String msg) {
        super(msg);
    }

    public MackerelStatusException(String msg, Throwable e) {
        super(msg, e);
    }
}
