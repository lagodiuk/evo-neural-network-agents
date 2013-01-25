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
		int w = 200;
		int h = 200;
		AgentsEnvironment env = new AgentsEnvironment(w, h);
		for (int i = 0; i < 10; i++) {
			NeuralNetworkDrivenFish fish =
					new NeuralNetworkDrivenFish(random.nextInt(w), random.nextInt(h), 2 * Math.PI * random.nextDouble());
			fish.setBrain(chromosome);
			env.addAgent(fish);
		}
		for (int i = 0; i < 5; i++) {
			Food food = new Food(random.nextInt(w), random.nextInt(h));
			env.addAgent(food);
		}
		EatenFoodObserver tournamentListener = new EatenFoodObserver();
		env.addListener(tournamentListener);
		for (int i = 0; i < 50; i++) {
			env.timeStep();
		}

		double score = tournamentListener.getScore();

		return 1.0 / score;
	}
}