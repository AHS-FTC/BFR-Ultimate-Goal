package com.bfr.hardware.sensors;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.TypeConversion;

import org.firstinspires.ftc.robotcore.internal.android.dx.util.Warning;

@I2cDeviceType
@DeviceProperties(name = "MB1242 Distance Sensor", description = "Sonar distance sensor from Maxbotix", xmlTag = "MB1242")
public class MB1242DistanceSensor extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    //1110000
    private final static I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create7bit(112);

    private static int writeAddress = 0xe0;

    @Override
    protected boolean doInitialize() {
        return true;
    }

    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {
        return "MB1242 Distance Sensor";
    }

    public int getDistance(){
        deviceClient.write8(0, 81);

        try {
            Thread.sleep(80);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //read two bytes from the sensor (see docs)
        byte[] bytes = deviceClient.read(0, 2);


        //java bytes are naturally in signed form, yet the sensor gives unsigned data. convert to unsigned byte.
        int highByte = bytes[0] & 0xFF;
        int lowByte = bytes[1] & 0xFF;


        //i2c only supports reads of one byte
        //in order to increase the distance that the sensor can read,
        //the sensor formats its data into two combined bytes, aka a 16 bit number
        //
        //format the two bytes into a 16 bit number

        int distance = (highByte * 256) + lowByte;

        System.out.println(highByte);
        System.out.println(lowByte);
        System.out.println("---");

        return distance;
    }


    public MB1242DistanceSensor(I2cDeviceSynch deviceClient) {
        super(deviceClient, true);

        this.deviceClient.setI2cAddress(ADDRESS_I2C_DEFAULT);

        super.registerArmingStateCallback(false);
        this.deviceClient.engage();
    }
}
