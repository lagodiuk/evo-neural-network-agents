package com.lagodiuk.nn.serializing.xml;

public class ThresholdFunctionElement {

	public String className;

	public String enumItem;

	public ThresholdFunctionElement() {
		// Required by JAXB
	}

	public ThresholdFunctionElement(String className, String enumName) {
		this.className = className;
		this.enumItem = enumName;
	}
}
