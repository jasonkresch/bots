package life;

import java.io.Serializable;

public class Configuration implements Serializable, Cloneable {

	private static final long serialVersionUID = 6165607118397822972L;

	public static Configuration createDefaultConfiguration() {
		return new Configuration();
	}

	/** Evolution Parameters **/

	// Number of Bots that compete in each generation
	private int generationSize = 30;

	// Fraction of bots that die after each generation (worst performing ones die)
	private double fractionThatDie = 0.25;

	// How many time steps between each generation
	private int timeStepsPerGeneration = 20_000;

	// Fraction of genes to mutate for a mutated organism
	private double mutationRate = 0.05;

	/** Bot Parameters **/

	// Number of neurons in hidden layer of bot's neural net
	private int brainSize = 16;

	// Maximum degrees per time step bot can turn
	private double maxTurnRate = 10;

	// Amount of course that bot can cover per time step
	private double maxSpeed = 0.01;

	// Angle of arc across which bot can change it's scan direction
	private double scanDegrees = 90;

	// Percent of distance across course which bot can scan
	private double antennaLength = 0.25;

	/** Environment Parameters **/

	// Number of green balls in environment
	private int numGreenBalls = 3;

	// Number of red balls in environment
	private int numRedBalls = 3;

	// How many times each generation balls are re-randomized
	private int ballResetsPerGeneration = 10;

	// How much benefit to fitness contact with green ball brings
	private int greenBallBenefit = 5;

	// How much detriment to firness contact with red ball brings
	private int redBallDetriment = 5;

	// Diameter of the ball relative to the size of the environment
	private double ballSize = 0.08;

	// Whether or not balls and bots can pass through walls to other side
	private boolean solidWalls = true;

	/** Display Settings **/

	// Number of bots to display in view
	private int displayBots = 5;

	// Number of green balls to display in view
	private int displayGreenBalls = 3;

	// Number of red balls to display in view
	private int displayRedBalls = 3;

	// How many time steps to show before updating with new bots and balls, etc.
	private int timeBetweenUpdates = 5;

	// How many frames per second to show in display view
	private int framesPerSecond = 100;

	/* Interally Track if Evolution has Started */
	// If this is true, we lock certain properties from changing
	private boolean started = false;
	
	/***** Getters and Setters *****/

	public int getGenerationSize() {
		return generationSize;
	}

	public void setGenerationSize(int generationSize) {
		this.generationSize = generationSize;
	}

	public double getFractionThatDie() {
		return fractionThatDie;
	}

	public void setFractionThatDie(double fractionThatDie) {
		this.fractionThatDie = fractionThatDie;
	}

	public int getTimeStepsPerGeneration() {
		return timeStepsPerGeneration;
	}

	public void setTimeStepsPerGeneration(int timeStepsPerGeneration) {
		this.timeStepsPerGeneration = timeStepsPerGeneration;
	}

	public double getMutationRate() {
		return mutationRate;
	}

	public void setMutationRate(double mutationRate) {
		this.mutationRate = mutationRate;
	}

	public int getBrainSize() {
		return brainSize;
	}

	public void setBrainSize(int brainSize) {
		this.brainSize = brainSize;
	}

	public double getMaxTurnRate() {
		return maxTurnRate;
	}

	public void setMaxTurnRate(double maxTurnRate) {
		this.maxTurnRate = maxTurnRate;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public double getScanDegrees() {
		return scanDegrees;
	}

	public void setScanDegrees(double scanDegrees) {
		this.scanDegrees = scanDegrees;
	}

	public double getAntennaLength() {
		return antennaLength;
	}

	public void setAntennaLength(double antennaLength) {
		this.antennaLength = antennaLength;
	}

	public int getNumGreenBalls() {
		return numGreenBalls;
	}

	public void setNumGreenBalls(int numGreenBalls) {
		this.numGreenBalls = numGreenBalls;
	}

	public int getNumRedBalls() {
		return numRedBalls;
	}

	public void setNumRedBalls(int numRedBalls) {
		this.numRedBalls = numRedBalls;
	}

	public int getBallResetsPerGeneration() {
		return ballResetsPerGeneration;
	}

	public void setBallResetsPerGeneration(int ballResetsPerGeneration) {
		this.ballResetsPerGeneration = ballResetsPerGeneration;
	}

	public int getGreenBallBenefit() {
		return greenBallBenefit;
	}

	public void setGreenBallBenefit(int greenBallBenefit) {
		this.greenBallBenefit = greenBallBenefit;
	}

	public int getRedBallDetriment() {
		return redBallDetriment;
	}

	public void setRedBallDetriment(int redBallDetriment) {
		this.redBallDetriment = redBallDetriment;
	}

	public double getBallSize() {
		return ballSize;
	}

	public void setBallSize(double ballSize) {
		this.ballSize = ballSize;
	}

	public boolean isSolidWalls() {
		return solidWalls;
	}

	public void setSolidWalls(boolean solidWalls) {
		this.solidWalls = solidWalls;
	}

	public int getDisplayBots() {
		return displayBots;
	}

	public void setDisplayBots(int displayBots) {
		this.displayBots = displayBots;
	}

	public int getDisplayGreenBalls() {
		return displayGreenBalls;
	}

	public void setDisplayGreenBalls(int displayGreenBalls) {
		this.displayGreenBalls = displayGreenBalls;
	}

	public int getDisplayRedBalls() {
		return displayRedBalls;
	}

	public void setDisplayRedBalls(int displayRedBalls) {
		this.displayRedBalls = displayRedBalls;
	}

	public int getFramesPerSecond() {
		return framesPerSecond;
	}

	public void setFramesPerSecond(int framesPerSecond) {
		this.framesPerSecond = framesPerSecond;
	}

	public int getTimeBetweenUpdates() {
		return timeBetweenUpdates;
	}

	public void setTimeBetweenUpdates(int timeStepsBetweenUpdates) {
		this.timeBetweenUpdates = timeStepsBetweenUpdates;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
