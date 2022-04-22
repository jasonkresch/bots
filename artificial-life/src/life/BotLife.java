package life;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import arena.Ball;
import arena.Line;
import brain.BotBrain;
import brain.NeuralNetwork;
import brain.Reproduce;

public class BotLife {

	// Configuration
	private final Configuration config;

	// Current state of the bot life
	private final List<Bot> bots = new ArrayList<Bot>();
	private final List<Ball> balls = new ArrayList<Ball>();

	// For tracking progress of evolution
	private volatile BotBrain bestOfAllTime;
	private volatile double bestScoreOfAllTime = -1;

	// Stats
	private volatile long timeStepNumber = 0;
	private volatile long generationNumber = 0;
	private volatile double bestBotFitness = 0.0;
	private volatile double averageBotFitness = 0.0;

	public BotLife(final Configuration config) {
		this(config, config.getGenerationSize(), config.getNumGreenBalls(), config.getNumRedBalls());
		this.bestOfAllTime = new BotBrain(config.getBrainSize());
	}

	public BotLife(final Configuration config, List<Bot> bots, List<Ball> balls) {
		this.config = config;
		this.bots.addAll(bots);
		this.balls.addAll(balls);

		// Count the colors of the balls we were provided
		int greenCount = 0;
		int redCount = 0;
		for (Ball ball : balls) {
			if (ball.getColor() == Color.RED) {
				redCount++;
			} else {
				greenCount++;
			}
		}

		this.config.setNumGreenBalls(greenCount);
		this.config.setNumRedBalls(redCount);
		this.bestOfAllTime = new BotBrain(config.getBrainSize());
	}

	public BotLife(final Configuration config, final List<Bot> bots, int numGreenBalls, int numRedBalls) {
		this.config = config;
		this.config.setNumGreenBalls(numGreenBalls);
		this.config.setNumRedBalls(numRedBalls);

		this.bots.addAll(bots);
		this.balls.addAll(Ball.createBalls(config.getBallSize(), config.getNumGreenBalls(), config.getNumRedBalls()));
		this.bestOfAllTime = new BotBrain(config.getBrainSize());
	}

	public BotLife(final Configuration config, int numBots, int numGreenBalls, int numRedBalls) {
		this.config = config;
		this.config.setNumGreenBalls(numGreenBalls);
		this.config.setNumRedBalls(numRedBalls);

		// Initialize bots
		for (int i = 0; i < numBots; i++) {
			this.bots.add(new Bot(config.getBrainSize()));
		}

		this.balls.addAll(Ball.createBalls(config.getBallSize(), config.getNumGreenBalls(), config.getNumRedBalls()));
		this.bestOfAllTime = new BotBrain(config.getBrainSize());
	}

