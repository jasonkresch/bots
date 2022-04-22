package brain;

import java.util.Arrays;

public class BotBrain {

	final NeuralNetwork neuralNet;

	public BotBrain(int brainSize) {
		this(22, brainSize, 4);
	}
	
	public BotBrain(final NeuralNetwork neuralNet) {

		this.neuralNet = neuralNet;
	}

	public BotBrain(int inputLayerSize, int hiddenLayerSize, int outputLayerSize) {

		// Inputs to Hidden Layer Weights
		double[][] inputsToHidden = new double[hiddenLayerSize][inputLayerSize];

		// Hidden to Outputs Layer Weights
		double[][] hiddenToOutputs = new double[outputLayerSize][hiddenLayerSize];

		// Create bias arrays
		double[][] hiddenBiases = new double[hiddenLayerSize][1];
		double[][] outputBiases = new double[outputLayerSize][1];

		double[][] layers[] = new double[][][] { inputsToHidden, hiddenToOutputs };
		double[][] biases[] = new double[][][] { hiddenBiases, outputBiases };
		this.neuralNet = new NeuralNetwork(layers, biases);
	}

	public void randomize() {
		this.neuralNet.randomize(this.neuralNet.layers);
		this.neuralNet.randomize(this.neuralNet.biases);
	}

	public double[] processInputs(double[] inputs) {
		return this.neuralNet.propagate(inputs);
	}

	public static void main(String args[]) {
		BotBrain brain1 = new BotBrain(16);
		brain1.neuralNet.randomize(brain1.neuralNet.layers);
		brain1.neuralNet.randomize(brain1.neuralNet.biases);

		BotBrain brain2 = new BotBrain(16);
		brain2.neuralNet.randomize(brain2.neuralNet.layers);
		brain2.neuralNet.randomize(brain2.neuralNet.biases);

		double[] inputs = new double[] { 0.3, 0.1, -0.8 };

		long start = System.nanoTime();
		double[] results1 = brain1.neuralNet.propagate(inputs);
		double[] results2 = brain2.neuralNet.propagate(inputs);
		long end = System.nanoTime();
		System.out.println(end - start);
		System.out.println("Results:");
		System.out.println(Arrays.toString(results1));
		System.out.println(Arrays.toString(results2));

		System.out.println("Parent1:");
		System.out.println(Arrays.deepToString(brain1.neuralNet.layers));
		System.out.println(Arrays.deepToString(brain1.neuralNet.biases));

		System.out.println("Parent2:");
		System.out.println(Arrays.deepToString(brain2.neuralNet.layers));
		System.out.println(Arrays.deepToString(brain2.neuralNet.biases));

		System.out.println("Clone:");
		NeuralNetwork clone = Reproduce.clone(brain2.neuralNet);
		System.out.println(Arrays.deepToString(clone.layers));
		System.out.println(Arrays.deepToString(clone.biases));

		System.out.println("Mutant:");
		NeuralNetwork mutant = Reproduce.mutate(brain2.neuralNet, 0.25);
		System.out.println(Arrays.deepToString(mutant.layers));
		System.out.println(Arrays.deepToString(mutant.biases));

		System.out.println("Average:");
		NeuralNetwork average = Reproduce.average(brain1.neuralNet, brain2.neuralNet);
		System.out.println(Arrays.deepToString(average.layers));
		System.out.println(Arrays.deepToString(average.biases));

		System.out.println("Sample:");
		NeuralNetwork sample = Reproduce.sample(brain1.neuralNet, brain2.neuralNet, 0.5);
		System.out.println(Arrays.deepToString(sample.layers));
		System.out.println(Arrays.deepToString(sample.biases));

	}

	public NeuralNetwork getNeuralNet() {
		return neuralNet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((neuralNet == null) ? 0 : neuralNet.hashCode());
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
		BotBrain other = (BotBrain) obj;
		if (neuralNet == null) {
			if (other.neuralNet != null)
				return false;
		} else if (!neuralNet.equals(other.neuralNet))
			return false;
		return true;
	}

	
}
