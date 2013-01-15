package com.lagodiuk.nn.genetic;

import java.util.List;

import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.Neuron;

public class NeuralNetworkContext {

	private NeuralNetwork nn;

	public NeuralNetworkContext(NeuralNetwork nn) {
		this.nn = nn;
	}

	public NeuralNetworkChromosome getChromosome() {
		List<Double> weights = this.nn.getWeightsOfLinks();
		List<Neuron> neurons = this.nn.getNeurons();
		int iterationsCount = this.nn.getActivationIterations();
		NeuralNetworkChromosome gene = new NeuralNetworkChromosome(neurons, weights, iterationsCount);
		return gene;
	}

	public void applyChromosome(NeuralNetworkChromosome gene) {
		this.nn.setActivationIterations(gene.getActivationIterations());
		this.nn.setNeurons(gene.getNeurons());
		this.nn.setWeightsOfLinks(gene.getWeights());
	}

	public NeuralNetwork getNeuralNetwork() {
		return this.nn;
	}

}
