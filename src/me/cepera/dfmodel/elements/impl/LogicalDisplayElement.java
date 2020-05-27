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

public class LogicalDisplayElement extends ElementBase<LogicalDisplayElement>{

	private boolean value;
	private boolean out;
	private boolean delay;
	
	public LogicalDisplayElement(IElementFactory<LogicalDisplayElement> factory) {
		super(factory);
		registerPriorityParam();
		parameters.add(new BooleanElementParameter() {
			@Override
			public String getIdentificator() {
				return "constant_value";
			}
			
			@Override
			public void setElementValue(Boolean value) {
				LogicalDisplayElement.this.value = value;
			}
			
			@Override
			public Boolean getElementValue() {
				return value;
			}
		});
		BooleanElementParameter outParam = new BooleanElementParameter() {
			
			@Override
			public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
				super.onParametersWindowInit(resource, container, scheme);
				check.selectedProperty().addListener((obs,ov,nv)->{
					scheme.checkAndCleanWires();
				});
			}
			
			@Override
			public String getIdentificator() {
				return "logical_display_out";
			}
			
			@Override
			public void setElementValue(Boolean value) {
				out = value;
			}
			
			@Override
			public Boolean getElementValue() {
				return out;
			}
		};
		parameters.add(outParam);
		parameters.add(new BooleanElementParameter() {
			{
				outParam.setCheckedChangeListener((obs, ov, nv)->{
					updateStatus(nv);
				});
			}
			
			private void updateStatus(boolean outVal) {
				if(!outVal) {
					check.setSelected(false);
					check.setDisable(true);
				}else {
					check.setDisable(false);
				}
			}
			
			@Override
			public String getIdentificator() {
				return "tick_delay";
			}

			@Override
			public Boolean getElementValue() {
				return out && delay;
			}
			
			@Override
			protected boolean disabled() {
				return !out;
			}

			@Override
			public void setElementValue(Boolean value) {
				delay = value;
			}
		});
	}
	
	public boolean getValue() {
		return value;
	}
	
	public boolean hasOut() {
		return out;
	}
	
	public boolean hasDelay() {
		return delay;
	}

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getOutputCount() {
		return out ? 1 : 0;
	}

	@Override
	public boolean[] process(boolean[] input, int tick) {
		boolean v = value;
		value = input[0];
		if(!delay) v = value;
		return out ? new boolean[] {v} : new boolean[0];
	}
	
	@Override
	public Optional<boolean[]> preprocessed(int tick) {
		if(out && delay) {
			return Optional.of(new boolean[] {value});
		}
		return super.preprocessed(tick);
	}

	@Override
	public boolean statesEquals(IElement another) {
		if(another instanceof LogicalDisplayElement) {
			LogicalDisplayElement target = (LogicalDisplayElement) another;
			return (target.value == value) && (target.out == out) && (target.delay == delay);
		}
		return false;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		super.readData(data);
		try {
			out = data.readBoolean();
			delay = data.readBoolean();
			value = data.readBoolean();
		}catch(Exception e) {
			DFModel.logException("Element read error", e);
		}
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		super.writeData(data);
		data.writeBoolean(out);
		data.writeBoolean(delay);
		data.writeBoolean(value);
	}

	@Override
	protected void copyState(LogicalDisplayElement from) {
		value = from.value;
		out = from.out;
		delay = from.delay;
	}

}
