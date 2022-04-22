package brain;

public class Reproduce {

	public static NeuralNetwork clone(final NeuralNetwork parent) {
		return new NeuralNetwork(parent.getLayers(), parent.getBiases());
	}

	private static double[][][] mutate(double[][][] array1, double mutationRate) {
				
		int x = array1.length;
		
		double arrayMutated[][][] = new double[x][][];
		
		for (int i = 0; i < x; i++) {
			int y = array1[i].length;
			arrayMutated[i] = new double[y][];
			for (int j = 0; j < y; j++) {
				int z = array1[i][j].length;
				arrayMutated[i][j] = new double[z];
				for (int k = 0; k < z; k++) {
					if (Math.random() < mutationRate) {
						arrayMutated[i][j][k] = Math.random() * 2.0 - 1.0;
					} else {
						arrayMutated[i][j][k] = array1[i][j][k];
					}
				}
			}
		}
		
		return arrayMutated;
	}

	public static NeuralNetwork mutate(final NeuralNetwork mom, double mutationRate) {

		double[][][] momLayers = mom.getLayers();
		double[][][] momBiases = mom.getBiases();

		double[][][] kidLayers = mutate(momLayers, mutationRate);
		double[][][] kidBiases = mutate(momBiases, mutationRate);

		return new NeuralNetwork(kidLayers, kidBiases);
	}

	private static double[][][] tweak(double[][][] array1, double mutationRate) {
		
		int x = array1.length;
		
		double arrayMutated[][][] = new double[x][][];
		
		for (int i = 0; i < x; i++) {
			int y = array1[i].length;
			arrayMutated[i] = new double[y][];
			for (int j = 0; j < y; j++) {
				int z = array1[i][j].length;
				arrayMutated[i][j] = new double[z];
				for (int k = 0; k < z; k++) {
					if (Math.random() < mutationRate) {
						arrayMutated[i][j][k] *= (1.0 + (Math.random() * 0.20 - 0.1));
					} else {
						arrayMutated[i][j][k] = array1[i][j][k];
					}
				}
			}
		}
		
		return arrayMutated;
	}
	
	public static NeuralNetwork tweak(final NeuralNetwork mom, double mutationRate) {

		double[][][] momLayers = mom.getLayers();
		double[][][] momBiases = mom.getBiases();

		double[][][] kidLayers = tweak(momLayers, mutationRate);
		double[][][] kidBiases = tweak(momBiases, mutationRate);

		return new NeuralNetwork(kidLayers, kidBiases);
	}
	
	private static double[][][] average(double[][][] array1, double[][][] array2) {

		int x = array1.length;
		
		double arrayAverage[][][] = new double[x][][];
		
		for (int i = 0; i < x; i++) {
			int y = array1[i].length;
			arrayAverage[i] = new double[y][];
			for (int j = 0; j < y; j++) {
				int z = array1[i][j].length;
				arrayAverage[i][j] = new double[z];
				for (int k = 0; k < z; k++) {
					arrayAverage[i][j][k] = (array1[i][j][k] + array2[i][j][k]) / 2.0;
				}
			}
		}

		return arrayAverage;
	}

	/**
	 * Create child network as average weights and biases between two parents
	 */
	public static NeuralNetwork average(final NeuralNetwork mom, final NeuralNetwork dad) {
		double[][][] momLayers = mom.getLayers();
		double[][][] momBiases = mom.getBiases();

		double[][][] dadLayers = dad.getLayers();
		double[][][] dadBiases = dad.getBiases();

		double[][][] kidLayers = average(momLayers, dadLayers);
		double[][][] kidBiases = average(momBiases, dadBiases);

		return new NeuralNetwork(kidLayers, kidBiases);
	}

	private static double[][][] sample(double[][][] array1, double[][][] array2, double bias) {

		int x = array1.length;
		
		double arraySampled[][][] = new double[x][][];
		
		for (int i = 0; i < x; i++) {
			int y = array1[i].length;
			arraySampled[i] = new double[y][];
			for (int j = 0; j < y; j++) {
				int z = array1[i][j].length;
				arraySampled[i][j] = new double[z];
				for (int k = 0; k < z; k++) {
					if (Math.random() < bias) {
						arraySampled[i][j][k] = array1[i][j][k];
					} else {
						arraySampled[i][j][k] = array2[i][j][k];
					}
				}
			}
		}

		return arraySampled;
	}

	/**
	 * Create child by randomly sampling either mom or dad weights and biases
	 */
	public static NeuralNetwork sample(final NeuralNetwork mom, final NeuralNetwork dad, double momsGenes) {
		double[][][] momLayers = mom.getLayers();
		double[][][] momBiases = mom.getBiases();

		double[][][] dadLayers = dad.getLayers();
		double[][][] dadBiases = dad.getBiases();

		double[][][] kidLayers = sample(momLayers, dadLayers, momsGenes);
		double[][][] kidBiases = sample(momBiases, dadBiases, momsGenes);

		return new NeuralNetwork(kidLayers, kidBiases);
	}

}
