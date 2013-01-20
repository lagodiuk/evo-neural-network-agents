package com.lagodiuk.agent.experimental.evolution2;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.AgentsEnvironmentListener;
import com.lagodiuk.agent.Fish;
import com.lagodiuk.agent.Food;
import com.lagodiuk.agent.Visualizator;
import com.lagodiuk.ga.GeneticAlgorithm;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

public class Test {

	private static Random rnd = new Random();

	public static void main(String[] args) throws Exception {
		OptimizableNeuralNetwork bestBrain = evolveBestBrain(300);
		System.out.println(bestBrain);

		final JFrame frame = new JFrame("Testing fishes visualizator");
		frame.setBounds(100, 100, 700, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Random random = new Random();
		AgentsEnvironment environment = new AgentsEnvironment(600, 400);
		environment.addListener(new TournamentListener());

		for (int i = 0; i < 15; i++) {
			NeuralNetworkDrivenFish fish =
					new NeuralNetworkDrivenFish(random.nextInt(600), random.nextInt(400), random.nextDouble() * 2 * Math.PI);
			fish.setBrain(bestBrain);
			environment.addAgent(fish);
		}

		for (int i = 0; i < 10; i++) {
			Food food = new Food(rnd.nextInt(environment.getWidth()), rnd.nextInt(environment.getHeight()));
			environment.addAgent(food);
		}

		final BufferedImage bufferedImage = new BufferedImage(environment.getWidth(), environment.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		Graphics2D canvas = (Graphics2D) bufferedImage.getGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (;;) {
			Thread.sleep(50);
			environment.timeStep();
			Visualizator.paintEnvironment(canvas, environment);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.getGraphics().drawImage(bufferedImage, 30, 90, null);
				}
			});
		}
	}

	private static OptimizableNeuralNetwork evolveBestBrain(int iterationsCount) {
		Population<OptimizableNeuralNetwork> brains = new Population<OptimizableNeuralNetwork>();
		int populationSize = 5;
		for (int i = 0; i < populationSize; i++) {
			brains.addChromosome(NeuralNetworkDrivenFish.randomNeuralNetworkBrain());
		}

		Fitness<OptimizableNeuralNetwork, Double> fit =
				new Fitness<OptimizableNeuralNetwork, Double>() {
					@Override
					public Double calculate(OptimizableNeuralNetwork chromosome) {
						int w = 200;
						int h = 200;
						AgentsEnvironment env = new AgentsEnvironment(w, h);
						for (int i = 0; i < 5; i++) {
							NeuralNetworkDrivenFish fish =
									new NeuralNetworkDrivenFish(rnd.nextInt(w), rnd.nextInt(h), 2 * Math.PI * rnd.nextDouble());
							fish.setBrain(chromosome);
							env.addAgent(fish);
						}
						for (int i = 0; i < 3; i++) {
							Food food = new Food(rnd.nextInt(w), rnd.nextInt(h));
							env.addAgent(food);
						}
						TournamentListener tournamentListener = new TournamentListener();
						env.addListener(tournamentListener);
						for (int i = 0; i < 70; i++) {
							env.timeStep();
						}

						double score = tournamentListener.getScore();

						return 1.0 / score;
					}
				};

		GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga =
				new GeneticAlgorithm<OptimizableNeuralNetwork, Double>(brains, fit);

		ga.addIterationListener(new IterartionListener<OptimizableNeuralNetwork, Double>() {
			@Override
			public void update(GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga) {
				OptimizableNeuralNetwork bestBrain = ga.getBest();
				Double fit = ga.fitness(bestBrain);
				System.out.println(ga.getIteration() + "\t" + fit);

				ga.clearCache();
			}
		});

		ga.setParentChromosomesSurviveCount(1);

		ga.iterate(iterationsCount);

		OptimizableNeuralNetwork bestBrain = ga.getBest();
		return bestBrain;
	}

	public static class TournamentListener implements AgentsEnvironmentListener {

		protected static final double minEatDistance = 5;

		protected static final double maxFishesDistance = 5;

		private double score = 0;

		@Override
		public void notify(AgentsEnvironment env) {
			List<Food> eatenFood = new LinkedList<Food>();

			F: for (Food food : this.getFood(env)) {
				for (Fish fish : this.getFishes(env)) {
					if (this.module(food.getX() - fish.getX(), food.getY() - fish.getY()) < minEatDistance) {
						this.score++;
						eatenFood.add(food);
						continue F;
					}
				}
			}

			List<Fish> fishes = this.getFishes(env);
			for (int i = 0; i < (fishes.size() - 1); i++) {
				Fish first = fishes.get(i);
				for (int j = i + 1; j < fishes.size(); j++) {
					Fish second = fishes.get(j);
					if (this.module(first.getX() - second.getX(), first.getY() - second.getY()) < maxFishesDistance) {
						this.score -= 0.5;
					}
				}
			}

			Random random = new Random();
			for (Food food : eatenFood) {
				env.removeAgent(food);

				Food newFood = new Food(random.nextInt(env.getWidth()), random.nextInt(env.getHeight()));
				env.addAgent(newFood);
			}
		}

		private List<Food> getFood(AgentsEnvironment env) {
			List<Food> food = new ArrayList<Food>();
			for (Agent agent : env.getAgents()) {
				if (agent instanceof Food) {
					food.add((Food) agent);
				}
			}
			return food;
		}

		private List<Fish> getFishes(AgentsEnvironment env) {
			List<Fish> fishes = new ArrayList<Fish>();
			for (Agent agent : env.getAgents()) {
				if (agent instanceof Fish) {
					fishes.add((Fish) agent);
				}
			}
			return fishes;
		}

		public double getScore() {
			if (this.score < 0) {
				return 0;
			}
			return this.score;
		}

		protected double module(double vx1, double vy1) {
			return Math.sqrt((vx1 * vx1) + (vy1 * vy1));
		}
	}

}
