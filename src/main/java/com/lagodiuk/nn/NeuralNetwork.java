package com.lagodiuk.nn;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetwork implements Cloneable {

	protected List<Neuron> neurons;

	protected Links links = new Links();

	protected int activationIterations = 1;

	public NeuralNetwork(int numberOfNeurons) {
		this.neurons = new ArrayList<Neuron>(numberOfNeurons);
		for (int i = 0; i < numberOfNeurons; i++) {
			this.neurons.add(new Neuron(ThresholdFunctions.SIGN, ThresholdFunctions.SIGN.getDefaultParams()));
		}
	}

	public void setNeuronFunction(int neuronNumber, ThresholdFunction function, List<Double> params) {
		if (neuronNumber >= this.neurons.size()) {
			throw new RuntimeException("Neural network has " + this.neurons.size()
					+ " neurons. But there was trying to accsess neuron with index " + neuronNumber);
		}
		this.neurons.get(neuronNumber).setFunctionAndParams(function, params);
	}

	public void addLink(int activatorNeuronNumber, int receiverNeuronNumber, double weight) {
		this.links.addWeight(activatorNeuronNumber, receiverNeuronNumber, weight);
	}

	public void putSignalToNeuron(int neuronIndx, double signalValue) {
		if (neuronIndx < this.neurons.size()) {
			this.neurons.get(neuronIndx).addSignal(signalValue);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public double getAfterActivationSignal(int neuronIndx) {
		if (neuronIndx < this.neurons.size()) {
			return this.neurons.get(neuronIndx).getAfterActivationSignal();
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void activate() {
		for (int iter = 0; iter < this.activationIterations; iter++) {

			for (int i = 0; i < this.neurons.size(); i++) {

				Neuron activator = this.neurons.get(i);
				activator.activate();
				double activatorSignal = activator.getAfterActivationSignal();

				for (Integer receiverNum : this.links.getReceivers(i)) {
					if (receiverNum >= this.neurons.size()) {
						throw new RuntimeException("Neural network has " + this.neurons.size()
								+ " neurons. But there was trying to accsess neuron with index " + receiverNum);
					}
					Neuron receiver = this.neurons.get(receiverNum);
					double weight = this.links.getWeight(i, receiverNum);
					receiver.addSignal(activatorSignal * weight);
				}
			}
		}
	}

	public List<Double> getWeightsOfLinks() {
		return this.links.getAllWeights();
	}

	public void setWeightsOfLinks(List<Double> weights) {
		this.links.setAllWeights(weights);
	}

	public List<Neuron> getNeurons() {
		List<Neuron> ret = new ArrayList<Neuron>(this.neurons.size());
		for (Neuron n : this.neurons) {
			ret.add(n.clone());
		}
		return ret;
	}

	public int getNeuronsCount() {
		return this.neurons.size();
	}

	public void setNeurons(List<Neuron> newNeurons) {
		this.neurons = newNeurons;
	}

	public int getActivationIterations() {
		return this.activationIterations;
	}

	public void setActivationIterations(int activationIterations) {
		this.activationIterations = activationIterations;
	}

	@Override
	public NeuralNetwork clone() {
		NeuralNetwork clone = new NeuralNetwork(this.neurons.size());
		clone.links = this.links.clone();
		clone.activationIterations = this.activationIterations;
		clone.neurons = new ArrayList<Neuron>(this.neurons.size());
		for (Neuron neuron : this.neurons) {
			clone.neurons.add(neuron.clone());
		}
		return clone;
	}

}
