package dev.glaeos.demonaday.serialization;

public class SimpleBuffer {

    private final byte[] data;

    private int position;

    public SimpleBuffer(byte[] data) {
        this.data = data;
        position = -1;
    }

    public boolean hasNext() {
        return position < data.length-1;
    }

    public byte next() {
        position += 1;
        return data[position];
    }

}
