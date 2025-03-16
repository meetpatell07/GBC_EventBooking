package ca.gbc.apigateway.routes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;

@Slf4j
@Configuration
public class Routes {

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.room.url}")
    private String roomServiceUrl;

    @Value("${services.booking.url}")
    private String bookingServiceUrl;

    @Value("${services.event.url}")
    private String eventServiceUrl;

    @Value("${services.approval.url}")
    private String approvalServiceUrl;



    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        log.info("Initializing user service route with URL: {}", userServiceUrl );

        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/users"), request -> {
                    log.info("Received request for user service: {}", request.uri());
                    ServerResponse response = HandlerFunctions.http(userServiceUrl).handle(request);
                    log.info("Response status: {}", response.statusCode());
                    return response;

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("userServiceCircuitBreaker", URI.create("forward:/fallbackRoute")) )
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roomServiceRoute() {
        log.info("Initializing room service route with URL: {}", roomServiceUrl);

        return GatewayRouterFunctions.route("room-service")
                .route(RequestPredicates.path("/api/room"), request -> {
                    log.info("Received request for room service: {}", request.uri());
                    ServerResponse response = HandlerFunctions.http(roomServiceUrl).handle(request);
                    log.info("Response status: {}", response.statusCode());
                    return response;

                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("roomServiceCircuitBreaker", URI.create("forward:/fallbackRoute")) )
                .build();

    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceRoute() {
        log.info("Initializing booking service route with URL: {}", bookingServiceUrl);

        return GatewayRouterFunctions.route("booking-service")
                .route(RequestPredicates.path("/api/bookings"), request -> {
                    log.info("Received request for booking service: {}", request.uri());
                    ServerResponse response = HandlerFunctions.http(bookingServiceUrl).handle(request);
                    log.info("Response status: {}", response.statusCode());
                    return response;
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("bookingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")) )
                .build();

    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceRoute() {
        log.info("Initializing event service route with URL: {}", eventServiceUrl);

        return GatewayRouterFunctions.route("event-service")
                .route(RequestPredicates.path("/api/events"), request -> {
                    log.info("Received request for event service: {}", request.uri());
                    ServerResponse response = HandlerFunctions.http(eventServiceUrl).handle(request);
                    log.info("Response status: {}", response.statusCode());
                    return response;
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("eventServiceCircuitBreaker", URI.create("forward:/fallbackRoute")) )
                .build();

    }

    @Bean
    public RouterFunction<ServerResponse> approvalServiceRoute() {
        log.info("Initializing approval service route with URL: {}", approvalServiceUrl);

        return GatewayRouterFunctions.route("approval-service")
                .route(RequestPredicates.path("/api/approvals/process"), request -> {
                    log.info("Received request for approval service: {}", request.uri());
                    ServerResponse response = HandlerFunctions.http(approvalServiceUrl).handle(request);
                    log.info("Response status: {}", response.statusCode());
                    return response;
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("approvalServiceCircuitBreaker", URI.create("forward:/fallbackRoute")) )
                .build();

    }




    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        log.info("Forwarding Swagger request for user-service to: {}", userServiceUrl + "/api-docs");

        return GatewayRouterFunctions.route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"),
                        HandlerFunctions.http(userServiceUrl))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roomServiceSwaggerRoute() {
        log.info("Forwarding Swagger request for room-service to: {}", roomServiceUrl + "/api-docs");

        return GatewayRouterFunctions.route("room_service_swagger")
                .route(RequestPredicates.path("/aggregate/room-service/v3/api-docs"),
                        HandlerFunctions.http(roomServiceUrl))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceSwaggerRoute() {
        log.info("Forwarding Swagger request for booking-service to: {}", bookingServiceUrl + "/api-docs");

        return GatewayRouterFunctions.route("booking_service_swagger")
                .route(RequestPredicates.path("/aggregate/booking-service/v3/api-docs"),
                        HandlerFunctions.http(bookingServiceUrl))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceSwaggerRoute() {
        log.info("Forwarding Swagger request for event-service to: {}", eventServiceUrl + "/api-docs");

        return GatewayRouterFunctions.route("event_service_swagger")
                .route(RequestPredicates.path("/aggregate/event-service/v3/api-docs"),
                        HandlerFunctions.http(eventServiceUrl))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> approvalServiceSwaggerRoute() {
        log.info("Forwarding Swagger request for approval-service to: {}", approvalServiceUrl + "/api-docs");

        return GatewayRouterFunctions.route("approval_service_swagger")
                .route(RequestPredicates.path("/aggregate/approval-service/v3/api-docs"),
                        HandlerFunctions.http(approvalServiceUrl))
                .filter(setPath("/api-docs"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return GatewayRouterFunctions.route("fallBackRoute")
                .route(request -> !(request.path().startsWith("/swagger-ui") ||
                                request.path().startsWith("/api-docs") ||
                                request.path().startsWith("/swagger-resources") ||
                                request.path().startsWith("/webjars") ||
                                request.path().startsWith("/api-docs/swagger-config")),
                        request -> {
                            log.warn("Fallback triggered for path: {}", request.path());
                            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                    .body("Service is Temporarily Unavailable, please try again later.");
                        })
                .build();
    }




}


