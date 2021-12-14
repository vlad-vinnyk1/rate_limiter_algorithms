package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.example.ratelimiter.dto.Response.StatusCode.ERROR_RATE_EXCEEDED;
import static com.example.ratelimiter.dto.Response.StatusCode.SUCCESS;

@RequiredArgsConstructor
public class BucketTokenRateLimiter {
    private final int capacity;
    private final int refillRateInSeconds;
    private final int numberToRefill;

    private final AtomicLong occupied = new AtomicLong();
    private volatile Instant lastRefill = Instant.now();

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        refillIfCapacityExceeded();
        if (capacity > occupied.get()) {
            occupied.incrementAndGet();
            return new Response(SUCCESS, f.apply(numb));
        } else {
            return new Response(ERROR_RATE_EXCEEDED, numb);
        }
    }

    private void refillIfCapacityExceeded() {
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
