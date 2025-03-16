package ca.gbc.approvalservice.client;

import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

@Slf4j
public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/api/users/{userId}/role")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    String getUserRole(@PathVariable("userId") Integer userId);

    @GetExchange("/api/users/{userId}/type")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackMethod")
    @Retry(name = "user")
    default String getUserType(@PathVariable("userId") Integer userId) {
        // Simulate a failure
        throw new RuntimeException("Simulated failure for testing fallback");
    }


    default String fallbackMethod(Integer userId, Throwable throwable) {
        log.info("Fallback executed for user fetch. User ID: {}, Reason: {}", userId, throwable.getMessage());
        return "UNKNOWN";
    }


}
