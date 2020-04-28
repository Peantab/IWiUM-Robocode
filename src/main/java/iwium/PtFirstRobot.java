package iwium;

import robocode.*;
import robocode.Robot;

import java.awt.*;

public class PtFirstRobot extends Robot {

    final int SEARCH_TURNING_SPEED = 4;
    final int FIRE_TURNING_SPEED = 1;

    volatile boolean firing = true;
    volatile int turnDirection = 1;
    volatile int turnSpeed = SEARCH_TURNING_SPEED;
    double width;
    double height;

    @Override
    public void run() {
        setBodyColor(Color.blue);
        setGunColor(Color.cyan);
        setRadarColor(Color.magenta);
        height = getBattleFieldHeight();
        width = getBattleFieldWidth();

        double topOfCentralRectangle = height/2 + 0.1 * height;
        double bottomOfCentralRectangle = height/2 - 0.1 * height;
        double leftEndOfCentralRectangle = width/2 - 0.1 * width;
        double rightEndOfCentralRectangle = width/2 + 0.1 * width;

        //noinspection InfiniteLoopStatement
        while (true) {
            turnRight(turnSpeed * turnDirection);
            if (turnSpeed == 0) scan();
            if (firing) fire(2);

            double x = getX();
            double y = getY();
            double heading = getHeading();

            if(isHeadingNorth(heading)){
                if (y < bottomOfCentralRectangle) ahead(bottomOfCentralRectangle - y);
                else if (y > topOfCentralRectangle) back(y - topOfCentralRectangle);
            } else if(isHeadingEast(heading)){
                if (x < leftEndOfCentralRectangle) ahead(leftEndOfCentralRectangle - x);
                else if (x > rightEndOfCentralRectangle) back(x - rightEndOfCentralRectangle);
            } else if(isHeadingSouth(heading)){
                if (y > topOfCentralRectangle) ahead(y - topOfCentralRectangle);
                else if (y < bottomOfCentralRectangle) back(bottomOfCentralRectangle - y);
            } else if(isHeadingWest(heading)){
                if (x > rightEndOfCentralRectangle) ahead(x - rightEndOfCentralRectangle);
                else if (x < leftEndOfCentralRectangle) back(leftEndOfCentralRectangle - x);
            }
        }
    }

    private boolean isHeadingNorth(double heading) {
        return heading >= 335 || heading < 25;
    }

    private boolean isHeadingEast(double heading) {
        return heading > 65 && heading < 115;
    }

    private boolean isHeadingSouth(double heading) {
        return heading > 155 && heading < 205;
    }

    private boolean isHeadingWest(double heading) {
        return heading > 245 && heading < 295;
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        firing = true;

        if (event.getBearing() > 1) {
            turnDirection = 1;
            turnSpeed = FIRE_TURNING_SPEED;
        } else if (event.getBearing() < -1) {
            turnDirection = -1;
            turnSpeed = FIRE_TURNING_SPEED;
        } else {
            turnSpeed = 0;
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        out.println("A ha!");
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        out.println("Idiota!");
        firing = false;
        turnSpeed = SEARCH_TURNING_SPEED;
    }

    @Override
    public void onDeath(DeathEvent event) {
        out.println("Do zobaczenia...");
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        out.println("Niech no cie zlapie!");
        if (turnSpeed == SEARCH_TURNING_SPEED){
            if (event.getBearing() > 0) turnDirection = 1;
            else turnDirection = -1;
        }
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
