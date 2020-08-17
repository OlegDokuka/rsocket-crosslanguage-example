import RSocketWebSocketClient from 'rsocket-websocket-client';
// @ts-ignore
import {RSocketClient, BufferEncoders, MESSAGE_RSOCKET_COMPOSITE_METADATA, MESSAGE_RSOCKET_ROUTING, CompositeMetadata, encodeCompositeMetadata, encodeRoute, decodeRoutes} from 'rsocket-core';
import {Single} from 'rsocket-flowable';

(() => {
    const client = new RSocketClient<Buffer, Buffer>({
        transport: new RSocketWebSocketClient(
            {
                url: 'ws://localhost:8080',
            },
            BufferEncoders,
        ),
        setup: {
            payload: {
                data: Buffer.from(`YourRandomIDHere`)
            },
            dataMimeType: "text/plain",
            metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
            keepAlive: 5000,
            lifetime: 60000,
        },
        responder: {
            requestResponse: payload => {
                for (const entryPayload of new CompositeMetadata(payload.metadata)) {
                    if (entryPayload.mimeType == MESSAGE_RSOCKET_ROUTING.string) {
                        // @ts-ignore
                        const routes: IterableIterator<string> = decodeRoutes(entryPayload.content);
                        for (const route of routes) {
                            if (route == "bitcoin.mine") {
                                console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'background: #000; color: #ff0000');
                                console.log("%c┃                     ₿ Mining ₿                      ┃", 'background: #000; color: #ff0000');
                                console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'background: #000; color: #ff0000');
                                setInterval(() => console.log(""), 10);
                                return Single.of({
                                    data: Buffer.from("Yes. I'm going to mine some ₿"),
                                    metadata: Buffer.from("")
                                });
                            }
                        }
                    }
                }

                return Single.error(new Error("Route Not Found"));
            }
        }
    });

    client.connect()
        .then(rsocket => {
            console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'color: #ffff00');
            console.log("%c┃             Connected to RSocket Server             ┃", 'color: #ffff00');
            console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'color: #ffff00');
            let requestRoute = "request.stream";
            rsocket
                .requestStream({
                    data: Buffer.from("Clients Request"),
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute(requestRoute)],
                    ]),
                })
                .subscribe({
                    onSubscribe: s => {
                        // @ts-ignore
                        window.exposedSubscription = s;
                    },
                    onNext: (p) => {
                        console.log("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
                        console.log(`┃     Response: { Data: ${p.data}, Metadata: ${p.metadata} }     ┃`);
                        console.log("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
                    },
                    onError: (e) => console.error(e),
                    onComplete: () => console.log("Done")
                });
        });
})();