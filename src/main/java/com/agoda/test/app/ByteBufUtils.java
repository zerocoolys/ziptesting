package com.agoda.test.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * ByteBufUtils
 *
 * @author yousheng
 * @since 2018/5/30
 */
public class ByteBufUtils {

    public static final int BUF_SIZE = (int) (Runtime.getRuntime().totalMemory() / 100);

    public static int requireArray() {
        return BUF_SIZE;
    }


    public static byte[] convertInt(int val) {
        return new byte[]{(byte) (val >> 24), (byte) (val >> 16), (byte) (val >> 8), (byte) val};
    }

    public static int parseInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static int readInt(InputStream inputStream) throws IOException {
        byte[] val = new byte[4];
        inputStream.read(val);
        return ByteBuffer.wrap(val).getInt();
    }

    public static String readString(InputStream inputStream, int len) throws IOException {
        byte[] buf = new byte[len];
        inputStream.read(buf);
        return new String(buf);
    }

    public static byte[] readBytes(InputStream inputStream, int len) throws IOException {
        byte[] val = new byte[len];
        inputStream.read(val);

        return val;
    }

    /**
     * @param outputStream
     * @param str
     * @throws IOException
     */
    public static void writeString(OutputStream outputStream, String str) throws IOException {
        outputStream.write(ByteBufUtils.convertInt(str.length()));
        outputStream.write(str.getBytes());
    }

    public static byte[] convertLong(long val) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(val);

        return buffer.array();
    }

    public static long parseLong(byte[] data) {
        return ByteBuffer.wrap(data).getLong();
    }
}
