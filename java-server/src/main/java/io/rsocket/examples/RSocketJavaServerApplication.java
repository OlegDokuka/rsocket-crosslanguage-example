package io.rsocket.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

@SpringBootApplication
@Controller
public class RSocketJavaServerApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RSocketJavaServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RSocketJavaServerApplication.class);
    }

    @ConnectMapping
    public void handleSetup(RSocketRequester rSocketRequester) {
        rSocketRequester
                .route("bitcoin.mine")
                .data("Please. Mine Bitcoins")
                .retrieveMono(String.class)
                .doOnNext(responseData -> {
                    LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                    LOGGER.info("┃            Received Mining Response:                ┃");
                    LOGGER.info("┃            {}            ┃", responseData);
                    LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                })
                .subscribe();
    }

    @MessageMapping("request.stream")
    public Flux<String> handleStream(@Payload String requestData) {
        return Flux.range(0, 100)
                   .doFirst(() -> {
                       LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                       LOGGER.info("┃          Received new Request for Stream            ┃");
                       LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                   })
                   .map(i -> "Echo[" + i + "]");
    }
}
