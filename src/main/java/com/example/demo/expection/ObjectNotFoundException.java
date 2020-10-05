package com.example.demo.expection;

import org.springframework.lang.Nullable;

public class ObjectNotFoundException extends Exception{
    @Nullable
    private final String reason;

    public ObjectNotFoundException(@Nullable String reason) {
        this.reason = reason;
    }

    public String getReason(){
        return this.reason;
    }
}
