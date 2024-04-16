package com.rinaboard.app;

import android.app.Application;
import android.graphics.Color;

import java.net.*;
import java.util.LinkedList;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.PacketUtils.GetLightBrightness;
import static com.rinaboard.app.RinaBoardApp.SystemState.*;
import static com.rinaboard.app.ConnectState.*;

public class RinaBoardApp extends Application {

    private static RinaBoardApp instance;
    private UDPInteraction udp1;
    private ConnectThread connectThread1;
    private final int ROWS = 16;
    private final int COLS = 18;
    private String deviceName = "";
    private String deviceType = "";
    private String ssid = "";
    private String password = "";
    private int customColor = Color.parseColor("#FF1493");
    private int boardBrightness = 75;
    private boolean lightState = true;
    private int lightBrightness = 127;
    private boolean damageLightState = false;
    private String damageWords = "";
    private SystemState mode = ExpressionMode;
    private final int rowBytes = (COLS + 7) / 8;
    private byte[] editBitmap = new byte[ROWS * rowBytes];
    private float batteryVoltage = 0.0f;
    private LinkedList<StartAnime> startAnimeLinkedList = new LinkedList<>();

    @Override
    public void onCreate() {
        //当打开应用程序时，开始udp通讯
        super.onCreate();
        instance = this;
        startUDPSockets();
        startConnectCheck();
        //创建expression.json文件用于存储表情的键值对
        ExpressionFileManager.createExpressionJsonFile(getApplicationContext());
        StartAnimeFileManager.createStartAnimeJsonFile(getApplicationContext());
    }

    public static RinaBoardApp getInstance() {
        return instance;
    }

    private void startUDPSockets() {
        try {
            // 创建第一个UDP通讯所需的参数
            DatagramSocket socket1 = new DatagramSocket(19198); // 本地端口号
            InetAddress serverAddress1 = InetAddress.getByName("192.168.4.22");
            int serverPort1 = 11451;

            // 创建并启动第一个UDP通讯
            udp1 = new UDPInteraction(socket1, serverAddress1, serverPort1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startConnectCheck() {
        connectThread1 = new ConnectThread(getApplicationContext());
        connectThread1.setOnConnectChangedListener(new ConnectThread.OnConnectChangedListener() {
            @Override
            public void onConnectChanged(ConnectState state) {
                if(state == CONNECTED){
                    if(udp1 != null){
                        //获取系统信息
                        setDeviceName(GetDeviceName(udp1));
                        setDeviceType(GetDeviceType(udp1));
                        setWifiSSID(GetWifiSSID(udp1));
                        setMode(GetSystemState(udp1));

                        //颜色设置
                        setCustomColor(GetColor(udp1));
                        setBoardBrightness(GetBoardBrightness(udp1));

                        //灯条设置
                        setLightState(GetLightState(udp1));
                        setLightBrightness(GetLightBrightness(udp1));

                        setBatteryVoltage(connectThread1.getVoltage());
                    }
                    System.out.println("Connect");
                }else {
                    //获取系统信息
                    setDeviceName("");
                    setDeviceType("");
                    setWifiSSID("");
                    setMode(ExpressionMode);

                    //颜色设置
                    setCustomColor(Color.parseColor("#FF1493"));
                    setBoardBrightness(75);

                    //灯条设置
                    setLightState(true);
                    setLightBrightness(127);
                    System.out.println("Disconnect");

                    setBatteryVoltage(0.0f);
                }

            }
        });

        connectThread1.setOnBatteryVoltageChangedListener(new ConnectThread.OnBatteryVoltageChangedListener() {
            @Override
            public void onBatteryVoltageChanged(float voltage) {
                setBatteryVoltage(voltage);
            }
        });

        // 启动线程前先检查线程状态
        if (connectThread1.getState() == Thread.State.NEW) {
            connectThread1.start();
            System.out.println("Connect checking thread has started");
        } else {
            System.out.println("Connect checking thread is already running");
        }
        
        
    }

    public UDPInteraction getUdp1() {
        return udp1;
    }

    public ConnectThread getConnectThread1() {
        return connectThread1;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getWifiSSID() {
        return ssid;
    }

    public void setWifiSSID(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCustomColor() {
        return customColor;
    }

    public void setCustomColor(int customColor) {
        this.customColor = customColor;
    }

    public int getBoardBrightness() {
        return boardBrightness;
    }

    public void setBoardBrightness(int boardBrightness) {
        this.boardBrightness = boardBrightness;
    }

    public boolean getLightState() {
        return lightState;
    }

    public void setLightState(boolean lightState) {
        this.lightState = lightState;
    }

    public int getLightBrightness() {
        return lightBrightness;
    }

    public void setLightBrightness(int lightBrightness) {
        this.lightBrightness = lightBrightness;
    }

    public SystemState getMode() {
        return mode;
    }

    public void setMode(SystemState mode) {
        this.mode = mode;
    }

    public byte[] getEditBitmap() {
        return editBitmap;
    }

    public void setEditBitmap(byte[] editBitmap) {
        this.editBitmap = editBitmap;
    }

    public float getBatteryVoltage() {
        return batteryVoltage;
    }

    public void setBatteryVoltage(float batteryVoltage) {
        this.batteryVoltage = batteryVoltage;
    }

    public void setStartAnimeLinkedList(LinkedList<StartAnime> startAnimeLinkedList) {
        this.startAnimeLinkedList = startAnimeLinkedList;
    }

    public LinkedList<StartAnime> getStartAnimeLinkedList() {
        return startAnimeLinkedList;
    }

    public String getDamageWords() {
        return damageWords;
    }

    public void setDamageWords(String damageWords) {
        this.damageWords = damageWords;
    }

    public boolean getDamageLightState() {
        return damageLightState;
    }

    public void setDamageLightState(boolean damageLightState) {
        this.damageLightState = damageLightState;
    }

    public enum SystemState {
        ExpressionMode,
        VideoMode,
        RecognitionMode,
        DamageMode
    }
}

