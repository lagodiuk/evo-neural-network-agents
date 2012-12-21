package com.lagodiuk.nn;

import java.util.List;

public interface ThresholdFunction {

    public double calculate( double value, List<Double> params );

    public List<Double> getDefaultParams();

}
