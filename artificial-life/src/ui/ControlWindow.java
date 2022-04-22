package ui;

import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import arena.Ball;
import io.StateSerializer;
import life.Bot;
import life.BotLife;
import life.Configuration;

public class ControlWindow implements ActionListener, ChangeListener, DocumentListener {

	private AtomicBoolean running = new AtomicBoolean(false);
	private final Object lock = new Object();

	private Configuration config = Configuration.createDefaultConfiguration();

	// Maintains state of the bots and balls in the arena
	private volatile BotLife botLife = new BotLife(config);

	// Frequency of autosave
	private static final int AUTOSAVE_FREQUENCY = 10;

	/* Start of UI Elements */
	private final JFrame frame;

	private final JButton startStopTraining;
	private final JProgressBar trainingProgressBar;
	private final JLabel generationNumberLabel;

	private final JButton loadTrainingState;
	private final JButton saveTrainingState;
	private final JCheckBox autoSaveOn;

	private final JTextArea outputMessageArea;

	// Generation Parameters

	private final JLabel generationSizeLabel;
	private final JSlider generationSizeSlider;
	private final JLabel generationSizeValue;

	private final JLabel fractionSurviveLabel;
	private final JSlider fractionSurviveSlider;
	private final JLabel fractionSurviveValue;

	private final JLabel numTimeStepsLabel;
	private final JSlider numTimeStepsSlider;
	private final JLabel numTimeStepsValue;

	private final JLabel mutationRateLabel;
	private final JSlider mutationRateSlider;
	private final JLabel mutationRateValue;

	// Bot Parameeters

	private final JLabel brainSizeLabel;
	private final JSlider brainSizeSlider;
	private final JLabel brainSizeValue;

	private final JLabel maxTurnRateLabel;
	private final JSlider maxTurnRateSlider;
	private final JLabel maxTurnRateValue;

	private final JLabel maxSpeedLabel;
	private final JSlider maxSpeedSlider;
	private final JLabel maxSpeedValue;

	private final JLabel fieldOfViewLabel;
	private final JSlider fieldOfViewSlider;
	private final JLabel fieldOfViewValue;

	private final JLabel antennaLengthLabel;
	private final JSlider antennaLengthSlider;
	private final JLabel antennaLengthValue;

	// Environment Parameters

	private final JLabel numGreenBallsLabel;
	private final JSlider numGreenBallsSlider;
	private final JLabel numGreenBallsValue;

	private final JLabel numRedBallsLabel;
	private final JSlider numRedBallsSlider;
	private final JLabel numRedBallsValue;

	private final JLabel ballResetsLabel;
	private final JSlider ballResetsSlider;
	private final JLabel ballResetsValue;

	private final JLabel greenBenefitLabel;
	private final JSlider greenBenefitSlider;
	private final JLabel greenBenefitValue;

	private final JLabel redDetrimentLabel;
	private final JSlider redDetrimentSlider;
	private final JLabel redDetrimentValue;

	private final JLabel ballSizeLabel;
	private final JSlider ballSizeSlider;
	private final JLabel ballSizeValue;

	private final JCheckBox wallsAreSolid;

	// Display Options

	private final JLabel botsToShowLabel;
	private final JSlider botsToShowSlider;
	private final JLabel botsToShowValue;

	private final JLabel fpsLabel;
	private final JSlider fpsSlider;
	private final JLabel fpsValue;

	private final JLabel refreshTimeLabel;
	private final JSlider refreshTimeSlider;
	private final JLabel refreshTimeValue;

	private final JButton showBotsButton;

	/* End of UI Elements */

	private final Runnable task;
	private final Thread botTrainingThread;

	// Shows UI of what training is producing
	private AnimationDisplay display;

