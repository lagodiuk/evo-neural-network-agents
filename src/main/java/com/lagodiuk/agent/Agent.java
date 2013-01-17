package com.lagodiuk.agent;


public interface Agent {

	void interact(AgentsEnvironment env);

	double getX();

	double getY();

	void setX(double x);

	void setY(double y);

}
