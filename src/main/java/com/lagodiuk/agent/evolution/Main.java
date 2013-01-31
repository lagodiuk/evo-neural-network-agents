package com.lagodiuk.agent.evolution;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.lagodiuk.agent.AbstractAgent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Food;
import com.lagodiuk.agent.MovingFood;
import com.lagodiuk.ga.Fitness;
import com.lagodiuk.ga.GeneticAlgorithm;
import com.lagodiuk.ga.IterartionListener;
import com.lagodiuk.ga.Population;
import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

public class Main {

	private static final String PREFS_KEY_BRAINS_DIRECTORY = "BrainsDirectory";

	private static Random random = new Random();

	private static GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga;

	private static AgentsEnvironment environment;

	private static int populationNumber = 0;

	private static volatile boolean play = true;

	private static volatile boolean staticFood = true;

	// UI

	private static JFrame appFrame;

	private static JPanel environmentPanel;

	private static JPanel controlsPanel;

	private static JTextField evolveTextField;

	private static JButton evolveButton;

	private static JButton playPauseButton;

	private static JButton loadBrainButton;

	private static JButton saveBrainButton;

	private static JRadioButton staticFoodRadioButton;

	private static JRadioButton dynamicFoodRadioButton;

	private static ButtonGroup foodTypeButtonGroup;

	private static JProgressBar progressBar;

	private static JLabel populationInfoLabel;

	private static BufferedImage displayEnvironmentBufferedImage;

	private static Graphics2D displayEnvironmentCanvas;

	private static JFileChooser fileChooser;

	private static Preferences prefs;

	public static void main(String[] args) throws Exception {
		// TODO maybe, add ability to define these parameters as environment
		// constants
		int gaPopulationSize = 5;
		int parentalChromosomesSurviveCount = 1;
		int environmentWidth = 600;
		int environmentHeight = 400;
		int agentsCount = 15;
		int foodCount = 10;

		initializeGeneticAlgorithm(gaPopulationSize, parentalChromosomesSurviveCount, null);

		initializeEnvironment(environmentWidth, environmentHeight, agentsCount, foodCount);

		initializeCanvas(environmentWidth, environmentHeight);

		initializeUI(environmentWidth, environmentHeight);

		initializeEvolveButtonFunctionality();

		initializePlayPauseButtonFunctionality();

		initializeAddingFoodFunctionality();

		initializeLoadBrainButtonFunctionality();

		initializeSaveBrainButtonFunctionality();

		initializeChangingFoodTypeFunctionality();

		displayUI();

		mainEnvironmentLoop();
	}

