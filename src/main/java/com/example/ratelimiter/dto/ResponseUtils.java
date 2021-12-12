package com.example.ratelimiter.dto;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ResponseUtils {
    public static Response toResponse(Response.StatusCode success, Integer f) {
        return Response.builder()
                .code(success)
                .value(f)
                .build();
    }
}
