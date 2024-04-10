package com.rinaboard.app;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

public class PacketUtils {
    //—————————————————————————————— 指令定义 ————————————————————————————————————————
    //命令定义
    //读和写
    private static final byte SetCMD = (byte) 0X80;
    private static final byte GetCMD = (byte) 0x00;
    //获取回复
    public static final byte ASK = (byte) 0x00;

    //重启
    private static final byte Reboot = (byte) 0x40;

    //数据定义
    //璃奈板相关
    private static final byte Color = (byte) 0x02;
    private static final byte BoardBrightness = (byte) 0x04;
    private static final byte BoardBrightnessOver = (byte) 0x05;
    private static final byte Bitmap = (byte) 0x06;
    private static final byte ExpressionList = (byte) 0x08;
    private static final byte SaveBitmap = (byte) 0x0A;
    private static final byte DeleteBitmap = (byte) 0x0B;
    private static final byte ChangeBitmap = (byte) 0x1B;
    private static final byte LastExpression = (byte) 0x1A;
    private static final byte NextExpression = (byte) 0x1C;

    //灯条相关
    private static final byte LightState = (byte) 0x0C;
    private static final byte LightBrightness = (byte) 0x0E;
    private static final byte LightBrightnessOver = (byte) 0x0F;

    //电量
    private static final byte Electricity = (byte) 0x10;

    //系统
    private static final byte DeviceName = (byte) 0x12;
    private static final byte DeviceType = (byte) 0x14;
    private static final byte WifiSSID = (byte) 0x16;
    private static final byte WifiPassword = (byte) 0x18;

    //状态模式
    private static final byte SystemState = (byte) 0x20;
    private static final byte ExpressionMode = (byte) 0x00;
    private static final byte VideoMode = (byte) 0x01;
    private static final byte RecognitionMode = (byte) 0x02;

    //—————————————————————————————— 指令定义结束 ————————————————————————————————————————

    //——————————————————————————————————————————— 命令发送 ——————————————————————————————————————————
    public static void ReBootBoard(UDPInteraction udp) {
        byte[] data = recombinePackage(CmdType.Set, DataName.REBOOT);
        udp.send(data);
    }

    public static void SaveBitmapToBoard(UDPInteraction udp, String bitmapName) {
        byte[] data = recombinePackage(CmdType.Set,
                DataName.SAVEBITMAP,
                bitmapName.getBytes(StandardCharsets.UTF_8),
                (byte) bitmapName.getBytes(StandardCharsets.UTF_8).length
        );
        udp.send(data);
    }

    public static void DeleteBitmapOnBoard(UDPInteraction udp, String bitmapName)
    {
        byte[] data = recombinePackage(CmdType.Set,
                DataName.DELETEBITMAP,
                bitmapName.getBytes(StandardCharsets.UTF_8),
                (byte) bitmapName.getBytes(StandardCharsets.UTF_8).length
        );
        udp.send(data);
    }

    public static void ChangeBitmapOnBoard(UDPInteraction udp, String bitmapName)
    {
        byte[] data = recombinePackage(CmdType.Set,
                DataName.CHANGEBITMAP,
                bitmapName.getBytes(StandardCharsets.UTF_8),
                (byte) bitmapName.getBytes(StandardCharsets.UTF_8).length
        );
        udp.send(data);
    }

    public static void NextExpression(UDPInteraction udp){
        byte[] data = recombinePackage(CmdType.Set, DataName.NEXTEXPRESSION);
        udp.send(data);
    }

    public static void LastExpression(UDPInteraction udp){
        byte[] data = recombinePackage(CmdType.Set, DataName.LASTEXPRESSION);
        udp.send(data);
    }

    //————————————————————————————————————————————— 信息获取 —————————————————————————————————————————
    public static int GetColor(UDPInteraction udp) {
        udp.send(getColorFromBoard());//告诉璃奈板获取颜色数据
        byte[] bytesData = udp.receive();
        if (bytesData.length != 4) {
            System.out.println("Byte array length must be 4");
            return 0;
        }
        return (((bytesData[0] | 0xFF) << 24) |
                ((bytesData[1] & 0xFF) << 16) |
                ((bytesData[2] & 0xFF) << 8) |
                (bytesData[3] & 0xFF));
    }