	private static void initializeCanvas(int environmentWidth, int environmentHeight) {
		displayEnvironmentBufferedImage = new BufferedImage(environmentWidth, environmentHeight, BufferedImage.TYPE_INT_RGB);

		displayEnvironmentCanvas = (Graphics2D) displayEnvironmentBufferedImage.getGraphics();
		displayEnvironmentCanvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private static void initializeEnvironment(int environmentWidth, int environmentHeight, int agentsCount, int foodCount) {
		environment = new AgentsEnvironment(environmentWidth, environmentHeight);
		environment.addListener(new EatenFoodObserver() {
			@Override
			protected void addRandomPieceOfFood(AgentsEnvironment env) {
				Food food = createFood(env.getWidth(), env.getHeight());
				env.addAgent(food);
			}
		});

		NeuralNetwork brain = ga.getBest();
		initializeAgents(environment, brain, agentsCount);
		initializeFood(environment, foodCount);
	}

	private static Food createFood(int width, int height) {
		int x = random.nextInt(width);
		int y = random.nextInt(height);

		Food food = null;
		if (staticFood) {
			food = new Food(x, y);
		} else {
			double speed = random.nextDouble() * 2;
			double direction = random.nextDouble() * 2 * Math.PI;

			food = new MovingFood(x, y, direction, speed);
		}

		return food;
	}

	private static void displayUI() {
		// put application frame to the center of screen
		appFrame.setLocationRelativeTo(null);

		appFrame.setVisible(true);
	}

	private static void initializeUI(int environmentWidth, int environmentHeight) {
		appFrame = new JFrame("Evolving neural network driven agnets");
		appFrame.setSize(environmentWidth + 130, environmentHeight + 50);
		appFrame.setResizable(false);
		appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		appFrame.setLayout(new BorderLayout());

		environmentPanel = new JPanel();
		environmentPanel.setSize(environmentWidth, environmentHeight);
		appFrame.add(environmentPanel, BorderLayout.CENTER);

		controlsPanel = new JPanel();
		appFrame.add(controlsPanel, BorderLayout.EAST);
		controlsPanel.setLayout(new GridLayout(11, 1, 5, 5));

		evolveTextField = new JTextField("10");
		controlsPanel.add(evolveTextField);

		evolveButton = new JButton("evolve");
		controlsPanel.add(evolveButton);

		saveBrainButton = new JButton("save brain");
		controlsPanel.add(saveBrainButton);

		loadBrainButton = new JButton("load brain");
		controlsPanel.add(loadBrainButton);

		staticFoodRadioButton = new JRadioButton("static food");
		dynamicFoodRadioButton = new JRadioButton("dynamic food");
		foodTypeButtonGroup = new ButtonGroup();
		foodTypeButtonGroup.add(staticFoodRadioButton);
		foodTypeButtonGroup.add(dynamicFoodRadioButton);
		controlsPanel.add(staticFoodRadioButton);
		controlsPanel.add(dynamicFoodRadioButton);
		if (staticFood) {
			staticFoodRadioButton.setSelected(true);
		} else {
			dynamicFoodRadioButton.setSelected(true);
		}

		playPauseButton = new JButton("pause");
		controlsPanel.add(playPauseButton);

		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setVisible(false);
		appFrame.add(progressBar, BorderLayout.SOUTH);

		populationInfoLabel = new JLabel("Population: " + populationNumber, SwingConstants.CENTER);
		appFrame.add(populationInfoLabel, BorderLayout.NORTH);

		prefs = Preferences.userNodeForPackage(Main.class);
		String brainsDirPath = prefs.get(PREFS_KEY_BRAINS_DIRECTORY, "");
		fileChooser = new JFileChooser(new File(brainsDirPath));
	}

	protected static void initializeChangingFoodTypeFunctionality() {
		ItemListener changingFoodTypeListener = new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					disableControls();
					boolean wasPlaying = play;
					play = false;

					staticFood = !staticFood;

					List<Food> food = new LinkedList<Food>();
					for (AbstractAgent a : environment.getAgents()) {
						if (a instanceof Food) {
							food.add((Food) a);
						}
					}
					for (Food f : food) {
						environment.removeAgent(f);

						Food newFood = createFood(1, 1);
						newFood.setX(f.getX());
						newFood.setY(f.getY());

						environment.addAgent(newFood);
					}

					play = wasPlaying;
					enableControls();
				}
			}
		};
		staticFoodRadioButton.addItemListener(changingFoodTypeListener);
		dynamicFoodRadioButton.addItemListener(changingFoodTypeListener);
	}

	private static void mainEnvironmentLoop() throws InterruptedException {
		for (;;) {
			Thread.sleep(50);
			if (play) {
				environment.timeStep();
			}
			Visualizator.paintEnvironment(displayEnvironmentCanvas, environment);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);
				}
			});
		}
	}

	private static void initializeLoadBrainButtonFunctionality() {
		loadBrainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();

				int returnVal = fileChooser.showOpenDialog(appFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						File brainFile = fileChooser.getSelectedFile();
						prefs.put(PREFS_KEY_BRAINS_DIRECTORY, brainFile.getParent());

						FileInputStream in = new FileInputStream(brainFile);

						NeuralNetwork newBrain = NeuralNetwork.unmarsall(in);
						in.close();

						for (AbstractAgent agent : environment.getAgents()) {
							if (agent instanceof NeuralNetworkDrivenAgent) {
								((NeuralNetworkDrivenAgent) agent).setBrain(newBrain);
							}
						}

						OptimizableNeuralNetwork optimizableNewBrain = new OptimizableNeuralNetwork(newBrain);
						int populationSize = ga.getPopulation().getSize();
						int parentalChromosomesSurviveCount = ga.getParentChromosomesSurviveCount();
						initializeGeneticAlgorithm(populationSize, parentalChromosomesSurviveCount, optimizableNewBrain);

						// reset population number counter
						populationNumber = 0;
						populationInfoLabel.setText("Population: " + populationNumber);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				enableControls();
			}
		});
	}

	private static void initializeSaveBrainButtonFunctionality() {
		saveBrainButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();

				int returnVal = fileChooser.showSaveDialog(appFrame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						File brainFile = fileChooser.getSelectedFile();
						prefs.put(PREFS_KEY_BRAINS_DIRECTORY, brainFile.getParent());

						FileOutputStream out = new FileOutputStream(brainFile);

						// current brain is the best evolved neural network
						// from genetic algorithm
						NeuralNetwork brain = ga.getBest();
						NeuralNetwork.marsall(brain, out);

						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				enableControls();
			}
		});
	}

	private static void initializeEvolveButtonFunctionality() {
		evolveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableControls();
				progressBar.setVisible(true);
				progressBar.setValue(0);
				environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);

				String iterCountStr = evolveTextField.getText();
				if (!iterCountStr.matches("\\d+")) {
					evolveButton.setEnabled(true);
					evolveTextField.setEnabled(true);
					progressBar.setVisible(false);
					environmentPanel.getGraphics().drawImage(displayEnvironmentBufferedImage, 0, 0, null);
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
						for (AbstractAgent agent : environment.getAgents()) {
							if (agent instanceof NeuralNetworkDrivenAgent) {
								((NeuralNetworkDrivenAgent) agent).setBrain(brain);
							}
						}

						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								progressBar.setVisible(false);
								populationInfoLabel.setText("Population: " + populationNumber);
								enableControls();
								evolveButton.requestFocusInWindow();
							}
						});
					}
				}).start();
			}
		});
	}

	private static void disableControls() {
		evolveButton.setEnabled(false);
		evolveTextField.setEnabled(false);
		loadBrainButton.setEnabled(false);
		saveBrainButton.setEnabled(false);
		staticFoodRadioButton.setEnabled(false);
		dynamicFoodRadioButton.setEnabled(false);
	}

	private static void enableControls() {
		evolveButton.setEnabled(true);
		evolveTextField.setEnabled(true);
		loadBrainButton.setEnabled(true);
		saveBrainButton.setEnabled(true);
		staticFoodRadioButton.setEnabled(true);
		dynamicFoodRadioButton.setEnabled(true);
	}

	private static void initializeAddingFoodFunctionality() {
		environmentPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent click) {
				double x = click.getX();
				double y = click.getY();

				Food food = createFood(1, 1);
				food.setX(x);
				food.setY(y);

				environment.addAgent(food);
			}
		});
	}

	private static void initializePlayPauseButtonFunctionality() {
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
	}

	private static void initializeAgents(AgentsEnvironment environment, NeuralNetwork brain, int agentsCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();

		for (int i = 0; i < agentsCount; i++) {
			int x = random.nextInt(environmentWidth);
			int y = random.nextInt(environmentHeight);
			double direction = random.nextDouble() * 2 * Math.PI;

			NeuralNetworkDrivenAgent agent = new NeuralNetworkDrivenAgent(x, y, direction);
			agent.setBrain(brain);

			environment.addAgent(agent);
		}
	}

	private static void initializeFood(AgentsEnvironment environment, int foodCount) {
		int environmentWidth = environment.getWidth();
		int environmentHeight = environment.getHeight();

		for (int i = 0; i < foodCount; i++) {
			Food food = createFood(environmentWidth, environmentHeight);
			environment.addAgent(food);
		}
	}

	private static void initializeGeneticAlgorithm(
			int populationSize,
			int parentalChromosomesSurviveCount,
			OptimizableNeuralNetwork baseNeuralNetwork) {
		Population<OptimizableNeuralNetwork> brains = new Population<OptimizableNeuralNetwork>();

		for (int i = 0; i < (populationSize - 1); i++) {
			if ((baseNeuralNetwork == null) || (random.nextDouble() < 0.5)) {
				brains.addChromosome(NeuralNetworkDrivenAgent.randomNeuralNetworkBrain());
			} else {
				brains.addChromosome(baseNeuralNetwork.mutate());
			}
		}
		if (baseNeuralNetwork != null) {
			brains.addChromosome(baseNeuralNetwork);
		} else {
			brains.addChromosome(NeuralNetworkDrivenAgent.randomNeuralNetworkBrain());
		}

		Fitness<OptimizableNeuralNetwork, Double> fit = new TournamentEnvironmentFitness();

		ga = new GeneticAlgorithm<OptimizableNeuralNetwork, Double>(brains, fit);

		addSystemOutIterationListener(ga);

		ga.setParentChromosomesSurviveCount(parentalChromosomesSurviveCount);
	}

	private static void addSystemOutIterationListener(GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga) {
		ga.addIterationListener(new IterartionListener<OptimizableNeuralNetwork, Double>() {
			@Override
			public void update(GeneticAlgorithm<OptimizableNeuralNetwork, Double> ga) {
				OptimizableNeuralNetwork bestBrain = ga.getBest();
				Double fit = ga.fitness(bestBrain);
				System.out.println(ga.getIteration() + "\t" + fit);

				ga.clearCache();
			}
		});
	}
}
