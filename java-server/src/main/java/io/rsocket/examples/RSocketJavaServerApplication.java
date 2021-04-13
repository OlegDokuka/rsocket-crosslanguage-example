package io.rsocket.examples;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.Tracer.SpanInScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketConnectorConfigurer;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketRequester.Builder;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@Controller
public class RSocketJavaServerApplication {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RSocketJavaServerApplication.class);

  public static void main(String[] args) {
    Hooks.onOperatorDebug();
    final ConfigurableApplicationContext context = SpringApplication
        .run(RSocketJavaServerApplication.class);

    final Tracer tracer = context.getBean(Tracer.class);
    final Builder builder = context.getBean(Builder.class);
    final ObjectProvider<RSocketConnectorConfigurer> configurersProvider = context
        .getBeanProvider(RSocketConnectorConfigurer.class);

    // TODO: suggestion for spring boot
    configurersProvider.forEach(builder::rsocketConnector);

    final RSocketRequester rSocketRequester = builder
        .tcp("localhost", 8080);

    //X-B3-TraceId = fe0a10d69a7a4a7f
    //X-B3-SpanId = ada9ead4073817e8
    Span nextSpan = tracer.nextSpan().start();

    Mono.just(nextSpan)
        .doOnNext(span -> tracer.withSpan(span.start()))
        .flatMap(span -> {
          LOGGER.info("HELLO");
          return rSocketRequester.route("example.api.request.replay")
              //        .metadata("fe0a10d69a7a4a7f", new MimeType("X-B3-TraceId") {
              //          @Override
              //          public String toString() {
              //            return "X-B3-TraceId";
              //          }
              //        })
              //        .metadata("ada9ead4073817e8", new MimeType("X-B3-SpanId") {
              //          @Override
              //          public String toString() {
              //            return "X-B3-SpanId";
              //          }
              //        })
              .data("test")
              .retrieveMono(String.class)
              .log("client");
        })
        // You need to update the context manually since we're outside of WebFlux
        .contextWrite(ctx -> ctx.put(TraceContext.class, nextSpan.context()))
        .doFinally(signalType -> nextSpan.end())
        .block();
  }

  @ConnectMapping
  public void handleSetup(RSocketRequester rSocketRequester) {
    LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
    LOGGER.info("┃               Received new Connection               ┃");
    LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
  }

  @MessageMapping("example.api.request.fire")
  public void handleFnf(@Payload String requestData) {
    LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
    LOGGER.info("┃          Received new Request for FnF               ┃");
    LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
    LOGGER.info(requestData);
  }

  @MessageMapping("example.api.request.stream")
  public Flux<String> handleStream(@Payload String requestData) {
    return Flux.range(1, 100)
        .doOnSubscribe(__ -> {
          LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
          LOGGER.info("┃          Received new Request for Stream            ┃");
          LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
        })
        .map(i -> "Echo[" + i + "]");
  }

  @MessageMapping("example.api.request.replay")
  public Mono<String> handleReplay(@Payload String requestData) {
    return Mono.just("Echo: " + requestData)
        .doOnSubscribe(__ -> {
          LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
          LOGGER.info("┃          Received new Request for Response          ┃");
          LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
        })
        .subscribeOn(Schedulers.single())
        .log("server")
        .delayElement(Duration.ofSeconds(2));
  }
}
