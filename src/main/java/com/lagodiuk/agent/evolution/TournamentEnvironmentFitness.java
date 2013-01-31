package com.lagodiuk.agent.evolution;

import java.util.Random;

import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Food;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

public class TournamentEnvironmentFitness implements Fitness<OptimizableNeuralNetwork, Double> {

	private static Random random = new Random();

	@Override
	public Double calculate(OptimizableNeuralNetwork chromosome) {
		// TODO maybe, its better to initialize these parameters in constructor
		final int width = 200;
		final int height = 200;
		int agentsCount = 10;
		int foodCount = 5;
		int environmentIterations = 50;

		AgentsEnvironment env = new AgentsEnvironment(width, height);

		for (int i = 0; i < agentsCount; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			double direction = 2 * Math.PI * random.nextDouble();

			NeuralNetworkDrivenAgent agent = new NeuralNetworkDrivenAgent(x, y, direction);
			agent.setBrain(chromosome);

			env.addAgent(agent);
		}

		for (int i = 0; i < foodCount; i++) {
			Food food = this.newPieceOfFood(width, height);
			env.addAgent(food);
		}

		EatenFoodObserver tournamentListener = new EatenFoodObserver() {
			@Override
			protected void addRandomPieceOfFood(AgentsEnvironment env) {
				Food newFood = TournamentEnvironmentFitness.this.newPieceOfFood(width, height);
				env.addAgent(newFood);
			}
		};
		env.addListener(tournamentListener);

		for (int i = 0; i < environmentIterations; i++) {
			env.timeStep();
		}

		double score = tournamentListener.getScore();
		return 1.0 / score;
	}

	protected Food newPieceOfFood(int width, int height) {
		Food food = new Food(random.nextInt(width), random.nextInt(height));
		return food;
	}
}