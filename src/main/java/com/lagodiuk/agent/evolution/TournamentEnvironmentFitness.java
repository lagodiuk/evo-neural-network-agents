package com.lagodiuk.agent.evolution;

import java.util.Random;

import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Food;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

final class TournamentEnvironmentFitness implements Fitness<OptimizableNeuralNetwork, Double> {

	private static Random random = new Random();

	@Override
	public Double calculate(OptimizableNeuralNetwork chromosome) {
		// TODO maybe, its better to initialize these parameters in constructor
		int width = 200;
		int height = 200;
		int fishesCount = 10;
		int foodCount = 5;
		int environmentIterations = 50;

		AgentsEnvironment env = new AgentsEnvironment(width, height);

		for (int i = 0; i < fishesCount; i++) {
			NeuralNetworkDrivenFish fish =
					new NeuralNetworkDrivenFish(random.nextInt(width), random.nextInt(height), 2 * Math.PI * random.nextDouble());
			fish.setBrain(chromosome);
			env.addAgent(fish);
		}

		for (int i = 0; i < foodCount; i++) {
			Food food = new Food(random.nextInt(width), random.nextInt(height));
			env.addAgent(food);
		}

		EatenFoodObserver tournamentListener = new EatenFoodObserver();
		env.addListener(tournamentListener);

		for (int i = 0; i < environmentIterations; i++) {
			env.timeStep();
		}

		double score = tournamentListener.getScore();

		return 1.0 / score;
	}
}