    public static int GetBoardBrightness(UDPInteraction udp) {
        udp.send(getBoardBrightnessFormBoard());//告诉璃奈板获取璃奈板亮度
        byte[] bytesData = udp.receive();
        if (bytesData.length != 1) {
            System.out.println("Byte array length must be 1");
            return 0;
        }
        return bytesData[0] & 0xFF;
    }

    public static byte[] GetBitmap(UDPInteraction udp) {
        udp.send(getBitmapFromBoard());
        byte[] bitmap = udp.receive();
        if (bitmap.length != 48) {
            System.out.println("Get the wrong bitmap data");
            return null;
        }
        return bitmap;
    }

    public static String[] getExpressionList(UDPInteraction udp){
        udp.send(getExpressionListFromBoard());
        String input = udp.receiveString();
        if(input != null){
            String[] subStrings = input.split("\\|");

            ArrayList<String> list = new ArrayList<>();
            Collections.addAll(list, subStrings);
            // 使用 removeIf() 方法删除空字符串
            list.removeIf(String::isEmpty);

            // 将结果转换为数组

            return list.toArray(new String[0]);
        }else {
            return null;
        }
    }

    public static boolean GetLightState(UDPInteraction udp) {
        udp.send(getLightStateFromBoard());
        byte[] bytesData = udp.receive();
        if (bytesData.length != 1) {
            System.out.println("Byte array length must be 1");
            return false;
        }
        return bytesData[0] != 0;
    }

    public static int GetLightBrightness(UDPInteraction udp) {
        udp.send(getLightBrightnessFormBoard());//告诉璃奈板获取灯条亮度
        byte[] bytesData = udp.receive();
        if (bytesData.length != 1) {
            System.out.println("Byte array length must be 1");
            return 0;
        }
        return bytesData[0] & 0xFF;
    }

    public static String GetDeviceName(UDPInteraction udp) {
        udp.send(getDeviceNameFormBoard());
        return udp.receiveString();
    }

    public static String GetDeviceType(UDPInteraction udp) {
        udp.send(getDeviceTypeFromBoard());
        return udp.receiveString();
    }

    public static String GetWifiSSID(UDPInteraction udp) {
        udp.send(getWifiSSIDFromBoard());
        return udp.receiveString();
    }

    public static RinaBoardApp.SystemState GetSystemState(UDPInteraction udp) {
        udp.send(getSystemState());
        byte[] state = udp.receive();
        if (state.length != 1) {
            System.out.println("Byte array length must be 1");
            return null;
        }
        switch (state[0]) {
            case ExpressionMode:
                return RinaBoardApp.SystemState.ExpressionMode;
            case VideoMode:
                return RinaBoardApp.SystemState.VideoMode;
            case RecognitionMode:
                return RinaBoardApp.SystemState.RecognitionMode;
        }
        return null;
    }

    //—————————————————————————————————————————— 获取重组数据包 ————————————————————————————————————————————
    //——————————————————————————————————————————— SET —————————————————————————————————————————


    public static byte[] setColorToBoard(int color) {
        byte[] data = new byte[4];
        data[0] = 0x00;
        data[1] = (byte) ((color >> 16) & 0xFF);
        data[2] = (byte) ((color >> 8) & 0xFF);
        data[3] = (byte) (color & 0xFF);
        return recombinePackage(CmdType.Set, DataName.COLOR, data, (byte) 4);
    }

    public static byte[] setBoardBrightnessToBoard(int brightness) {
        byte[] data = {(byte) (brightness & 0xFF)};
        return recombinePackage(CmdType.Set, DataName.BOARDBRIGHTNESS, data, (byte) 1);
    }

    public static byte[] setBoardBrightnessOver() {
        return recombinePackage(CmdType.Set, DataName.BOARDBRIGHTNESSOVER);
    }

    public static byte[] setBitmapToBoard(byte[] bitmap) {
        return recombinePackage(CmdType.Set, DataName.BITMAP, bitmap, (byte) 48);
    }

    public static byte[] setLightStateToBoard(boolean lightIsOn) {
        byte[] data = new byte[1];
        data[0] = (byte) (lightIsOn ? 1 : 0);
        return recombinePackage(CmdType.Set, DataName.LIGHTSTATE, data, (byte) 1);
    }

    public static byte[] setLightBrightnessToBoard(int brightness) {
        byte[] data = {(byte) (brightness & 0xFF)};
        return recombinePackage(CmdType.Set, DataName.LIGHTBRIGHTNESS, data, (byte) 1);
    }

    public static byte[] setLightBrightnessOver() {
        return recombinePackage(CmdType.Set, DataName.LIGHTBRIGHTNESSOVER);
    }

