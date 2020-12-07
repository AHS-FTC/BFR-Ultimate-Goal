package com.bfr.util;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Util class that manages network functions, TCP and UDP
 * @author Alex Appleby
 * (with help from 6929 Miles)
 */
public class Network {
    private static final String IP = "192.168.43.57";

    private static DataOutputStream clientout;

    private static Mat tcpMat = new Mat();
    private static TCPThread tcpThread;

    public static void initTCP() throws IOException {
        Socket s = new Socket(InetAddress.getByName(IP), 6969);
        clientout = new DataOutputStream(s.getOutputStream());
    }

    /**
     * Sends string data to the pathfinder. Often encode data like position in the string.
     */
    public static void sendUDP(String message){
        try(DatagramSocket serverSocket = new DatagramSocket()){
            DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), InetAddress.getByName(IP), 5555);
            serverSocket.send(datagramPacket);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void startTCP(Mat mat){
        tcpMat = mat;
        tcpThread = new TCPThread();
        tcpThread.start();
    }

    public static void stopTCP(){
        tcpThread.kill();
    }

    private static class TCPThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (running){
               sendTCPMat();
            }
        }

        public synchronized void kill(){
            running = false;
        }
    }

    public static synchronized void updateTCPMat(Mat mat){
        tcpMat = mat;
    }

    /**
     * Sends an OpenCV Mat over TCP to the pathfinder
     */
    private static synchronized void sendTCPMat(){
        Bitmap image = Bitmap.createBitmap(tcpMat.cols(),
                tcpMat.rows(), Bitmap.Config.RGB_565);

        Utils.matToBitmap(tcpMat, image);

        Bitmap bitmap = (Bitmap) image;
        // toggle with filter
        bitmap = Bitmap.createScaledBitmap(bitmap, 320, 240, true); // 600x600 is very clear 320x240 is also good

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        try {
            clientout.writeInt(byteArray.length);
            clientout.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }
}