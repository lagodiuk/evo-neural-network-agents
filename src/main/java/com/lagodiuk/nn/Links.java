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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.lagodiuk.nn.serializing.xml.MapAdapter;

public class Links implements Cloneable {

	@XmlJavaTypeAdapter(value = MapAdapter.class)
	private Map<Integer, Map<Integer, Double>> links = new LinkedHashMap<Integer, Map<Integer, Double>>();

	@XmlElement(name = "linksCount")
	private int totalLinksCount = 0;

	public Collection<Integer> getReceivers(int activatorNeuronNumber) {
		Collection<Integer> ret = null;
		if (this.links.containsKey(activatorNeuronNumber)) {
			ret = Collections.unmodifiableSet(this.links.get(activatorNeuronNumber).keySet());
		} else {
			ret = Collections.emptySet();
		}
		return ret;
	}

	public Double getWeight(int activatorNeuronNumber, int receiverNeuronNumber) {
		double weight = 0;

		if (this.links.containsKey(activatorNeuronNumber)) {
			Map<Integer, Double> receiverNumToWeight = this.links.get(activatorNeuronNumber);

			if (receiverNumToWeight.containsKey(receiverNeuronNumber)) {
				weight = receiverNumToWeight.get(receiverNeuronNumber);
			} else {
				throw new IllegalArgumentException();
			}
		} else {
			throw new IllegalArgumentException();
		}
		return weight;
	}

	public void addWeight(int activatorNeuronNumber, int receiverNeuronNumber, double weight) {
		if (!this.links.containsKey(activatorNeuronNumber)) {
			this.links.put(activatorNeuronNumber, new LinkedHashMap<Integer, Double>());
		}
		this.links.get(activatorNeuronNumber).put(receiverNeuronNumber, weight);
		this.totalLinksCount++;
	}

	@XmlTransient
	public List<Double> getAllWeights() {
		List<Double> weights = new ArrayList<Double>(this.totalLinksCount);

		for (Integer activatorIndx : this.links.keySet()) {
			Map<Integer, Double> receiverIndxToWeight = this.links.get(activatorIndx);

			for (Integer receiverIndx : receiverIndxToWeight.keySet()) {
				weights.add(receiverIndxToWeight.get(receiverIndx));
			}
		}

		return weights;
	}

	public void setAllWeights(List<Double> weights) {
		if (weights.size() != this.totalLinksCount) {
			throw new IllegalArgumentException("Number of links is " + this.totalLinksCount
					+ ". But weights list has size " + weights.size());
		}

		int indx = 0;
		for (Integer activatorIndx : this.links.keySet()) {
			Map<Integer, Double> receiverIndxToWeight = this.links.get(activatorIndx);

			for (Integer receiverIndx : receiverIndxToWeight.keySet()) {
				receiverIndxToWeight.put(receiverIndx, weights.get(indx));
				indx++;
			}
		}
	}

	@Override
	public Links clone() {
		Links clone = new Links();
		clone.totalLinksCount = this.totalLinksCount;
		clone.links = new LinkedHashMap<Integer, Map<Integer, Double>>();
		for (int key : this.links.keySet()) {
			Map<Integer, Double> val = new LinkedHashMap<Integer, Double>();
			for (int valKey : this.links.get(key).keySet()) {
				val.put(valKey, this.links.get(key).get(valKey));
			}
			clone.links.put(key, val);
		}
		return clone;
	}

	@Override
	public String toString() {
		return "Links [links=" + this.links + ", totalLinksCount=" + this.totalLinksCount + "]";
	}
}
