package com.lagodiuk.nn.genetic;

import com.lagodiuk.ga.Fitness;
import com.lagodiuk.nn.NeuralNetwork;

public abstract class NeuralNetworkFitness implements Fitness<NeuralNetworkChromosome, Double> {

	private NeuralNetworkContext context;

	public NeuralNetworkFitness(NeuralNetworkContext context) {
		this.context = context;
	}

	@Override
	public Double calculate(NeuralNetworkChromosome gene) {
		this.context.applyChromosome(gene);
		NeuralNetwork nn = this.context.getNeuralNetwork();
		return this.calculateNnFitness(nn);
	}

	public abstract double calculateNnFitness(NeuralNetwork nn);

}