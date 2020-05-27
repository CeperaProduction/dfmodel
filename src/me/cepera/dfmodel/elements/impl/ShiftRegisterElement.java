package me.cepera.dfmodel.elements.impl;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.layout.Pane;
import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.Scheme;
import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.elements.BooleanElementParameter;
import me.cepera.dfmodel.elements.ElementBase;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementFactory;
import me.cepera.dfmodel.elements.IElementParameter;
import me.cepera.dfmodel.elements.SliderIntegerElementParameter;

public class ShiftRegisterElement extends ElementBase<ShiftRegisterElement>{
	
	private boolean delay;
	private boolean sync;
	private int size = 1;
	private int value;
	private boolean out;
	
	public ShiftRegisterElement(IElementFactory<ShiftRegisterElement> factory) {
		super(factory);
		registerPriorityParam();
		parameters.add(new BooleanElementParameter() {
			
			private Scheme scheme;
			
			@Override
			public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
				this.scheme = scheme;
				super.onParametersWindowInit(resource, container, scheme);
			}
			
			@Override
			public String getIdentificator() {
				return "sync_input";
			}
			
			@Override
			public void setElementValue(Boolean value) {
				sync = value;
				scheme.checkAndCleanWires();
			}
			
			@Override
			public Boolean getElementValue() {
				return sync;
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
		SliderIntegerElementParameter siep = new SliderIntegerElementParameter() {
			
			@Override
			public void onValueChange(int newValue) {
				value = newValue;
			}
			
			@Override
			public void onSizeChange(int newSize) {
				size = newSize;
			}
			
			@Override
			public int minSize() {
				return 1;
			}
			
			@Override
			public int maxSize() {
				return 8;
			}
			
			@Override
			public int getCurrentValue() {
				return value;
			}
			
			@Override
			public int getCurrentSize() {
				return size;
			}
		};
		parameters.add(siep);
	}
	
	public boolean hasDelay() {
		return delay;
	}
	
	public boolean hasSync() {
		return sync;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public int getInputCount() {
		return sync ? 2 : 1;
	}

	@Override
	public int getOutputCount() {
		return 1;
	}
	
	public int getSize() {
		return size;
	}

	@Override
	public boolean[] process(boolean[] input, int tick) {
		if(sync && !input[1]) return new boolean[] {false};
		boolean lv = out;
		int v = value;
		v = v << 1;
		if(input[0]) v |= 1;
		int mask = 1 << size;
		out = (v & mask) != 0;
		value = v & (mask - 1);
		if(!delay) lv = out;
		return new boolean[] {lv};
	}
	
	@Override
	public Optional<boolean[]> preprocessed(int tick) {
		if(delay) {
			return Optional.of(new boolean[] {out});
		}
		return super.preprocessed(tick);
	}

	@Override
	protected void copyState(ShiftRegisterElement from) {
		this.delay = from.delay;
		this.sync = from.sync;
		this.size = from.size;
		this.out = from.out;
		this.value = from.value;
	}

	@Override
	public boolean statesEquals(IElement another) {
		return another instanceof ShiftRegisterElement
				&& ((ShiftRegisterElement)another).sync == this.sync
				&& ((ShiftRegisterElement)another).value == this.value
				&& ((ShiftRegisterElement)another).size == this.size
				&& ((ShiftRegisterElement)another).out == this.out
				&& ((ShiftRegisterElement)another).delay == this.delay;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		super.readData(data);
		try {
			this.delay = data.readBoolean();
			this.sync = data.readBoolean();
			this.size = data.readInt();
			this.value = data.readInt();
		}catch (Exception e) {
			DFModel.logException("Element read error", e);
		}
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(delay);
		data.writeBoolean(sync);
		data.writeInt(size);
		data.writeInt(value);
	}

}
