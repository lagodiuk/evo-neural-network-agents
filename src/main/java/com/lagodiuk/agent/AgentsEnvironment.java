package com.lagodiuk.agent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AgentsEnvironment {

	private int width;

	private int height;

	private List<Agent> agents = new ArrayList<Agent>();

	private List<AgentsEnvironmentListener> listeners = new ArrayList<AgentsEnvironmentListener>();

	public AgentsEnvironment(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void addListener(AgentsEnvironmentListener listener) {
		this.listeners.add(listener);
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void timeStep() {
		// to avoid concurrent modification exception
		Iterable<Agent> agentsWrap = new LinkedList<Agent>(this.agents);
		for (Agent agent : agentsWrap) {
			agent.interact(this);
		}
		for (AgentsEnvironmentListener l : this.listeners) {
			l.notify(this);
		}
	}

	public List<Agent> getAgents() {
		return this.agents;
	}

	public void addAgent(Agent agent) {
		this.agents.add(agent);
	}

	public void removeAgent(Agent agent) {
		this.agents.remove(agent);
	}
}