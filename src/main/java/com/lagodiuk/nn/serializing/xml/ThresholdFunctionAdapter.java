package com.lagodiuk.nn.serializing.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.lagodiuk.nn.ThresholdFunction;

public class ThresholdFunctionAdapter extends XmlAdapter<ThresholdFunctionElement, ThresholdFunction> {

	@Override
	public ThresholdFunctionElement marshal(ThresholdFunction function) throws Exception {
		Class<?> functionClass = function.getClass();
		if (functionClass.getSuperclass().isEnum()) {
			// enum
			String enumContainerClassName = functionClass.getSuperclass().getName();
			String enumItemName = (String) functionClass.getMethod("name").invoke(function);
			return new ThresholdFunctionElement(enumContainerClassName, enumItemName);
		}
		return new ThresholdFunctionElement(functionClass.getName(), null);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ThresholdFunction unmarshal(ThresholdFunctionElement elem) throws Exception {
		ThresholdFunction function = null;
		if ((elem.className != null) && (elem.enumItem != null)) {
			function = (ThresholdFunction) Enum.valueOf((Class<? extends Enum>) Class.forName(elem.className), elem.enumItem);
		} else if (elem.className != null) {
			Class<?> thresholdFunctionClass = Class.forName(elem.className);
			// TODO instantiate non-enums
		}
		if (function == null) {
			throw new Exception("Can't unmarshall threshold function");
		}
		return function;
	}
}