	public void doTimeStep(boolean createGeneration) {

		this.timeStepNumber++;

		// Check if we need to transition to new generation
		int timeStepsPerBallReset = config.getTimeStepsPerGeneration() / config.getBallResetsPerGeneration();
		if ((timeStepNumber % timeStepsPerBallReset) == 0) {
			resetBalls();
		}
		if (timeStepNumber >= config.getTimeStepsPerGeneration()) {
			if (createGeneration) {
				createNextGeneration();
			} else {
				this.generationNumber++;
				this.timeStepNumber = 0;
			}
		}

		// Update Position of Each Ball
		for (final Ball ball : this.balls) {
			ball.move(config.isSolidWalls());
		}

		// Update position of each bot
		for (final Bot bot : this.bots) {

			// Get angle of bot and angle of vision
			double orientation = bot.getOrientation();

			// Send bot data about closest ball (hearing)
			double closestBallDistance = 5.0;
			double closestBallRelativeAngle = 0;
			double closetBallColor = 0;

			// Change color to intersection of any balls.
			Color colorSeen = Color.BLACK;
			double distance = 1;
			double lateralSpeed = 0.0;
			double closingSpeed = 0.0;
			double relativeAngle = 0.0;
			final Line line = Line.fromBotSight(bot, config);

			bot.setIntersectedColor(null);
			for (final Ball ball : balls) {

				Line lineToBall = new Line(line.getP1(), ball.getCenter());
				double ballDistance = lineToBall.getLength();
				if (ballDistance < closestBallDistance) {
					closestBallDistance = ballDistance;
					closestBallRelativeAngle = line.getAngleRadians() - Math.toRadians(bot.getOrientation());
					if (ball.getColor() == Color.RED) {
						closetBallColor = -1;
					} else {
						closetBallColor = 1;
					}
				}

				if (line.doesIntersect(ball)) {

					// Save this with the bot so we can draw it appropriately
					bot.setIntersectedColor(ball.getColor());

					// Save properties of ball to feed into neural network
					colorSeen = ball.getColor();

					// Express distance as from tip of nose to the ball center, as fraction of
					// length of sight
					distance = Line.calculateDistance(line.getP1(), ball.getCenter()) / line.getLength();

					// Create line from nose to center of ball
					relativeAngle = line.getAngleRadians() - lineToBall.getAngleRadians();

					// Ball motion
					double ballLateralSpeed = ball.getxVel(); // -1 going left, 1 going right
					double ballVerticalSpeed = ball.getyVel(); // -1 going up, 1 closing

					// Bot motion
					double maxSpeed = config.getMaxSpeed();
					double botXSpeed = bot.getSpeed(maxSpeed) * Math.cos(Math.toRadians(bot.getOrientation() - 90.0));
					double botYSpeed = bot.getSpeed(maxSpeed) * Math.sin(Math.toRadians(bot.getOrientation() - 90.0));

					// Relative motion (expressed as fraction of our maximum forward speed)
					double relXSpeed = (ballLateralSpeed + botXSpeed) / maxSpeed;
					double relYSpeed = (ballVerticalSpeed + botYSpeed) / maxSpeed;

					double ballVelocity = Math.sqrt(relXSpeed * relXSpeed + relYSpeed * relYSpeed);

					// Calculate direction of ball in degrees (orientated with arena view)
					double ballAngle = Math.toDegrees(Math.atan2(ballVerticalSpeed, ballLateralSpeed));

					// We need to adjust the above values to account for our orientation and angle
					// of sight
					double scanOffsetDegrees = orientation + bot.getScanAngle(config.getScanDegrees());

					// Compensate for our own rotatation to compute relative angle of motion
					double adjustedBallAngle = ballAngle - scanOffsetDegrees;

					// Apply adjusted angle and velocity to compute relative lateral and closing
					// velocity
					lateralSpeed = ballVelocity * Math.cos(Math.toRadians(adjustedBallAngle));
					closingSpeed = ballVelocity * Math.sin(Math.toRadians(adjustedBallAngle));

					// For making ball disappear
					ball.feed();
				}
			}

			// Process the state using the bots neural network to get bot's outputs
			bot.processInputs(colorSeen, distance, relativeAngle, lateralSpeed, closingSpeed, closestBallDistance,
					closestBallRelativeAngle, closetBallColor);

			// Update bot's position and orientation
			bot.updateBotPosition(config.isSolidWalls(), config.getMaxTurnRate(), config.getMaxSpeed());

			// Update bot's fitness based on ball contact
			if (colorSeen == Color.RED) {
				bot.fitness -= config.getRedBallDetriment();
			} else if (colorSeen == Color.GREEN) {
				bot.fitness += config.getGreenBallBenefit();
			}

		}
	}

