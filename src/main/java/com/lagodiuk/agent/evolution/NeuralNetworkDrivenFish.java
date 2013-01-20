package com.lagodiuk.agent.evolution;

import java.util.LinkedList;
import java.util.List;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Fish;
import com.lagodiuk.agent.Food;
import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.ThresholdFunction;
import com.lagodiuk.nn.ThresholdFunctions;
import com.lagodiuk.nn.genetic.OptimizableNeuralNetwork;

public class NeuralNetworkDrivenFish extends Fish {

	private static final double maxSpeed = 4;

	protected static final double maxFishesDistance = 5;

	private static final double FISH = -10;

	private static final double EMPTY = 0;

	private static final double FOOD = 10;

	private NeuralNetwork brain;

	public NeuralNetworkDrivenFish(double x, double y, double angle) {
		super(x, y, angle);
	}

	public void setBrain(NeuralNetwork brain) {
		this.brain = brain;
	}

	@Override
	public void interact(AgentsEnvironment env) {
		List<Double> nnInputs = this.createNnInputs(env);

		this.activateNeuralNetwork(nnInputs);

		int neuronsCount = this.brain.getNeuronsCount();
		double deltaAngle = this.brain.getAfterActivationSignal(neuronsCount - 2);
		double deltaSpeed = this.brain.getAfterActivationSignal(neuronsCount - 1);

		double newSpeed = this.normalizeSpeed(this.getSpeed() + deltaSpeed);
		double newAngle = this.getAngle() + this.normalizeDeltaAngle(deltaAngle);

		this.setAngle(newAngle);
		this.setSpeed(newSpeed);

		this.move();

		// avoid moving outside of environment
		double newX = this.getX();
		double newY = this.getY();
		if (newX < 0) {
			newX = env.getWidth() - 1;
		}
		if (newY < 0) {
			newY = env.getHeight() - 1;
		}
		if (newX > env.getWidth()) {
			newX = 1;
		}
		if (newY > env.getHeight()) {
			newY = 1;
		}

		this.setX(newX);
		this.setY(newY);
	}

	private void activateNeuralNetwork(List<Double> nnInputs) {
		for (int i = 0; i < nnInputs.size(); i++) {
			this.brain.putSignalToNeuron(i, nnInputs.get(i));
		}
		this.brain.activate();
	}

	protected List<Double> createNnInputs(AgentsEnvironment environment) {
		// Find nearest food
		Food nearestFood = null;
		double nearestFoodDist = Double.MAX_VALUE;
		// TODO use Guava
		for (Agent obj : environment.getAgents()) {
			if (obj instanceof Food) {
				Food currFood = (Food) obj;

				double currFoodDist = this.distanceTo(currFood);

				if ((nearestFood == null) || (currFoodDist <= nearestFoodDist)) {
					// fish can see only ahead
					if (this.inSight(currFood)) {
						nearestFood = currFood;
						nearestFoodDist = currFoodDist;
					}
				}
			}
		}

		// Find nearest fish
		Fish nearestFish = null;
		double nearestFishDist = maxFishesDistance;
		// TODO use Guava
		for (Agent obj : environment.getAgents()) {
			if (obj instanceof Fish) {
				Fish currFish = (Fish) obj;

				if (this == currFish) {
					continue;
				}

				double currFishDist = this.distanceTo(currFish);

				if (currFishDist <= nearestFishDist) {
					// fish can see only ahead
					if (this.inSight(currFish)) {
						nearestFish = currFish;
						nearestFishDist = currFishDist;
					}
				}
			}
		}

		List<Double> nnInputs = new LinkedList<Double>();

		double rx = this.getRx();
		double ry = this.getRy();

		double x = this.getX();
		double y = this.getY();

		if (nearestFood != null) {
			double foodDirectionVectorX = nearestFood.getX() - x;
			double foodDirectionVectorY = nearestFood.getY() - y;

			// left/right cos
			double foodDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, foodDirectionVectorX, foodDirectionVectorY))
							* this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(nearestFoodDist);
			nnInputs.add(foodDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(0.0);
			nnInputs.add(0.0);
		}

		if (nearestFish != null) {
			double fishDirectionVectorX = nearestFish.getX() - x;
			double fishDirectionVectorY = nearestFish.getY() - y;

			// left/right cos
			double fishDirectionCosTeta =
					Math.signum(this.pseudoScalarProduct(rx, ry, fishDirectionVectorX, fishDirectionVectorY))
							* this.cosTeta(rx, ry, fishDirectionVectorX, fishDirectionVectorY);

			nnInputs.add(FISH);
			nnInputs.add(nearestFishDist);
			nnInputs.add(fishDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(0.0);
			nnInputs.add(0.0);
		}
		return nnInputs;
	}

	protected boolean inSight(Agent agent) {
		double crossProduct = this.cosTeta(this.getRx(), this.getRy(), agent.getX() - this.getX(), agent.getY() - this.getY());
		return (crossProduct > 0);
	}

	protected double distanceTo(Agent agent) {
		return this.module(agent.getX() - this.getX(), agent.getY() - this.getY());
	}

	protected double cosTeta(double vx1, double vy1, double vx2, double vy2) {
		double v1 = this.module(vx1, vy1);
		double v2 = this.module(vx2, vy2);
		if (v1 == 0) {
			v1 = 1E-5;
		}
		if (v2 == 0) {
			v2 = 1E-5;
		}
		double ret = ((vx1 * vx2) + (vy1 * vy2)) / (v1 * v2);
		return ret;
	}

	protected double module(double vx1, double vy1) {
		return Math.sqrt((vx1 * vx1) + (vy1 * vy1));
	}

	protected double pseudoScalarProduct(double vx1, double vy1, double vx2, double vy2) {
		return (vx1 * vy2) - (vy1 * vx2);
	}

	private double normalizeSpeed(double speed) {
		if (Math.abs(speed) > maxSpeed) {
			speed = Math.signum(speed)
					* (Math.abs(speed) - (Math.floor(Math.abs(speed) / maxSpeed) * maxSpeed));
		}
		return speed;
	}

	private double normalizeDeltaAngle(double angle) {
		if (Math.abs(angle) > 1) {
			angle = Math.signum(angle)
					* (Math.abs(angle) - Math.floor(Math.abs(angle)));
		}
		return angle;
	}

	public static OptimizableNeuralNetwork randomNeuralNetworkBrain() {
		OptimizableNeuralNetwork nn = new OptimizableNeuralNetwork(15);
		for (int i = 0; i < 15; i++) {
			ThresholdFunction f = ThresholdFunctions.getRandomFunction();
			nn.setNeuronFunction(i, f, f.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			nn.setNeuronFunction(i, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams());
		}
		for (int i = 0; i < 6; i++) {
			for (int j = 6; j < 15; j++) {
				nn.addLink(i, j, Math.random());
			}
		}
		for (int i = 6; i < 15; i++) {
			for (int j = 6; j < 15; j++) {
				if (i < j) {
					nn.addLink(i, j, Math.random());
				}
			}
		}
		return nn;
	}
}
