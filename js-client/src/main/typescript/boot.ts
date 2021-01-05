import RSocketWebSocketClient from 'rsocket-websocket-client';
// @ts-ignore
import {RSocketClient, BufferEncoders, MESSAGE_RSOCKET_COMPOSITE_METADATA, MESSAGE_RSOCKET_ROUTING, CompositeMetadata, encodeCompositeMetadata, encodeRoute, decodeRoutes} from 'rsocket-core';
import {Single} from 'rsocket-flowable';

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
                data: Buffer.from(`YourRandomIDHere`)
            },
            dataMimeType: "text/plain",
            metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
            keepAlive: 5000,
            lifetime: 60000,
        }
    });

    client.connect()
        .then(rsocket => {
            console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'color: #ffff00');
            console.log("%c┃             Connected to RSocket Server             ┃", 'color: #ffff00');
            console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'color: #ffff00');
            rsocket
                .requestStream({
                    data: Buffer.from("Clients Request"),
                    metadata: encodeCompositeMetadata([
                        [MESSAGE_RSOCKET_ROUTING, encodeRoute("request.stream")],
                    ]),
                })
                .subscribe({
                    onSubscribe: s => {
                        // @ts-ignore
                        s.request(2147483647)
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