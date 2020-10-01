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
    private static final String IP = "192.168.49.57";

    private static DataOutputStream clientout;

    public static void initTCP(){
        try {
            Socket s = new Socket(InetAddress.getByName("192.168.49.57"), 6969);
            clientout = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Sends an OpenCV Mat over TCP to the pathfinder
     */
    public static void sendTCP(Mat mat){
        Bitmap image = Bitmap.createBitmap(mat.cols(),
                mat.rows(), Bitmap.Config.RGB_565);

        Utils.matToBitmap(mat, image);

        Bitmap bitmap = (Bitmap) image;
        // toggle with filter
        bitmap = Bitmap.createScaledBitmap(bitmap, 600, 600, true); // 600x600 is very clear 320x240 is also good

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        try {
            clientout.writeInt(byteArray.length);
            clientout.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}