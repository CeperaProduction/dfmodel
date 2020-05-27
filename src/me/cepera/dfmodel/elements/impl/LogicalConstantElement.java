package me.cepera.dfmodel.elements.impl;

import java.io.IOException;

import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.elements.BooleanElementParameter;
import me.cepera.dfmodel.elements.ElementBase;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementFactory;

public class LogicalConstantElement extends ElementBase<LogicalConstantElement>{

	private boolean value;
	
	public LogicalConstantElement(IElementFactory<LogicalConstantElement> factory) {
		super(factory);
		parameters.add(new BooleanElementParameter() {
			@Override
			public String getIdentificator() {
				return "constant_value";
			}
			
			@Override
			public void setElementValue(Boolean value) {
				LogicalConstantElement.this.value = value;
			}
			
			@Override
			public Boolean getElementValue() {
				return value;
			}
		});
	}
	
	public boolean getValue() {
		return value;
	}

	@Override
	public int getInputCount() {
		return 0;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public boolean[] process(boolean[] input, int tick) {
		return new boolean[] {value};
	}

	@Override
	public boolean statesEquals(IElement another) {
		if(another instanceof LogicalConstantElement) {
			return ((LogicalConstantElement) another).value == value;
		}
		return false;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		super.readData(data);
		value = data.readBoolean();
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(value);
	}

	@Override
	protected void copyState(LogicalConstantElement from) {
		value = from.value;
	}

}
