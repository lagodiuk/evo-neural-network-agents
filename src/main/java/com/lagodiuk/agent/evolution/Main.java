package com.lagodiuk.agent.evolution;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
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

	private static volatile boolean play = true;

	private static String brainXmlPath = "brain.xml";

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

		final JButton playPauseButton = new JButton("pause");
		controlsPanel.add(playPauseButton);

		final JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);
		frame.add(progressBar, BorderLayout.SOUTH);

		final JLabel populationNumberLabel = new JLabel("Population: " + populationNumber, SwingConstants.CENTER);
		frame.add(populationNumberLabel, BorderLayout.NORTH);

		evolveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				evolveButton.setEnabled(false);
				evolveTextField.setEnabled(false);
				progressBar.setVisible(true);
				progressBar.setValue(0);
				environmentPanel.getGraphics().drawImage(bufferedImage, 0, 0, null);

				String iterCountStr = evolveTextField.getText();
				if (!iterCountStr.matches("\\d+")) {
					evolveButton.setEnabled(true);
					evolveTextField.setEnabled(true);
					progressBar.setVisible(false);
					environmentPanel.getGraphics().drawImage(bufferedImage, 0, 0, null);
					return;
				}

				final int iterCount = Integer.parseInt(iterCountStr);

				new Thread(new Runnable() {
					@Override
					public void run() {
						IterartionListener<OptimizableNeuralNetwork, Double> progressListener =
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

						ga.addIterationListener(progressListener);
						ga.evolve(iterCount);
						ga.removeIterationListener(progressListener);
						populationNumber += iterCount;

						NeuralNetwork brain = ga.getBest();
						for (Agent agent : environment.getAgents()) {
							if (agent instanceof NeuralNetworkDrivenFish) {
								((NeuralNetworkDrivenFish) agent).setBrain(brain);
							}
						}

						try {
							FileOutputStream out = new FileOutputStream(brainXmlPath);
							NeuralNetwork.marsall(brain, out);
							out.close();
						} catch (Exception e) {
							e.printStackTrace();
						}

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisible(false);
								evolveButton.setEnabled(true);
								evolveTextField.setEnabled(true);
								populationNumberLabel.setText("Population: " + populationNumber);
							}
						});
					}
				}).start();
			}
		});

		playPauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				play = !play;
				if (play) {
					playPauseButton.setText("pause");
				} else {
					playPauseButton.setText("play");
				}
			}
		});

		environmentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent click) {
				double x = click.getX();
				double y = click.getY();

				Food food = new Food(x, y);
				environment.addAgent(food);
			}
		});

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		for (;;) {
			Thread.sleep(50);
			if (play) {
				environment.timeStep();
			}
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

		Fitness<OptimizableNeuralNetwork, Double> fit = new TournamentEnvironmentFitness();

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
