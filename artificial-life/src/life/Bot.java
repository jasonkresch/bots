package life;

import java.awt.Color;
import java.util.UUID;

import arena.Point;
import brain.BotBrain;
import brain.NeuralNetwork;

public class Bot implements Comparable<Bot> {

	// Fraction of course we can traverse per time step in forward direction
	//public static double MAX_SPEED = 1.0 / 100;
	//public static double MAX_REVERSE_SPEED = MAX_SPEED / 2.0;

	// 60 degree arc, from -30 to +30 from straight ahead
	//public static double MAX_SCAN_RANGE = 90;

	// 5 degrees per time step
	//public static double MAX_TURN_RATE = 10;
	
	// 25% of the way across the arena
	//double scanDistance = 0.25; // 10% of the way across the arena

	// Neural network
	final BotBrain brain;

	// Spatial Position and Orientation
	private double xPos;
	private double yPos;
	private double orientation = 45; // degrees (to test)

	// Time
	volatile long timeCount = 0;
	private static long memory = 100; // 100 time steps

	// Vision
	double extendedLength = 1;

	// outputs from neural network
	private volatile double scanAngle = 0;
	private volatile double turnRate = 0;
	private volatile double speed = 0;

	// If we've run into a wall
	private double stuck = 0.0;

	// Variables to track internal state
	private long timeOfLastDetection = -100;
	private double lookAngleOfLastDetection = 0;
	private double lastSeenColor = 0.0;
	private double lastAngleDifference = 0;

	// Tracks fitness as minimizing distance to green ball and maximizing distance
	// to red ball
	protected volatile double fitness;

	// Used to draw line of sight with right color
	private volatile Color intersectedBallColor = null;

	// To prevent collisions
	final UUID botId = UUID.randomUUID();

	public Bot(final int brainSize) {
		this.brain = new BotBrain(brainSize);
		this.brain.randomize(); // Set random weights and biases

		this.xPos = Math.random(); // Middle of arena
		this.yPos = Math.random(); // Middle of arena
		this.orientation = Math.random() * 360;
	}

	public Bot(BotBrain brain) {
		this.brain = brain;

		this.xPos = Math.random(); // Middle of arena
		this.yPos = Math.random(); // Middle of arena
		this.orientation = Math.random() * 360;
	}

	public void processInputs(final Color color, double distanceFromObject, double angleDifference, double lateralSpeed,
			double closingSpeed, double closestBallDistance, double closestBallRelativeAngle, double closestBallColor) {

		// Advance the clock
		timeCount++;

		if (color != Color.BLACK) {
			this.timeOfLastDetection = timeCount;
			this.lookAngleOfLastDetection = this.scanAngle;
			this.lastSeenColor = (color == Color.RED ? -1 : 1);
			this.lastAngleDifference = angleDifference;
		}
		long timeSinceLastDetection = (timeCount - this.timeOfLastDetection);

		double nowSeeing = 0;
		if (color == Color.GREEN) {
			nowSeeing = 1.0;
		}
		if (color == Color.RED) {
			nowSeeing = -1.0;
		}

		double noise1 = Math.random();
		double noise2 = Math.random();

		double timeInput = timeSinceLastDetection > memory ? 1.0 : (timeSinceLastDetection / memory);

		// Inputs for the neural network
		double[] inputs = new double[] { nowSeeing, timeInput, distanceFromObject, angleDifference, lateralSpeed,
				closingSpeed, this.lastSeenColor, this.lookAngleOfLastDetection, this.lastAngleDifference,
				this.scanAngle, this.turnRate, this.speed, this.extendedLength, (this.orientation / 180.0) - 1.0, this.getxPos(),
				this.getyPos(), this.stuck, closestBallDistance, closestBallRelativeAngle, closestBallColor, noise1, noise2 };

		double[] results = this.brain.processInputs(inputs);

		this.scanAngle = NeuralNetwork.tanh(results[0]); // Adjust to -1 to 1
		this.turnRate = NeuralNetwork.tanh(results[1]); // Adjust to -1 to 1
		this.speed = NeuralNetwork.tanh(results[2]); // Adjust to -1 to 1
		this.extendedLength = NeuralNetwork.sigmoid(results[3]); // Adjust to 0 to 1
	}

	protected void updateBotPosition(boolean wallsAreSolid, double maxTurnRate, double maxSpeed) {
		
		// Turn Bot
		this.orientation += this.getTurnRate(maxTurnRate);
		if (this.orientation < 0)
			this.orientation += 360;
		if (this.orientation > 360)
			this.orientation -= 360;

		// Move Bot
		this.xPos += this.getSpeed(maxSpeed) * Math.cos(Math.toRadians(this.orientation - 90.0));
		this.yPos += this.getSpeed(maxSpeed) * Math.sin(Math.toRadians(this.orientation - 90.0));

		this.stuck = 0.0;
		
		if (wallsAreSolid) {
			// Get Stuck against wall
			if (this.xPos < 0) {
				this.xPos = 0;
				this.stuck = 1.0;
			}
			if (this.yPos < 0) {
				this.yPos = 0;
				this.stuck = 1.0;
			}
			if (this.xPos > 1) {
				this.xPos = 1;
				this.stuck = 1.0;
			}
			if (this.yPos > 1) {
				this.yPos = 1;
				this.stuck = 1.0;
			}
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
		
		// TODO: Experiment on continuous motion more


	}

	public double getScanAngle(double maxScanRange) {
		// Convert -1 to 1 to the range (-30 to +30)
		return this.scanAngle * (maxScanRange / 2.0);
	}

	public double getTurnRate(double maxTurnRate) {
		// Convert -1 to 1 to the range (-5 to +5) degrees per time unit
		return this.turnRate * maxTurnRate;
	}

	public double getSpeed(double maxSpeed) {
		// Convert -1 to 1 to the range (max reverse speed to max speed)
		if (this.speed > 0)
			return this.speed * maxSpeed;
		else {
			return this.speed * (maxSpeed / 2.0);
		}
	}

	public double getxPos() {
		return xPos;
	}

	public void setxPos(double xPos) {
		this.xPos = xPos;
	}

	public double getyPos() {
		return yPos;
	}

	public void setyPos(double yPos) {
		this.yPos = yPos;
	}

	public Point getCenter() {
		return new Point(getxPos() + getSize() / 2.0, getyPos() + getSize() / 2.0);
	}

	public double getOrientation() {
		return orientation;
	}

	public void setOrientation(double orientation) {
		this.orientation = orientation;
	}

	public double getSize() {
		return 0.08; // 8% of display
	}

	public double getHeadSize() {
		return getSize() / 3.0;
	}

	public double getScanDistance(double maxScanDistance) {
		return this.extendedLength * maxScanDistance;
	}

	public double getFitness() {
		return this.fitness;
	}

	public BotBrain getBrain() {
		return this.brain;
	}

	@Override
	public int compareTo(final Bot other) {
		Double myFitness = Double.valueOf(this.getFitness());
		Double otherFitness = Double.valueOf(other.getFitness());
		int result = otherFitness.compareTo(myFitness);
		if (result == 0) {
			return other.botId.compareTo(this.botId);
		} else {
			return result;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((botId == null) ? 0 : botId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bot other = (Bot) obj;
		if (botId == null) {
			if (other.botId != null)
				return false;
		} else if (!botId.equals(other.botId))
			return false;
		return true;
	}

	public void setIntersectedColor(final Color intersectedBallColor) {
		this.intersectedBallColor = intersectedBallColor;
	}

	public Color getIntersectedBallColor() {
		return intersectedBallColor;
	}

}
