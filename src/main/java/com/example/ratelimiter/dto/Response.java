package com.example.ratelimiter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
public class Response {
    private StatusCode code;
    private Integer value;

    public enum StatusCode {
        SUCCESS,
        ERROR_RATE_EXCEEDED
    }
}
