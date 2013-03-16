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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "basic-threshold-functions")
@XmlEnum
public enum ThresholdFunction {

	@XmlEnumValue("LINEAR")
	LINEAR {
		@Override
		public double calculate(double value, List<Double> params) {
			double a = params.get(0);
			double b = params.get(1);
			return (a * value) + b;
		};

		@Override
		public List<Double> getDefaultParams() {
			double a = 1;
			double b = 0;
			List<Double> ret = new LinkedList<Double>();
			ret.add(a);
			ret.add(b);
			return ret;
		}
	},
	@XmlEnumValue("SIGN")
	SIGN {
		@Override
		public double calculate(double value, List<Double> params) {
			double threshold = params.get(0);
			if (value > threshold) {
				return 1;
			} else {
				return 0;
			}
		};

		@Override
		public List<Double> getDefaultParams() {
			double threshold = 0;
			List<Double> ret = new LinkedList<Double>();
			ret.add(threshold);
			return ret;
		}
	},
	@XmlEnumValue("SIGMA")
	SIGMA {
		@Override
		public double calculate(double value, List<Double> params) {
			double a = params.get(0);
			double b = params.get(1);
			double c = params.get(2);
			return a / (b + Math.expm1(-value * c) + 1);
		}

		@Override
		public List<Double> getDefaultParams() {
			double a = 1;
			double b = 1;
			double c = 1;
			List<Double> ret = new ArrayList<Double>(3);
			ret.add(a);
			ret.add(b);
			ret.add(c);
			return ret;
		}
	};

	private static final Random random = new Random();

	public static ThresholdFunction getRandomFunction() {
		ThresholdFunction[] allFunctions = values();
		return allFunctions[random.nextInt(allFunctions.length)];
	}

	public double calculate(double value, List<Double> params) {
		// Stub
		return 0;
	}

	public List<Double> getDefaultParams() {
		// Stub
		return Collections.emptyList();
	}
}
