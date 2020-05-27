package me.cepera.dfmodel.elements.impl;

import java.io.IOException;
import java.util.Optional;

import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.elements.BooleanElementParameter;
import me.cepera.dfmodel.elements.ElementBase;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementFactory;
import me.cepera.dfmodel.elements.IElementParameter;

public class DTriggerElement extends ElementBase<DTriggerElement>{
	
	private boolean delay;
	private boolean value;
	
	public DTriggerElement(IElementFactory<DTriggerElement> factory) {
		super(factory);
		registerPriorityParam();
		parameters.add(new BooleanElementParameter() {
			@Override
			public String getIdentificator() {
				return "constant_value";
			}
			
			@Override
			public void setElementValue(Boolean value) {
				DTriggerElement.this.value = value;
			}
			
			@Override
			public Boolean getElementValue() {
				return value;
			}
		});
		IElementParameter delayParameter = new BooleanElementParameter() {
			@Override
			public String getIdentificator() {
				return "tick_delay";
			}

			@Override
			public Boolean getElementValue() {
				return delay;
			}

			@Override
			public void setElementValue(Boolean value) {
				delay = value;
			}
		};
		parameters.add(delayParameter);
	}
	
	public boolean hasDelay() {
		return delay;
	}
	
	public boolean getValue() {
		return value;
	}

	@Override
	public int getInputCount() {
		return 2;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}

	@Override
	public boolean[] process(boolean[] input, int tick) {
		boolean lv = value;
		if(input[1]) {
			value = input[0];
			if(!delay) lv = value;
		}
		return new boolean[] {lv};
	}
	
	@Override
	public Optional<boolean[]> preprocessed(int tick) {
		if(delay) {
			return Optional.of(new boolean[] {value});
		}
		return super.preprocessed(tick);
	}

	@Override
	protected void copyState(DTriggerElement from) {
		this.value = from.value;
		this.delay = from.delay;
	}

	@Override
	public boolean statesEquals(IElement another) {
		return another instanceof DTriggerElement
				&& ((DTriggerElement)another).value == this.value
				&& ((DTriggerElement)another).delay == this.delay;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		super.readData(data);
		try {
			value = data.readBoolean();
			delay = data.readBoolean();
		}catch (Exception e) {
			DFModel.logException("Element read error", e);
		}
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(value);
		data.writeBoolean(delay);
	}

}