    public static byte[] setDeviceNameToBoard(String deviceName) {
        return recombinePackage(CmdType.Set, DataName.DEVICENAME, deviceName.getBytes(StandardCharsets.UTF_8), (byte) deviceName.getBytes(StandardCharsets.UTF_8).length);
    }

    public static byte[] setWifiSSIDToBoard(String ssid) {
        return recombinePackage(CmdType.Set, DataName.WIFISSID, ssid.getBytes(StandardCharsets.UTF_8), (byte) ssid.getBytes(StandardCharsets.UTF_8).length);
    }

    public static byte[] setWifiPasswordToBoard(String password) {
        return recombinePackage(CmdType.Set, DataName.WIFIPASSWORD, password.getBytes(StandardCharsets.UTF_8), (byte) password.getBytes(StandardCharsets.UTF_8).length);
    }

    public static byte[] setSystemState(RinaBoardApp.SystemState state) {
        byte[] data = new byte[1];
        switch (state) {
            case ExpressionMode:
                data[0] = ExpressionMode;
                break;
            case VideoMode:
                data[0] = VideoMode;
                break;
            case RecognitionMode:
                data[0] = RecognitionMode;
                break;
        }
        return recombinePackage(CmdType.Set, DataName.SYSTEMSTATE, data, (byte) 1);
    }

    //——————————————————————————————————————————— GET ——————————————————————————————————————————————

    public static byte[] getColorFromBoard() {
        return recombinePackage(CmdType.Get, DataName.COLOR);
    }

    public static byte[] getBoardBrightnessFormBoard() {
        return recombinePackage(CmdType.Get, DataName.BOARDBRIGHTNESS);
    }

    public static byte[] getBitmapFromBoard() {
        return recombinePackage(CmdType.Get, DataName.BITMAP);
    }

    public static byte[] getExpressionListFromBoard(){
        return recombinePackage(CmdType.Get, DataName.EXPRESSIONLIST);
    }

    public static byte[] getLightStateFromBoard() {
        return recombinePackage(CmdType.Get, DataName.LIGHTSTATE);
    }

    public static byte[] getLightBrightnessFormBoard() {
        return recombinePackage(CmdType.Get, DataName.LIGHTBRIGHTNESS);
    }

    public static byte[] getDeviceNameFormBoard() {
        return recombinePackage(CmdType.Get, DataName.DEVICENAME);
    }

    public static byte[] getDeviceTypeFromBoard() {
        return recombinePackage(CmdType.Get, DataName.DEVICETYPE);
    }

    public static byte[] getWifiSSIDFromBoard() {
        return recombinePackage(CmdType.Get, DataName.WIFISSID);
    }

    public static byte[] getSystemState() {
        return recombinePackage(CmdType.Get, DataName.SYSTEMSTATE);
    }

    //——————————————————————————————————————— 指令包重构 ——————————————————————————————————————————————
    //指令结构：1byte:读或写+数据名 2byte：数据长度 3byte - ∞byte(可有可无按照情况定)，数据包
    private static byte[] recombinePackage(CmdType cmd, DataName dataName, byte[] data, byte dataSize) {
        byte[] combinedPackage = new byte[dataSize + 2];//数据包包含前两个byte加后面的数据包，所以长度为dataSize+2

        switch (cmd) {//设置第一位表示读写状态
            case Set:
                combinedPackage[0] |= SetCMD;
                break;
            case Get:
                combinedPackage[0] |= GetCMD;
                break;
        }

        switch (dataName) {//设置后7位表示数据名
            case COLOR:
                combinedPackage[0] |= Color;
                break;
            case BOARDBRIGHTNESS:
                combinedPackage[0] |= BoardBrightness;
                break;
            case BOARDBRIGHTNESSOVER:
                combinedPackage[0] |= BoardBrightnessOver;
                break;
            case BITMAP:
                combinedPackage[0] |= Bitmap;
                break;
            case EXPRESSIONLIST:
                combinedPackage[0] |= ExpressionList;
                break;
            case SAVEBITMAP:
                combinedPackage[0] |= SaveBitmap;
                break;
            case CHANGEBITMAP:
                combinedPackage[0] |= ChangeBitmap;
                break;
            case DELETEBITMAP:
                combinedPackage[0] |= DeleteBitmap;
                break;
            case NEXTEXPRESSION:
                combinedPackage[0] |= NextExpression;
                break;
            case LASTEXPRESSION:
                combinedPackage[0] |= LastExpression;
                break;
            case LIGHTSTATE:
                combinedPackage[0] |= LightState;
                break;
            case LIGHTBRIGHTNESS:
                combinedPackage[0] |= LightBrightness;
                break;
            case LIGHTBRIGHTNESSOVER:
                combinedPackage[0] |= LightBrightnessOver;
                break;
            case ELECTRICITY:
                combinedPackage[0] |= Electricity;
                break;
            case DEVICENAME:
                combinedPackage[0] |= DeviceName;
                break;
            case DEVICETYPE:
                combinedPackage[0] |= DeviceType;
                break;
            case WIFISSID:
                combinedPackage[0] |= WifiSSID;
                break;
            case WIFIPASSWORD:
                combinedPackage[0] |= WifiPassword;
                break;
            case REBOOT:
                combinedPackage[0] |= Reboot;
                break;
            case SYSTEMSTATE:
                combinedPackage[0] |= SystemState;
        }

        combinedPackage[1] = dataSize;//第二字节表示数据包长度

        for (int i = 0; i < dataSize; i++) {
            combinedPackage[i + 2] = data[i]; //将数据包data中的所有内容填入重组数据包combinedPackage中，从第三字节开始
        }

        return combinedPackage;
    }

