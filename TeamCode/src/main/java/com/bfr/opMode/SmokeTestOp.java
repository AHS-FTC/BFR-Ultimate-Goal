package com.bfr.opMode;

import com.bfr.hardware.Motor;
import com.bfr.hardware.SerialServo;
import com.bfr.util.FTCUtilities;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.internal.android.dx.rop.cst.CstArray;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;

import java.util.ArrayList;
import java.util.List;

import static com.bfr.util.FTCUtilities.opModeIsActive;
import static com.bfr.util.FTCUtilities.sleep;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.opMode;

@TeleOp(name = "SmokeTestOp", group = "Iterative Opmode")
//@Disabled
public class SmokeTestOp extends OpMode {
    private Motor r, l, s1, s2, intakeMotor;
    SerialServo indexerServo, holderServo;

    List<Motor> motors;
    List<SerialServo> servos;
    Boolean firstTime = true;
    OpenCvCamera webcam;

    @Override
    public void init() {
        FTCUtilities.setOpMode(this);
        motors = new ArrayList();
        servos = new ArrayList<>();

        r = new Motor("R", 0, false);
        l = new Motor("L", 0, false);
        s1 = new Motor("s1", 41.0, true);
        s2 = new Motor("s1",41.0, true);

        indexerServo = new SerialServo("indexer", false);
        holderServo = new SerialServo("holder", false);
        intakeMotor = new Motor("intake", 103.6, true);
        indexerServo.mapPosition(-.05,.02);
        holderServo.mapPosition(.3,.5);

        motors.add(l);
        motors.add(r);
        motors.add(intakeMotor);

        servos.add(holderServo);
        servos.add(indexerServo);



    }

    @Override
    public void init_loop() {

    }


    @Override
    public void start() {

    }
    @Override
    public void loop() {
            int counter = 0;
            firstTime = true;
            if (gamepad1.b && firstTime) {
                firstTime = false;
                for (int i = 0; i < motors.size(); i++) {
                    motors.get(i).setPower(1);
                    sleep(1000);
                    motors.get(i).setPower(0);
                    while (!gamepad1.b && opModeIsActive()) {
                        System.out.println("testing");

                    }

                }
                for(int i = 0; i<servos.size(); i++){
                    servos.get(i).setPosition(-1);
                    sleep(1000);
                    servos.get(i).setPosition(0);
                    while(!gamepad1.x && opModeIsActive()){
                        System.out.println("testing");
                    }
                }

            }



    }




    @Override
    public void stop() {
        r.setPower(0);
        l.setPower(0);
        s1.setPower(0);
        s2.setPower(0);
        intakeMotor.setPower(0);
        indexerServo.setPosition(0);
        holderServo.setPosition(0);
    }
}




