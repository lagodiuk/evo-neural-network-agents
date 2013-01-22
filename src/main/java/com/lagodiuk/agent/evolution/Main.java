package com.lagodiuk.agent.evolution;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Food;
import com.lagodiuk.agent.Visualizator;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.GeneticAlgorithm;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;
import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

public class Main {

	private static Random random = new Random();

	private static GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga;

	private static AgentsEnvironment environment;

	private static int populationNumber = 0;

	public static void main(String[] args) throws Exception {
		ga = initializeGeneticAlgorithm();

		int environmentWidth = 600;
		int environmentHeight = 400;
		int fishesCount = 15;
		int foodCount = 10;

		environment = new AgentsEnvironment(environmentWidth, environmentHeight);
		environment.addListener(new EatenFoodObserver());

		NeuralNetwork brain = ga.getBest();
		addFishes(environment, brain, fishesCount);
		addFood(environment, foodCount);

		final BufferedImage bufferedImage = new BufferedImage(environmentWidth, environmentHeight, BufferedImage.TYPE_INT_RGB);

		Graphics2D canvas = (Graphics2D) bufferedImage.getGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		JFrame frame = new JFrame("Testing fishes visualizator");
		frame.setSize(environmentWidth + 80, environmentHeight + 50);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLayout(new BorderLayout());

		final JPanel environmentPanel = new JPanel();
		environmentPanel.setSize(environmentWidth, environmentHeight);
		frame.add(environmentPanel, BorderLayout.CENTER);

		JPanel controlsPanel = new JPanel();
		frame.add(controlsPanel, BorderLayout.EAST);
		controlsPanel.setLayout(new GridLayout(11, 1, 5, 5));

		final JTextField evolveTextField = new JTextField("10");
		controlsPanel.add(evolveTextField);

		final JButton evolveButton = new JButton("evolve");
		controlsPanel.add(evolveButton);

		final JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);
		frame.add(progressBar, BorderLayout.SOUTH);

		final JLabel populationNumberLabel = new JLabel("Population: " + populationNumber, SwingConstants.CENTER);
		frame.add(populationNumberLabel, BorderLayout.NORTH);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		evolveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				evolveButton.setEnabled(false);
				evolveTextField.setEnabled(false);
				progressBar.setVisible(true);
				progressBar.setValue(0);

				String iterCountStr = evolveTextField.getText();
				if (!iterCountStr.matches("\\d+")) {
					evolveButton.setEnabled(true);
					evolveTextField.setEnabled(true);
					progressBar.setVisible(false);
					return;
				}

				final int iterCount = Integer.parseInt(iterCountStr);

				new Thread(new Runnable() {
					@Override
					public void run() {
						IterartionListener<OptimizableNeuralNetwork, Double> listener =
								new IterartionListener<OptimizableNeuralNetwork, Double>() {
									@Override
									public void update(GeneticAlgorithm<OptimizableNeuralNetwork, Double> environment) {
										final int iteration = environment.getIteration();
										SwingUtilities.invokeLater(new Runnable() {
											@Override
											public void run() {
												progressBar.setValue((iteration * 100) / iterCount);
											}
										});
									}
								};

						ga.addIterationListener(listener);
						ga.evolve(iterCount);
						ga.removeIterationListener(listener);
						populationNumber += iterCount;

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisible(false);
								evolveButton.setEnabled(true);
								evolveTextField.setEnabled(true);
								populationNumberLabel.setText("Population: " + populationNumber);
							}
						});

						NeuralNetwork brain = ga.getBest();
						for (Agent agent : environment.getAgents()) {
							if (agent instanceof NeuralNetworkDrivenFish) {
								((NeuralNetworkDrivenFish) agent).setBrain(brain);
							}
						}
					}
				}).start();
			}
		});

		for (;;) {
			Thread.sleep(50);
			environment.timeStep();
			Visualizator.paintEnvironment(canvas, environment);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					environmentPanel.getGraphics().drawImage(bufferedImage, 0, 0, null);
				}
			});
		}
	}

	private static void addFishes(AgentsEnvironment environment, NeuralNetwork brain, int fishesCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();
		for (int i = 0; i < fishesCount; i++) {
			NeuralNetworkDrivenFish fish =
					new NeuralNetworkDrivenFish(random.nextInt(environmentWidth), random.nextInt(environmentHeight), random.nextDouble() * 2 * Math.PI);
			fish.setBrain(brain);
			environment.addAgent(fish);
		}
	}

	private static void addFood(AgentsEnvironment environment, int foodCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();
		for (int i = 0; i < foodCount; i++) {
			Food food = new Food(random.nextInt(environmentWidth), random.nextInt(environmentHeight));
			environment.addAgent(food);
		}
	}

	private static GeneticAlgorithm<OptimizableNeuralNetwork, Double> initializeGeneticAlgorithm() {
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
		return ga;
	}
}
