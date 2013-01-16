package com.lagodiuk.nn.genetic.test;

import java.util.Random;

import com.lagodiuk.ga.Environment;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;
import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.ThresholdFunction;
import com.lagodiuk.nn.ThresholdFunctions;
import com.lagodiuk.nn.genetic.NeuralNetworkChromosome;
import com.lagodiuk.nn.genetic.NeuralNetworkContext;

public class Launcher {

	private static final int maxWeightNum = 10;

	private static int getRandomWeight(Random random) {
		return random.nextInt(maxWeightNum) - random.nextInt(maxWeightNum);
	}

	public static void main(String[] args) {
		NeuralNetwork nn = initilNeuralNetwork();
		NeuralNetworkContext context = new NeuralNetworkContext(nn);

		Population<NeuralNetworkChromosome> population = new Population<NeuralNetworkChromosome>();
		NeuralNetworkChromosome initialGene = context.getChromosome();
		for (int i = 0; i < 20; i++) {
			population.addChromosome(initialGene.mutate());
		}

		Fitness<NeuralNetworkChromosome, Double> fit = new NnXorFitness(context);

		Environment<NeuralNetworkChromosome, Double> env = new Environment<NeuralNetworkChromosome, Double>(population, fit);
		env.setParentChromosomesSurviveCount(1);

		env.addIterationListener(new IterartionListener<NeuralNetworkChromosome, Double>() {
			private Random random = new Random();

			@Override
			public void update(Environment<NeuralNetworkChromosome, Double> environment) {
				NeuralNetworkChromosome gene = environment.getBest();
				Double d = environment.fitness(gene);
				System.out.println(environment.getIteration() + "\t" + d);

				if (d <= 0.1) {
					environment.terminate();
				}

				environment.setParentChromosomesSurviveCount(this.random.nextInt(environment.getPopulation().getSize()));
			}
		});

		env.iterate(5500);

		NeuralNetworkChromosome gene = env.getBest();
		System.out.println(gene.getWeights());
		System.out.println(gene.getNeurons());
		context.applyChromosome(gene);
		NeuralNetwork evoNn = context.getNeuralNetwork();
		for (int i = -10; i < -6; i++) {
			System.out.println();
			for (int j = -10; j < -6; j++) {
				evoNn.putSignalToNeuron(0, i);
				evoNn.putSignalToNeuron(1, j);
				evoNn.activate();
				System.out.println(i + " XOR " + j + " = " + evoNn.getAfterActivationSignal(5));
			}
		}
	}

	private static NeuralNetwork initilNeuralNetwork() {
		NeuralNetwork nn = new NeuralNetwork(6);
		for (int i = 0; i < 6; i++) {
			ThresholdFunction f = ThresholdFunctions.getRandomFunction();
			nn.setNeuronFunction(i, f, f.getDefaultParams());
		}
		nn.setNeuronFunction(0, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams());
		nn.setNeuronFunction(1, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams());

		Random rnd = new Random();
		nn.addLink(0, 2, getRandomWeight(rnd));
		nn.addLink(0, 3, getRandomWeight(rnd));
		nn.addLink(0, 4, getRandomWeight(rnd));
		nn.addLink(1, 2, getRandomWeight(rnd));
		nn.addLink(1, 3, getRandomWeight(rnd));
		nn.addLink(1, 4, getRandomWeight(rnd));
		nn.addLink(2, 5, getRandomWeight(rnd));
		nn.addLink(3, 5, getRandomWeight(rnd));
		nn.addLink(4, 5, getRandomWeight(rnd));
		return nn;
	}
}
