package com.javahood.admin.security;

import com.javahood.admin.common.BizException;
import com.javahood.admin.common.ErrorCode;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Atomic brute-force login guard (#03 lab.tally):
 * Uses ConcurrentHashMap.compute() so checking + incrementing failure counts is strictly atomic per username,
 * avoiding the classic get()-then-put() lost-update race condition.
 */
@Component
public class LoginAttemptGuard {

    public static final int MAX_FAILURES = 5;
    private static final long WINDOW_SECONDS = 900; // 15 minutes

    private record AttemptWindow(int failures, long expiresAtEpochSec) {
        boolean isExpired(long nowSec) {
            return nowSec >= expiresAtEpochSec;
        }
    }

    private final ConcurrentHashMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String username) {
        AttemptWindow win = attempts.get(username);
        long now = Instant.now().getEpochSecond();
        if (win != null && !win.isExpired(now) && win.failures() >= MAX_FAILURES) {
            throw new BizException(ErrorCode.TOO_MANY_REQUESTS,
                    "Account temporarily locked after " + MAX_FAILURES + " failed login attempts");
        }
    }

    public void recordFailure(String username) {
        long now = Instant.now().getEpochSecond();
        attempts.compute(username, (k, existing) -> {
            if (existing == null || existing.isExpired(now)) {
                return new AttemptWindow(1, now + WINDOW_SECONDS);
            }
            return new AttemptWindow(existing.failures() + 1, existing.expiresAtEpochSec());
        });
    }

    public void reset(String username) {
        attempts.remove(username);
    }
}
