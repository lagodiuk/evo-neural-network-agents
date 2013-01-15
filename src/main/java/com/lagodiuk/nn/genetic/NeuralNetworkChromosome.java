package com.lagodiuk.nn.genetic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.lagodiuk.ga.Chromosome;
import com.lagodiuk.nn.Neuron;
import com.lagodiuk.nn.ThresholdFunction;
import com.lagodiuk.nn.ThresholdFunctions;

public class NeuralNetworkChromosome implements Chromosome<NeuralNetworkChromosome>, Cloneable {

	private static double weightsMutationInterval = 1;

	private static double neuronParamsMutationInterval = 1;

	private List<Double> weights;

	private List<Neuron> neurons;

	private int activationIterations;

	private Random random = new Random();

	public NeuralNetworkChromosome(List<Neuron> neurons, List<Double> weights, int activationIterations) {
		this.neurons = neurons;
		this.weights = weights;
		this.activationIterations = activationIterations;
	}

	@Override
	public List<NeuralNetworkChromosome> crossover(NeuralNetworkChromosome anotherGene) {
		NeuralNetworkChromosome thisClone = this.clone();
		NeuralNetworkChromosome anotherClone = anotherGene.clone();

		switch (this.random.nextInt(4)) {
			case 0:
				this.twoPointsWeightsCrossover(thisClone, anotherClone);
				break;
			case 1:
				this.uniformelyDistributedWeightsCrossover(thisClone, anotherClone);
				break;
			case 2:
				this.uniformelyDistributedNeuronsCrossover(thisClone, anotherClone);
				break;
			case 3:
				this.twoPointsNeuronsCrossover(thisClone, anotherClone);
				break;
		}

		List<NeuralNetworkChromosome> ret = new LinkedList<NeuralNetworkChromosome>();

		ret.add(thisClone);
		ret.add(anotherClone);

		ret.add(thisClone.mutate());
		ret.add(anotherClone.mutate());

		return ret;
	}

	private void uniformelyDistributedNeuronsCrossover(NeuralNetworkChromosome thisClone, NeuralNetworkChromosome anotherClone) {
		int itersCount = this.random.nextInt(this.neurons.size());
		if (itersCount == 0) {
			itersCount = 1;
		}
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(this.neurons.size());
			Neuron thisNeuron = thisClone.neurons.get(i);
			Neuron anotherNeuron = anotherClone.neurons.get(i);

			anotherClone.neurons.set(i, thisNeuron);
			thisClone.neurons.set(i, anotherNeuron);
		}
	}

	private void uniformelyDistributedWeightsCrossover(NeuralNetworkChromosome thisClone, NeuralNetworkChromosome anotherClone) {
		int itersCount = this.random.nextInt(this.weights.size());
		if (itersCount == 0) {
			itersCount = 1;
		}
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(this.weights.size());
			double thisWeight = thisClone.weights.get(i);
			double anotherWeight = anotherClone.weights.get(i);

			anotherClone.weights.set(i, thisWeight);
			thisClone.weights.set(i, anotherWeight);
		}
	}

	private void twoPointsWeightsCrossover(NeuralNetworkChromosome thisClone, NeuralNetworkChromosome anotherClone) {
		int left = this.random.nextInt(this.weights.size());
		int right = this.random.nextInt(this.weights.size());
		if (left > right) {
			int tmp = right;
			right = left;
			left = tmp;
		}
		for (int i = left; i < right; i++) {
			double thisWeight = thisClone.weights.get(i);
			thisClone.weights.set(i, anotherClone.weights.get(i));
			anotherClone.weights.set(i, thisWeight);
		}
	}

	private void twoPointsNeuronsCrossover(NeuralNetworkChromosome thisClone, NeuralNetworkChromosome anotherClone) {
		int left = this.random.nextInt(this.neurons.size());
		int right = this.random.nextInt(this.neurons.size());
		if (left > right) {
			int tmp = right;
			right = left;
			left = tmp;
		}
		for (int i = left; i < right; i++) {
			Neuron thisNeuron = thisClone.neurons.get(i);
			thisClone.neurons.set(i, anotherClone.neurons.get(i));
			anotherClone.neurons.set(i, thisNeuron);
		}
	}

	@Override
	public NeuralNetworkChromosome mutate() {
		NeuralNetworkChromosome mutated = this.clone();
		int mutationType = this.random.nextInt(4);
		switch (mutationType) {
			case 0:
				mutated.mutateWeights();
				break;
			case 1:
				mutated.mutateNeuronsFunctionsParams();
				break;
			case 2:
				mutated.mutateChangeNeuronsFunctions();
				break;
			case 3:
				mutated.shuffleWeightsOnSubinterval();
				break;
		}
		return mutated;
	}

	private void mutateWeights() {
		int itersCount = this.random.nextInt(this.weights.size());
		if (itersCount == 0) {
			itersCount = 1;
		}
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(this.weights.size());
			double w = this.weights.get(i);
			w += (this.random.nextDouble() - this.random.nextDouble()) * weightsMutationInterval;
			this.weights.set(i, w);
		}
	}

	private void shuffleWeightsOnSubinterval() {
		int left = this.random.nextInt(this.weights.size());
		int right = this.random.nextInt(this.weights.size());
		if (left > right) {
			int tmp = right;
			right = left;
			left = tmp;
		}
		List<Double> subListOfWeights = new ArrayList<Double>((right - left) + 1);
		for (int i = 0; i < ((right - left) + 1); i++) {
			subListOfWeights.add(this.weights.get(left + i));
		}
		Collections.shuffle(subListOfWeights);
		for (int i = 0; i < ((right - left) + 1); i++) {
			this.weights.set(left + i, subListOfWeights.get(i));
		}
	}

	private void mutateNeuronsFunctionsParams() {
		int itersCount = this.random.nextInt(this.neurons.size());
		if (itersCount == 0) {
			itersCount = 1;
		}
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(this.neurons.size());
			Neuron n = this.neurons.get(i);

			List<Double> params = n.getParams();
			for (int j = 0; j < params.size(); j++) {
				double param = params.get(j);
				param += (this.random.nextDouble() - this.random.nextDouble()) * neuronParamsMutationInterval;
				params.set(j, param);
			}
			n.setFunctionAndParams(n.getFunction(), params);
		}
	}

	private void mutateChangeNeuronsFunctions() {
		int itersCount = this.random.nextInt(this.neurons.size()) + 1;
		if (itersCount > 1) {
			itersCount--;
		}
		for (int iter = 0; iter < itersCount; iter++) {
			int i = this.random.nextInt(this.neurons.size());
			Neuron n = this.neurons.get(i);
			ThresholdFunction f = ThresholdFunctions.getRandomFunction();
			n.setFunctionAndParams(f, f.getDefaultParams());
		}
	}

	@Override
	protected NeuralNetworkChromosome clone() {
		List<Double> clonedWeights = new ArrayList<Double>(this.weights.size());
		for (double d : this.weights) {
			clonedWeights.add(d);
		}
		List<Neuron> clonedNeurons = new ArrayList<Neuron>(this.neurons.size());
		for (Neuron n : this.neurons) {
			clonedNeurons.add(n.clone());
		}
		NeuralNetworkChromosome clonedChromosome = new NeuralNetworkChromosome(clonedNeurons, clonedWeights, this.activationIterations);
		return clonedChromosome;
	}

	public int getActivationIterations() {
		return this.activationIterations;
	}

	public List<Neuron> getNeurons() {
		return this.neurons;
	}

	public List<Double> getWeights() {
		return this.weights;
	}
}
