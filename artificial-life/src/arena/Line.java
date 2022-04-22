package arena;

import life.Bot;
import life.Configuration;

public class Line {

	private static final double ERR = 0.0000001;

	private final Point p1;
	private final Point p2;

	private final double length;

	public Line(final Point p1, final Point p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.length = calculateDistance(this.p1, this.p2);
	}

	public Line(double x1, double y1, double x2, double y2) {
		this(new Point(x1, y1), new Point(y1, y2));
	}

	public static Line fromBotSight(final Bot bot, final Configuration config) {

		double topShoulderX = bot.getxPos();
		double topShoulderY = bot.getyPos();
		double botWidth = bot.getSize();

		double botBodyCenterX = topShoulderX + (botWidth / 2.0);
		double botBodyCenterY = topShoulderY + (botWidth / 2.0);

		double lengthBotCenterToNose = (botWidth / 2.0) + bot.getHeadSize();

		// Calculate point of nose end from body orientation
		double bodyOrientationAngle = bot.getOrientation() - 90.0;
		double noseX = botBodyCenterX + (Math.cos(Math.toRadians(bodyOrientationAngle)) * lengthBotCenterToNose);
		double noseY = botBodyCenterY + (Math.sin(Math.toRadians(bodyOrientationAngle)) * lengthBotCenterToNose);
		final Point nose = new Point(noseX, noseY);

		// Calculate end of scan line from nose, and scan angle and scan length
		double scanAngle = bodyOrientationAngle + bot.getScanAngle(config.getScanDegrees());
		double scanEndX = noseX + (Math.cos(Math.toRadians(scanAngle)) * bot.getScanDistance(config.getAntennaLength()));
		double scanEndY = noseY + (Math.sin(Math.toRadians(scanAngle)) * bot.getScanDistance(config.getAntennaLength()));
		final Point scanEnd = new Point(scanEndX, scanEndY);

		return new Line(nose, scanEnd);
	}

	public Point getP1() {
		return p1;
	}

	public Point getP2() {
		return p2;
	}

	public double getX1() {
		return this.p1.getX();
	}

	public double getY1() {
		return this.p1.getY();
	}

	public double getX2() {
		return this.p2.getX();
	}

	public double getY2() {
		return this.p2.getY();
	}

	public double getLength() {
		return this.length;
	}

	double getSlope() {
		return (getY2() - getY1()) / (getX2() - getX1());
	}

	public boolean doesIntersect(final Ball ball) {
		final Point c = ball.getCenter();
		double radius = ball.getSize() / 2.0;

		// https://stackoverflow.com/questions/23016676/line-segment-and-circle-intersection
		double dX = (getX2() - getX1());
		double dY = (getY2() - getY1());

		double xDiff = getX1() - c.getX();
		double yDiff = getY1() - c.getY();

		double A = (dX * dX) + (dY * dY);
		double B = 2.0 * (dX * xDiff + dY * yDiff);
		double C = (xDiff * xDiff) + (yDiff * yDiff) - (radius * radius);

		double determinate = B * B - 4 * A * C;

		if ((A <= ERR) || (determinate < 0)) {
			// No points of intersection
			return false;
		} else if (determinate == 0) {
			// One solution, make sure it is a point on our line segment
			double t = -B / (2 * A);
			Point i = new Point(getX1() + t * dX, getY1() + t * dY);

			return isPointOnMe(i);
		} else // determinate > 0
		{
			// Two solutions, make sure one is a point on our line segment
			double t1 = ((-B + Math.sqrt(determinate)) / (2.0 * A));
			Point i1 = new Point(getX1() + t1 * dX, getY1() + t1 * dY);

			double t2 = ((-B - Math.sqrt(determinate)) / (2.0 * A));
			Point i2 = new Point(getX1() + t2 * dX, getY1() + t2 * dY);

			return (isPointOnMe(i1) || isPointOnMe(i2));
		}
	}

	public boolean isPointOnMe(final Point p) {
		// Calculate d1 = distance from P1 to p
		double d1 = calculateDistance(p1, p);

		// Calculate d2 = distance from P2 to p
		double d2 = calculateDistance(p2, p);

		// Check that (d1 + d2) ~= (length of this line)
		double sum = d1 + d2;
		return (sum - ERR < this.length) && (sum + ERR > this.length);
	}

	public static double calculateDistance(final Point p1, final Point p2) {
		return Math.sqrt(Math.pow(p2.getX() - p1.getX(), 2.0) + Math.pow(p2.getY() - p1.getY(), 2.0));
	}
	
	public double getAngleRadians() {
		double deltaX = getX2() - getX1();
		double deltaY = getY2() - getY1();
		return Math.atan2(deltaY, deltaX);
	}

}
