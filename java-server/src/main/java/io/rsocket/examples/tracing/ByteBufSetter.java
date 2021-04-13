package io.rsocket.examples.tracing;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.rsocket.metadata.CompositeMetadataCodec;
import org.springframework.cloud.sleuth.propagation.Propagator;

class ByteBufSetter implements Propagator.Setter<ByteBuf> {

  final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;

  @Override
  public void set(ByteBuf carrier, String key, String value) {

    CompositeMetadataCodec
        .encodeAndAddMetadataWithCompression(
            (CompositeByteBuf) carrier,
            allocator,
            key,
            ByteBufUtil.writeUtf8(allocator, value)
        );
  }
}
