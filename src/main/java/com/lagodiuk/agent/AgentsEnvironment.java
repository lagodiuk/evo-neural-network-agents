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

	public void timeStep() {
		for (Agent agent : this.getAgents()) {
			agent.interact(this);
		}
		for (AgentsEnvironmentObserver l : this.listeners) {
			l.notify(this);
		}
	}

	public List<Agent> getAgents() {
		// to avoid concurrent modification exception
		return new LinkedList<Agent>(this.agents);
	}

	public void addAgent(Agent agent) {
		this.agents.add(agent);
	}

	public void removeAgent(Agent agent) {
		this.agents.remove(agent);
	}
}