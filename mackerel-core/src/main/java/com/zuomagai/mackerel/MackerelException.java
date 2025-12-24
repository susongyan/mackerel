package com.zuomagai.mackerel;

/**
 * @author susongyan
 **/
public class MackerelException extends RuntimeException {

    public MackerelException(String msg) {
        super(msg);
    }

    public MackerelException(String msg, Throwable e) {
        super(msg, e);
    }
}
