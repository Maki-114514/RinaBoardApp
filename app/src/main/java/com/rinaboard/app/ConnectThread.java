package com.rinaboard.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.*;

import static com.rinaboard.app.PacketUtils.*;
import static com.rinaboard.app.ConnectState.*;

public class ConnectThread extends Thread {
    private static final String SERVER_IP = "192.168.4.22";
    private static final int SERVER_PORT = 14514;
    private static final int TIMEOUT = 2500; // 超时时间为 2.5 秒
    private static final byte[] DATA_TO_SEND = {ASK};

    private ConnectState connectState = DISCONNECT;
    private OnConnectChangedListener onConnectChangedListener;
    private Context context;

    public ConnectThread(Context context) {
        this.context = context;
    }
    public void setOnConnectChangedListener(OnConnectChangedListener onConnectChangedListener){
        this.onConnectChangedListener = onConnectChangedListener;
    }

    public ConnectState getConnectState(){
        return connectState;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            DatagramPacket sendPacket = new DatagramPacket(DATA_TO_SEND, DATA_TO_SEND.length, serverAddress, SERVER_PORT);

            while (!Thread.interrupted()) {
                if(isNetworkAvailable(context))//判断是否有网络连接
                {
                    try {
                        // 发送数据
                        socket.send(sendPacket);
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
                                if(onConnectChangedListener != null)
                                {
                                    onConnectChangedListener.OnConnectChanged(CONNECTED);//调用回传
                                }
                                connectState = CONNECTED;//更新为连接状态
                                System.out.println("Connect to RinaBoard");
                            }

                        } else {
                            //System.out.println("Did not receive expected response from RinaBoard");
                        }

                        // 等待 2 秒
                        Thread.sleep(1000);
                    }catch (SocketTimeoutException e){
                        //如果超时，说明连接断开
                        if(connectState == CONNECTED){//如果原先是连接状态，此时断开连接
                            if(onConnectChangedListener != null)
                            {
                                onConnectChangedListener.OnConnectChanged(DISCONNECT);//调用回传
                            }
                            connectState = DISCONNECT;//更新为未连接状态
                            System.out.println("Disconnect from RinaBoard");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else {
                    if(connectState == CONNECTED){//如果原先是连接状态，此时断开连接
                        if(onConnectChangedListener != null)
                        {
                            onConnectChangedListener.OnConnectChanged(DISCONNECT);//调用回传
                        }
                        connectState = DISCONNECT;//更新为未连接状态
                        System.out.println("Disconnect from RinaBoard");
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
        void OnConnectChanged(ConnectState state);
    }
}


enum ConnectState{
    CONNECTED,
    DISCONNECT;
}