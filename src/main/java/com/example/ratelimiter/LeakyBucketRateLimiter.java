package com.example.ratelimiter;

import com.example.ratelimiter.dto.Response;
import io.vavr.Function1;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.example.ratelimiter.dto.Response.StatusCode.*;

@Slf4j
public class LeakyBucketRateLimiter {
    private final ArrayBlockingQueue<Integer> queue;

    public LeakyBucketRateLimiter(int capacity, int pollingInterval) {
        this.queue = new ArrayBlockingQueue<>(capacity);
        ScheduledExecutorService dummyConsumer = new ScheduledThreadPoolExecutor(1);
        dummyConsumer.scheduleAtFixedRate(this::someDummyPollLogic, 0, pollingInterval, TimeUnit.SECONDS);
    }

    public Response limitFunc(Integer numb, Function1<Integer, Integer> f) {
        if (queue.remainingCapacity() > 0) {
            queue.add(numb);
            return new Response(SUCCESS, f.apply(numb));
        } else {
            return new Response(ERROR_RATE_EXCEEDED, numb);
        }
    }

    private static final String QUEUE_EMPTY_WARN = "The queue is empty, waiting polling interval";
    private static final String RECORD_CONSUMED_MSG_TMP = "Consumed %s";

    private void someDummyPollLogic() {
        if (!queue.isEmpty()) {
            log.info(String.format(RECORD_CONSUMED_MSG_TMP, queue.poll()));
        } else {
            log.warn(QUEUE_EMPTY_WARN);
        }
    }
}
