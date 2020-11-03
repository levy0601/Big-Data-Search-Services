package com.example.demo.expection;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
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
