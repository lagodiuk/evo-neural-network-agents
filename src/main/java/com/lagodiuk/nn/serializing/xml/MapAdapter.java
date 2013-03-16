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
package com.lagodiuk.nn.serializing.xml;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class MapAdapter extends XmlAdapter<MapElement[], Map<Integer, Map<Integer, Double>>> {

	@Override
	public MapElement[] marshal(Map<Integer, Map<Integer, Double>> map) throws Exception {
		List<MapElement> list = new LinkedList<MapElement>();

		for (Integer from : map.keySet()) {
			Map<Integer, Double> toMap = map.get(from);
			for (Integer to : toMap.keySet()) {
				Double weight = toMap.get(to);
				list.add(new MapElement(from, to, weight));
			}
		}

		return list.toArray(new MapElement[] {});
	}

	@Override
	public Map<Integer, Map<Integer, Double>> unmarshal(MapElement[] mapElements) throws Exception {
		Map<Integer, Map<Integer, Double>> result = new HashMap<Integer, Map<Integer, Double>>();
		for (MapElement me : mapElements) {
			Integer from = me.from;
			Map<Integer, Double> toMap = result.get(from);
			if (toMap == null) {
				toMap = new HashMap<Integer, Double>();
				result.put(from, toMap);
			}
			toMap.put(me.to, me.weight);
		}
		return result;
	}
}