	public ControlWindow() {

		this.frame = new JFrame("Bot Control");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container pane = frame.getContentPane();

		JPanel listPane = new JPanel();
		listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));

		/*************************************************************************/

		final JPanel trainingControlRow = new JPanel();

		// Start/Stop button
		this.startStopTraining = new JButton("Start Training");
		this.startStopTraining.setEnabled(true);
		startStopTraining.setFont(new Font("Mono", Font.BOLD, 16));
		startStopTraining.addActionListener(this);
		trainingControlRow.add(startStopTraining);

		// Progress Bar
		this.trainingProgressBar = new JProgressBar(0, 100);
		this.trainingProgressBar.setEnabled(true);
		trainingControlRow.add(trainingProgressBar);

		// Generation Label
		this.generationNumberLabel = new JLabel("Generation #0");
		this.generationNumberLabel.setFont(new Font("Mono", Font.BOLD, 16));
		trainingControlRow.add(generationNumberLabel);

		listPane.add(trainingControlRow);

		/*************************************************************************/

		final JPanel loadSaveRow = new JPanel();

		this.loadTrainingState = new JButton("Load State");
		this.loadTrainingState.setEnabled(true);
		loadTrainingState.setFont(new Font("Mono", Font.BOLD, 16));
		loadTrainingState.addActionListener(this);
		loadSaveRow.add(loadTrainingState);

		this.saveTrainingState = new JButton("Save State");
		this.saveTrainingState.setEnabled(true);
		saveTrainingState.setFont(new Font("Mono", Font.BOLD, 16));
		saveTrainingState.addActionListener(this);
		loadSaveRow.add(saveTrainingState);

		this.autoSaveOn = new JCheckBox("Enable Autosave", true);
		autoSaveOn.setFont(new Font("Mono", Font.BOLD, 16));
		autoSaveOn.addActionListener(this);
		loadSaveRow.add(autoSaveOn);

		listPane.add(loadSaveRow);

		/*************************************************************************/

		final JPanel textOutputRow = new JPanel();

		// Output text
		outputMessageArea = new JTextArea(6, 40);
		outputMessageArea.setFont(new Font("Mono", Font.BOLD, 16));
		outputMessageArea.setLineWrap(true);
		outputMessageArea.setEditable(false);
		final JScrollPane scrollPane = new JScrollPane(outputMessageArea);

		textOutputRow.add(scrollPane);

		listPane.add(textOutputRow);

		/*************************************************************************/

		// Add horizontal separator
		JSeparator s1 = new JSeparator();
		s1.setOrientation(SwingConstants.HORIZONTAL);
		listPane.add(s1);

		/*************************************************************************/

		final JPanel generationSizeRow = new JPanel();

		// Generation Size Label
		generationSizeLabel = new JLabel("Generation Size:");
		generationSizeLabel.setFont(new Font("Mono", Font.BOLD, 16));
		generationSizeRow.add(generationSizeLabel);

		// Generation Size Slider
		generationSizeSlider = new JSlider(20, 100, 30 /* default */);
		generationSizeSlider.setPaintTrack(true);
		generationSizeSlider.setPaintTicks(true);
		generationSizeSlider.setPaintLabels(false);
		generationSizeSlider.setMinorTickSpacing(5);
		generationSizeSlider.setMajorTickSpacing(20);
		generationSizeSlider.addChangeListener(this);
		generationSizeRow.add(generationSizeSlider);

		// Generation Size Value
		generationSizeValue = new JLabel("30 bots");
		generationSizeValue.setFont(new Font("Mono", Font.BOLD, 16));
		generationSizeRow.add(generationSizeValue);

		listPane.add(generationSizeRow);

		/*************************************************************************/

		final JPanel fractionSurviveRow = new JPanel();

		// Fraction Survive Label
		fractionSurviveLabel = new JLabel("Fraction that survive:");
		fractionSurviveLabel.setFont(new Font("Mono", Font.BOLD, 16));
		fractionSurviveRow.add(fractionSurviveLabel);

		// Fraction Survive Slider
		fractionSurviveSlider = new JSlider(50, 95, 75 /* default */);
		fractionSurviveSlider.setPaintTrack(true);
		fractionSurviveSlider.setPaintTicks(true);
		fractionSurviveSlider.setPaintLabels(false);
		fractionSurviveSlider.setMinorTickSpacing(5);
		fractionSurviveSlider.setMajorTickSpacing(10);
		fractionSurviveSlider.addChangeListener(this);
		fractionSurviveRow.add(fractionSurviveSlider);

		// Fraction Survive Value
		fractionSurviveValue = new JLabel("75%");
		fractionSurviveValue.setFont(new Font("Mono", Font.BOLD, 16));
		fractionSurviveRow.add(fractionSurviveValue);

		listPane.add(fractionSurviveRow);

		/*************************************************************************/

		final JPanel numTimeStepsRow = new JPanel();

		// Time Steps Label
		numTimeStepsLabel = new JLabel("Generation Time:");
		numTimeStepsLabel.setFont(new Font("Mono", Font.BOLD, 16));
		numTimeStepsRow.add(numTimeStepsLabel);

		// Time Steps Slider
		numTimeStepsSlider = new JSlider(10, 100, 20 /* default */);
		numTimeStepsSlider.setPaintTrack(true);
		numTimeStepsSlider.setPaintTicks(true);
		numTimeStepsSlider.setPaintLabels(false);
		numTimeStepsSlider.setMinorTickSpacing(5);
		numTimeStepsSlider.setMajorTickSpacing(10);
		numTimeStepsSlider.addChangeListener(this);
		numTimeStepsRow.add(numTimeStepsSlider);

		// Time Steps Value
		numTimeStepsValue = new JLabel("20000 steps");
		numTimeStepsValue.setFont(new Font("Mono", Font.BOLD, 16));
		numTimeStepsRow.add(numTimeStepsValue);

		listPane.add(numTimeStepsRow);

		/*************************************************************************/

		final JPanel mutationRateRow = new JPanel();

		// Mutation Rate Label
		mutationRateLabel = new JLabel("Mutation Rate:");
		mutationRateLabel.setFont(new Font("Mono", Font.BOLD, 16));
		mutationRateRow.add(mutationRateLabel);

		// Mutation Rate Slider
		mutationRateSlider = new JSlider(1, 20, 5 /* default */);
		mutationRateSlider.setPaintTrack(true);
		mutationRateSlider.setPaintTicks(true);
		mutationRateSlider.setPaintLabels(false);
		mutationRateSlider.setMinorTickSpacing(1);
		mutationRateSlider.setMajorTickSpacing(5);
		mutationRateSlider.addChangeListener(this);
		mutationRateRow.add(mutationRateSlider);

		// Mutation Rate Value
		mutationRateValue = new JLabel("5%");
		mutationRateValue.setFont(new Font("Mono", Font.BOLD, 16));
		mutationRateRow.add(mutationRateValue);

		listPane.add(mutationRateRow);

		/*************************************************************************/

		// Add horizontal separator
		JSeparator s2 = new JSeparator();
		s2.setOrientation(SwingConstants.HORIZONTAL);
		listPane.add(s2);

		/*************************************************************************/

		final JPanel brainSizeRow = new JPanel();

		// Brain Size Label
		brainSizeLabel = new JLabel("Bot Brain Size:");
		brainSizeLabel.setFont(new Font("Mono", Font.BOLD, 16));
		brainSizeRow.add(brainSizeLabel);

		// Brain Size Slider
		brainSizeSlider = new JSlider(8, 80, 16 /* default */);
		brainSizeSlider.setPaintTrack(true);
		brainSizeSlider.setPaintTicks(true);
		brainSizeSlider.setPaintLabels(false);
		brainSizeSlider.setMinorTickSpacing(4);
		brainSizeSlider.setMajorTickSpacing(8);
		brainSizeSlider.addChangeListener(this);
		brainSizeRow.add(brainSizeSlider);

		// Brain Size Value
		brainSizeValue = new JLabel("16 neurons");
		brainSizeValue.setFont(new Font("Mono", Font.BOLD, 16));
		brainSizeRow.add(brainSizeValue);

		listPane.add(brainSizeRow);

		/*************************************************************************/

		final JPanel maxTurnRateRow = new JPanel();

		// Brain Size Label
		maxTurnRateLabel = new JLabel("Max Bot Turn Rate:");
		maxTurnRateLabel.setFont(new Font("Mono", Font.BOLD, 16));
		maxTurnRateRow.add(maxTurnRateLabel);

		// Brain Size Slider
		maxTurnRateSlider = new JSlider(0, 20, 10 /* default */);
		maxTurnRateSlider.setPaintTrack(true);
		maxTurnRateSlider.setPaintTicks(true);
		maxTurnRateSlider.setPaintLabels(false);
		maxTurnRateSlider.setMinorTickSpacing(5);
		maxTurnRateSlider.setMajorTickSpacing(10);
		maxTurnRateSlider.addChangeListener(this);
		maxTurnRateRow.add(maxTurnRateSlider);

		// Brain Size Value
		maxTurnRateValue = new JLabel("10°");
		maxTurnRateValue.setFont(new Font("Mono", Font.BOLD, 16));
		maxTurnRateRow.add(maxTurnRateValue);

		listPane.add(maxTurnRateRow);

		/*************************************************************************/

		final JPanel maxSpeedRow = new JPanel();

		// Brain Size Label
		maxSpeedLabel = new JLabel("Max Bot Speed:");
		maxSpeedLabel.setFont(new Font("Mono", Font.BOLD, 16));
		maxSpeedRow.add(maxSpeedLabel);

		// Brain Size Slider
		maxSpeedSlider = new JSlider(0, 5, 1 /* default */);
		maxSpeedSlider.setPaintTrack(true);
		maxSpeedSlider.setPaintTicks(true);
		maxSpeedSlider.setPaintLabels(false);
		maxSpeedSlider.setMinorTickSpacing(1);
		maxSpeedSlider.setMajorTickSpacing(5);
		maxSpeedSlider.addChangeListener(this);
		maxSpeedRow.add(maxSpeedSlider);

		// Brain Size Value
		maxSpeedValue = new JLabel("1%");
		maxSpeedValue.setFont(new Font("Mono", Font.BOLD, 16));
		maxSpeedRow.add(maxSpeedValue);

		listPane.add(maxSpeedRow);

		/*************************************************************************/

		final JPanel fieldOfViewRow = new JPanel();

		// Brain Size Label
		fieldOfViewLabel = new JLabel("Bot Field of View:");
		fieldOfViewLabel.setFont(new Font("Mono", Font.BOLD, 16));
		fieldOfViewRow.add(fieldOfViewLabel);

		// Brain Size Slider
		fieldOfViewSlider = new JSlider(0, 180, 90 /* default */);
		fieldOfViewSlider.setPaintTrack(true);
		fieldOfViewSlider.setPaintTicks(true);
		fieldOfViewSlider.setPaintLabels(false);
		fieldOfViewSlider.setMinorTickSpacing(5);
		fieldOfViewSlider.setMajorTickSpacing(15);
		fieldOfViewSlider.addChangeListener(this);
		fieldOfViewRow.add(fieldOfViewSlider);

		// Brain Size Value
		fieldOfViewValue = new JLabel("90°");
		fieldOfViewValue.setFont(new Font("Mono", Font.BOLD, 16));
		fieldOfViewRow.add(fieldOfViewValue);

		listPane.add(fieldOfViewRow);

		/*************************************************************************/

		final JPanel antennaLengthRow = new JPanel();

		// Brain Size Label
		antennaLengthLabel = new JLabel("Bot Antenna Length:");
		antennaLengthLabel.setFont(new Font("Mono", Font.BOLD, 16));
		antennaLengthRow.add(antennaLengthLabel);

		// Brain Size Slider
		antennaLengthSlider = new JSlider(10, 50, 25 /* default */);
		antennaLengthSlider.setPaintTrack(true);
		antennaLengthSlider.setPaintTicks(true);
		antennaLengthSlider.setPaintLabels(false);
		antennaLengthSlider.setMinorTickSpacing(5);
		antennaLengthSlider.setMajorTickSpacing(10);
		antennaLengthSlider.addChangeListener(this);
		antennaLengthRow.add(antennaLengthSlider);

		// Brain Size Value
		antennaLengthValue = new JLabel("25%");
		antennaLengthValue.setFont(new Font("Mono", Font.BOLD, 16));
		antennaLengthRow.add(antennaLengthValue);

		listPane.add(antennaLengthRow);

		/*************************************************************************/

		// Add horizontal separator
		JSeparator s3 = new JSeparator();
		s3.setOrientation(SwingConstants.HORIZONTAL);
		listPane.add(s3);

		/*************************************************************************/

		final JPanel numGreenBallsRow = new JPanel();

		// Num Green Balls Label
		numGreenBallsLabel = new JLabel("Green Balls:");
		numGreenBallsLabel.setFont(new Font("Mono", Font.BOLD, 16));
		numGreenBallsRow.add(numGreenBallsLabel);

		// Num Green Balls Slider
		numGreenBallsSlider = new JSlider(1, 10, 3 /* default */);
		numGreenBallsSlider.setPaintTrack(true);
		numGreenBallsSlider.setPaintTicks(true);
		numGreenBallsSlider.setPaintLabels(false);
		numGreenBallsSlider.setMinorTickSpacing(1);
		numGreenBallsSlider.setMajorTickSpacing(10);
		numGreenBallsSlider.addChangeListener(this);
		numGreenBallsRow.add(numGreenBallsSlider);

		// Num Green Balls Value
		numGreenBallsValue = new JLabel("3");
		numGreenBallsValue.setFont(new Font("Mono", Font.BOLD, 16));
		numGreenBallsRow.add(numGreenBallsValue);

		listPane.add(numGreenBallsRow);

		/*************************************************************************/

		final JPanel numRedBallsRow = new JPanel();

		// Num Green Balls Label
		numRedBallsLabel = new JLabel("Red Balls:");
		numRedBallsLabel.setFont(new Font("Mono", Font.BOLD, 16));
		numRedBallsRow.add(numRedBallsLabel);

		// Num Green Balls Slider
		numRedBallsSlider = new JSlider(0, 10, 3 /* default */);
		numRedBallsSlider.setPaintTrack(true);
		numRedBallsSlider.setPaintTicks(true);
		numRedBallsSlider.setPaintLabels(false);
		numRedBallsSlider.setMinorTickSpacing(1);
		numRedBallsSlider.setMajorTickSpacing(10);
		numRedBallsSlider.addChangeListener(this);
		numRedBallsRow.add(numRedBallsSlider);

		// Num Green Balls Value
		numRedBallsValue = new JLabel("3");
		numRedBallsValue.setFont(new Font("Mono", Font.BOLD, 16));
		numRedBallsRow.add(numRedBallsValue);

		listPane.add(numRedBallsRow);

		/*************************************************************************/

		final JPanel ballResetsRow = new JPanel();

		// Num Green Balls Label
		ballResetsLabel = new JLabel("Ball Resets Per Generation:");
		ballResetsLabel.setFont(new Font("Mono", Font.BOLD, 16));
		ballResetsRow.add(ballResetsLabel);

		// Num Green Balls Slider
		ballResetsSlider = new JSlider(1, 20, 10 /* default */);
		ballResetsSlider.setPaintTrack(true);
		ballResetsSlider.setPaintTicks(true);
		ballResetsSlider.setPaintLabels(false);
		ballResetsSlider.setMinorTickSpacing(1);
		ballResetsSlider.setMajorTickSpacing(5);
		ballResetsSlider.addChangeListener(this);
		ballResetsRow.add(ballResetsSlider);

		// Num Green Balls Value
		ballResetsValue = new JLabel("10");
		ballResetsValue.setFont(new Font("Mono", Font.BOLD, 16));
		ballResetsRow.add(ballResetsValue);

		listPane.add(ballResetsRow);

		/*************************************************************************/

		final JPanel greenBenefitRow = new JPanel();

		// Num Green Balls Label
		greenBenefitLabel = new JLabel("Green Benefit:");
		greenBenefitLabel.setFont(new Font("Mono", Font.BOLD, 16));
		greenBenefitRow.add(greenBenefitLabel);

		// Num Green Balls Slider
		greenBenefitSlider = new JSlider(1, 10, 5 /* default */);
		greenBenefitSlider.setPaintTrack(true);
		greenBenefitSlider.setPaintTicks(true);
		greenBenefitSlider.setPaintLabels(false);
		greenBenefitSlider.setMinorTickSpacing(1);
		greenBenefitSlider.setMajorTickSpacing(5);
		greenBenefitSlider.addChangeListener(this);
		greenBenefitRow.add(greenBenefitSlider);

		// Num Green Balls Value
		greenBenefitValue = new JLabel("5");
		greenBenefitValue.setFont(new Font("Mono", Font.BOLD, 16));
		greenBenefitRow.add(greenBenefitValue);

		listPane.add(greenBenefitRow);

		/*************************************************************************/

		final JPanel redDetrimentRow = new JPanel();

		// Num Green Balls Label
		redDetrimentLabel = new JLabel("Red Detriment:");
		redDetrimentLabel.setFont(new Font("Mono", Font.BOLD, 16));
		redDetrimentRow.add(redDetrimentLabel);

		// Num Green Balls Slider
		redDetrimentSlider = new JSlider(1, 10, 5 /* default */);
		redDetrimentSlider.setPaintTrack(true);
		redDetrimentSlider.setPaintTicks(true);
		redDetrimentSlider.setPaintLabels(false);
		redDetrimentSlider.setMinorTickSpacing(1);
		redDetrimentSlider.setMajorTickSpacing(5);
		redDetrimentSlider.addChangeListener(this);
		redDetrimentRow.add(redDetrimentSlider);

		// Num Green Balls Value
		redDetrimentValue = new JLabel("5");
		redDetrimentValue.setFont(new Font("Mono", Font.BOLD, 16));
		redDetrimentRow.add(redDetrimentValue);

		listPane.add(redDetrimentRow);

		/*************************************************************************/

		final JPanel ballSizeRow = new JPanel();

		// Num Green Balls Label
		ballSizeLabel = new JLabel("Ball Size:");
		ballSizeLabel.setFont(new Font("Mono", Font.BOLD, 16));
		ballSizeRow.add(ballSizeLabel);

		// Num Green Balls Slider
		ballSizeSlider = new JSlider(1, 25, 8 /* default */);
		ballSizeSlider.setPaintTrack(true);
		ballSizeSlider.setPaintTicks(true);
		ballSizeSlider.setPaintLabels(false);
		ballSizeSlider.setMinorTickSpacing(1);
		ballSizeSlider.setMajorTickSpacing(5);
		ballSizeSlider.addChangeListener(this);
		ballSizeRow.add(ballSizeSlider);

		// Num Green Balls Value
		ballSizeValue = new JLabel("8");
		ballSizeValue.setFont(new Font("Mono", Font.BOLD, 16));
		ballSizeRow.add(ballSizeValue);

		listPane.add(ballSizeRow);

		/*************************************************************************/

		final JPanel solidWallsRow = new JPanel();

		wallsAreSolid = new JCheckBox("Solid Walls", true);
		wallsAreSolid.setFont(new Font("Mono", Font.BOLD, 16));
		wallsAreSolid.addActionListener(this);
		solidWallsRow.add(wallsAreSolid);

		listPane.add(solidWallsRow);

		/*************************************************************************/

		// Add horizontal separator
		JSeparator s4 = new JSeparator();
		s4.setOrientation(SwingConstants.HORIZONTAL);
		listPane.add(s4);

		/*************************************************************************/

		final JPanel botsToShowRow = new JPanel();

		// Num Green Balls Label
		botsToShowLabel = new JLabel("Bots to Show:");
		botsToShowLabel.setFont(new Font("Mono", Font.BOLD, 16));
		botsToShowRow.add(botsToShowLabel);

		// Num Green Balls Slider
		botsToShowSlider = new JSlider(1, 10, 5 /* default */);
		botsToShowSlider.setPaintTrack(true);
		botsToShowSlider.setPaintTicks(true);
		botsToShowSlider.setPaintLabels(false);
		botsToShowSlider.setMinorTickSpacing(1);
		botsToShowSlider.setMajorTickSpacing(5);
		botsToShowSlider.addChangeListener(this);
		botsToShowRow.add(botsToShowSlider);

		// Num Green Balls Value
		botsToShowValue = new JLabel("5");
		botsToShowValue.setFont(new Font("Mono", Font.BOLD, 16));
		botsToShowRow.add(botsToShowValue);

		listPane.add(botsToShowRow);

		/*************************************************************************/

		final JPanel fpsRow = new JPanel();

		// Num Green Balls Label
		fpsLabel = new JLabel("Frames Per Second:");
		fpsLabel.setFont(new Font("Mono", Font.BOLD, 16));
		fpsRow.add(fpsLabel);

		// Num Green Balls Slider
		fpsSlider = new JSlider(20, 500, 100 /* default */);
		fpsSlider.setPaintTrack(true);
		fpsSlider.setPaintTicks(true);
		fpsSlider.setPaintLabels(false);
		fpsSlider.setMinorTickSpacing(20);
		fpsSlider.setMajorTickSpacing(100);
		fpsSlider.addChangeListener(this);
		fpsRow.add(fpsSlider);

		// Num Green Balls Value
		fpsValue = new JLabel("100");
		fpsValue.setFont(new Font("Mono", Font.BOLD, 16));
		fpsRow.add(fpsValue);

		listPane.add(fpsRow);

		/*************************************************************************/

		final JPanel refreshTimeRow = new JPanel();

		// Num Green Balls Label
		refreshTimeLabel = new JLabel("Time to Refresh:");
		refreshTimeLabel.setFont(new Font("Mono", Font.BOLD, 16));
		refreshTimeRow.add(refreshTimeLabel);

		// Num Green Balls Slider
		refreshTimeSlider = new JSlider(5, 120, 5 /* default */);
		refreshTimeSlider.setPaintTrack(true);
		refreshTimeSlider.setPaintTicks(true);
		refreshTimeSlider.setPaintLabels(false);
		refreshTimeSlider.setMinorTickSpacing(30);
		refreshTimeSlider.setMajorTickSpacing(10);
		refreshTimeSlider.addChangeListener(this);
		refreshTimeRow.add(refreshTimeSlider);

		// Num Green Balls Value
		refreshTimeValue = new JLabel("5 seconds");
		refreshTimeValue.setFont(new Font("Mono", Font.BOLD, 16));
		refreshTimeRow.add(refreshTimeValue);

		listPane.add(refreshTimeRow);

		/*************************************************************************/

		final JPanel showBotsRow = new JPanel();

		// Show Bots button
		showBotsButton = new JButton("Show Bot Arena");
		showBotsButton.setEnabled(false);
		showBotsButton.setFont(new Font("Mono", Font.BOLD, 16));
		showBotsButton.addActionListener(this);
		showBotsRow.add(showBotsButton);

		listPane.add(showBotsRow);

		/*************************************************************************/

		pane.add(listPane);

		frame.pack();
		frame.setVisible(true);

		task = new Runnable() {

			public void run() {

				String threadName = Thread.currentThread().getName();
				System.out.println("Hello " + threadName);

				while (true) {
					Thread.yield();

					// Wait for memory allocations to be ready
					try {
						synchronized (lock) {
							lock.wait();
						}
					} catch (InterruptedException e) {
						// Start
					}

					while (ControlWindow.this.running.get()) {

						// Start work
						// long startTime = System.nanoTime();

						synchronized (ControlWindow.this) {

							ControlWindow.this.botLife.doTimeStep(true);

						}

						// Complete work
						// long endTime = System.nanoTime();
						// long timeStepsPerSecond = 1_000_000_000 / ((long) (endTime - startTime));

						// Update stats
						long currentTimeStep = ControlWindow.this.botLife.getTimeStepNumber();
						long currentGeneration = ControlWindow.this.botLife.getGenerationNumber();

						double bestFitness = Math.round(10_000.0 * ControlWindow.this.botLife.getBestBotFitness())
								/ 100.0;
						double avgFitness = Math.round(10_000.0 * ControlWindow.this.botLife.getAverageBotFitness())
								/ 100.0;

						// Every new generation save state to autosave file...
						if (currentTimeStep == 0) {

							if (ControlWindow.this.autoSaveOn.isSelected()
									&& ((currentGeneration % AUTOSAVE_FREQUENCY) == 0)) {
								(new File("./saves")).mkdirs();
								File autosavePath = new File("saves/autosave.bot");
								StateSerializer.saveState(autosavePath, ControlWindow.this.botLife);
							}

							ControlWindow.this.outputMessageArea.append("Generation #" + (currentGeneration - 1)
									+ " highest fitness: " + bestFitness + "%, average fitness: " + avgFitness + "%\n");
							int lastCharPosition = ControlWindow.this.outputMessageArea.getText().length();
							ControlWindow.this.outputMessageArea.setCaretPosition(lastCharPosition);
						}

						double progress = 100.0
								* (((double) currentTimeStep) / ((double) config.getTimeStepsPerGeneration()));
						ControlWindow.this.trainingProgressBar.setValue((int) progress);
						ControlWindow.this.generationNumberLabel.setText("Generation #" + currentGeneration);

					}
				}
			}
		};

		// Start thread
		botTrainingThread = new Thread(task);
		botTrainingThread.start();
	}

	public static void main(String args[]) {

		// Show control window
		final ControlWindow controlWindow = new ControlWindow();
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {

		// Button Clicked
		if (actionEvent.getSource() == this.startStopTraining) {
			if (this.running.compareAndSet(false, true)) {
				this.startStopTraining.setText("Pause Training");

				if (this.showBotsButton.isEnabled() == false) {
					if (this.config.isStarted() == false) {
						// This is the first time started ever, last chance to reset bot brains
						this.botLife.resetBots();
					}

					// This window is hidden and inactive by default
					ControlWindow.this.display = new AnimationDisplay(ControlWindow.this, ControlWindow.this.config);
					this.showBotsButton.setEnabled(true);

					this.config.setStarted(true);
					this.generationSizeSlider.setEnabled(false);
					this.brainSizeSlider.setEnabled(false);
				}

				synchronized (lock) {
					this.lock.notifyAll();
				}
			} else {
				this.running.compareAndSet(true, false);
				this.startStopTraining.setText("Resume Training");
			}
		}

		// Button Clicked
		if (actionEvent.getSource() == this.loadTrainingState) {
			final BotLife loadedBotLife = StateSerializer.loadState(this.frame);

			if (loadedBotLife != null) {
				this.botLife = loadedBotLife;

				// Load configuration settings
				this.processedConfigForUI(this.botLife.getConfig());
				updated();
				this.config = this.botLife.getConfig();

				ControlWindow.this.generationNumberLabel.setText("Generation #" + this.botLife.getGenerationNumber());
			}
		}

		// Button Clicked
		if (actionEvent.getSource() == this.saveTrainingState) {
			StateSerializer.saveState(this.frame, this.botLife);
		}

		// Button Clicked
		if (actionEvent.getSource() == this.showBotsButton) {
			if ("Show Bot Arena".equals(this.showBotsButton.getText())) {
				ControlWindow.this.display.startAnimation();
				this.showBotsButton.setText("Hide Bot Arena");
			} else {
				ControlWindow.this.display.stopAnimation();
				this.showBotsButton.setText("Show Bot Arena");
			}
		}

		updated();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updated();
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		updated();
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		updated();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		updated();
	}

	public void updated() {

		if (this.startStopTraining != null) {
			// this.startStopTraining.setEnabled(inputMessageArea.getText().length() != 0);
		}

		// inputHashLabel.setText("Input Hash: " + inputMessageArea.getText());
		// allocationSizeLabel.setText("Allocation Size: MB");

		ControlWindow.this.generationSizeValue
				.setText(Integer.toString(this.generationSizeSlider.getValue()) + " bots");
		this.config.setGenerationSize(this.generationSizeSlider.getValue());

		ControlWindow.this.fractionSurviveValue.setText(Integer.toString(this.fractionSurviveSlider.getValue()) + "%");
		double fractionThatSurvive = ((double) this.fractionSurviveSlider.getValue()) / 100.0;
		this.config.setFractionThatDie(1.0 - fractionThatSurvive);

		ControlWindow.this.numTimeStepsValue
				.setText(Integer.toString(this.numTimeStepsSlider.getValue() * 1000) + " steps");
		this.config.setTimeStepsPerGeneration(this.numTimeStepsSlider.getValue() * 1000);

		ControlWindow.this.mutationRateValue.setText(Integer.toString(this.mutationRateSlider.getValue()) + "%");
		this.config.setMutationRate(Double.valueOf((this.mutationRateSlider.getValue()) / 100.0));

		ControlWindow.this.brainSizeValue.setText(Integer.toString(this.brainSizeSlider.getValue()) + " neurons");
		this.config.setBrainSize(this.brainSizeSlider.getValue());

		ControlWindow.this.maxTurnRateValue.setText(Integer.toString(this.maxTurnRateSlider.getValue()) + "°");
		this.config.setMaxTurnRate(this.maxTurnRateSlider.getValue());

		ControlWindow.this.maxSpeedValue.setText(Integer.toString(this.maxSpeedSlider.getValue()) + "%");
		this.config.setMaxSpeed(Double.valueOf((this.maxSpeedSlider.getValue()) / 100.0));

		ControlWindow.this.fieldOfViewValue.setText(Integer.toString(this.fieldOfViewSlider.getValue()) + "°");
		this.config.setScanDegrees(this.fieldOfViewSlider.getValue());

		ControlWindow.this.antennaLengthValue.setText(Integer.toString(this.antennaLengthSlider.getValue()) + "%");
		this.config.setAntennaLength(Double.valueOf((this.antennaLengthSlider.getValue()) / 100.0));

		ControlWindow.this.numGreenBallsValue.setText(Integer.toString(this.numGreenBallsSlider.getValue()));
		this.config.setNumGreenBalls(this.numGreenBallsSlider.getValue());

		ControlWindow.this.numRedBallsValue.setText(Integer.toString(this.numRedBallsSlider.getValue()));
		this.config.setNumRedBalls(this.numRedBallsSlider.getValue());

		ControlWindow.this.ballResetsValue.setText(Integer.toString(this.ballResetsSlider.getValue()));
		this.config.setBallResetsPerGeneration(this.ballResetsSlider.getValue());

		ControlWindow.this.greenBenefitValue.setText(Integer.toString(this.greenBenefitSlider.getValue()));
		this.config.setGreenBallBenefit(this.greenBenefitSlider.getValue());

		ControlWindow.this.redDetrimentValue.setText(Integer.toString(this.redDetrimentSlider.getValue()));
		this.config.setRedBallDetriment(this.redDetrimentSlider.getValue());

		ControlWindow.this.ballSizeValue.setText(Integer.toString(this.ballSizeSlider.getValue()));
		this.config.setBallSize(Double.valueOf((this.ballSizeSlider.getValue()) / 100.0));

		this.config.setSolidWalls(ControlWindow.this.wallsAreSolid.isSelected());

		ControlWindow.this.botsToShowValue.setText(Integer.toString(this.botsToShowSlider.getValue()));
		this.config.setDisplayBots(this.botsToShowSlider.getValue());

		ControlWindow.this.fpsValue.setText(Integer.toString(this.fpsSlider.getValue()));
		this.config.setFramesPerSecond(this.fpsSlider.getValue());

		ControlWindow.this.refreshTimeValue.setText(Integer.toString(this.refreshTimeSlider.getValue()) + " seconds");
		this.config.setTimeBetweenUpdates(this.refreshTimeSlider.getValue());
	}

	/**
	 * Creates a temport view of the bots based on current bot population and
	 * configuration
	 */
	protected synchronized BotLife getLatestBots(final int numBots) {

		// Create sub sample of bot population to display
		final List<Bot> botCopies = new ArrayList<Bot>();
		for (int i = 0; i < numBots && i < this.botLife.getBots().size(); i++) {
			Bot bot = this.botLife.getBots().get(i);
			botCopies.add(new Bot(bot.getBrain()));
		}

		// Create balls based on current configuration
		final List<Ball> ballCopies = new ArrayList<Ball>();
		for (final Ball ball : this.botLife.getBalls()) {
			ballCopies.add(new Ball(ball.getSize(), ball.getxPos(), ball.getyPos(), ball.getxVel(), ball.getyVel(),
					ball.getColor()));
		}

		return new BotLife(config, botCopies, ballCopies);
	}

	protected long getCurrentGeneration() {
		return this.botLife.getGenerationNumber();
	}

	public void processedConfigForUI(final Configuration config) {

		this.generationSizeSlider.setValue(config.getGenerationSize());
		this.fractionSurviveSlider.setValue((int) ((1.0 - config.getFractionThatDie()) * 100));
		this.numTimeStepsSlider.setValue((int) (config.getTimeStepsPerGeneration() / 1000));
		this.mutationRateSlider.setValue((int) (config.getMutationRate() * 100));
		this.brainSizeSlider.setValue(config.getBrainSize());
		this.maxTurnRateSlider.setValue((int) config.getMaxTurnRate());
		this.maxSpeedSlider.setValue((int) (config.getMaxSpeed() * 100));
		this.fieldOfViewSlider.setValue((int) config.getScanDegrees());
		this.antennaLengthSlider.setValue((int) (config.getAntennaLength() * 100));
		this.numGreenBallsSlider.setValue(config.getNumGreenBalls());
		this.numRedBallsSlider.setValue(config.getNumRedBalls());
		this.ballResetsSlider.setValue(config.getBallResetsPerGeneration());
		this.greenBenefitSlider.setValue(config.getGreenBallBenefit());
		this.redDetrimentSlider.setValue(config.getRedBallDetriment());
		this.ballSizeSlider.setValue((int) (config.getBallSize() * 100));
		this.wallsAreSolid.setSelected(config.isSolidWalls());
		this.botsToShowSlider.setValue(config.getDisplayBots());
		this.fpsSlider.setValue(config.getFramesPerSecond());
		this.refreshTimeSlider.setValue(config.getTimeBetweenUpdates());

		if (config.isStarted()) {
			// These parameters are locked once evolution starts
			this.generationSizeSlider.setEnabled(false);
			this.brainSizeSlider.setEnabled(false);
		}
	}

}
