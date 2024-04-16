package com.rinaboard.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;

import java.net.*;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.ConnectState.*;

public class ConnectThread extends Thread {
    private static final String SERVER_IP = "192.168.4.22";
    private static final int SERVER_PORT = 14514;
    private static final int TIMEOUT = 3000; // 超时时间为 3000 秒
    private static final byte[] DATA_TO_ASK = {ASK};
    private static final byte[] DATA_TO_ELECTRICITY = {Electricity};

    private ConnectState connectState = DISCONNECT;
    private OnConnectChangedListener onConnectChangedListener;
    private OnBatteryVoltageChangedListener onBatteryVoltageChangedListener;
    private float voltage = 0.0f;
    private float lastVoltage = voltage;
    private Context context;

    public ConnectThread(Context context) {
        this.context = context;
    }

    public void setOnConnectChangedListener(@NonNull OnConnectChangedListener onConnectChangedListener) {
        this.onConnectChangedListener = onConnectChangedListener;
    }

    public void setOnBatteryVoltageChangedListener(@NonNull OnBatteryVoltageChangedListener onBatteryVoltageChangedListener) {
        this.onBatteryVoltageChangedListener = onBatteryVoltageChangedListener;
    }

    public ConnectState getConnectState() {
        return connectState;
    }

    public float getVoltage() {
        return voltage;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            DatagramPacket sendAskPacket = new DatagramPacket(DATA_TO_ASK, DATA_TO_ASK.length, serverAddress, SERVER_PORT);
            DatagramPacket sendElectricityPacket = new DatagramPacket(DATA_TO_ELECTRICITY, DATA_TO_ELECTRICITY.length, serverAddress, SERVER_PORT);

            while (!Thread.interrupted()) {
                if (isNetworkAvailable(context))//判断是否有网络连接
                {
                    try {
                        // 发送ASK请求
                        socket.send(sendAskPacket);
                        //System.out.println("Sent data to server");

                        // 接收响应前设置超时时间
                        socket.setSoTimeout(TIMEOUT);

                        // 接收响应
                        byte[] receiveBuffer = new byte[20];
                        DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        socket.receive(receivePacket);
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        if (response.equals("Ok to Link")) {//收到回复
                            //System.out.println("Received response from RinaBoard");
                            if (connectState == DISCONNECT)//如果原先状态为未连接，则此时进行连接
                            {
                                connectState = CONNECTED;//更新为连接状态
                                System.out.println("Connect to RinaBoard");
                                if (onConnectChangedListener != null) {
                                    onConnectChangedListener.onConnectChanged(CONNECTED);//调用回传
                                }
                            }

                        } else {
                            System.out.println("Did not receive expected response from RinaBoard");
                        }

                        if (connectState == CONNECTED) {
                            // 发送ELECTRICITY请求
                            socket.send(sendElectricityPacket);

                            // 接收响应前设置超时时间
                            socket.setSoTimeout(TIMEOUT);

                            byte[] receiveBuffer2 = new byte[4];
                            DatagramPacket receivePacket2 = new DatagramPacket(receiveBuffer2, 4);
                            socket.receive(receivePacket2);
                            byte[] bytes = receivePacket2.getData();
                            if(bytes != null){
                                int result = ((bytes[3] & 0xFF) << 24) | ((bytes[2] & 0xFF) << 16) | ((bytes[1] & 0xFF) << 8) | (bytes[0] & 0xFF);
                                voltage = Float.intBitsToFloat(result);
                                if(Math.abs(voltage - lastVoltage) >= 0.01){
                                    lastVoltage = voltage;
                                    if(onBatteryVoltageChangedListener != null){
                                        onBatteryVoltageChangedListener.onBatteryVoltageChanged(voltage);
                                    }
                                }
                            }
                        }

                        // 等待 1 秒
                        Thread.sleep(1000);
                    } catch (SocketTimeoutException e) {
                        //如果超时，说明连接断开
                        if (connectState == CONNECTED) {//如果原先是连接状态，此时断开连接
                            connectState = DISCONNECT;//更新为未连接状态
                            voltage = 0.0f;
                            lastVoltage = 0.0f;
                            System.out.println("Disconnect from RinaBoard");
                            if (onConnectChangedListener != null) {
                                onConnectChangedListener.onConnectChanged(DISCONNECT);//调用回传
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (connectState == CONNECTED) {//如果原先是连接状态，此时断开连接
                        connectState = DISCONNECT;//更新为未连接状态
                        voltage = 0.0f;
                        lastVoltage = 0.0f;
                        System.out.println("Disconnect from RinaBoard");
                        if (onConnectChangedListener != null) {
                            onConnectChangedListener.onConnectChanged(DISCONNECT);//调用回传
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    public interface OnConnectChangedListener {
        void onConnectChanged(ConnectState state);
    }

    public interface OnBatteryVoltageChangedListener {
        void onBatteryVoltageChanged(float voltage);
    }
}


enum ConnectState {
    CONNECTED,
    DISCONNECT;
}