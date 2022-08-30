package com.zuomagai.mackerel;

/**
 * @author S.S.Y
 **/
public class MackerelStatusException extends RuntimeException {

    public MackerelStatusException(String msg) {
        super(msg);
    }

    public MackerelStatusException(String msg, Throwable e) {
        super(msg, e);
    }
}
