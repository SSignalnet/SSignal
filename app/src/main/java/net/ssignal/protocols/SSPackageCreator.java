package net.ssignal.protocols;

import android.util.Base64;

import net.ssignal.util.SharedMethod;

import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.Cipher;

public class SSPackageCreator {

    private class 字节数据_复合数据 {
        byte 字节数组[];
        byte 长度信息字节数;
    }

    private SSPackageCell SS包数据[];
    int 数据数量;
    private boolean 无标签;
    private short 查询结果;
    private byte 编码_数值;
    private String 编码_文本;

    public SSPackageCreator() {
        查询结果 = ProtocolParameters.查询结果_无;
        初始化(false, 0, ProtocolParameters.SS包编码_Unicode_LittleEndian);
    }

    public SSPackageCreator(boolean 无标签) {
        查询结果 = ProtocolParameters.查询结果_无;
        初始化(无标签, 0, ProtocolParameters.SS包编码_Unicode_LittleEndian);
    }

    public SSPackageCreator(boolean 无标签, int 数据数量, byte SS包编码) {
        查询结果 = ProtocolParameters.查询结果_无;
        初始化(无标签, 数据数量, SS包编码);
    }

    public SSPackageCreator(SSPackageReader SS包读取器) {
        查询结果 = ProtocolParameters.查询结果_无;
        初始化(false, 0, SS包读取器.编码_数值);
        SS包数据 = SS包读取器.有标签数据;
        this.数据数量 = SS包读取器.数据数量;
    }

    private void 初始化(boolean 无标签, int 数据数量, byte SS包编码) {
        this.无标签 = 无标签;
        if (数据数量 < 1) { 数据数量 = 10;}
        SS包数据 = new SSPackageCell[数据数量];
        this.编码_数值 = SS包编码;
        switch (SS包编码) {
            case ProtocolParameters.SS包编码_Unicode_LittleEndian:
                编码_文本 = "UNICODELITTLEUNMARKED";
                break;
            case ProtocolParameters.SS包编码_Unicode_BigEndian:
                编码_文本 = "UNICODE";
                break;
            case ProtocolParameters.SS包编码_UTF8:
                编码_文本 = "UTF8";
                break;
            case ProtocolParameters.SS包编码_ASCII:
                编码_文本 = "ASCII";
                break;
            case ProtocolParameters.SS包编码_UTF32:
                编码_文本 = "UTF32";
                break;
            case ProtocolParameters.SS包编码_UTF7:
                编码_文本 = "UTF7";
                break;
            default:
                this.编码_数值 = ProtocolParameters.SS包编码_UTF8;
                编码_文本 = "UTF8";
        }
    }

    short 获取查询结果() throws Exception {
        if (查询结果 == ProtocolParameters.查询结果_无) {
            throw new Exception("此SS包没有查询结果");
        } else {
            return 查询结果;
        }
    }

    String 获取出错提示文本() {
        if (查询结果 == ProtocolParameters.查询结果_出错) {
            int i;
            for (i = 0; i < 数据数量; i++) {
                if (ProtocolParameters.感叹号_SS包保留标签.equals(SS包数据[i].标签)) {
                    return (String)SS包数据[i].数据;
                }
            }
        }
        return "";
    }

