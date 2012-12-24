package com.lagodiuk.nn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestPerceptronXOR {

	@Test
	public void testXOR() {
		NeuralNetwork perceptron = this.makePerceptronXOR();
		int lastNeuronIndx = perceptron.getNeuronsCount() - 1;

		// 1 XOR 1 = 0
		perceptron.putSignalToNeuron(0, 1);
		perceptron.putSignalToNeuron(1, 1);
		perceptron.activate();
		assertEquals(0.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 0 XOR 0 = 0
		perceptron.putSignalToNeuron(0, 0);
		perceptron.putSignalToNeuron(1, 0);
		perceptron.activate();
		assertEquals(0.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 100 XOR 100 = 0
		perceptron.putSignalToNeuron(0, 100);
		perceptron.putSignalToNeuron(1, 100);
		perceptron.activate();
		assertEquals(0.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// -5 XOR -5 = 0
		perceptron.putSignalToNeuron(0, -5);
		perceptron.putSignalToNeuron(1, -5);
		perceptron.activate();
		assertEquals(0.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 1 XOR 0 = 1
		perceptron.putSignalToNeuron(0, 1);
		perceptron.putSignalToNeuron(1, 0);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 0 XOR 1 = 1
		perceptron.putSignalToNeuron(0, 0);
		perceptron.putSignalToNeuron(1, 1);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 10 XOR 1 = 1
		perceptron.putSignalToNeuron(0, 10);
		perceptron.putSignalToNeuron(1, 1);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// -10 XOR 1 = 1
		perceptron.putSignalToNeuron(0, -10);
		perceptron.putSignalToNeuron(1, 1);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// 100 XOR 111 = 1
		perceptron.putSignalToNeuron(0, 100);
		perceptron.putSignalToNeuron(1, 111);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);

		// -4.9901 XOR -4.9902 = 1
		perceptron.putSignalToNeuron(0, -4.9901);
		perceptron.putSignalToNeuron(1, -4.9902);
		perceptron.activate();
		assertEquals(1.0, perceptron.getAfterActivationSignal(lastNeuronIndx), 1e-5);
	}

	@Test
	public void testLinks() {
		NeuralNetwork perceptron = this.makePerceptronXOR();

		List<Double> expectedWeights = new ArrayList<Double>();
		expectedWeights.add(-1.0);
		expectedWeights.add(1.0);
		expectedWeights.add(1.0);
		expectedWeights.add(1.0);
		expectedWeights.add(-1.0);
		expectedWeights.add(1.0);
		expectedWeights.add(2.0);
		expectedWeights.add(2.0);
		expectedWeights.add(-1.0);

		// expected list of links:
		// [-1.0, 1.0, 1.0, 1.0, -1.0, 1.0, 2.0, 2.0, -1.0]
		assertEquals(expectedWeights, perceptron.getWeightsOfLinks());

		// test that getter returns collection, which doesn't affect actual
		// neural network
		List<Double> perceptronWeights1 = perceptron.getWeightsOfLinks();
		perceptronWeights1.clear();
		assertEquals(expectedWeights, perceptron.getWeightsOfLinks());

		// test that after setting new weights, neural network doesn't affected
		// by collection of new weights
		List<Double> perceptronWeights2 = new ArrayList<Double>();
		for (int i = 0; i < 9; i++) {
			perceptronWeights2.add(100.0);
		}
		perceptron.setWeightsOfLinks(perceptronWeights2);
		perceptronWeights2.clear();
		List<Double> expectedWeights2 = new ArrayList<Double>(perceptronWeights2);
		for (int i = 0; i < 9; i++) {
			expectedWeights2.add(100.0);
		}
		assertEquals(expectedWeights2, perceptron.getWeightsOfLinks());

		// test, that setting smaller amount of weights causes exception
		List<Double> emptyWeights = new ArrayList<Double>();
		try {
			perceptron.setWeightsOfLinks(emptyWeights);
			fail("Successfully set empty list of weights");
		} catch (IllegalArgumentException e) {
			assertEquals("Number of links is 9. But weights list has size 0", e.getMessage());
		}

		// test, that setting smaller amount of weights causes exception
		List<Double> smallerWeights = new ArrayList<Double>();
		smallerWeights.add(0.0);
		smallerWeights.add(0.0);
		try {
			perceptron.setWeightsOfLinks(smallerWeights);
			fail("Successfully set empty list of weights");
		} catch (IllegalArgumentException e) {
			assertEquals("Number of links is 9. But weights list has size 2", e.getMessage());
		}

		// test, that setting greasther amount of weights causes exception
		List<Double> greathreWeights = new ArrayList<Double>();
		for (int i = 0; i < 12; i++) {
			greathreWeights.add(1.0);
		}
		try {
			perceptron.setWeightsOfLinks(greathreWeights);
			fail("Successfully set empty list of weights");
		} catch (IllegalArgumentException e) {
			assertEquals("Number of links is 9. But weights list has size 12", e.getMessage());
		}
	}

	private NeuralNetwork makePerceptronXOR() {
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
