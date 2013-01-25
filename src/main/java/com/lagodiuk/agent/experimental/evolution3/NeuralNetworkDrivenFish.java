package com.lagodiuk.agent.experimental.evolution3;

import java.util.LinkedList;
import java.util.List;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.Fish;
import com.lagodiuk.agent.Food;
import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.ThresholdFunction;
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

		double angle = this.brain.getAfterActivationSignal(this.brain.getNeuronsCount() - 2);
		double speed = this.brain.getAfterActivationSignal(this.brain.getNeuronsCount() - 1);

		speed = this.normalizeSpeed(this.getSpeed() + speed);
		angle = this.normalizeAngle(angle);

		this.setAngle(this.getAngle() + angle);
		this.setSpeed(speed);

		this.move();

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

		Food secondNearestFood = null;
		double secondNearestFoodDist = Double.MAX_VALUE;
		for (Agent obj : environment.getAgents()) {
			if (obj instanceof Food) {
				Food currFood = (Food) obj;
				if ((nearestFood != null) && (currFood == nearestFood)) {
					continue;
				}

				double currFoodDist = this.distanceTo(currFood);

				if ((secondNearestFood == null) || (currFoodDist <= secondNearestFoodDist)) {
					// fish can see only ahead
					if (this.inSight(currFood)) {
						secondNearestFood = currFood;
						secondNearestFoodDist = currFoodDist;
					}
				}
			}
		}

		// Find nearest fish
		Fish nearestFish = null;
		double nearestFishDist = maxFishesDistance;
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

		if (Math.random() < 0.5) {
			Food tmpFood = secondNearestFood;
			secondNearestFood = nearestFood;
			nearestFood = tmpFood;

			double tmpDist = secondNearestFoodDist;
			secondNearestFoodDist = nearestFishDist;
			nearestFishDist = tmpDist;
		}

		List<Double> nnInputs = new LinkedList<Double>();

		double rx = this.getRx();
		double ry = this.getRy();

		double x = this.getX();
		double y = this.getY();

		if (nearestFood != null) {
			double foodDirectionVectorX = nearestFood.getX() - x;
			double foodDirectionVectorY = nearestFood.getY() - y;

			// left/right
			double foodDirectionCosTeta =
					Math.signum(this.vectorCrossProduct(rx, ry, foodDirectionVectorX, foodDirectionVectorY))
							* this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(nearestFoodDist);
			nnInputs.add(foodDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(1000.0);
			nnInputs.add(0.0);
		}

		if (secondNearestFood != null) {
			double foodDirectionVectorX = secondNearestFood.getX() - x;
			double foodDirectionVectorY = secondNearestFood.getY() - y;

			// left/right
			double foodDirectionCosTeta =
					Math.signum(this.vectorCrossProduct(rx, ry, foodDirectionVectorX, foodDirectionVectorY))
							* this.cosTeta(rx, ry, foodDirectionVectorX, foodDirectionVectorY);

			nnInputs.add(FOOD);
			nnInputs.add(secondNearestFoodDist);
			nnInputs.add(foodDirectionCosTeta);

		} else {
			nnInputs.add(EMPTY);
			nnInputs.add(1000.0);
			nnInputs.add(0.0);
		}

		if (nearestFish != null) {
			double fishDirectionVectorX = nearestFish.getX() - x;
			double fishDirectionVectorY = nearestFish.getY() - y;

			// left/right
			double fishDirectionCosTeta =
					Math.signum(this.vectorCrossProduct(rx, ry, fishDirectionVectorX, fishDirectionVectorY))
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

	protected double vectorCrossProduct(double vx1, double vy1, double vx2, double vy2) {
		return (vx1 * vy2) - (vy1 * vx2);
	}

	private double normalizeSpeed(double speed) {
		if (Math.abs(speed) > maxSpeed) {
			speed = Math.signum(speed)
					* (Math.abs(speed) - (Math.floor(Math.abs(speed) / maxSpeed) * maxSpeed));
		}
		return speed;
	}

	private double normalizeAngle(double angle) {
		if (Math.abs(angle) > 1) {
			angle = Math.signum(angle)
					* (Math.abs(angle) - Math.floor(Math.abs(angle)));
		}
		return angle;
	}

	public static OptimizableNeuralNetwork randomNeuralNetworkBrain() {
		OptimizableNeuralNetwork nn = new OptimizableNeuralNetwork(18);
		for (int i = 0; i < 18; i++) {
			ThresholdFunction f = ThresholdFunction.getRandomFunction();
			nn.setNeuronFunction(i, f, f.getDefaultParams());
		}
		for (int i = 0; i < 9; i++) {
			nn.setNeuronFunction(i, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 9; j < 18; j++) {
				nn.addLink(i, j, Math.random());
			}
		}
		for (int i = 9; i < 18; i++) {
			for (int j = 9; j < 18; j++) {
				if (i < j) {
					nn.addLink(i, j, Math.random());
				}
			}
		}
		return nn;
	}
}
