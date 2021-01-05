package io.rsocket.examples;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
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

  @Bean
  public RSocketServerCustomizer rSocketServerCustomizer() {
    return rSocketServer -> rSocketServer.fragment(128);
  }

  @MessageMapping("request.stream")
  public Flux<String> handleStream(@Payload String requestData) {
    return Flux.range(0, 100)
        .map(i -> {
          final int numbersCnt = ThreadLocalRandom.current().nextInt(128, 512);
          final StringBuilder builder = new StringBuilder()
              .append("[").append(numbersCnt)
              .append("]").append("\n");

          for (int j = 0; j < numbersCnt; j++) {
            builder.append(j).append(" | ");
          }

          return builder;
        })
        .doOnSubscribe(__ -> {
          LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
          LOGGER.info("┃          Received new Request for Stream            ┃");
          LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
        })
        .map(i -> "Echo[\n" + i + "\n    ]")
        .log();
  }
}
