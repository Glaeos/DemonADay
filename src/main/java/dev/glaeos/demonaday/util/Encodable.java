package dev.glaeos.demonaday.util;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface Encodable {

    void encode(@NotNull ByteBuf buffer);

}
