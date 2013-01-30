package com.lagodiuk.agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AgentsEnvironment {

	private int width;

	private int height;

	private List<Agent> agents = new ArrayList<Agent>();

	private List<AgentsEnvironmentObserver> listeners = new ArrayList<AgentsEnvironmentObserver>();

	public AgentsEnvironment(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void addListener(AgentsEnvironmentObserver listener) {
		this.listeners.add(listener);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public synchronized void timeStep() {
		for (Agent agent : this.getAgents()) {
			agent.interact(this);
			this.avoidMovingOutsideOfBounds(agent);
		}
		for (AgentsEnvironmentObserver l : this.listeners) {
			l.notify(this);
		}
	}

	/**
	 * avoid moving outside of environment
	 */
	private void avoidMovingOutsideOfBounds(Agent agent) {
		double newX = agent.getX();
		double newY = agent.getY();
		if (newX < 0) {
			newX = this.width - 1;
		}
		if (newY < 0) {
			newY = this.height - 1;
		}
		if (newX > this.width) {
			newX = 1;
		}
		if (newY > this.height) {
			newY = 1;
		}

		agent.setX(newX);
		agent.setY(newY);
	}

	public List<Agent> getAgents() {
		// to avoid concurrent modification exception
		return new LinkedList<Agent>(this.agents);
	}

	public synchronized void addAgent(Agent agent) {
		this.agents.add(agent);
	}

	public synchronized void removeAgent(Agent agent) {
		this.agents.remove(agent);
	}
}