	public String createNextGeneration() {

		// Update generation number and time step number
		this.generationNumber++;
		this.timeStepNumber = 0;

		SortedSet<Bot> sortedByFitness = new TreeSet<>();
		sortedByFitness.addAll(this.bots);

		// Determine two bots with greatest fitness
		final Bot mom = sortedByFitness.first();
		sortedByFitness.remove(mom);
		final Bot dad = sortedByFitness.first();
		sortedByFitness.remove(dad);
		final Bot thirdBest = sortedByFitness.first();
		sortedByFitness.remove(thirdBest);
		final Bot fourthBest = sortedByFitness.first();
		sortedByFitness.remove(fourthBest);
		final Bot fifthBest = sortedByFitness.first();

		// Save best of all time if new record set
		if (mom.getFitness() > this.bestScoreOfAllTime) {
			this.bestOfAllTime = mom.getBrain();
			this.bestScoreOfAllTime = mom.getFitness();
		}

		// Calculate stats
		double totalFitness = 0.0;
		for (Bot bot : bots) {
			totalFitness += bot.getFitness();
		}
		final String returnStats = "New Generation #: " + generationNumber + " best fitness: " + mom.getFitness()
				+ ", average fitness: " + (totalFitness / bots.size());
		System.out.println(returnStats);
		this.bestBotFitness = mom.getFitness();
		this.averageBotFitness = (totalFitness / bots.size());

		// Parents
		final NeuralNetwork p1 = mom.getBrain().getNeuralNet();
		final NeuralNetwork p2 = dad.getBrain().getNeuralNet();
		final NeuralNetwork p3 = thirdBest.getBrain().getNeuralNet();
		final NeuralNetwork p4 = fourthBest.getBrain().getNeuralNet();
		final NeuralNetwork p5 = fifthBest.getBrain().getNeuralNet();

		// Create some average offspring
		final NeuralNetwork p1p2Avg = Reproduce.average(p1, p2);
		final NeuralNetwork p1p3Avg = Reproduce.average(p1, p3);
		final NeuralNetwork p2p3Avg = Reproduce.average(p2, p3);

		// Create some mixed offspring
		final NeuralNetwork p1p2even = Reproduce.sample(p1, p2, 0.5);
		final NeuralNetwork p1p2mostly = Reproduce.sample(p1, p2, 0.90);
		final NeuralNetwork p1p2bias = Reproduce.sample(p1, p2, 0.75);
		final NeuralNetwork p2p3even = Reproduce.sample(p2, p3, 0.5);
		final NeuralNetwork p2p3mostly = Reproduce.sample(p2, p3, 0.90);
		final NeuralNetwork p2p3bias = Reproduce.sample(p2, p3, 0.75);
		final NeuralNetwork p3p4even = Reproduce.sample(p3, p4, 0.50);

		// Create mutants
		double mutationRate = Math.random() * config.getMutationRate();

		final NeuralNetwork p1Mutant = Reproduce.mutate(p1, mutationRate);
		final NeuralNetwork p2Mutant = Reproduce.mutate(p2, mutationRate);
		final NeuralNetwork p3Mutant = Reproduce.mutate(p3, mutationRate);
		final NeuralNetwork p4Mutant = Reproduce.mutate(p4, mutationRate);
		final NeuralNetwork p5Mutant = Reproduce.mutate(p5, mutationRate);

		final NeuralNetwork p1Tweaked = Reproduce.tweak(p1, mutationRate);
		final NeuralNetwork p2Tweaked = Reproduce.tweak(p2, mutationRate);
		final NeuralNetwork p3Tweaked = Reproduce.tweak(p3, mutationRate);
		final NeuralNetwork p4Tweaked = Reproduce.tweak(p4, mutationRate);
		final NeuralNetwork p5Tweaked = Reproduce.tweak(p5, mutationRate);

		final NeuralNetwork p1TweakedMore = Reproduce.tweak(p1, mutationRate * 5);
		final NeuralNetwork p2TweakedMore = Reproduce.tweak(p2, mutationRate * 5);
		final NeuralNetwork p3TweakedMore = Reproduce.tweak(p3, mutationRate * 5);
		final NeuralNetwork p4TweakedMore = Reproduce.tweak(p4, mutationRate * 5);
		final NeuralNetwork p5TweakedMore = Reproduce.tweak(p5, mutationRate * 5);

		// Make new bots from the new generation
		final List<Bot> nextGeneration = new ArrayList<>();

		// Save best performer of all time
		nextGeneration.add(new Bot(this.bestOfAllTime));

		// Best performers from prior generation
		nextGeneration.add(new Bot(new BotBrain(p1)));
		nextGeneration.add(new Bot(new BotBrain(p2)));
		nextGeneration.add(new Bot(new BotBrain(p3)));
		nextGeneration.add(new Bot(new BotBrain(p4)));
		nextGeneration.add(new Bot(new BotBrain(p5)));

		// Averaged Children
		nextGeneration.add(new Bot(new BotBrain(p1p2Avg)));
		nextGeneration.add(new Bot(new BotBrain(p1p3Avg)));
		nextGeneration.add(new Bot(new BotBrain(p2p3Avg)));

		// Mixed Children
		nextGeneration.add(new Bot(new BotBrain(p1p2even)));
		nextGeneration.add(new Bot(new BotBrain(p1p2mostly)));
		nextGeneration.add(new Bot(new BotBrain(p1p2bias)));
		nextGeneration.add(new Bot(new BotBrain(p2p3even)));
		nextGeneration.add(new Bot(new BotBrain(p2p3mostly)));
		nextGeneration.add(new Bot(new BotBrain(p2p3bias)));
		nextGeneration.add(new Bot(new BotBrain(p3p4even)));

		// Mutants
		nextGeneration.add(new Bot(new BotBrain(p1Mutant)));
		nextGeneration.add(new Bot(new BotBrain(p2Mutant)));
		nextGeneration.add(new Bot(new BotBrain(p3Mutant)));
		nextGeneration.add(new Bot(new BotBrain(p4Mutant)));
		nextGeneration.add(new Bot(new BotBrain(p5Mutant)));

		// Tweaked
		nextGeneration.add(new Bot(new BotBrain(p1Tweaked)));
		nextGeneration.add(new Bot(new BotBrain(p2Tweaked)));
		nextGeneration.add(new Bot(new BotBrain(p3Tweaked)));
		nextGeneration.add(new Bot(new BotBrain(p4Tweaked)));
		nextGeneration.add(new Bot(new BotBrain(p5Tweaked)));

		// Tweaked More
		nextGeneration.add(new Bot(new BotBrain(p1TweakedMore)));
		nextGeneration.add(new Bot(new BotBrain(p2TweakedMore)));
		nextGeneration.add(new Bot(new BotBrain(p3TweakedMore)));
		nextGeneration.add(new Bot(new BotBrain(p4TweakedMore)));
		nextGeneration.add(new Bot(new BotBrain(p5TweakedMore)));

		// Random -- keep things fresh
		nextGeneration.add(new Bot(config.getBrainSize()));
		nextGeneration.add(new Bot(config.getBrainSize()));

		// Delete only the worst bots from each generation, and replace randomly
		// with candidates for the new generation
		final int botsToReplace = (int) (config.getFractionThatDie() * config.getGenerationSize());
		final SortedSet<Bot> survivingBots = new TreeSet<>();
		survivingBots.addAll(this.bots);
		for (int i = 0; i < botsToReplace; i++) {
			final Bot worstPerformer = survivingBots.last();
			survivingBots.remove(worstPerformer);
		}
		// Randomly pick replacement bots to form next generation
		Collections.shuffle(nextGeneration);
		for (int i = 0; i < botsToReplace; i++) {
			survivingBots.add(nextGeneration.get(i));
		}

		// Erase old bots and add copies of surviving bot's brains
		this.bots.clear();
		for (final Bot bot : survivingBots) {
			this.bots.add(new Bot(bot.getBrain()));
		}

		// Reset balls with each generation
		resetBalls();

		return returnStats;
	}

