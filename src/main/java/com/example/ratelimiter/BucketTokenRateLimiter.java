package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
public class BucketTokenRateLimiter {
    private final int capacity;
    private final int refillRateInSeconds;
    private final int numberToRefill;

    private volatile long occupied;
    private volatile Instant lastRefill = Instant.now();

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        refillIfNeeded();
        if (capacity > occupied) {
            synchronized (BucketTokenRateLimiter.class) {
                occupied++;
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

    private void refillIfNeeded() {
        if (occupied >= capacity) {
            val thisCall = Instant.now();
            synchronized (BucketTokenRateLimiter.class) {
                long diffInSeconds = Duration.between(lastRefill, thisCall).toSeconds();
                long tokensToRefill = (diffInSeconds / refillRateInSeconds) * numberToRefill;
                if (tokensToRefill > 0) {
                    occupied = tokensToRefill >= occupied ? 0 : occupied - tokensToRefill;
                    lastRefill = thisCall;
                }
            }
        }
    }
}
