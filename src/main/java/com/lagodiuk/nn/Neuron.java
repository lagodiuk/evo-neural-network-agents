package com.lagodiuk.nn;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.lagodiuk.nn.serializing.xml.ThresholdFunctionAdapter;

@XmlRootElement(name = "neuron")
public class Neuron implements Cloneable {

	@XmlTransient
	private double inputSignal;

	@XmlTransient
	private double afterActivationSignal;

	@XmlJavaTypeAdapter(value = ThresholdFunctionAdapter.class)
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
