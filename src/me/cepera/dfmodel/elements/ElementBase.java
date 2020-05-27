package me.cepera.dfmodel.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;

public abstract class ElementBase<T extends ElementBase> implements IElement{

	protected final IElementFactory<T> factory;
	protected final IElementRenderer<T> renderer;
	
	protected final List<IElementParameter> parameters;
	
	private IntegerElementParameter priorityParam;
	protected int priority;
	
	public ElementBase(IElementFactory<T> factory) {
		this.factory = factory;
		this.renderer = factory.createRenderer((T) this);
		this.parameters = new ArrayList<>();
		
	}
	
	protected void registerPriorityParam() {
		if(priorityParam == null) {
			this.parameters.add(priorityParam = new IntegerElementParameter() {
				@Override
				public String getIdentificator() {
					return "priority";
				}
				
				@Override
				public void setElementValue(Integer value) {
					priority = value;
				}
				
				@Override
				public Integer getElementValue() {
					return priority;
				}
				
				@Override
				public boolean signed() {
					return true;
				}
			});
		}
	}
	
	@Override
	public IElementFactory<T> getFactory() {
		return factory;
	}
	
	@Override
	public IElementRenderer<T> getRenderer() {
		return renderer;
	}
	
	@Override
	public final void copyState(IElement from) {
		T target = (T) from;
		priority = target.priority;
		copyState((T) target);
	}
	
	protected abstract void copyState(T from);
	
	@Override
	public List<IElementParameter> getParameters() {
		return parameters;
	}
	
	@Override
	public Optional<boolean[]> preprocessed(int tick) {
		return Optional.empty();
	}
	
	@Override
	public void onSimulationStart() {}
	
	@Override
	public int getPriority() {
		return priority;
	}
	
	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		if(priorityParam != null) data.writeInt(priority);
	}
	
	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		if(priorityParam != null) priority = data.readInt();
	}
	
}
