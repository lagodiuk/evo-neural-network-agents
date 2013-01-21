package com.lagodiuk.agent.evolution;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.lagodiuk.agent.Agent;
import com.lagodiuk.agent.AgentsEnvironment;
import com.lagodiuk.agent.AgentsEnvironmentObserver;
import com.lagodiuk.agent.Fish;
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

		List<Fish> collidedFishes = this.getCollidedFishes(env);
		this.score -= collidedFishes.size() * 0.5;

		this.removeEatenAndCreateNewFood(env, eatenFood);
	}

	private List<Fish> getCollidedFishes(AgentsEnvironment env) {
		List<Fish> collidedFishes = new LinkedList<Fish>();

		List<Fish> allFishes = this.getFishes(env);
		int fishesCount = allFishes.size();

		for (int i = 0; i < (fishesCount - 1); i++) {
			Fish firstFish = allFishes.get(i);
			for (int j = i + 1; j < fishesCount; j++) {
				Fish secondFish = allFishes.get(j);
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
			for (Fish fish : this.getFishes(env)) {
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

	private void removeEatenAndCreateNewFood(AgentsEnvironment env, List<Food> eatenFood) {
		for (Food food : eatenFood) {
			env.removeAgent(food);

			Food newFood = new Food(this.random.nextInt(env.getWidth()), this.random.nextInt(env.getHeight()));
			env.addAgent(newFood);
		}
	}

	private List<Food> getFood(AgentsEnvironment env) {
		// TODO use Guava
		List<Food> food = new ArrayList<Food>();
		for (Agent agent : env.getAgents()) {
			if (agent instanceof Food) {
				food.add((Food) agent);
			}
		}
		return food;
	}

	private List<Fish> getFishes(AgentsEnvironment env) {
		// TODO use Guava
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