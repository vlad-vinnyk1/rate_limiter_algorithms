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
    private final int bucketCapacity;
    private final int refillRateSec;
    private final int tokensPerRefill;

    private final AtomicLong tokensInBucket = new AtomicLong();
    private volatile Instant lastBucketRefill = Instant.now();

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        tryToRefillIfNeed();
        if (bucketCapacity > tokensInBucket.get()) {
            tokensInBucket.incrementAndGet();
            return new Response(SUCCESS, f.apply(numb));
        } else {
            return new Response(ERROR_RATE_EXCEEDED, null);
        }
    }

    private void tryToRefillIfNeed() {
        if (isCapacityExceeded()) {
            val thisRequest = Instant.now();
            long tokensToRefill = calculateTokensToRefill(thisRequest);
            if (tokensToRefill > 0) {
                long occupationAfterRefill = tokensToRefill >= tokensInBucket.get() ? 0 : tokensInBucket.get() - tokensToRefill;
                tokensInBucket.set(occupationAfterRefill);
                lastBucketRefill = thisRequest;
            }
        }
    }

    private boolean isCapacityExceeded() {
        return tokensInBucket.get() >= bucketCapacity;
    }

    private long calculateTokensToRefill(Instant thisRequest) {
        long secondsFromLastRefill = Duration.between(lastBucketRefill, thisRequest).toSeconds();
        return (secondsFromLastRefill / refillRateSec) * tokensPerRefill;
    }
}
