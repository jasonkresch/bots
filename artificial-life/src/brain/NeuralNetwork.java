package brain;

import java.util.Arrays;

public class NeuralNetwork {

	// Each layer is a 2d array of weights
	// Each layer represents synapse strength between every pair of neurons between
	// each layer
	protected final double[][] layers[];

	// Each neuron at each layer has its own bias
	protected final double[][] biases[];

	// Example 1:
	// layer[0] (input to hidden layer weights)
	// layer[1] (hidden to output layer weights)

	public NeuralNetwork(final double[][] layers[], double[][] biases[]) {
		// Check that number of rows of layer n equals number of columns in layer n+1
		for (int i = 0; i < layers.length - 1; i++) {
			double[][] thisLayer = layers[i];
			double[][] nextLayer = layers[i + 1];

			if (thisLayer.length != nextLayer[0].length) {
				throw new RuntimeException("Column row mismatch between layers");
			}
		}

		this.layers = layers;
		this.biases = biases;
	}

	// Return outputs of this network for given inputs
	public double[] propagate(final double inputs[]) {

		// Convert array into a matrix of 1 column
		double[][] inputMatrix = new double[inputs.length][1];
		for (int i = 0; i < inputs.length; i++) {
			inputMatrix[i][0] = inputs[i];
		}

		return propagate(inputMatrix);
	}

	// Return outputs of this network for given inputs
	public double[] propagate(final double inputs[][]) {

		// First multiply inputs with first layer
		double[][] result = matrixMultiply(layers[0], inputs);
		result = matrixAdd(result, biases[0]);
		normalizeMatrix(result);

		// Do matrix multiplication starting from inputs through each layer
		for (int i = 1; i < layers.length; i++) {
			result = matrixMultiply(layers[i], result);
			result = matrixAdd(result, biases[i]);

			// Don't normalize the outputs
			if (i < (layers.length - 1)) {
				normalizeMatrix(result);
			}
		}

		// Convert results matrix to array
		double[] resultArray = new double[result.length];
		for (int i = 0; i < result.length; i++) {
			resultArray[i] = result[i][0];
		}
		return resultArray;
	}

	public static double[][] matrixMultiply(double[][] matrix1, double[][] matrix2) {
		int rows = matrix1.length;
		int cols = matrix2[0].length;
		double[][] result = new double[rows][cols];

		// Do dot product multiplication
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				for (int k = 0; k < cols; k++) {
					result[i][j] += matrix1[i][k] * matrix2[k][j];
				}
			}
		}

		return result;
	}

	public static double[][] matrixAdd(double[][] matrix1, double[][] matrix2) {
		int rows = matrix1.length;
		int cols = matrix1[0].length;
		double[][] result = new double[rows][cols];

		// Do matrix addition
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				result[i][j] = matrix1[i][j] + matrix2[i][j];
			}
		}

		return result;
	}

	/**
	 * Use sigmoid function (exp(x)/(exp(x)+1)) to put all values between -1 and 1.
	 */
	public static void normalizeMatrix(double[][] matrix) {
		int rows = matrix.length;
		int cols = matrix[0].length;

		// Do dot product multiplication
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				matrix[i][j] = rectifiedLinearUnit(matrix[i][j]);
			}
		}
	}

	// Implements activation above 0
	public static double rectifiedLinearUnit(double input) {
		if (input < 0) {
			return 0;
		} else {
			return input;
		}
		// return Math.max(0, input);
	}

	// Squishes all valaues to (0, 1)
	public static double sigmoid(double input) {
		return Math.exp(input) / (Math.exp(input) + 1.0);
	}

	// Squishes all valaues to (-1, 1)
	public static double tanh(double input) {
		return Math.tanh(input);
	}

	// Randomize all weights
	public void randomize(double array[][][]) {
		for (double layer[][] : array) {
			int rows = layer.length;
			int cols = layer[0].length;
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					layer[i][j] = Math.random() * 2.0 - 1.0;
				}
			}
		}
	}

	public static double[][][] deepCopyArray(double[][][] array) {
		int x = array.length;

		double arrayCopy[][][] = new double[x][][];

		for (int i = 0; i < x; i++) {
			int y = array[i].length;
			arrayCopy[i] = new double[y][];
			for (int j = 0; j < y; j++) {
				int z = array[i][j].length;
				arrayCopy[i][j] = new double[z];
				for (int k = 0; k < z; k++) {
					arrayCopy[i][j][k] = array[i][j][k];
				}
			}
		}

		return arrayCopy;
	}

	public double[][][] getLayers() {
		return deepCopyArray(this.layers);
	}

	public double[][][] getBiases() {
		return deepCopyArray(this.biases);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(biases);
		result = prime * result + Arrays.deepHashCode(layers);
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
		NeuralNetwork other = (NeuralNetwork) obj;
		if (!Arrays.deepEquals(biases, other.biases))
			return false;
		if (!Arrays.deepEquals(layers, other.layers))
			return false;
		return true;
	}

}
