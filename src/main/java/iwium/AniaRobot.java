package iwium;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.*;

public class AniaRobot extends Robot {

	public void run() {
		setBodyColor(new Color(0, 200, 0));
		setGunColor(new Color(0, 150, 50));
		setRadarColor(new Color(0, 100, 100));
		setBulletColor(new Color(255, 255, 100));
		setScanColor(new Color(255, 200, 200));

		while (true) {
			ahead(100);
			turnGunRight(360);
			ahead(100);
			turnGunRight(360);
			ahead(100);
			turnRight(90);
		}
	}

	public void onHitWall(HitWallEvent event){
		if (event.getBearing() > -90 && event.getBearing() <= 90) {
			back(150);
		} else {
			ahead(150);
		}
	}

	public void onHitRobot(HitRobotEvent event) {
		turnRight(event.getBearing());
		fire(3);
		ahead(20);
	}

	public void onScannedRobot(ScannedRobotEvent e){
		double distance = e.getDistance();
		if(distance > 800){
			fire(3);
		}
		else if(distance > 400 && distance <= 800) {
			fire(2);
		}
		else if(distance <= 400) {
			fire(1);
		}
	}
}
