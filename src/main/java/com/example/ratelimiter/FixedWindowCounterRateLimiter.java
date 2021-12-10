package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class FixedWindowCounterRateLimiter {
    private final int windowCapacity;
    private final int windowRefreshInSec;
    private Instant lastWindowReset = Instant.now();

    private volatile AtomicInteger currentWindowSize = new AtomicInteger();

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        resetWindow();
        if (windowCapacity > currentWindowSize.get()) {
            currentWindowSize.incrementAndGet();

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
        if (windowCapacity <= currentWindowSize.get() && diffInSeconds >= windowRefreshInSec) {
            currentWindowSize.set(0);
            lastWindowReset = thisCall;
        }
    }
}
