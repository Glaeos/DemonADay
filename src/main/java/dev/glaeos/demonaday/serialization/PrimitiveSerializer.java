package dev.glaeos.demonaday.serialization;

import java.util.List;
import java.util.Scanner;

public class PrimitiveSerializer {

    public static int readVarInt(Scanner reader) {
        int value = 0;
        int size = 0;
        int b;

        while (((b = reader.nextByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << (size++ * 7);
            if (size > 5) {
                throw new IllegalArgumentException("VarInt is too big");
            }
        }
        return value | ((b & 0x7F) << (size * 7));
    }

    public static void writeVarInt(List<Byte> dst, int data) {
        do {
            byte temp = (byte) (data & 0x7F);
            data >>>= 7;
            if (data != 0) {
                temp |= 0x80;
            }
            dst.add(temp);
        } while (data != 0);
    }

    public static long readVarLong(Scanner reader) {
        long value = 0L;
        int size = 0;
        long b;

        while (((b = reader.nextByte()) & 0x80) == 0x80) {
            value |= (b & 0x7F) << (size++ * 7);
            if (size > 10) {
                throw new IllegalArgumentException("VarLong is too big");
            }
        }
        return value | ((b & 0x7FL) << (size * 7));
    }

    public static void writeVarLong(List<Byte> dst, long data) {
        do {
            byte temp = (byte) (data & 0x7F);
            data >>>= 7;
            if (data != 0) {
                temp |= 0x80;
            }
            dst.add(temp);
        } while (data != 0);
    }

}
