package com.rinaboard.app;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class UDPInteraction {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public UDPInteraction(DatagramSocket socket, InetAddress serverAddress, int serverPort) {
        this.socket = socket;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    // 发送字节包的方法
    public void send(byte[] data) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddress, serverPort);
            socket.send(sendPacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 接收数据的方法
    public byte[] receive() {
        try {
            socket.setSoTimeout(2000);
            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket); // 阻塞直到接收到数据

            // 从 DatagramPacket 中获取接收到的数据
            byte[] receivedData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), 0, receivedData, 0, receivePacket.getLength());
            return receivedData;
        } catch (SocketTimeoutException e) {
            //如果此时发生超时说明连接断开
            System.out.println("Receive data failed!");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String receiveString() {
        try {
            socket.setSoTimeout(2000);

            byte[] receiveData = new byte[200];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket); // 阻塞直到接收到数据

            // 从 DatagramPacket 中获取接收到的数据并转换为字符串
            String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return receivedMessage;

        } catch (SocketTimeoutException e) {
            //如果此时发生超时说明连接断开
            System.out.println("Receive data overtime!");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}