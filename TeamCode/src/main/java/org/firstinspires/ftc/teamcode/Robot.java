package org.firstinspires.ftc.teamcode;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This is NOT an opmode.
 * <p>
 * This class can be used to define all the specific hardware for a single robot.
 * In this case that robot is a K9 robot.
 * <p>
 * This hardware class assumes the following device names have been configured on the robot:
 * Note:  All names are lower case and some have single spaces between words.
 * <p>
 * Motor channel:  Left  drive motor:        "left_drive"
 * Motor channel:  Right drive motor:        "right_drive"
 * Servo channel:  Servo to raise/lower arm: "arm"
 * Servo channel:  Servo to open/close claw: "claw"
 * <p>
 * Note: the configuration of the servos is such that:
 * As the arm servo approaches 0, the arm position moves up (away from the floor).
 * As the claw servo approaches 0, the claw opens up (drops the game element).
 */
public class Robot {
    /* Public OpMode members. */
    public DcMotor leftFrontMotor = null;
    public DcMotor leftRearMotor = null;
    public DcMotor rightFrontMotor = null;
    public DcMotor rightRearMotor = null;

    public DcMotor collectorMotor = null;

    public DcMotor launcherMotor = null;
    public Servo launcherServo;

    public TouchSensor launcherLimitTouchSensor;
    public ColorSensor colorSensor;

    /* Local OpMode members. */
    private HardwareMap hwMap = null;
    private ElapsedTime period = new ElapsedTime();
    private LinearOpMode linearOpMode;
    private Thread pullbackThread;
    private DcMotor ENCODER_MOTOR; //initialize in init
    private boolean isLauncherPulledBack = false;

    public boolean isLauncherPulledBack() {
        return isLauncherPulledBack;
    }

    /* Constructor */
    public Robot(LinearOpMode opmode) {
        linearOpMode = opmode;
    }

    /* Initialize standard Hardware interfaces */
    public void init(HardwareMap ahwMap) {
        // save reference to HW Map
        hwMap = ahwMap;

        // Define and Initialize Motors
        leftFrontMotor = hwMap.dcMotor.get("left_front_drive");
        leftRearMotor = hwMap.dcMotor.get("left_rear_drive");
        rightFrontMotor = hwMap.dcMotor.get("right_front_drive");
        rightRearMotor = hwMap.dcMotor.get("right_rear_drive");
        leftFrontMotor.setDirection(DcMotor.Direction.REVERSE);
        leftRearMotor.setDirection(DcMotor.Direction.REVERSE);

        collectorMotor = hwMap.dcMotor.get("collector");

        launcherMotor = hwMap.dcMotor.get("launcher_motor");
        launcherServo = hwMap.servo.get("launcher_servo");

        launcherLimitTouchSensor = hwMap.touchSensor.get("launcher_limit_sensor");
        colorSensor = hwMap.colorSensor.get("beacon_sensor");

        // Set all motors to zero power
        leftFrontMotor.setPower(0);
        leftRearMotor.setPower(0);
        rightFrontMotor.setPower(0);
        rightRearMotor.setPower(0);
        collectorMotor.setPower(0);
        launcherMotor.setPower(0);

        // May want to use RUN_USING_ENCODERS if encoders are installed.
        leftFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftRearMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightRearMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        collectorMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        launcherMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        // Define and initialize ALL installed servos.

        ENCODER_MOTOR = leftFrontMotor;
    }

