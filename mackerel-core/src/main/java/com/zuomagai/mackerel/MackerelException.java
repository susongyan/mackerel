package com.zuomagai.mackerel;

/**
 * @author S.S.Y
 **/
public class MackerelException extends RuntimeException {
    
    public MackerelException(String msg) {
        super(msg);
    }

    public MackerelException(String msg, Throwable e) {
        super(msg, e);
    }
}
