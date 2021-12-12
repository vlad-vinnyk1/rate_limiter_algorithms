package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import com.example.ratelimiter.dto.ResponseUtils;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class FixedWindowCounterRateLimiter {
    private final int windowCapacity;
    private final int windowRefreshRateInSeconds;
    private Instant lastWindowReset = Instant.now();

    private final AtomicInteger currentWindowCounter = new AtomicInteger();

    public Response rateLimitFunc(Integer numb, Function1<Integer, Integer> f) {
        resetWindow();
        if (windowCapacity > currentWindowCounter.get()) {
            currentWindowCounter.incrementAndGet();
            return ResponseUtils.toResponse(Response.StatusCode.SUCCESS, f.apply(numb));
        } else {
            return ResponseUtils.toResponse(Response.StatusCode.ERROR_RATE_EXCEEDED, numb);
        }
    }

    private void resetWindow() {
        Instant currentRequest = Instant.now();
        if (isCapacityExceeded() && isTimeToReset(currentRequest)) {
            currentWindowCounter.set(0);
            lastWindowReset = currentRequest;
        }
    }

    private boolean isTimeToReset(Instant currentRequest) {
        long lastResetAndCurrentRequestDif = Duration.between(lastWindowReset, currentRequest).toSeconds();
        return lastResetAndCurrentRequestDif >= windowRefreshRateInSeconds;
    }

    private boolean isCapacityExceeded() {
        return windowCapacity <= currentWindowCounter.get();
    }
}
