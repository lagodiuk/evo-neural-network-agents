package com.lagodiuk.nn.genetic.test;

import com.lagodiuk.nn.NeuralNetwork;
import com.lagodiuk.nn.genetic.NeuralNetworkContext;
import com.lagodiuk.nn.genetic.NeuralNetworkFitness;

public class NnXorFitness extends NeuralNetworkFitness {

	public NnXorFitness(NeuralNetworkContext context) {
		super(context);
	}

	@Override
	public double calculateNnFitness(NeuralNetwork nn) {
		double delt = 0;

		for (int i = -5; i < 6; i++) {
			for (int j = -5; j < 6; j++) {
				double target;
				if (i == j) {
					target = 0;
				} else {
					target = 1;
				}

				nn.putSignalToNeuron(0, i);
				nn.putSignalToNeuron(1, j);

				nn.activate();

				double nnOutput = nn.getAfterActivationSignal(5);

				double d = nnOutput - target;
				delt += d * d;
			}
		}
		return delt;
	}
}