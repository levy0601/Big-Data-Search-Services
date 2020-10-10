package com.example.demo.expection;

import org.springframework.lang.Nullable;

public class IdExistingException extends Exception{
    @Nullable
    private final String reason;

    public IdExistingException(@Nullable String reason) {
        this.reason = reason;
    }

    public String getReason(){
        return this.reason;
    }
}
