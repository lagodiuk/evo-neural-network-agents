package com.lagodiuk.agent.visualization;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Fish;
import com.lagodiuk.agent.Food;
import com.lagodiuk.agent.evolution.NeuralNetworkDrivenFish;

public class Visualizator {

	private static int fishRadius = 5;

	public static void paintEnvironment(Graphics2D canvas, AgentsEnvironment environment) {
		canvas.clearRect(0, 0, environment.getWidth(), environment.getHeight());

		canvas.setColor(new Color(200, 30, 70));
		for (Agent agent : environment.getAgents()) {
			if (agent instanceof Food) {
				Food food = (Food) agent;
				int x = (int) food.getX();
				int y = (int) food.getY();
				canvas.fillOval(x - fishRadius, y - fishRadius, fishRadius * 2, fishRadius * 2);
			}
		}

		canvas.setColor(Color.GREEN);
		for (Agent agent : environment.getAgents()) {
			if (agent instanceof Fish) {
				Fish fish = (Fish) agent;

				int x = (int) fish.getX();
				int y = (int) fish.getY();

				canvas.fillOval(x - fishRadius, y - fishRadius, fishRadius * 2, fishRadius * 2);
			}
		}

		canvas.setColor(Color.WHITE);
		for (Agent agent : environment.getAgents()) {
			if (agent instanceof Fish) {
				Fish fish = (Fish) agent;

				int x = (int) fish.getX();
				int y = (int) fish.getY();
				int rx = (int) ((fish.getRx() * (fishRadius + 4)) + x);
				int ry = (int) ((fish.getRy() * (fishRadius + 4)) + y);

				canvas.drawOval(x - fishRadius, y - fishRadius, fishRadius * 2, fishRadius * 2);
				canvas.drawLine(x, y, rx, ry);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		final JFrame frame = new JFrame("Testing fishes visualizator");
		frame.setBounds(100, 100, 700, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Random random = new Random();
		AgentsEnvironment environment = new AgentsEnvironment(600, 400);
		for (int i = 0; i < 30; i++) {
			NeuralNetworkDrivenFish fish =
					new NeuralNetworkDrivenFish(random.nextInt(600), random.nextInt(400), random.nextDouble() * 2 * Math.PI);
			fish.setBrain(NeuralNetworkDrivenFish.randomNeuralNetworkBrain());
			environment.addAgent(fish);
		}

		final BufferedImage bufferedImage = new BufferedImage(environment.getWidth(), environment.getHeight(),
				BufferedImage.TYPE_INT_RGB);

		Graphics2D canvas = (Graphics2D) bufferedImage.getGraphics();
		canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		for (;;) {
			Thread.sleep(100);
			environment.timeStep();
			paintEnvironment(canvas, environment);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.getGraphics().drawImage(bufferedImage, 30, 90, null);
				}
			});
		}
	}
}