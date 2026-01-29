package com.icoder.core.utils;

import org.springframework.stereotype.Component;

@Component
public class ConvertFromString {

    public Long toLong(String s) {
        return Long.parseLong(s);
    }

    public Integer toInteger(String s) {
        return Integer.parseInt(s);
    }
}