	public void resetBalls() {
		this.balls.clear();
		for (int i = 0; i < this.config.getNumGreenBalls(); i++) {
			balls.add(Ball.createRandomGreenBall(config.getBallSize()));
		}
		for (int i = 0; i < this.config.getNumRedBalls(); i++) {
			balls.add(Ball.createRandomRedBall(config.getBallSize()));
		}
	}

	public void resetBots() {
		this.bots.clear();
		for (int i = 0; i < config.getGenerationSize(); i++) {
			this.bots.add(new Bot(config.getBrainSize()));
		}
	}

	public List<Bot> getBots() {
		return bots;
	}

	public List<Ball> getBalls() {
		return balls;
	}

	public long getTimeStepNumber() {
		return timeStepNumber;
	}

	public long getGenerationNumber() {
		return generationNumber;
	}

	public void setGenerationNumber(long generationNumber) {
		this.generationNumber = generationNumber;
	}

	public int getNumGreenBalls() {
		return config.getNumGreenBalls();
	}

	public int getNumRedBalls() {
		return config.getNumRedBalls();
	}

	public BotBrain getBestOfAllTime() {
		return bestOfAllTime;
	}

	public void setBestOfAllTime(BotBrain bestOfAllTime) {
		this.bestOfAllTime = bestOfAllTime;
	}

	public double getBestScoreOfAllTime() {
		return bestScoreOfAllTime;
	}

	public void setBestScoreOfAllTime(double bestScoreOfAllTime) {
		this.bestScoreOfAllTime = bestScoreOfAllTime;
	}

	public double getBestBotFitness() {
		return (this.bestBotFitness / (double) this.config.getTimeStepsPerGeneration())
				/ ((double) this.config.getGreenBallBenefit());
	}

	public double getAverageBotFitness() {
		return (this.averageBotFitness / (double) this.config.getTimeStepsPerGeneration())
				/ ((double) this.config.getGreenBallBenefit());
	}

	public Configuration getConfig() {
		return config;
	}

}
