package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.example.ratelimiter.dto.Response.StatusCode.ERROR_RATE_EXCEEDED;
import static com.example.ratelimiter.dto.Response.StatusCode.SUCCESS;

@Slf4j
public class SlidingWindowCounterRateLimiter {
    private final Instant firstWindow;

    private final long windowSizeSec;

    private final int capacity;

    private volatile SortedSet<Instant> requests = Collections.synchronizedSortedSet(new TreeSet<>());

    public SlidingWindowCounterRateLimiter(int capacity, int windowSizeSec) {
        if (windowSizeSec <= 1) {
            throw new IllegalArgumentException("Window size should be greater than 1");
        }
        this.windowSizeSec = windowSizeSec;
        this.capacity = capacity;
        firstWindow = Instant.now();
    }

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        Instant thisRequestTime = Instant.now();
        long count;
        if (isStillInFirstWindow(thisRequestTime)) {
            count = requests.tailSet(firstWindow).size();
        } else {
            long secondsFromWindowStart = toSecondsFromWindowStart(thisRequestTime);

            Instant thisWindowStart = Instant.ofEpochSecond(thisRequestTime.getEpochSecond() - secondsFromWindowStart);
            Instant prevWindowStart = Instant.ofEpochSecond(thisWindowStart.getEpochSecond() - windowSizeSec);

            int thisWindowRequestsCount = requests.tailSet(thisWindowStart).size();
            int prevWindowRequestsCount = requests.subSet(prevWindowStart, thisWindowStart).size();

            long prevRequestCountAdjusted = adjustRequestCount(prevWindowRequestsCount, secondsFromWindowStart);
            count = prevRequestCountAdjusted + thisWindowRequestsCount;

            clenupOutdated(prevWindowStart);
        }

        if (count >= capacity) {
            return new Response(ERROR_RATE_EXCEEDED, numb);
        } else {
            requests.add(thisRequestTime);
            return new Response(SUCCESS, f.apply(numb));
        }
    }

    private boolean isStillInFirstWindow(Instant thisRequestTime) {
        return Duration.between(firstWindow, thisRequestTime).getSeconds() <= windowSizeSec;
    }

    private synchronized long toSecondsFromWindowStart(Instant thisRequestTime) {
        return Duration.between(firstWindow, thisRequestTime).toSeconds() % windowSizeSec;
    }

    private long adjustRequestCount(int requestCount, long secFromWindowStart) {
        long percentsToTake = 100 - ((100 * secFromWindowStart) / windowSizeSec);
        return (requestCount * percentsToTake) / 100;
    }

    private void clenupOutdated(Instant prevWindowStart) {
        SortedSet<Instant> withinWindow = requests.tailSet(prevWindowStart);
        requests = Collections.synchronizedSortedSet(withinWindow);
    }
}