    /***
     * waitForTick implements a periodic delay. However, this acts like a metronome with a regular
     * periodic tick.  This is used to compensate for varying processing times for each cycle.
     * The function looks at the elapsed cycle time, and sleeps for the remaining time interval.
     *
     * @param periodMs Length of wait cycle in mSec.
     */
    public void waitForTick(long periodMs) {

        long remaining = periodMs - (long) period.milliseconds();

        // sleep for the remaining portion of the regular cycle period.
        if (remaining > 0) {
            try {
                Thread.sleep(remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Reset the cycle clock for the next pass.
        period.reset();
    }

    public void moveForward(double distance, double speed) {
        double encoderSubtractor = leftFrontMotor.getCurrentPosition();
        if (distance >= 0) {
            while (ENCODER_MOTOR.getCurrentPosition() - encoderSubtractor < distance && linearOpMode.opModeIsActive()) {
                leftFrontMotor.setPower(speed);
                leftRearMotor.setPower(speed);
                rightFrontMotor.setPower(speed);
                rightRearMotor.setPower(speed);
            }
            leftFrontMotor.setPower(0);
            leftRearMotor.setPower(0);
            rightFrontMotor.setPower(0);
            rightRearMotor.setPower(0);
        } else {
            while (ENCODER_MOTOR.getCurrentPosition() - encoderSubtractor > distance && linearOpMode.opModeIsActive()) {
                leftFrontMotor.setPower(-speed);
                leftRearMotor.setPower(-speed);
                rightFrontMotor.setPower(-speed);
                rightRearMotor.setPower(-speed);
            }
            leftFrontMotor.setPower(0);
            leftRearMotor.setPower(0);
            rightFrontMotor.setPower(0);
        }
    }

    public void moveSideways(double distance, double speed) {
        double encoderSubtractor = leftFrontMotor.getCurrentPosition();
        if (distance >= 0) {
            while (ENCODER_MOTOR.getCurrentPosition() - encoderSubtractor < distance && linearOpMode.opModeIsActive()) {
                leftFrontMotor.setPower(speed);
                leftRearMotor.setPower(-speed);
                rightFrontMotor.setPower(-speed);
                rightRearMotor.setPower(speed);
            }
            leftFrontMotor.setPower(0);
            leftRearMotor.setPower(0);
            rightFrontMotor.setPower(0);
            rightRearMotor.setPower(0);
        } else {
            while (ENCODER_MOTOR.getCurrentPosition() - encoderSubtractor > distance && linearOpMode.opModeIsActive()) {
                leftFrontMotor.setPower(-speed);
                leftRearMotor.setPower(speed);
                rightFrontMotor.setPower(speed);
                rightRearMotor.setPower(-speed);
            }
            leftFrontMotor.setPower(0);
            leftRearMotor.setPower(0);
            rightFrontMotor.setPower(0);
            rightRearMotor.setPower(0);
        }
    }
    public void engageLauncher() {
        launcherServo.setPosition(0.1);
    }

    public void disengageLauncher() {
        launcherServo.setPosition(1);
        isLauncherPulledBack = false;
    }

    public void launchAndReload() {
        double initialRuntime = linearOpMode.getRuntime();
        if (pullbackThread == null || !pullbackThread.isAlive()) {
            pullbackThread = new Thread(new PullBackLauncherRunnable());
            pullbackThread.start();
        }
    }

    public boolean initLauncher(boolean initMethod) {
        engageLauncher();
        waitForTick(500);

        double beginingTime = linearOpMode.getRuntime();

        if (initMethod) {
            while (!launcherLimitTouchSensor.isPressed() && linearOpMode.getRuntime() < beginingTime + 1.67) {
                launcherMotor.setPower(1);
            }
        } else {
            while (!launcherLimitTouchSensor.isPressed() && linearOpMode.getRuntime() < beginingTime + 1.67 && linearOpMode.opModeIsActive()) {
                launcherMotor.setPower(1);
            }
        }
        launcherMotor.setPower(0);
        isLauncherPulledBack = true;
//        opmode.telemetry.addData("status", "Launcher ready to fire");
//        opmode.telemetry.update();

        Log.i("RKR", "Pullback took " + (linearOpMode.getRuntime() - beginingTime));

        return launcherLimitTouchSensor.isPressed();
    }


    private class PullBackLauncherRunnable implements Runnable {

        @Override
        public void run() {
            disengageLauncher();
            waitForTick(500);
            initLauncher(false);
        }
    }
}
