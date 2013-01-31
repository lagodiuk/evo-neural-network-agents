package com.lagodiuk.agent.evolution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.lagodiuk.agent.AbstractAgent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.AgentsEnvironmentObserver;
import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.Food;

/**
 * Calculating eaten pieces of food
 */
public class EatenFoodObserver implements AgentsEnvironmentObserver {

	protected static final double minEatDistance = 5;

	protected static final double maxFishesDistance = 5;

	private Random random = new Random();

	private double score = 0;

	@Override
	public void notify(AgentsEnvironment env) {
		List<Food> eatenFood = this.getEatenFood(env);
		this.score += eatenFood.size();

		List<Agent> collidedFishes = this.getCollidedFishes(env);
		this.score -= collidedFishes.size() * 0.5;

		this.removeEatenAndCreateNewFood(env, eatenFood);
	}

	private List<Agent> getCollidedFishes(AgentsEnvironment env) {
		List<Agent> collidedFishes = new LinkedList<Agent>();

		List<Agent> allFishes = this.getFishes(env);
		int fishesCount = allFishes.size();

		for (int i = 0; i < (fishesCount - 1); i++) {
			Agent firstFish = allFishes.get(i);
			for (int j = i + 1; j < fishesCount; j++) {
				Agent secondFish = allFishes.get(j);
				double distanceToSecondFish = this.module(firstFish.getX() - secondFish.getX(), firstFish.getY() - secondFish.getY());
				if (distanceToSecondFish < maxFishesDistance) {
					collidedFishes.add(secondFish);
					// this.score -= 0.5;
				}
			}
		}
		return collidedFishes;
	}

	private List<Food> getEatenFood(AgentsEnvironment env) {
		List<Food> eatenFood = new LinkedList<Food>();

		F: for (Food food : this.getFood(env)) {
			for (Agent fish : this.getFishes(env)) {
				double distanceToFood = this.module(food.getX() - fish.getX(), food.getY() - fish.getY());
				if (distanceToFood < minEatDistance) {
					// this.score++;
					eatenFood.add(food);
					continue F;
				}
			}
		}
		return eatenFood;
	}

	protected void removeEatenAndCreateNewFood(AgentsEnvironment env, List<Food> eatenFood) {
		for (Food food : eatenFood) {
			env.removeAgent(food);

			this.addRandomPieceOfFood(env);
		}
	}

	protected void addRandomPieceOfFood(AgentsEnvironment env) {
		int x = this.random.nextInt(env.getWidth());
		int y = this.random.nextInt(env.getHeight());
		Food newFood = new Food(x, y);
		env.addAgent(newFood);
	}

	private List<Food> getFood(AgentsEnvironment env) {
		// TODO use Guava
		List<Food> food = new ArrayList<Food>();
		for (AbstractAgent agent : env.getAgents()) {
			if (agent instanceof Food) {
				food.add((Food) agent);
			}
		}
		return food;
	}

	private List<Agent> getFishes(AgentsEnvironment env) {
		// TODO use Guava
		List<Agent> fishes = new ArrayList<Agent>();
		for (AbstractAgent agent : env.getAgents()) {
			if (agent instanceof Agent) {
				fishes.add((Agent) agent);
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