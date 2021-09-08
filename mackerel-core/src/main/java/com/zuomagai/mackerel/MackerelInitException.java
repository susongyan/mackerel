package com.zuomagai.mackerel;

/**
 * @author susongyan
 **/
public class MackerelInitException extends RuntimeException {
    
    public MackerelInitException(String msg) {
        super(msg);
    }

    public MackerelInitException(String msg, Throwable e) {
        super(msg, e);
    }
}
