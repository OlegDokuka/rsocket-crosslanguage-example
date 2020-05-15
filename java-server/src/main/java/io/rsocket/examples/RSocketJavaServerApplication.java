package io.rsocket.examples;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.WebsocketServerTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static io.rsocket.util.ByteBufPayload.create;

@SpringBootApplication
public class RSocketJavaServerApplication {
    private static final Logger LOGGER =
        LoggerFactory.getLogger(RSocketJavaServerApplication.class);

    public static void main(String[] args) {
        RSocketServer
            .create((setup, sendingSocket) -> {
                LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                LOGGER.info("┃               Received new Connection               ┃");
                LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");

                sendingSocket.requestResponse(create("Please. Mine Bitcoins"))
                             .doOnNext(payload -> {
                                 LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                                 LOGGER.info("┃            Received Mining Response:                ┃");
                                 LOGGER.info("┃            {}            ┃", payload.getDataUtf8());
                                 LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                             })
                             .subscribe();

                return Mono.just(new RSocket() {
                    @Override
                    public Flux<Payload> requestStream(Payload payload) {
                        return Flux.range(0, 100)
                                   .doFirst(() -> {
                                       LOGGER.info("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                                       LOGGER.info("┃          Received new Request for Stream            ┃");
                                       LOGGER.info("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                                   })
                                   .map(i -> DefaultPayload.create("Echo[" + i + "]"));
                    }
                });
            })
            .bindNow(WebsocketServerTransport.create(8080))
            .onClose()
            .block();


    }
}
