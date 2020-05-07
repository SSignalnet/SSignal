package net.ssignal.protocols;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Convert {//字节转换

    public static byte[] 反转(byte[] 字节数组) {
        return 反转(字节数组, 0, 0);
    }

    public static byte[] 反转(byte[] 字节数组, int 开始位置, int 长度) {
        byte[] 字节数组2;
        if (长度 > 0) {
            字节数组2 = new byte[长度];
            int i, j = 0;
            for (i = 开始位置 + 长度 - 1; i >= 开始位置; i--) {
                字节数组2[j] = 字节数组[i];
                j += 1;
            }
        } else {
            字节数组2 = new byte[字节数组.length - 开始位置];
            int i, j = 0;
            for (i = 字节数组.length - 1; i >= 开始位置; i--) {
                字节数组2[j] = 字节数组[i];
                j += 1;
            }
        }
        return 字节数组2;
    }

    public static short toint16(byte[] 字节数组, int 起始位置) throws Exception {
        ByteArrayInputStream 字节数组输入流 = new ByteArrayInputStream(字节数组);
        DataInputStream 数据输入流 = new DataInputStream(字节数组输入流);
        if (起始位置>0) {数据输入流.skipBytes(起始位置);}
        short 短整数 = 数据输入流.readShort();
        数据输入流.close();
        字节数组输入流.close();
        return  短整数;
    }

    public static int toint32(byte[] 字节数组, int 起始位置) throws Exception {
        ByteArrayInputStream 字节数组输入流 = new ByteArrayInputStream(字节数组);
        DataInputStream 数据输入流 = new DataInputStream(字节数组输入流);
        if (起始位置>0) {数据输入流.skipBytes(起始位置);}
        int 整数 = 数据输入流.readInt();
        数据输入流.close();
        字节数组输入流.close();
        return 整数;
    }

    public static long toint64(byte[] 字节数组, int 起始位置) throws Exception {
        ByteArrayInputStream 字节数组输入流 = new ByteArrayInputStream(字节数组);
        DataInputStream 数据输入流 = new DataInputStream(字节数组输入流);
        if (起始位置>0) {数据输入流.skipBytes(起始位置);}
        long 长整数 = 数据输入流.readLong();
        数据输入流.close();
        字节数组输入流.close();
        return 长整数;
    }

    public static float tofloat(byte[] 字节数组, int 起始位置) throws Exception {
        ByteArrayInputStream 字节数组输入流 = new ByteArrayInputStream(字节数组);
        DataInputStream 数据输入流 = new DataInputStream(字节数组输入流);
        if (起始位置>0) {数据输入流.skipBytes(起始位置);}
        float 单精度浮点数 = 数据输入流.readFloat();
        数据输入流.close();
        字节数组输入流.close();
        return 单精度浮点数;
    }

    public static double todouble(byte[] 字节数组, int 起始位置) throws Exception {
        ByteArrayInputStream 字节数组输入流 = new ByteArrayInputStream(字节数组);
        DataInputStream 数据输入流 = new DataInputStream(字节数组输入流);
        if (起始位置>0) {数据输入流.skipBytes(起始位置);}
        double 双精度浮点数 = 数据输入流.readDouble();
        数据输入流.close();
        字节数组输入流.close();
        return 双精度浮点数;
    }

    public static byte[] getbytes(short 短整数) throws Exception {
        ByteArrayOutputStream 字节数组输出流 = new ByteArrayOutputStream();
        DataOutputStream 数据输出流 = new DataOutputStream(字节数组输出流);
        数据输出流.writeShort(短整数);
        数据输出流.flush();
        byte[] 字节数组 = 字节数组输出流.toByteArray();
        数据输出流.close();
        字节数组输出流.close();
        return 字节数组;
    }

    public static byte[] getbytes(int 整数) throws Exception {
        ByteArrayOutputStream 字节数组输出流 = new ByteArrayOutputStream();
        DataOutputStream 数据输出流 = new DataOutputStream(字节数组输出流);
        数据输出流.writeInt(整数);
        数据输出流.flush();
        byte[] 字节数组 = 字节数组输出流.toByteArray();
        数据输出流.close();
        字节数组输出流.close();
        return 字节数组;
    }

    public static byte[] getbytes(long 长整数) throws Exception {
        ByteArrayOutputStream 字节数组输出流 = new ByteArrayOutputStream();
        DataOutputStream 数据输出流 = new DataOutputStream(字节数组输出流);
        数据输出流.writeLong(长整数);
        数据输出流.flush();
        byte[] 字节数组 = 字节数组输出流.toByteArray();
        数据输出流.close();
        字节数组输出流.close();
        return 字节数组;
    }

    public static byte[] getbytes(float 单精度浮点数) throws Exception {
        ByteArrayOutputStream 字节数组输出流 = new ByteArrayOutputStream();
        DataOutputStream 数据输出流 = new DataOutputStream(字节数组输出流);
        数据输出流.writeFloat(单精度浮点数);
        数据输出流.flush();
        byte[] 字节数组 = 字节数组输出流.toByteArray();
        数据输出流.close();
        字节数组输出流.close();
        return 字节数组;
    }

    public static byte[] getbytes(double 双精度浮点数) throws Exception {
        ByteArrayOutputStream 字节数组输出流 = new ByteArrayOutputStream();
        DataOutputStream 数据输出流 = new DataOutputStream(字节数组输出流);
        数据输出流.writeDouble(双精度浮点数);
        数据输出流.flush();
        byte[] 字节数组 = 字节数组输出流.toByteArray();
        数据输出流.close();
        字节数组输出流.close();
        return 字节数组;
    }

}
