package com.lagodiuk.agent;

public class MovingFood extends Food {

	private double angle;

	private double speed;

	public MovingFood(double x, double y, double angle, double speed) {
		super(x, y);
		this.speed = speed;
		this.angle = angle;
	}

	@Override
	public void interact(AgentsEnvironment env) {
		this.move(env);
	}

	protected void move(AgentsEnvironment env) {
		double rx = -Math.sin(this.angle);
		double ry = Math.cos(this.angle);
		this.setX(this.getX() + (rx * this.speed));
		this.setY(this.getY() + (ry * this.speed));
	}
}
