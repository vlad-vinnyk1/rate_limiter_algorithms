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
        this.windowSizeSec = windowSizeSec;
        this.capacity = capacity;
        firstWindow = Instant.now();
    }

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        Instant thisRequestTime = Instant.now();
        long count;
        if (thisRequestTime.getEpochSecond() - firstWindow.getEpochSecond() <= windowSizeSec) {
            count = requests.tailSet(firstWindow).size();
        } else {
            long partition = partition(thisRequestTime);
            long prevWindowPercent = 100 - ((100 * partition) / windowSizeSec);
            Instant thisWindowStart = Instant.ofEpochSecond(thisRequestTime.getEpochSecond() - partition);
            Instant prevWindowStart = Instant.ofEpochSecond(thisWindowStart.getEpochSecond() - windowSizeSec);
            shrink(prevWindowStart);
            int previousRequestsCount = requests.subSet(prevWindowStart, thisWindowStart).size();
            int thisRequestsCount = requests.tailSet(thisWindowStart).size();
            count = (previousRequestsCount * prevWindowPercent) / 100 + thisRequestsCount;
        }

        if (count >= capacity) {
            return new Response(ERROR_RATE_EXCEEDED, numb);
        } else {
            requests.add(thisRequestTime);
            return new Response(SUCCESS, f.apply(numb));
        }
    }

    private void shrink(Instant prevWindowStart) {
        SortedSet<Instant> withinWindow = requests.tailSet(prevWindowStart);
        requests = Collections.synchronizedSortedSet(withinWindow);
    }

    private synchronized long partition(Instant thisRequestTime) {
        long diffInSeconds = Duration.between(firstWindow, thisRequestTime).toSeconds();
        return diffInSeconds % windowSizeSec;
    }

}