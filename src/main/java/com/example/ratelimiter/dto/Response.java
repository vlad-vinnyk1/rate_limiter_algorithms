package com.example.ratelimiter.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Response {
    private StatusCode code;
    private int value;

    public static enum StatusCode {
        SUCCESS,
        ERROR_RATE_EXCEEDED
    }
}
