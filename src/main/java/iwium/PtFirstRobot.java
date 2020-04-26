package iwium;

import robocode.*;
import robocode.Robot;

import java.awt.*;

public class PtFirstRobot extends Robot {

    volatile boolean turning = true;
    int turnDirection = 1;
    double width;
    double height;

    @Override
    public void run() {
        setBodyColor(Color.blue);
        setGunColor(Color.cyan);
        setRadarColor(Color.magenta);
        height = getBattleFieldHeight();
        width = getBattleFieldWidth();

        while (true) {
            if(turning) {
                turnRight(4 * turnDirection);
            }

            double x = getX();
            double y = getY();
            double heading = getHeading();

            if(heading >= 335 || heading < 25){
                if (y < (height/2 - 0.1 * height)) ahead(height/2 - 0.1 * height - y);
                else if (y > (height/2 + 0.1 * height)) back(y - (height/2 + 0.1 * height));
            } else if(heading > 65 && heading < 115){
                if (x < (width/2 - 0.1 * width)) ahead((width/2 - 0.1 * width) - x);
                else if (x > (width/2 + 0.1 * width)) back(x - (width/2 + 0.1 * width));
            } else if(heading > 155 && heading < 205){
                if (y > (height/2 + 0.1 * height)) ahead(y - (height/2 + 0.1 * height));
                else if (y < (height/2 - 0.1 * height)) back((height/2 - 0.1 * height) - y);
            } else if(heading > 245 &&heading < 295){
                if (x > (width/2 + 0.1 * width)) ahead(x - (width/2 + 0.1 * width));
                else if (x < (width/2 - 0.1 * width)) back((width/2 - 0.1 * width) - x);
            }
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        turning = false;
        fire(2);

        if (event.getBearing() >= 0) {
            turnDirection = 1;
        } else {
            turnDirection = -1;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        out.println("A ha!");
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        out.println("Idiota!");
        turning = true;
    }

    @Override
    public void onDeath(DeathEvent event) {
        out.println("Do zobaczenia...");
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        out.println("Niech no cię złapię!");
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        out.println("blblblb!");
    }

    // Chodzi o INNE roboty.
    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);
    }
}
