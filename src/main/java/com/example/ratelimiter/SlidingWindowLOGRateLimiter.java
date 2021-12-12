package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.example.ratelimiter.dto.Response.StatusCode.*;

@RequiredArgsConstructor
public class SlidingWindowLOGRateLimiter {
    private final int capacity;
    private final int windowSizeSec;
    private volatile SortedSet<Instant> requests = Collections.synchronizedSortedSet(new TreeSet<>());

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        Instant thisRequestTime = Instant.now();
        leaveWithinWindow(thisRequestTime);
        if (capacity > requests.size()) {
            requests.add(thisRequestTime);
            return new Response(SUCCESS, f.apply(numb));
        } else {
            return new Response(ERROR_RATE_EXCEEDED, numb);
        }
    }

    private synchronized void leaveWithinWindow(Instant thisRequestTime) {
        SortedSet<Instant> withinWindow = requests.tailSet(thisRequestTime.minusSeconds(windowSizeSec));
        requests = Collections.synchronizedSortedSet(withinWindow);
    }
}
