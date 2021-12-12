package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import com.example.ratelimiter.dto.ResponseUtils;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class BucketTokenRateLimiter {
    private final int capacity;
    private final int refillRateInSeconds;
    private final int numberToRefill;

    private final AtomicLong occupied = new AtomicLong();
    private volatile Instant lastRefill = Instant.now();

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        refillIfNeeded();
        if (capacity > occupied.get()) {
            occupied.incrementAndGet();
            return ResponseUtils.toResponse(Response.StatusCode.SUCCESS, f.apply(numb));
        } else {
            return ResponseUtils.toResponse(Response.StatusCode.ERROR_RATE_EXCEEDED, numb);
        }

    }

    private void refillIfNeeded() {
        if (isCapacityExceeded()) {
            val thisRequest = Instant.now();
            long tokensToRefill = calculateTokensToRefill(thisRequest);
            if (tokensToRefill > 0) {
                occupied.set(tokensToRefill >= occupied.get() ? 0 : occupied.get() - tokensToRefill);
                lastRefill = thisRequest;
            }
        }
    }

    private boolean isCapacityExceeded() {
        return occupied.get() >= capacity;
    }

    private long calculateTokensToRefill(Instant thisCall) {
        long diffInSeconds = Duration.between(lastRefill, thisCall).toSeconds();
        return (diffInSeconds / refillRateInSeconds) * numberToRefill;
    }
}
