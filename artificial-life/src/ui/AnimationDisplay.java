package ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import arena.Ball;
import life.Bot;
import life.BotLife;
import life.Configuration;

public class AnimationDisplay implements ActionListener {

	// Have the time rate be configurable 1 to 1000 FPS
	private final Timer timer;

	// Don't make this configurable without a lot of testing...
	private final double pixels = 600;

	// Display all the progress of each time step
	private final AnimationPane animationPane;

	// Maintains state of the bots and balls in the arena
	private volatile BotLife botLife;
	private ControlWindow controlWindow;
	private final Configuration config;
	private JFrame frame;
	
	private volatile long timeSinceLastReset = System.currentTimeMillis();

	// UI Elements
	public class AnimationPane extends JPanel {

		private static final long serialVersionUID = -6659698050645923783L;

		Graphics2D g2;
		AffineTransform original;
		Stroke originalStroke;
		BasicStroke wideStroke;

		private void drawBot(final Graphics2D g, final Bot bot) {
			
			// Convert to position in arena
			int x = (int) (pixels * bot.getxPos());
			int y = (int) (pixels * bot.getyPos());

			int width = (int) (pixels * bot.getSize());
			int height = (int) (pixels * bot.getSize());
			int headSize = (int) (pixels * bot.getHeadSize());

			// Get angle of bot and angle of vision
			double orientation = bot.getOrientation();
			double scanAngle = bot.getScanAngle(config.getScanDegrees()) - 90; // Straight up from head
			int scanLength = (int) (pixels * bot.getScanDistance(config.getAntennaLength()));

			newShape();

			// original = g2.getTransform();
			g2.rotate(Math.toRadians(orientation), x + width / 2, y + height / 2);

			// Draw body
			float fitnessRange = (float) (bot.getFitness() / 1000.0f);
			if (fitnessRange > 0.2) {
				fitnessRange = 0.2f;
			}
			if (fitnessRange < -0.2) {
				fitnessRange = -0.2f;
			}
			Color botColor = new Color(0.5f - fitnessRange, 0.5f + fitnessRange, 0.5f);
			g2.setColor(Color.BLACK);
			g2.drawRect(x, y, width, height);
			g2.setColor(botColor);
			g2.fillRect(x, y, width, height);

			// Draw head
			g2.setColor(Color.BLACK);
			g2.drawRect(x + headSize, y - headSize, headSize, headSize);
			g2.setColor(botColor);
			g2.fillRect(x + headSize, y - headSize, headSize, headSize);

			// Draw feeler
			int middleOfHead = x + headSize + headSize / 2;
			int topOfHead = y - headSize;
			int xAdjustment = (int) (Math.cos(Math.toRadians(scanAngle)) * scanLength);
			int yAdjustment = (int) (Math.sin(Math.toRadians(scanAngle)) * scanLength);

			// Change color to intersection of any balls.
			final Color colorSeen = bot.getIntersectedBallColor();

			// Draw line
			if (colorSeen != null) {
				g2.setColor(bot.getIntersectedBallColor());
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.drawLine(middleOfHead, topOfHead, middleOfHead + xAdjustment, topOfHead + yAdjustment);
		}

		private void drawBall(final Graphics2D g, final Ball ball) {

			newShape();

			int ballX = (int) (pixels * ball.getxPos());
			int ballY = (int) (pixels * ball.getyPos());
			int ballSize = (int) (pixels * ball.getSize());

			// Draw the ball outline
			g2.setColor(Color.BLACK);
			g2.drawOval(ballX, ballY, ballSize, ballSize);

			// Fill the ball color
			g2.setColor(ball.getColor());
			g2.fillOval(ballX, ballY, ballSize, ballSize);
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);

			// Store variables
			this.g2 = (Graphics2D) g;
			this.original = g2.getTransform();
			this.wideStroke = new BasicStroke(8.0f);
			this.originalStroke = g2.getStroke();

			setSize((int) pixels, (int) pixels);
			setBackground(Color.white);

			// Draw all the balls
			for (final Ball ball : AnimationDisplay.this.botLife.getBalls()) {
				drawBall(g2, ball);
			}

			// Draw all the bots
			for (Bot bot : AnimationDisplay.this.botLife.getBots()) {
				drawBot(g2, bot);
			}
		}

		public void newShape() {
			if (this.original != null) {
				this.g2.setTransform(original);
			}
			if (g2.getStroke() == this.wideStroke) {
				g2.setStroke(this.originalStroke);
			}
		}

	};

	public AnimationDisplay(final ControlWindow controlWindow, final Configuration config) {

		this.controlWindow = controlWindow;
		this.config = config;
		this.botLife = controlWindow.getLatestBots(3);

		// Setup the frame
		this.frame = new JFrame("Bot Arena - Generation #0");

		// Stop the timer when the window is closed
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopAnimation();
			}
		});
		frame.setSize((int) pixels, (int) pixels);
		frame.setResizable(false);
		frame.setBackground(Color.WHITE);

		final Container pane = frame.getContentPane();
		this.animationPane = new AnimationPane();
		pane.add(animationPane, BorderLayout.CENTER);

		frame.setVisible(false);

		// Setup the animation timer
		this.timer = new Timer(1000 / config.getFramesPerSecond(), this);
		this.timer.setInitialDelay(0);
		this.timer.setCoalesce(false);
	}

	public synchronized void startAnimation() {

		this.frame.setVisible(true);

		this.timer.setDelay(1000 / config.getFramesPerSecond());
		
		if (!this.timer.isRunning()) {
			this.timer.start();
		}
	}

	public synchronized void stopAnimation() {

		this.frame.setVisible(false);

		if (this.timer.isRunning()) {
			this.timer.stop();
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent arg0) {

		// Update positions of all balls and bots
		this.botLife.doTimeStep(false);

		long timeElapsed = System.currentTimeMillis() - this.timeSinceLastReset;
		if (timeElapsed >= (config.getTimeBetweenUpdates() * 1000)) {
			// We finished a generation, get the latest from the control
			this.botLife = controlWindow.getLatestBots(config.getDisplayBots());
			this.frame.setTitle("Bot Arena - Generation #" + this.controlWindow.getCurrentGeneration());
			this.timeSinceLastReset = System.currentTimeMillis();
		}

		// Next we will redraw everything accordingly...
		this.animationPane.repaint();
		
		this.timer.setDelay(1000 / config.getFramesPerSecond());
	}

}
