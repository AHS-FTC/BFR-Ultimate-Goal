package com.bfr.util;

import android.graphics.Bitmap;
import android.os.Environment;

import com.bfr.hardware.sensors.OdometerImpl;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.internal.android.dx.util.Warning;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * General Utilities class to manage the FTC classes
 * Enables Mocking via the get-InsertHardwareDeviceHere- methods and the testMode boolean
 *
 * @author Alex Appleby
 */
//todo just refactor all of this gross code
public class FTCUtilities { //handles inaccessable objects in FTCApp. hardwareMap exists under OpMode.
    private static HardwareMap hardwareMap;
    private static OpMode opMode;

    private static boolean testMode = false;

    //for logging with Dashboard
    private static boolean dashboardMode = false;
    private static Map<String, DcMotor> testMotors = new HashMap();
    private static Map<String, OdometerImpl> testOdometers = new HashMap();

    private static Controller controller1, controller2;

    private static long mockTime = 0;

    public static String getLogDirectory() {
        if (testMode) {
            return System.getProperty("user.home") + "/Desktop/";
        } else {
            return (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/");
        }
    }

    public static HardwareMap getHardwareMap() {
        if(hardwareMap == null){
            throw new NullPointerException("You forgot to set the hardwareMap in the OpMode, idiot!");
        }

        return hardwareMap;
    }

    public static Controller getController1(){
        return controller1;
    }

    public static Controller getController2(){
        return controller2;
    }

    public static void setOpMode(OpMode opMode) {
        FTCUtilities.opMode = opMode;
        FTCUtilities.hardwareMap = opMode.hardwareMap;

        controller1 = new Controller(opMode.gamepad1);
        controller2 = new Controller(opMode.gamepad2);

        dashboardMode = true;
    }

    public static OpMode getOpMode() {
        return opMode;
    }

    public static void OpLogger(String caption, Object object) {
        if (!testMode) {
            opMode.telemetry.addData(caption, object);
            opMode.telemetry.update();
        } else {
            System.out.println(caption + ": " + object);
        }
    }

    public static void addData(String caption, Object object) {
        if (!testMode) {
            opMode.telemetry.addData(caption, object);
        }
        System.out.println(caption + ": " + object);
    }

    public static void addLine(String line) {
        if (!testMode) {
            opMode.telemetry.addLine(line);
        }
        System.out.println(line);

    }

    public static void updateTelemetry() {
        if (!testMode) {
            opMode.telemetry.update();
        }
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new Warning("Died in FTCUtilities sleep");
        }

    }

    public static boolean opModeIsActive() {
        if (opMode instanceof LinearOpMode) {
            return ((LinearOpMode) opMode).opModeIsActive();
        } else {
            return true;
        }
    }

    public static long getCurrentTimeMillis() {
        if (!testMode) {
            return System.currentTimeMillis();
        } else {
            return mockTime++;
        }
    }

    /**
     * Saves a bitmap to the phone. Sets the file name using time & date information.
     * Saves can be found in the downloads folder of the phone.
     */
    public static void saveImage(Bitmap bitmap) {
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        String fileName = getFileName();
        File img = new File(filePath, fileName);
        if (img.exists())
            img.delete();
        try {
            FileOutputStream out = new FileOutputStream(img);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            throw new Warning(e.getMessage());
        }
    }
    
    public static void saveImage(Mat mat, String fileName){
        Mat rgb = new Mat();

        if(testMode){
            mat.copyTo(rgb);
        } else {
            Imgproc.cvtColor(mat, rgb, Imgproc.COLOR_BGR2RGB);
        }

        Imgcodecs.imwrite(getLogDirectory() + fileName, rgb);
        rgb.release();
    }

    public static void saveImage(Mat mat, String fileName, int conversion2BGR){
        Mat converted = new Mat();
        Imgproc.cvtColor(mat, converted, conversion2BGR);
        saveImage(converted, fileName);
        converted.release();
    }

    public static void saveImage(Mat mat){
        saveImage(mat, getFileName());
    }


    private static String getFileName(){
        Calendar now = Calendar.getInstance();
        return "BotImg_" + now.get(Calendar.DAY_OF_MONTH) + "_" + now.get(Calendar.HOUR_OF_DAY) + "_" + now.get(Calendar.MINUTE) + "_" + now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND) + ".jpg";
    }

    public static void startTestMode() {
        testMode = true;
    }

    /**
     * In debugMode, logging with Dashboard is valid.
     * @return
     */
    public static boolean isDashboardMode(){
        return dashboardMode;
    }

    private FTCUtilities() {
    }
}

