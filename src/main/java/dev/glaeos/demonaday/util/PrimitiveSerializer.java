package dev.glaeos.demonaday.util;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Scanner;

public final class PrimitiveSerializer {

    private PrimitiveSerializer() {

    }

    public static int readVarInt(@NotNull ByteBuf buffer) {
        int value = 0;
        int size = 0;
        int b;

        while (((b = buffer.readByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << (size++ * 7);
            if (size > 5) {
                throw new IllegalArgumentException("VarInt is too big");
            }
        }
        return value | ((b & 0x7F) << (size * 7));
    }

    public static void writeVarInt(@NotNull ByteBuf dst, int data) {
        do {
            byte temp = (byte) (data & 0x7F);
            data >>>= 7;
            if (data != 0) {
                temp |= 0x80;
            }
            dst.writeByte(temp);
        } while (data != 0);
    }

    public static long readVarLong(@NotNull ByteBuf buffer) {
        long value = 0L;
        int size = 0;
        long b;

        while (((b = buffer.readByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << (size++ * 7);
            if (size > 9) {
                return value | ((b & 0x7FL) << (size * 7));
                //throw new IllegalArgumentException("VarLong is too big");
            }
        }
        return value | ((b & 0x7FL) << (size * 7));
    }

    public static void writeVarLong(@NotNull ByteBuf dst, long data) {
        do {
            byte temp = (byte) (data & 0x7F);
            data >>>= 7;
            if (data != 0) {
                temp |= 0x80;
            }
            dst.writeByte(temp);
        } while (data != 0);
    }

}
