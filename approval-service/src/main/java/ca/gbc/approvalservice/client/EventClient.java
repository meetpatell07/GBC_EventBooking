package ca.gbc.approvalservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface EventClient {

    @PutExchange("/api/events/{eventId}/status")
    void updateEventStatus(@PathVariable("eventId") String eventId, @PathVariable("status") String status);

    @DeleteExchange("/api/events/{eventId}")
    void deleteEvent(@PathVariable("eventId") String eventId);

    @GetExchange("/api/events/{eventId}/exists")
    boolean checkEventExists(@PathVariable("eventId") String eventId);


}
