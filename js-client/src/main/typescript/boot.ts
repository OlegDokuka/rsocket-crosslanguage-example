import RSocketWebSocketClient from 'rsocket-websocket-client';
// @ts-ignore
import { RSocketClient, BufferEncoders, MESSAGE_RSOCKET_COMPOSITE_METADATA, MESSAGE_RSOCKET_ROUTING, CompositeMetadata, encodeCompositeMetadata, encodeRoute, decodeRoutes} from 'rsocket-core';
import {Payload, ReactiveSocket} from "rsocket-types"
import {Flowable, Single} from 'rsocket-flowable';
import {every} from "rsocket-flowable";

(() => {
    const client = new RSocketClient<Buffer, Buffer>({
        transport: new RSocketWebSocketClient(
            {
                url: 'ws://localhost:8080',
                wsCreator: (url: string) => new WebSocket(url),
                debug: true,
            },
            BufferEncoders,
        ),
        setup: {
            payload: {
                data: Buffer.from(`YourRandomIDHere ` + Date())
            },
            dataMimeType: "text/plain",
            metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
            keepAlive: 5000,
            lifetime: 60000,
        },
        responder: {
            fireAndForget(payload: Payload<Buffer, Buffer>) {
                for (const entryPayload of new CompositeMetadata(payload.metadata)) {
                    if (entryPayload.mimeType == MESSAGE_RSOCKET_ROUTING.string) {
                        // @ts-ignore
                        const routes: IterableIterator<string> = decodeRoutes(entryPayload.content);
                        for (const route of routes) {
                            if (route == "notifiy.me") {
                                console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'background: #000; color: #ff0000');
                                console.log("%c┃                     ₿ NOTIFICATION ₿                ┃", 'background: #000; color: #ff0000');
                                console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'background: #000; color: #ff0000');
                                console.log(payload.data.toString(), 'background: #000; color: #ff0000');
                            }
                        }
                    }
                }
            },
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
            },

            requestStream(payload: Payload<Buffer, Buffer>): Flowable<Payload<Buffer, Buffer>> {
                for (const headerItem of new CompositeMetadata(payload.metadata)) {
                    if (headerItem.mimeType == MESSAGE_RSOCKET_ROUTING.string) {
                        // @ts-ignore
                        const routes: IterableIterator<string> = decodeRoutes(headerItem.content);
                        for (const route of routes) {
                            if (route === "browser.endpoint.give.me.something") {
                                return every(1000)
                                    .map(tick => ({
                                        data: Buffer.from(JSON.stringify({
                                            tickId: tick,
                                            message: `Hello # ${tick}`
                                        }))
                                    } as Payload<Buffer, Buffer>));
                            }
                        }
                    }
                }

                return Flowable.error(new Error("Route is now found"));
            }
        }
    });

    client.connect()
        .then((rsocket: ReactiveSocket<Buffer, Buffer>) => {
            console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'color: #ffff00');
            console.log("%c┃             Connected to RSocket Server             ┃", 'color: #ffff00');
            console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'color: #ffff00');
            // rsocket
            //     .requestStream({
            //         data: Buffer.from("Clients Request"),
            //         metadata: encodeCompositeMetadata([
            //             [MESSAGE_RSOCKET_ROUTING, encodeRoute("notifications")],
            //         ]),
            //     })
            //     .subscribe({
            //         onSubscribe: s => {
            //             // @ts-ignore
            //             // window.exposedSubscription = s;
            //
            //             s.request(2147483647);
            //         },
            //         onNext: (p) => {
            //             console.log("┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓");
            //             console.log(`┃     Response: { Data: ${p.data}, Metadata: ${p.metadata} }     ┃`);
            //             console.log("┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛");
            //         },
            //         onError: (e) => console.error(e),
            //         onComplete: () => console.log("Done")
            //     });
        });
})();