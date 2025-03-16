package ca.gbc.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user", url = "${user.service.url}")
public interface UserClient {

    // Retrieves the user's role based on userId
    @GetMapping("/api/users/{userId}/type")
    String getUserType(@PathVariable("userId") Integer userId);
}
