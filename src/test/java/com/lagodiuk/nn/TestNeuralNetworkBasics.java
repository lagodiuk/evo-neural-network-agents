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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Perceptron which can solve 'XOR' problem: <br/>
 * http://upload.wikimedia.org/wikipedia/commons/9/97/Perceptron_XOR_task_v2.svg
 */
public class TestNeuralNetworkBasics {

	@Test
	public void testXOR() {
		NeuralNetwork perceptron = this.makePerceptronXOR();

		double result;

		// 1 XOR 1 = 0
		result = this.xor(1, 1, perceptron);
		assertEquals(0.0, result, 1e-5);

		// 0 XOR 0 = 0
		result = this.xor(0, 0, perceptron);
		assertEquals(0.0, result, 1e-5);

		// 100 XOR 100 = 0
		result = this.xor(100, 100, perceptron);
		assertEquals(0.0, result, 1e-5);

		// -5 XOR -5 = 0
		result = this.xor(-5, -5, perceptron);
		assertEquals(0.0, result, 1e-5);

		// 1 XOR 0 = 1
		result = this.xor(1, 0, perceptron);
		assertEquals(1.0, result, 1e-5);

		// 0 XOR 1 = 1
		result = this.xor(0, 1, perceptron);
		assertEquals(1.0, result, 1e-5);

		// 10 XOR 1 = 1
		result = this.xor(10, 1, perceptron);
		assertEquals(1.0, result, 1e-5);

		// -10 XOR 1 = 1
		result = this.xor(-10, 1, perceptron);
		assertEquals(1.0, result, 1e-5);

		// 100 XOR 111 = 1
		result = this.xor(100, 111, perceptron);
		assertEquals(1.0, result, 1e-5);

		// -4.9901 XOR -4.9902 = 1
		result = this.xor(-4.9901, -4.9902, perceptron);
		assertEquals(1.0, result, 1e-5);

		for (int i = -10; i < 11; i++) {
			for (int j = -1; j < 11; j++) {
				result = this.xor(i, j, perceptron);

				if (i == j) {
					assertEquals(0.0, result, 1e-5);
				} else {
					assertEquals(1.0, result, 1e-5);
				}
			}
		}
	}

	private double xor(double x, double y, NeuralNetwork perceptron) {
		int lastNeuronIndx = perceptron.getNeuronsCount() - 1;
		perceptron.putSignalToNeuron(0, x);
		perceptron.putSignalToNeuron(1, y);
		perceptron.activate();
		double result = perceptron.getAfterActivationSignal(lastNeuronIndx);
		return result;
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

		// expected list of weights of links:
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
			fail("Successfully set smaller list of weights");
		} catch (IllegalArgumentException e) {
			assertEquals("Number of links is 9. But weights list has size 2", e.getMessage());
		}

		// test, that setting greater amount of weights causes exception
		List<Double> greathreWeights = new ArrayList<Double>();
		for (int i = 0; i < 12; i++) {
			greathreWeights.add(1.0);
		}
		try {
			perceptron.setWeightsOfLinks(greathreWeights);
			fail("Successfully set greather list of weights");
		} catch (IllegalArgumentException e) {
			assertEquals("Number of links is 9. But weights list has size 12", e.getMessage());
		}
	}

	@Test
	public void testTransformingToXml() {
		try {
			NeuralNetwork nn1 = this.makePerceptronXOR();

			// serialize nn1 to xml
			ByteArrayOutputStream out1 = new ByteArrayOutputStream();
			NeuralNetwork.marsall(nn1, out1);
			out1.flush();
			byte[] data1 = out1.toByteArray();

			// deserialize nn2 from that xml
			ByteArrayInputStream in = new ByteArrayInputStream(data1);
			NeuralNetwork nn2 = NeuralNetwork.unmarsall(in);

			// serialize nn2 to xml
			ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			NeuralNetwork.marsall(nn1, out2);
			out2.flush();
			byte[] data2 = out2.toByteArray();

			// equality by toString() representations
			assertEquals(nn1.toString(), nn2.toString());

			// equality of xml representations
			assertEquals(new String(data1), new String(data2));

			// equality of behaviour
			for (int i = -10; i < 11; i++) {
				for (int j = -1; j < 11; j++) {
					assertEquals(this.xor(i, j, nn1), this.xor(i, j, nn2), 1e-5);
				}
			}
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	private NeuralNetwork makePerceptronXOR() {
		NeuralNetwork nn = new NeuralNetwork(6);

		nn.setNeuronFunction(0, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
		nn.setNeuronFunction(1, ThresholdFunction.LINEAR, ThresholdFunction.LINEAR.getDefaultParams());
		for (int i = 2; i < 6; i++) {
			nn.setNeuronFunction(i, ThresholdFunction.SIGN, ThresholdFunction.SIGN.getDefaultParams());
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