    private static byte[] recombinePackage(CmdType cmd, DataName dataName) {
        byte[] combinedPackage = new byte[2];

        switch (cmd) {//设置第一位表示读写状态
            case Set:
                combinedPackage[0] |= SetCMD;
                break;
            case Get:
                combinedPackage[0] |= GetCMD;
                break;
        }

        switch (dataName) {//设置后7位表示数据名
            case COLOR:
                combinedPackage[0] |= Color;
                break;
            case BOARDBRIGHTNESS:
                combinedPackage[0] |= BoardBrightness;
                break;
            case BOARDBRIGHTNESSOVER:
                combinedPackage[0] |= BoardBrightnessOver;
                break;
            case BITMAP:
                combinedPackage[0] |= Bitmap;
                break;
            case EXPRESSIONLIST:
                combinedPackage[0] |= ExpressionList;
                break;
            case SAVEBITMAP:
                combinedPackage[0] |= SaveBitmap;
                break;
            case DELETEBITMAP:
                combinedPackage[0] |= DeleteBitmap;
                break;
            case CHANGEBITMAP:
                combinedPackage[0] |= ChangeBitmap;
                break;
            case LASTEXPRESSION:
                combinedPackage[0] |= LastExpression;
                break;
            case NEXTEXPRESSION:
                combinedPackage[0] |= NextExpression;
                break;
            case LIGHTSTATE:
                combinedPackage[0] |= LightState;
                break;
            case LIGHTBRIGHTNESS:
                combinedPackage[0] |= LightBrightness;
                break;
            case LIGHTBRIGHTNESSOVER:
                combinedPackage[0] |= LightBrightnessOver;
                break;
            case ELECTRICITY:
                combinedPackage[0] |= Electricity;
                break;
            case DEVICENAME:
                combinedPackage[0] |= DeviceName;
                break;
            case DEVICETYPE:
                combinedPackage[0] |= DeviceType;
                break;
            case WIFISSID:
                combinedPackage[0] |= WifiSSID;
                break;
            case WIFIPASSWORD:
                combinedPackage[0] |= WifiPassword;
                break;
            case REBOOT:
                combinedPackage[0] |= Reboot;
                break;
            case SYSTEMSTATE:
                combinedPackage[0] |= SystemState;
        }

        combinedPackage[1] = 0x00;//GET命令的数据包长度为0

        return combinedPackage;
    }
}

enum DataName {
    COLOR,
    BOARDBRIGHTNESS,
    BOARDBRIGHTNESSOVER,
    BITMAP,
    EXPRESSIONLIST,
    SAVEBITMAP,
    DELETEBITMAP,
    CHANGEBITMAP,
    LASTEXPRESSION,
    NEXTEXPRESSION,

    LIGHTSTATE,
    LIGHTBRIGHTNESS,
    LIGHTBRIGHTNESSOVER,

    ELECTRICITY,

    DEVICENAME,
    DEVICETYPE,
    WIFISSID,
    WIFIPASSWORD,

    REBOOT,

    SYSTEMSTATE;
}

enum CmdType {
    Set,
    Get;
}