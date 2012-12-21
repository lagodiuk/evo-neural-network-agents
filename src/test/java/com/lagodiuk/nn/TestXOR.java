package com.lagodiuk.nn;

import java.util.List;


public class TestXOR {

    public static void main( String[] args ) {

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

        nn.putSignalToNeuron( 0, -4.9901 );
        nn.putSignalToNeuron( 1, -4.9902 );

        System.out.println( nn.getWeightsOfLinks() );
        List<Double> weights = nn.getWeightsOfLinks();
        nn.setWeightsOfLinks( weights );

        nn.activate();

        System.out.println( nn.getAfterActivationSignal( 5 ) );
        System.out.println( nn.getWeightsOfLinks() );
        System.out.println( nn.getNeurons() );
    }
}
