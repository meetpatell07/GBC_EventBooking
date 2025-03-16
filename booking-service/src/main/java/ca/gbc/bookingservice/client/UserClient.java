package ca.gbc.bookingservice.client;
import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.service.annotation.GetExchange;


@Slf4j
public interface UserClient {
    Logger log = LoggerFactory.getLogger(UserClient.class);

    @GetExchange("/api/users/{userId}/type")
    @CircuitBreaker(name = "user", fallbackMethod = "fallbackUserType")
//    @Retry(name = "user", fallbackMethod = "fallbackUserType")
    String getUserType(@PathVariable("userId") Integer userId);

    default String fallbackUserType(Integer userId, Throwable throwable) {
        if (throwable instanceof ResourceAccessException) {
            log.warn("Fallback executed due to resource access issue. User ID: {}, Reason: {}", userId, throwable.getMessage());
        } else {
            log.warn("Fallback executed due to exception. User ID: {}, Reason: {}", userId, throwable.getMessage());
        }
        return "UNKNOWN_TYPE";
    }

}
