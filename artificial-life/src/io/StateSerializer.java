package io;

import java.awt.FileDialog;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;

import brain.BotBrain;
import brain.NeuralNetwork;
import life.Bot;
import life.BotLife;
import life.Configuration;

public class StateSerializer {

	public static String BOT_MAGIC = "BOT BRAIN FILE";
	public static String VERSION = "1.0";

	public static byte[] serializeBotLife(final BotLife botLife) throws IOException {
		
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(bos);
		
		oos.writeObject(BOT_MAGIC);
		oos.writeObject(VERSION);
		
		// Save aspects of training
		oos.writeObject(Long.valueOf(botLife.getGenerationNumber()));
		oos.writeObject(Long.valueOf(botLife.getNumGreenBalls()));
		oos.writeObject(Long.valueOf(botLife.getNumRedBalls()));
		
		// Save bots
		oos.writeObject(Long.valueOf(botLife.getBots().size()));

		// Serialize each bot neural net
		for (final Bot bot : botLife.getBots()) {
			double[][][] layers = bot.getBrain().getNeuralNet().getLayers();
			double[][][] biases = bot.getBrain().getNeuralNet().getBiases();
			oos.writeObject(layers);
			oos.writeObject(biases);
		}

		
		// Save best score and best bot brain
		oos.writeObject(Double.valueOf(botLife.getBestScoreOfAllTime()));
		double[][][] layers = botLife.getBestOfAllTime().getNeuralNet().getLayers();
		double[][][] biases =  botLife.getBestOfAllTime().getNeuralNet().getBiases();
		oos.writeObject(layers);
		oos.writeObject(biases);
		
		// Save configuration settings
		oos.writeObject(botLife.getConfig());
		
		oos.flush();
		bos.flush();

		return bos.toByteArray();
	}

	public static BotLife deserializeBotLife(byte[] botData) throws IOException {

		final ByteArrayInputStream bis = new ByteArrayInputStream(botData);
		ObjectInputStream ois = new ObjectInputStream(bis);

		try {

			final String magic = (String) ois.readObject();
			if (!BOT_MAGIC.equals(magic)) {
				throw new IOException("Invalid file type, magic not found");
			}

			final String version = (String) ois.readObject();
			if (!VERSION.equals(version)) {
				throw new IOException("Invalid file type, version mismatch");
			}

			// Load aspects of training
			final Long generationNumber = (Long) ois.readObject();
			final Long numGreenBalls = (Long) ois.readObject();
			final Long numRedBalls = (Long) ois.readObject();

			
			
			// Load bot brains
			final Long numBots = (Long) ois.readObject();
			System.out.println("Loading " + numBots + " bot brains");
			final List<Bot> bots = new ArrayList<>();
			// Serialize each bot neural net
			for (int i = 0; i < numBots; i++) {
				double[][][] layers = (double[][][]) ois.readObject();
				double[][][] biases = (double[][][]) ois.readObject();
				NeuralNetwork neuralNet = new NeuralNetwork(layers, biases);
				BotBrain botBrain = new BotBrain(neuralNet);
				bots.add(new Bot(botBrain));
			}
			
			// Load best of all time
			final Double bestScoreOfAllTime = (Double) ois.readObject();
			double[][][] layers = (double[][][]) ois.readObject();
			double[][][] biases = (double[][][]) ois.readObject();
			NeuralNetwork neuralNet = new NeuralNetwork(layers, biases);
			BotBrain bestBotBrain = new BotBrain(neuralNet);

			// Attempt to load the config (not all save files have this)
			Configuration config;
			try {
				config = (Configuration) ois.readObject();
			} catch (Exception e) {
				config = Configuration.createDefaultConfiguration();
			}
			
			final BotLife botLife = new BotLife(config, bots, numGreenBalls.intValue(),  numRedBalls.intValue());
			botLife.setBestOfAllTime(bestBotBrain);
			botLife.setBestScoreOfAllTime(bestScoreOfAllTime);
			botLife.setGenerationNumber(generationNumber);
			
			return botLife;

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveBytesToFile(final File outputFile, byte[] data) throws FileNotFoundException, IOException {
		try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
			outputStream.write(data);
		}
	}

	public static byte[] loadBytesFromFile(final File inputFile) throws IOException {
		return Files.readAllBytes(Paths.get(inputFile.getAbsolutePath()));
	}

	public static boolean saveState(final JFrame frame, final BotLife botLife) {
		
		try {
			FileDialog fileDialog = new FileDialog(frame, "Choose Save Location", FileDialog.SAVE);
			(new File("./saves")).mkdirs();
			fileDialog.setDirectory(new File("./saves").getCanonicalPath());
			long dateTime = (new Date()).getTime();
			fileDialog.setFile("botbrains." + dateTime + ".bot");
			fileDialog.setVisible(true);

			final String fileName = fileDialog.getFile();
			if (fileName == null) {
				System.out.println("File selection canceled");
				return false;
			} else {
				return saveState(new File(fileDialog.getDirectory(), fileName), botLife);
			}
		} catch (IOException e) {
			System.err.println("Failed to deserialize bots: " + e.getMessage());
		}

		return false;
	}

	public static boolean saveState(final File saveFile, final BotLife botLife) {
		try {
			System.out.println("Saving bots to " + saveFile);
			byte[] botDataToSave = serializeBotLife(botLife);
			saveBytesToFile(saveFile, botDataToSave);
			return true;
		} catch (IOException e) {
			System.err.println("Failed to deserialize bots: " + e.getMessage());
		}

		return false;
	}

	public static BotLife loadState(JFrame frame) {

		try {
			FileDialog fileDialog = new FileDialog(frame, "Choose State File", FileDialog.LOAD);
			(new File("./saves")).mkdirs();
			fileDialog.setDirectory(new File("./saves").getCanonicalPath());
			fileDialog.setFile("*.bot");
			fileDialog.setVisible(true);

			final String fileName = fileDialog.getFile();
			if (fileName == null) {
				System.out.println("File selection canceled");
				return null;
			} else {
				System.out.println("Loading bots from " + fileName);
				byte[] loadedBotData = loadBytesFromFile(new File(fileDialog.getDirectory(), fileName));
				return deserializeBotLife(loadedBotData);
			}
		} catch (IOException e) {
			System.err.println("Failed to deserialize bots: " + e.getMessage());
		}

		return null;
	}

	
}
