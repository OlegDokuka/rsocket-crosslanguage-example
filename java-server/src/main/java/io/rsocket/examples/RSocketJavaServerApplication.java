package io.rsocket.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class RSocketJavaServerApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RSocketJavaServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RSocketJavaServerApplication.class, args);
    }

    @Controller
    public static class HelloController {

        @MessageMapping("hello.world")
        public Flux<String> helloWorldHandler(@RequestBody String message) {
            LOGGER.info("Request Stream {}", message);
            return Flux.range(0, 100)
                    .map(i -> "Hello " + i);
        }
    }
}
