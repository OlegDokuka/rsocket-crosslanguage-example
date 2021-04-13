package io.rsocket.examples.tracing;

import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

class SpanAndScope {

  final Span span;

  final Tracer.SpanInScope scope;

  SpanAndScope(Span span, Tracer.SpanInScope scope) {
    this.span = span;
    this.scope = scope;
  }

}
