package io.rsocket.examples.tracing;

import static io.rsocket.examples.tracing.PayloadUtils.cleanTracingMetadata;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadata.Entry;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.util.ByteBufPayload;
import io.rsocket.util.DefaultPayload;
import io.rsocket.util.RSocketProxy;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.TraceContext;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.propagation.Propagator;
import reactor.core.publisher.Mono;

public class RequesterTracingRSocket extends RSocketProxy {

  private static final Log log = LogFactory.getLog(RequesterTracingRSocket.class);

  final Propagator propagator;
  final Propagator.Setter<ByteBuf> setter;
  // final MessageSpanCustomizer messageSpanCustomizer;
  final Tracer tracer;

//
//  public ResponderTracingRSocket(RSocket source, Propagator propagator,
//      Propagator.Setter<ByteBuf> setter, Propagator.Getter<ByteBuf> getter, MessageSpanCustomizer messageSpanCustomizer, Tracer tracer) {

  public RequesterTracingRSocket(RSocket source, Propagator propagator,
      Propagator.Setter<ByteBuf> setter, Tracer tracer) {
    super(source);
    this.propagator = propagator;
    this.setter = setter;
//    this.messageSpanCustomizer = messageSpanCustomizer;
    this.tracer = tracer;
  }

  @Override
  public Mono<Void> fireAndForget(Payload payload) {
    return Mono.deferContextual(contextView -> {
      Span.Builder spanBuilder = this.tracer.spanBuilder();
      // TODO: customizing
      //  spanBuilder = this.messageSpanCustomizer.customizeSend(spanBuilder, message, channel);
     if (contextView.hasKey(TraceContext.class)) {
        spanBuilder = spanBuilder.setParent(contextView.get(TraceContext.class));
      } else  if (this.tracer.currentSpan() != null) {
      // a use case where e.g. rSocketRequest is used outside spring
       spanBuilder = spanBuilder.setParent(this.tracer.currentSpan().context());
      }

      /*

      Span span = tracer.nextSpan().start();
      try (SpanInScope ws = tracer.withSpanInScope(span)) {
        rSocketRequester.fireAndForget(...);
      } finally {
        span.end();
      }


       */


      Span span = spanBuilder.kind(Span.Kind.PRODUCER).start();
      if (log.isDebugEnabled()) {
        log.debug("Extracted result from context or thread local " + span);
      }
      final Payload newPayload = cleanTracingMetadata(payload, new HashSet<>(propagator.fields()));
      this.propagator.inject(span.context(), newPayload.sliceMetadata(), this.setter);

      return super.fireAndForget(payload)
          .doOnError(span::error)
          .doFinally(signalType -> span.end());
    });
  }

  @Override
  public Mono<Payload> requestResponse(Payload payload) {
    return Mono.deferContextual(contextView -> {
      Span.Builder spanBuilder = this.tracer.spanBuilder();
      // TODO: customizing
      //  spanBuilder = this.messageSpanCustomizer.customizeSend(spanBuilder, message, channel);
      if (contextView.hasKey(TraceContext.class)) {
        spanBuilder = spanBuilder.setParent(contextView.get(TraceContext.class));
      } else if (this.tracer.currentSpan() != null) {
        // a use case where e.g. rSocketRequest is used outside spring
        spanBuilder = spanBuilder.setParent(this.tracer.currentSpan().context());
      }

        Span span = spanBuilder.kind(Span.Kind.PRODUCER).start();
      if (log.isDebugEnabled()) {
        log.debug("Extracted result from context or thread local " + span);
      }
      final Payload newPayload = cleanTracingMetadata(payload, new HashSet<>(propagator.fields()));
      this.propagator.inject(span.context(), newPayload.metadata(), this.setter);

      return super.requestResponse(newPayload)
          .doOnError(span::error)
          .doFinally(signalType -> span.end());
    });
  }

}


