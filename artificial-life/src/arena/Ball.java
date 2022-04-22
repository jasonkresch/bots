package arena;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Ball {

	public static final double fraction = 0.003;

	double xPos;
	double yPos;

	double xVel;
	double yVel;

	final Color color;

	final double size; // % of display

	final double maxBallEnergy = Double.MAX_VALUE; // Double.MAX_VALUE;
	double ballEnergy = maxBallEnergy;

	public Ball() {
		this(0.05 /* size */, 0 /* xpos */ , 0 /* ypos */ , 0 /* xvel */, 0 /* yvel */ , Color.YELLOW);
	}

	public Ball(double size, double xPos, double yPos, double xVel, double yVel, Color color) {
		this.size = size;
		
		this.xPos = xPos;
		this.yPos = yPos;

		this.xVel = xVel;
		this.yVel = yVel;

		this.color = color;
	}

	public static double getMinRandomDouble(double min) {
		double rand = 0;
		while (Math.abs(rand) < min) {
			rand = (2.0 * Math.random()) - 1.0;
		}
		return rand;
	}

	public static Ball createRandomBall(final double size, final Color color) {
		return new Ball(size, Math.random(), Math.random(), fraction * getMinRandomDouble(0.15),
				fraction * getMinRandomDouble(0.15), color);
	}

	public static Ball createRandomRedBall(final double size) {
		return createRandomBall(size, Color.RED);
	}

	public static Ball createRandomGreenBall(final double size) {
		return createRandomBall(size, Color.GREEN);
	}

	public static Ball createRandomYellowBall(final double size) {
		return createRandomBall(size, Color.YELLOW);
	}

	public double getxPos() {
		return xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public double getxVel() {
		return xVel;
	}

	public double getyVel() {
		return yVel;
	}

	public Color getColor() {
		return color;
	}

	public double getSize() {
		return size;
	}

	public void move(boolean wallsAreSolid) {
		
		// Update position
		this.xPos += this.xVel;
		this.yPos += this.yVel;

		if (wallsAreSolid) {
			// Bounce
			if (this.xPos < 0 || this.xPos > 1)
				this.xVel = -this.xVel;
			if (this.yPos < 0 || this.yPos > 1)
				this.yVel = -this.yVel;
		} else {
			// Jump to other side
			if (this.xPos < 0)
				this.xPos += 1;
			if (this.yPos < 0)
				this.yPos += 1;
			if (this.xPos > 1)
				this.xPos -= 1;
			if (this.yPos > 1)
				this.yPos -= 1;
		}
	}

	public Point getCenter() {
		double radius = size / 2.0;
		return new Point(this.xPos + radius, this.yPos + radius);
	}

	public void feed() {
		this.ballEnergy -= 0.10;
		if (this.ballEnergy < 0) {
			// Put energy back
			this.ballEnergy = maxBallEnergy;

			// Teleport to new position and direction
			this.xPos = Math.random();
			this.yPos = Math.random();

			this.xVel = fraction * getMinRandomDouble(0.15);
			this.yVel = fraction * getMinRandomDouble(0.15);
		}

	}

	public static List<Ball> createBalls(final double size, int numGreenBalls, int numRedBalls) {
		List<Ball> balls = new ArrayList<Ball>();

		// Initialize balls
		for (int i = 0; i < numGreenBalls; i++) {
			balls.add(Ball.createRandomGreenBall(size));
		}
		for (int i = 0; i < numRedBalls; i++) {
			balls.add(Ball.createRandomRedBall(size));
		}

		return balls;
	}

}
