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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class NeuralNetwork implements Cloneable {

	@XmlElementWrapper(name = "neurons")
	@XmlElement(name = "neuron")
	protected List<Neuron> neurons;

	@XmlElement
	protected Links neuronsLinks = new Links();

	@XmlElement
	protected int activationIterations = 1;

	public NeuralNetwork() {
		// Required by JAXB
	}

	public NeuralNetwork(int numberOfNeurons) {
		this.neurons = new ArrayList<Neuron>(numberOfNeurons);
		for (int i = 0; i < numberOfNeurons; i++) {
			this.neurons.add(new Neuron(ThresholdFunction.SIGN, ThresholdFunction.SIGN.getDefaultParams()));
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
		this.neuronsLinks.addWeight(activatorNeuronNumber, receiverNeuronNumber, weight);
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

				for (Integer receiverNum : this.neuronsLinks.getReceivers(i)) {
					if (receiverNum >= this.neurons.size()) {
						throw new RuntimeException("Neural network has " + this.neurons.size()
								+ " neurons. But there was trying to accsess neuron with index " + receiverNum);
					}
					Neuron receiver = this.neurons.get(receiverNum);
					double weight = this.neuronsLinks.getWeight(i, receiverNum);
					receiver.addSignal(activatorSignal * weight);
				}
			}
		}
	}

	@XmlTransient
	public List<Double> getWeightsOfLinks() {
		return this.neuronsLinks.getAllWeights();
	}

	public void setWeightsOfLinks(List<Double> weights) {
		this.neuronsLinks.setAllWeights(weights);
	}

	@XmlTransient
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

	@XmlTransient
	public int getActivationIterations() {
		return this.activationIterations;
	}

	public void setActivationIterations(int activationIterations) {
		this.activationIterations = activationIterations;
	}

	public Links getNeuronsLinks() {
		return this.neuronsLinks.clone();
	}

	@Override
	public NeuralNetwork clone() {
		NeuralNetwork clone = new NeuralNetwork(this.neurons.size());
		clone.neuronsLinks = this.neuronsLinks.clone();
		clone.activationIterations = this.activationIterations;
		clone.neurons = new ArrayList<Neuron>(this.neurons.size());
		for (Neuron neuron : this.neurons) {
			clone.neurons.add(neuron.clone());
		}
		return clone;
	}

	@Override
	public String toString() {
		return "NeuralNetwork [neurons=" + this.neurons + ", links=" + this.neuronsLinks + ", activationIterations=" + this.activationIterations + "]";
	}

	public static void marsall(NeuralNetwork nn, OutputStream out) throws Exception {
		// TODO refactoring
		JAXBContext context = JAXBContext.newInstance(NeuralNetwork.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(nn, out);
		out.flush();
	}

	public static NeuralNetwork unmarsall(InputStream in) throws Exception {
		// TODO refactoring
		JAXBContext context = JAXBContext.newInstance(NeuralNetwork.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		NeuralNetwork unmarshalledNn = (NeuralNetwork) unmarshaller.unmarshal(in);
		return unmarshalledNn;
	}
}
