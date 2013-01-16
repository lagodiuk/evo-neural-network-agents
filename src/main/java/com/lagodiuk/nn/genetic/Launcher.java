package com.lagodiuk.nn.genetic;

import java.util.Random;

import com.lagodiuk.ga.Environment;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;
import com.lagodiuk.nn.ThresholdFunction;
import com.lagodiuk.nn.ThresholdFunctions;

public class Launcher {

	private static final int maxWeightNum = 10;

	public static void main(String[] args) {
		Population<OptimizableNeuralNetwork> population = new Population<OptimizableNeuralNetwork>();
		OptimizableNeuralNetwork nn = initilNeuralNetwork();
		for (int i = 0; i < 20; i++) {
			population.addChromosome(nn.mutate());
		}

		Fitness<OptimizableNeuralNetwork, Double> fit = new Fitness<OptimizableNeuralNetwork, Double>() {
			@Override
			public Double calculate(OptimizableNeuralNetwork nn) {
				double delt = 0;
				for (int i = -5; i < 6; i++) {
					for (int j = -5; j < 6; j++) {
						double target;
						if (i == j) {
							target = 0;
						} else {
							target = 1;
						}

						nn.putSignalToNeuron(0, i);
						nn.putSignalToNeuron(1, j);

						nn.activate();

						double nnOutput = nn.getAfterActivationSignal(5);

						double d = nnOutput - target;
						delt += d * d;
					}
				}
				return delt;
			}
		};

		Environment<OptimizableNeuralNetwork, Double> env = new Environment<OptimizableNeuralNetwork, Double>(population, fit);

		env.addIterationListener(new IterartionListener<OptimizableNeuralNetwork, Double>() {
			private Random random = new Random();

			@Override
			public void update(Environment<OptimizableNeuralNetwork, Double> environment) {
				OptimizableNeuralNetwork gene = environment.getBest();
				Double d = environment.fitness(gene);
				System.out.println(environment.getIteration() + "\t" + d);

				if (d <= 0.1) {
					environment.terminate();
				}

				environment.setParentChromosomesSurviveCount(this.random.nextInt(environment.getPopulation().getSize()));
			}
		});

		env.iterate(5500);

		OptimizableNeuralNetwork evoNn = env.getBest();
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

	private static OptimizableNeuralNetwork initilNeuralNetwork() {
		OptimizableNeuralNetwork nn = new OptimizableNeuralNetwork(6);
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

	private static int getRandomWeight(Random random) {
		return random.nextInt(maxWeightNum) - random.nextInt(maxWeightNum);
	}

}
