package ca.gbc.bookingservice.client;

import groovy.util.logging.Slf4j;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

@Slf4j
public interface RoomClient {
    Logger log = LoggerFactory.getLogger(RoomClient.class);

    @GetExchange("/api/room/{roomId}/availability")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackMethod")
    @Retry(name = "room")
    boolean getRoomAvailability(@PathVariable Integer roomId);

    @GetExchange("/api/room/{roomId}/exists")
    @CircuitBreaker(name = "room", fallbackMethod = "fallbackMethod")
//    @Retry(name = "room")
    boolean checkRoomExists(@PathVariable Integer roomId);

    default boolean fallbackMethod(Integer roomId, Throwable throwable) {
        log.warn("Fallback executed for room check. Room ID: {}, Reason: {}", roomId, throwable.getMessage());
        return false; // Return false by default, as rooms may be considered unavailable or non-existent in case of failure
    }

}
