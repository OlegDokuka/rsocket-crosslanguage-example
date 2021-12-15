package io.rsocket.examples;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@SpringBootApplication
@Controller
public class RSocketJavaServerApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RSocketJavaServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RSocketJavaServerApplication.class);

    }

    @Bean
   CommandLineRunner runner() {
      return (args) -> {
        Flux.interval(Duration.ofSeconds(2))
            .doOnNext((__) -> {
              connectedClients.entrySet().forEach(
                  entry -> entry.getValue().route("notifiy.me")
                      .data("hey! " + entry.getKey())
                      .send()
                      .subscribe()
              );
            })
            .subscribe();
      };
    }


    ConcurrentMap<String, RSocketRequester> connectedClients = new ConcurrentHashMap<>();

    @ConnectMapping
  public void handleSetup(@Payload String clientId, RSocketRequester rSocketRequester) {
    LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
    LOGGER.info("┃            Received new Client Connection           ┃");
    LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
    LOGGER.info(clientId);


    connectedClients.put(clientId, rSocketRequester);

    rSocketRequester
        .rsocket()
        .onClose()
        .doFinally(__ -> connectedClients.remove(clientId, rSocketRequester))
        .subscribe();
  }

  @MessageMapping("request.response")
  public Mono<String> handleResponse(@Payload String requestData) {
    return Mono.just(requestData)
        .doOnSubscribe(__ -> {
          LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
          LOGGER.info("┃         Received new Request for Response           ┃");
          LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
        })
        .map(i -> "Echo[" + i + "]");
  }

    @MessageMapping("request.stream")
    public Flux<String> handleStream(@Payload String toTake) {
        return Flux.interval(Duration.ofSeconds(1))
                   .doOnSubscribe(__ -> {
                       LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                       LOGGER.info("┃          Received new Request for Stream            ┃");
                       LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                   })
                   .onBackpressureDrop()
                   .map(i -> "Echo[" + i + "]");
    }
}
