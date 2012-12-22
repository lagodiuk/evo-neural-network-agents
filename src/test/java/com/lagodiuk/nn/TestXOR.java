package com.lagodiuk.nn;

import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;


public class TestXOR {

	@Test
    public void testXOR() {
        NeuralNetwork perceptron = makePerceptronXOR();
        int lastNeuronIndx = perceptron.getNeuronsCount() - 1;
        
        // 1 XOR 1 = 0
        perceptron.putSignalToNeuron( 0, 1 );
        perceptron.putSignalToNeuron( 1, 1 );
        perceptron.activate();
        assertEquals(0.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);

        // 0 XOR 0 = 0
        perceptron.putSignalToNeuron( 0, 0 );
        perceptron.putSignalToNeuron( 1, 0 );
        perceptron.activate();
        assertEquals(0.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // 100 XOR 100 = 0
        perceptron.putSignalToNeuron( 0, 100 );
        perceptron.putSignalToNeuron( 1, 100 );
        perceptron.activate();
        assertEquals(0.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // -5 XOR -5 = 0
        perceptron.putSignalToNeuron( 0, -5 );
        perceptron.putSignalToNeuron( 1, -5 );
        perceptron.activate();
        assertEquals(0.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // 1 XOR 0 = 1
        perceptron.putSignalToNeuron( 0, 1 );
        perceptron.putSignalToNeuron( 1, 0 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // 0 XOR 1 = 1
        perceptron.putSignalToNeuron( 0, 0 );
        perceptron.putSignalToNeuron( 1, 1 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // 10 XOR 1 = 1
        perceptron.putSignalToNeuron( 0, 10 );
        perceptron.putSignalToNeuron( 1, 1 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // -10 XOR 1 = 1
        perceptron.putSignalToNeuron( 0, -10 );
        perceptron.putSignalToNeuron( 1, 1 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // 100 XOR 111 = 1
        perceptron.putSignalToNeuron( 0, 100 );
        perceptron.putSignalToNeuron( 1, 111 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
        
        // -4.9901 XOR -4.9902 = 1
        perceptron.putSignalToNeuron( 0, -4.9901 );
        perceptron.putSignalToNeuron( 1, -4.9902 );
        perceptron.activate();
        assertEquals(1.0, perceptron.getAfterActivationSignal( lastNeuronIndx ), 1e-5);
    }

	private void test() {
		NeuralNetwork perceptron = makePerceptronXOR();
		
		System.out.println( perceptron.getWeightsOfLinks() );
        List<Double> weights = perceptron.getWeightsOfLinks();
        perceptron.setWeightsOfLinks( weights );

        

        System.out.println( perceptron.getAfterActivationSignal( 5 ) );
        System.out.println( perceptron.getWeightsOfLinks() );
        System.out.println( perceptron.getNeurons() );
	}

	private NeuralNetwork makePerceptronXOR() {
		NeuralNetwork nn = new NeuralNetwork( 6 );

        nn.setNeuronFunction( 0, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams() );
        nn.setNeuronFunction( 1, ThresholdFunctions.LINEAR, ThresholdFunctions.LINEAR.getDefaultParams() );
        for ( int i = 2; i < 6; i++ ) {
            nn.setNeuronFunction( i, ThresholdFunctions.SIGN, ThresholdFunctions.SIGN.getDefaultParams() );
        }

        nn.addLink( 0, 2, -1 );
        nn.addLink( 0, 3, 1 );
        nn.addLink( 0, 4, 1 );
        nn.addLink( 1, 2, 1 );
        nn.addLink( 1, 3, -1 );
        nn.addLink( 1, 4, 1 );
        nn.addLink( 2, 5, 2 );
        nn.addLink( 3, 5, 2 );
        nn.addLink( 4, 5, -1 );
		return nn;
	}
}
