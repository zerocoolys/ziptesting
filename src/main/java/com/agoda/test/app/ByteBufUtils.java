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

    public static byte[] requireArray(long size) {
        if (size >= Runtime.getRuntime().freeMemory()) {

            if (Runtime.getRuntime().freeMemory() > Integer.MAX_VALUE) {
                return new byte[Integer.MAX_VALUE / 2];
            } else {
                return new byte[(int) (Runtime.getRuntime().freeMemory() / 2)];
            }
        } else {
            return new byte[(int) size];
        }
    }

//    public static int require(long size) {
//        if (size >= Runtime.getRuntime().freeMemory()) {
//
//            if (Runtime.getRuntime().freeMemory() > Integer.MAX_VALUE) {
//                return Integer.MAX_VALUE / 2;
//            } else {
//                return (int) (Runtime.getRuntime().freeMemory() / 2);
//            }
//        } else {
//            return (int) size;
//        }
//    }


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
}
