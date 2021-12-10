package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public class FixedWindowCounterRateLimiter {
    private final int windowCapacity;
    private final int windowRefreshInSec;
    private Instant lastWindowReset = Instant.now();

    private volatile int currentWindowSize;

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        resetWindow();
        if (windowCapacity > currentWindowSize) {
            synchronized (FixedWindowCounterRateLimiter.class) {
                currentWindowSize++;
            }

            return Response.builder()
                    .code(Response.StatusCode.SUCCESS)
                    .value(f.apply(numb))
                    .build();
        } else {
            return Response.builder()
                    .code(Response.StatusCode.ERROR_RATE_EXCEEDED)
                    .value(numb)
                    .build();
        }
    }

    private void resetWindow() {
        Instant thisCall = Instant.now();
        long diffInSeconds = Duration.between(lastWindowReset, thisCall).toSeconds();
        synchronized (FixedWindowCounterRateLimiter.class) {
            if (windowCapacity <= currentWindowSize && diffInSeconds >= windowRefreshInSec) {
                currentWindowSize = 0;
                lastWindowReset = thisCall;
            }
        }
    }
}
