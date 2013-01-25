package com.lagodiuk.nn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

	public void marsall(OutputStream out) throws Exception {
		// TODO refactoring
		JAXBContext context = JAXBContext.newInstance(NeuralNetwork.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(this, out);
	}

	public static void main(String[] args) throws Exception {
		NeuralNetwork nn = makePerceptronXOR();

		// nn.setNeuronFunction(0, new ThresholdFunction() {
		// @Override
		// public List<Double> getDefaultParams() {
		// return new LinkedList<Double>();
		// }
		//
		// @Override
		// public double calculate(double value, List<Double> params) {
		// return 0;
		// }
		// }, new LinkedList<Double>());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		JAXBContext context = JAXBContext.newInstance(NeuralNetwork.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(nn, baos);

		baos.flush();
		byte[] data = baos.toByteArray();

		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		NeuralNetwork nn2 = (NeuralNetwork) unmarshaller.unmarshal(bais);

		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		marshaller.marshal(nn2, baos2);
		byte[] data2 = baos2.toByteArray();

		String s1 = new String(data);
		String s2 = new String(data2);

		System.out.println(s1.equals(s2));
	}

	private static NeuralNetwork makePerceptronXOR() {
		NeuralNetwork nn = new NeuralNetwork(6);

		nn.setNeuronFunction(0, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams());
		nn.setNeuronFunction(1, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams());
		for (int i = 2; i < 6; i++) {
			nn.setNeuronFunction(i, ThresholdFunctions.SIGN, ThresholdFunctions.SIGN.getDefaultParams());
		}

		nn.addLink(0, 2, -1);
		nn.addLink(0, 3, 1);
		nn.addLink(0, 4, 1);

		nn.addLink(1, 2, 1);
		nn.addLink(1, 3, -1);
		nn.addLink(1, 4, 1);

		nn.addLink(2, 5, 2);

		nn.addLink(3, 5, 2);

		nn.addLink(4, 5, -1);

		return nn;
	}
}
