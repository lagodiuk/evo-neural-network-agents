/*******************************************************************************
 * Copyright 2012 Yuriy Lagodiuk
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
