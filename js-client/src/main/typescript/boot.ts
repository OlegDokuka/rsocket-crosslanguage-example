import RSocketWebSocketClient from 'rsocket-websocket-client';
import {RSocketClient, Utf8Encoders} from 'rsocket-core';
import {Single} from 'rsocket-flowable';

(() => {
    const client = new RSocketClient({
        transport: new RSocketWebSocketClient(
            {
                url: 'ws://localhost:8080',
            },
            Utf8Encoders,
        ),
        setup: {
            dataMimeType:"text/plain",
            metadataMimeType:"text/plain",
            keepAlive: 5000,
            lifetime: 60000,
        },
        responder: {
            requestResponse: payload => {
                console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'background: #000; color: #ff0000');
                console.log("%c┃                     ₿ Mining ₿                      ┃", 'background: #000; color: #ff0000');
                console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'background: #000; color: #ff0000');
                setInterval(() => console.log(""), 10);
                return Single.of({
                    data: "Yes. I'm going to mine some ₿",
                    metadata: ""
                })
            }
        }
    });

    client.connect()
        .then(rsocket => {
            console.log("%c┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓", 'color: #ffff00');
            console.log("%c┃             Connected to RSocket Server             ┃", 'color: #ffff00');
            console.log("%c┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛", 'color: #ffff00');
            rsocket
                .requestStream({
                    data: "Clients Request",
                    metadata: ""
                })
                .subscribe({
                    onSubscribe: s => s.request(10),
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