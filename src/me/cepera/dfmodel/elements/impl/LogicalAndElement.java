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

public class LogicalAndElement extends ElementBase<LogicalAndElement>{
	
	private boolean delay;
	private boolean lastValue;
	
	public LogicalAndElement(IElementFactory<LogicalAndElement> factory) {
		super(factory);
		registerPriorityParam();
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
		boolean lv = lastValue;
		lastValue = input[0] & input[1];
		if(!delay) lv = lastValue;
		return new boolean[] {lv};
	}
	
	@Override
	public Optional<boolean[]> preprocessed(int tick) {
		if(delay) {
			return Optional.of(new boolean[] {lastValue});
		}
		return super.preprocessed(tick);
	}

	@Override
	protected void copyState(LogicalAndElement from) {
		this.delay = from.delay;
	}

	@Override
	public boolean statesEquals(IElement another) {
		return another instanceof LogicalAndElement
				&& ((LogicalAndElement)another).delay == this.delay;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		super.readData(data);
		try {
			delay = data.readBoolean();
		}catch (Exception e) {
			DFModel.logException("Element read error", e);
		}
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(delay);
	}

}
