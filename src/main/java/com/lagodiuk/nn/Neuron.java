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
package com.lagodiuk.nn;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "neuron")
public class Neuron implements Cloneable {

	@XmlTransient
	private double inputSignal;

	@XmlTransient
	private double afterActivationSignal;

	@XmlElement(name = "thresholdFunction")
	private ThresholdFunction thresholdFunction;

	@XmlElementWrapper(name = "parameters")
	@XmlElement(name = "param")
	private List<Double> params;

	public Neuron() {
		// Required by JAXB
	}

	public Neuron(ThresholdFunction function, List<Double> params) {
		this.setFunctionAndParams(function, params);
	}

	public void setFunctionAndParams(ThresholdFunction function, List<Double> params) {
		if (params.size() != function.getDefaultParams().size()) {
			throw new IllegalArgumentException("Function needs " + function.getDefaultParams().size()
					+ " parameters. But params count is " + params.size());
		}
		this.thresholdFunction = function;
		this.params = params;
	}

	public void addSignal(double value) {
		this.inputSignal += value;
	}

	public void activate() {
		this.afterActivationSignal = this.thresholdFunction.calculate(this.inputSignal, this.params);
		this.inputSignal = 0;
	}

	public double getAfterActivationSignal() {
		return this.afterActivationSignal;
	}

	public ThresholdFunction getFunction() {
		return this.thresholdFunction;
	}

	public List<Double> getParams() {
		List<Double> ret = new ArrayList<Double>(this.params.size());
		for (Double d : this.params) {
			ret.add(d);
		}
		return ret;
	}

	@Override
	public Neuron clone() {
		List<Double> cloneParams = new ArrayList<Double>(this.params.size());
		for (double d : this.params) {
			cloneParams.add(d);
		}
		Neuron clone = new Neuron(this.thresholdFunction, cloneParams);
		clone.inputSignal = 0;
		clone.afterActivationSignal = 0;
		return clone;
	}

	@Override
	public String toString() {
		return "Neuron [thresholdFunction=" + this.thresholdFunction + ", params=" + this.params + "]";
	}

}
