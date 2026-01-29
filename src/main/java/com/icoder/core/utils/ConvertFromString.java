package com.icoder.core.utils;

import org.springframework.stereotype.Component;

@Component
public class ConvertFromString {

    public Long toLong(String s) {
        if (s == null) throw new IllegalArgumentException("String cannot be null");
        return Long.parseLong(s);
    }

    public Integer toInteger(String s) {
        if (s == null) throw new IllegalArgumentException("String cannot be null");
        return Integer.parseInt(s);
    }
}