    public void 添加_有标签(String 标签, boolean 真假值) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_真假值, 真假值, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(boolean 真假值) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_真假值, 真假值, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, byte 字节) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_字节, 字节, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(byte 字节) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_字节, 字节, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, short 有符号短整数) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号短整数, 有符号短整数, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(short 有符号短整数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号短整数, 有符号短整数, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, int 有符号整数) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号整数, 有符号整数, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(int 有符号整数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号整数, 有符号整数, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, long 有符号长整数) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号长整数, 有符号长整数, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(long 有符号长整数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_有符号长整数, 有符号长整数, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, float 单精度浮点数) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_单精度浮点数, 单精度浮点数, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(float 单精度浮点数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_单精度浮点数, 单精度浮点数, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, double 双精度浮点数) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_双精度浮点数, 双精度浮点数, 标签, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_无标签(double 双精度浮点数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_双精度浮点数, 双精度浮点数, null, ProtocolParameters.长度信息字节数_零字节);
    }

    public void 添加_有标签(String 标签, String 字符串) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_字符串, 字符串, 标签, ProtocolParameters.长度信息字节数_四字节);
    }

    public void 添加_无标签(String 字符串, byte 长度信息字节数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        switch (长度信息字节数) {
            case ProtocolParameters.长度信息字节数_两字节:
            case ProtocolParameters.长度信息字节数_四字节:
                break;
            case ProtocolParameters.长度信息字节数_零字节:
                if (SharedMethod.字符串未赋值或为空(字符串)) {
                    throw new Exception("长度信息字节数不能为零");
                } else {
                    break;
                }
            default: throw new Exception("长度信息字节数不正确。");
        }
        添加(ProtocolParameters.SS包数据类型_字符串, 字符串, null, 长度信息字节数);
    }

    public void 添加_有标签(String 标签, byte 字节数组[]) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_字节数组, 字节数组, 标签, ProtocolParameters.长度信息字节数_四字节);
    }

    public void 添加_无标签(byte 字节数组[], byte 长度信息字节数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        switch (长度信息字节数) {
            case ProtocolParameters.长度信息字节数_两字节:
            case ProtocolParameters.长度信息字节数_四字节:
                break;
            case ProtocolParameters.长度信息字节数_零字节:
                if (字节数组 == null) {
                    throw new Exception("长度信息字节数不能为零");
                } else {
                    break;
                }
            default: throw new Exception("长度信息字节数不正确。");
        }
        添加(ProtocolParameters.SS包数据类型_字节数组, 字节数组, null, 长度信息字节数);
    }

    public void 添加_有标签(String 标签, SSPackageCreator SS包生成器) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_子SS包, SS包生成器, 标签, ProtocolParameters.长度信息字节数_四字节);
    }

    public void 添加_有标签(String 标签, SSPackageReader SS包解读器) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        添加(ProtocolParameters.SS包数据类型_子SS包, SS包解读器, 标签, ProtocolParameters.长度信息字节数_四字节);
    }

    public void 添加_无数据(byte 长度信息字节数) throws Exception {
        if (!无标签) { throw new Exception("此为有标签SS包"); }
        switch (长度信息字节数) {
            case ProtocolParameters.长度信息字节数_两字节:
            case ProtocolParameters.长度信息字节数_四字节:
                break;
            default: throw new Exception("长度信息字节数不正确。");
        }
        添加(ProtocolParameters.SS包数据类型_无, null, null, 长度信息字节数);
    }

    private void 添加(byte SS包数据类型, Object 数据, String 标签, byte 长度信息字节数) {
        SSPackageCell 数据单元 = new SSPackageCell();
        数据单元.类型 = SS包数据类型;
        数据单元.数据 = 数据;
        数据单元.标签 = 标签;
        数据单元.长度信息字节数 = 长度信息字节数;
        if (数据数量 == SS包数据.length) {
            SSPackageCell SS包数据2[] = new SSPackageCell[数据数量 * 2];
            System.arraycopy(SS包数据, 0, SS包数据2, 0, 数据数量);
            SS包数据 = SS包数据2;
        }
        SS包数据[数据数量] = 数据单元;
        数据数量 += 1;
    }

    public boolean 发送SS包(Socket 网络连接器) throws Exception {
        return 发送SS包(网络连接器, null);
    }

    public boolean 发送SS包(Socket 网络连接器, Cipher AES加密器) throws Exception {
        byte[] 字节数组 = 生成SS包(AES加密器);
        if (字节数组 != null) {
            SSPackageCreator SS包生成器 = new SSPackageCreator(true);
            SS包生成器.添加_无标签((byte) ProtocolParameters.SS包高低位标识_B);
            SS包生成器.添加_无标签(字节数组, ProtocolParameters.长度信息字节数_四字节);
            字节数组 = SS包生成器.生成字节数组();
            OutputStream 输出流 = 网络连接器.getOutputStream();
            输出流.write(字节数组);
            输出流.flush();
            return true;
        }
        return false;
    }

    public byte[] 生成SS包() throws Exception {
        return 生成SS包(null);
    }

    public byte[] 生成SS包(Cipher AES加密器) throws Exception {
        if (数据数量 == 0) {
            return null;
        }
        字节数据_复合数据 字节数据数组[] = 转换();
        字节数据_复合数据 某一数据;
        int 数据区总长度 = 0;
        for (int i = 0; i < 字节数据数组.length; i++) {
            某一数据 = 字节数据数组[i];
            switch (某一数据.长度信息字节数) {
                case ProtocolParameters.长度信息字节数_零字节:
                    数据区总长度 += 某一数据.字节数组.length;
                    break;
                case ProtocolParameters.长度信息字节数_两字节:
                case ProtocolParameters.长度信息字节数_四字节:
                    数据区总长度 += 某一数据.长度信息字节数 + 某一数据.字节数组.length;
                    break;
                default:
                    return null;
            }
        }
        byte 字节数组[] = new byte[ProtocolParameters.SS包标识_有标签.length() + 5 + 数据区总长度 + 1];
        byte 字节数组1[];
        if (!无标签) {
            字节数组1 = ProtocolParameters.SS包标识_有标签.getBytes("ASCII");
        } else {
            字节数组1 = ProtocolParameters.SS包标识_无标签.getBytes("ASCII");
        }
        int j = 0;
        System.arraycopy(字节数组1, 0, 字节数组, j, 字节数组1.length);
        j += 字节数组1.length;
        字节数组[j] = (byte) ProtocolParameters.SS包高低位标识_B;
        j += 1;
        字节数组[j] = 编码_数值;
        j += 1;
        if (!无标签) {
            字节数组1 = Convert.getbytes((int) (字节数据数组.length / 3));
        } else {
            字节数组1 = Convert.getbytes((int) 数据区总长度);
        }
        System.arraycopy(字节数组1, 0, 字节数组, j, 字节数组1.length);
        j += 字节数组1.length;
        for (int i = 0; i < 字节数据数组.length; i++) {
            某一数据 = 字节数据数组[i];
            if (某一数据.长度信息字节数 != ProtocolParameters.长度信息字节数_零字节) {
                switch (某一数据.长度信息字节数) {
                    case ProtocolParameters.长度信息字节数_两字节:
                        字节数组1 = Convert.getbytes((short) 某一数据.字节数组.length);
                        break;
                    case ProtocolParameters.长度信息字节数_四字节:
                        字节数组1 = Convert.getbytes(某一数据.字节数组.length);
                        break;
                    default:
                        continue;
                }
                System.arraycopy(字节数组1, 0, 字节数组, j, 字节数组1.length);
                j += 字节数组1.length;            }
            System.arraycopy(某一数据.字节数组, 0, 字节数组, j, 某一数据.字节数组.length);
            j += 某一数据.字节数组.length;
        }
        if (AES加密器 == null) {
            return 字节数组;
        } else {
            return AES加密器.doFinal(字节数组);
        }
    }

    public byte[] 生成字节数组() throws Exception {
        if (查询结果 != ProtocolParameters.查询结果_无) {
            throw new Exception("作为查询结果的SS包，不能生成字节数组。请生成SS包。");
        }
        if (数据数量 == 0) {
            return null;
        }
        字节数据_复合数据 字节数据数组[] = 转换();
        字节数据_复合数据 某一数据;
        int 总长度 = 0;
        for (int i = 0; i < 字节数据数组.length; i++) {
            某一数据 = 字节数据数组[i];
            switch (某一数据.长度信息字节数) {
                case ProtocolParameters.长度信息字节数_零字节:
                    总长度 += 某一数据.字节数组.length;
                    break;
                case ProtocolParameters.长度信息字节数_两字节:
                case ProtocolParameters.长度信息字节数_四字节:
                    总长度 += 某一数据.长度信息字节数 + 某一数据.字节数组.length;
                    break;
                default:
                    return null;
            }
        }
        byte 字节数组[] = new byte[总长度];
        byte 字节数组1[];
        int j = 0;
        for (int i = 0; i < 字节数据数组.length; i++) {
            某一数据 = 字节数据数组[i];
            if (某一数据.长度信息字节数 != ProtocolParameters.长度信息字节数_零字节) {
                switch (某一数据.长度信息字节数) {
                    case ProtocolParameters.长度信息字节数_两字节:
                        字节数组1 = Convert.getbytes((short) 某一数据.字节数组.length);
                        break;
                    case ProtocolParameters.长度信息字节数_四字节:
                        字节数组1 = Convert.getbytes(某一数据.字节数组.length);
                        break;
                    default:
                        continue;
                }
                System.arraycopy(字节数组1, 0, 字节数组, j, 字节数组1.length);
                j += 字节数组1.length;            }
            System.arraycopy(某一数据.字节数组, 0, 字节数组, j, 某一数据.字节数组.length);
            j += 某一数据.字节数组.length;
        }
        return 字节数组;
    }

    private 字节数据_复合数据[] 转换() throws Exception {
        字节数据_复合数据 数据[];
        if (!无标签) {
            数据 = new 字节数据_复合数据[数据数量 * 3];
            int j = 0;
            for (int i = 0; i < 数据数量; i++) {
                数据[j] = 转换成字节数据(SS包数据[i].标签, ProtocolParameters.SS包数据类型_字符串, ProtocolParameters.长度信息字节数_两字节);
                j += 1;
                数据[j] = 转换成字节数据(SS包数据[i].类型, ProtocolParameters.SS包数据类型_字节, ProtocolParameters.长度信息字节数_零字节);
                j += 1;
                数据[j] = 转换成字节数据(SS包数据[i].数据, SS包数据[i].类型, SS包数据[i].长度信息字节数);
                j += 1;
            }
        } else {
            数据 = new 字节数据_复合数据[数据数量];
            for (int i = 0; i < 数据数量; i++) {
                数据[i] = 转换成字节数据(SS包数据[i].数据, SS包数据[i].类型, SS包数据[i].长度信息字节数);
            }
        }
        return 数据;
    }

    private 字节数据_复合数据 转换成字节数据(Object 待转换数据, byte SS包数据类型, byte 长度信息字节数) throws Exception {
        字节数据_复合数据 数据 = new 字节数据_复合数据();
        数据.长度信息字节数 = 长度信息字节数;
        switch (SS包数据类型) {
            case ProtocolParameters.SS包数据类型_字符串:
                if (待转换数据 != null && !SharedMethod.字符串未赋值或为空((String)待转换数据)) {
                    数据.字节数组 = ((String)待转换数据).getBytes(编码_文本);
                } else {
                    数据.字节数组 = new byte[长度信息字节数];
                    数据.长度信息字节数 = ProtocolParameters.长度信息字节数_零字节;
                }
                break;
            case ProtocolParameters.SS包数据类型_有符号长整数:
                数据.字节数组 = Convert.getbytes((long) 待转换数据);
                break;
            case ProtocolParameters.SS包数据类型_有符号整数:
                数据.字节数组 = Convert.getbytes((int) 待转换数据);
                break;
            case ProtocolParameters.SS包数据类型_有符号短整数:
                数据.字节数组 = Convert.getbytes((short) 待转换数据);
                break;
            case ProtocolParameters.SS包数据类型_子SS包:
                if (待转换数据 != null) {
                    if (待转换数据 instanceof SSPackageCreator) {
                        数据.字节数组 = ((SSPackageCreator) 待转换数据).生成SS包();
                    } else {
                        数据.字节数组 = ((SSPackageReader) 待转换数据).要解读的SS包;
                    }
                    if (数据.字节数组 == null) {
                        数据.字节数组 = new byte[长度信息字节数];
                        数据.长度信息字节数 = ProtocolParameters.长度信息字节数_零字节;
                    }
                } else {
                    数据.字节数组 = new byte[长度信息字节数];
                    数据.长度信息字节数 = ProtocolParameters.长度信息字节数_零字节;
                }
                break;
            case ProtocolParameters.SS包数据类型_字节数组:
                if (待转换数据 != null) {
                    数据.字节数组 = (byte[]) 待转换数据;
                } else {
                    数据.字节数组 = new byte[长度信息字节数];
                    数据.长度信息字节数 = ProtocolParameters.长度信息字节数_零字节;
                }
                break;
            case ProtocolParameters.SS包数据类型_真假值:
                if (!(boolean) 待转换数据) {
                    数据.字节数组 = new byte[]{0};
                } else {
                    数据.字节数组 = new byte[]{1};
                }
                break;
            case ProtocolParameters.SS包数据类型_字节:
                数据.字节数组 = new byte[]{(byte)待转换数据};
                break;
            case ProtocolParameters.SS包数据类型_单精度浮点数:
                数据.字节数组 = Convert.getbytes((float) 待转换数据);
                break;
            case ProtocolParameters.SS包数据类型_双精度浮点数:
                数据.字节数组 = Convert.getbytes((double)待转换数据);
                break;
        }
        return 数据;
    }

    public int 获取SS包数据数量() {
        return 数据数量;
    }

    public void 能否生成纯文本() throws Exception {
        if (无标签) { throw new Exception("无标签SS包不能生成纯文本"); }
        if (数据数量 > 0) {
            for (int i = 0; i < 数据数量; i++) {
                if (!标签是否合格(SS包数据[i].标签)) {
                    throw new Exception("标签名称有非法字符。");
                }
                if (SS包数据[i].类型 == ProtocolParameters.SS包数据类型_子SS包) {
                    ((SSPackageCreator) SS包数据[i].数据).能否生成纯文本();
                }
            }
        }
    }

    public String 生成纯文本() throws Exception {
        return 生成纯文本(0);
    }

    public String 生成纯文本(int 层级) throws Exception {
        能否生成纯文本();
        if (数据数量 > 0) {
            StringBuffer 字符串合并器 = new StringBuffer();
            String 空格字符串 = null;
            int i;
            if (层级 > 0) {
                StringBuffer 字符串合并器2 = new StringBuffer();
                for (i = 0; i < 层级; i++) {
                    字符串合并器2.append(" ");
                }
                空格字符串 = 字符串合并器2.toString();
            } else {
                if (层级 < 0) { 层级 = 0; }
                字符串合并器.append(ProtocolParameters.SS包标识_纯文本);
            }
            SSPackageCell 某个包;
            for (i = 0; i < 数据数量; i++) {
                某个包 = SS包数据[i];
                字符串合并器.append("\n");
                if (层级 > 0) {
                    字符串合并器.append(空格字符串);
                }
                switch (某个包.类型) {
                    case ProtocolParameters.SS包数据类型_字符串:
                        字符串合并器.append("S");
                        break;
                    case ProtocolParameters.SS包数据类型_有符号长整数:
                        字符串合并器.append("8");
                        break;
                    case ProtocolParameters.SS包数据类型_有符号整数:
                        字符串合并器.append("4");
                        break;
                    case ProtocolParameters.SS包数据类型_有符号短整数:
                        字符串合并器.append("2");
                        break;
                    case ProtocolParameters.SS包数据类型_子SS包:
                        字符串合并器.append("SS");
                        break;
                    case ProtocolParameters.SS包数据类型_字节数组:
                        字符串合并器.append("BT");
                        break;
                    case ProtocolParameters.SS包数据类型_真假值:
                        字符串合并器.append("BL");
                        break;
                    case ProtocolParameters.SS包数据类型_字节:
                        字符串合并器.append("1");
                        break;
                    case ProtocolParameters.SS包数据类型_单精度浮点数:
                        字符串合并器.append("4F");
                        break;
                    case ProtocolParameters.SS包数据类型_双精度浮点数:
                        字符串合并器.append("8F");
                        break;
                }
                字符串合并器.append(":");
                字符串合并器.append(某个包.标签);
                字符串合并器.append("=");
                switch (某个包.类型) {
                    case ProtocolParameters.SS包数据类型_字符串:
                        if (某个包.数据 != null) {
                            if (!SharedMethod.字符串未赋值或为空((String) 某个包.数据)) {
                                字符串合并器.append(((String) 某个包.数据).replace("&", "&amp;").replace("\r", "").replace("\n", "&;"));
                            }
                        }
                        break;
                    case ProtocolParameters.SS包数据类型_有符号长整数:
                        字符串合并器.append((long)某个包.数据);
                        break;
                    case ProtocolParameters.SS包数据类型_有符号整数:
                        字符串合并器.append((int)某个包.数据);
                        break;
                    case ProtocolParameters.SS包数据类型_有符号短整数:
                        字符串合并器.append((short)某个包.数据);
                        break;
                    case ProtocolParameters.SS包数据类型_子SS包:
                        if (某个包.数据 != null) {
                            字符串合并器.append(((SSPackageCreator)某个包.数据).生成纯文本(层级 + 1));
                        }
                        break;
                    case ProtocolParameters.SS包数据类型_字节数组:
                        if (某个包.数据 != null) {
                            字符串合并器.append(Base64.encodeToString((byte[]) 某个包.数据, Base64.DEFAULT));
                        }
                        break;
                    case ProtocolParameters.SS包数据类型_真假值:
                        字符串合并器.append(((boolean)某个包.数据? "true":"false"));
                        break;
                    case ProtocolParameters.SS包数据类型_字节:
                        字符串合并器.append((byte)某个包.数据);
                        break;
                    case ProtocolParameters.SS包数据类型_单精度浮点数:
                        字符串合并器.append((float)某个包.数据);
                        break;
                    case ProtocolParameters.SS包数据类型_双精度浮点数:
                        字符串合并器.append((double)某个包.数据);
                        break;
                }
            }
            return 字符串合并器.toString();
        } else {
            return "";
        }
    }

    private boolean 标签是否合格(String 标签) {
        if (标签.startsWith(" ")) { return false; }
        String 非法字符[] = new String[]{"=", "\r", "\n"};
        for (int i = 0; i < 非法字符.length; i++) {
            if (标签.contains(非法字符[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean 修改某个标签的数据(String 标签, Object 新值) throws Exception {
        if (无标签) { throw new Exception("此为无标签SS包"); }
        if (数据数量 > 0) {
            int i;
            for (i = 0; i < 数据数量; i++) {
                if (标签.equals(SS包数据[i].标签)) {
                    SS包数据[i].数据 = 新值;
                    return true;
                }
            }
        }
        return false;
    }

}
