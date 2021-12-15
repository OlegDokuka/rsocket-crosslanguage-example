package io.rsocket.examples;


import io.rsocket.SocketAcceptor;
import java.net.URI;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class SprintBootRSocketTest {


  @Autowired
  RSocketRequester.Builder builder;
  @Autowired
  RSocketStrategies strategies;



  @Test
  void someTest() {
    final String id = UUID.randomUUID().toString();
    final MyTestController myTestController = new MyTestController();
    final RSocketRequester rSocketRequester = builder.setupData(id)
        .rsocketConnector(connector -> {
          connector.acceptor(RSocketMessageHandler.responder(strategies, myTestController));
        })
        .websocket(URI.create("ws://localhost:8080"));

    rSocketRequester.rsocketClient().source().subscribe();

    myTestController.receivedMessage.asFlux()
        .as(StepVerifier::create)
        .expectSubscription()
        .expectNextMatches(message -> message.contains(id))
        .expectNextCount(5)
        .expectNextMatches(message -> message.contains(id))
        .thenCancel()
        .verify();
  }


  @Test
  void someTest2() {
    final String id = UUID.randomUUID().toString();
    final RSocketRequester rSocketRequester = builder.setupData(id)
        .websocket(URI.create("ws://localhost:8080"));

    rSocketRequester
        .route("request.stream")
        .data("hello")
        .retrieveFlux(String.class)
        .log()
        .as(StepVerifier::create)
        .expectSubscription()
        .expectNextCount(5)
        .thenCancel()
        .verify();
  }


  @Controller
  static class MyTestController {

    final Sinks.Many<String> receivedMessage = Sinks.many().unicast().onBackpressureBuffer();

    @MessageMapping("notifiy.me")
    void handleMessage(String message) {
      receivedMessage.tryEmitNext(message);
    }
  }

}
