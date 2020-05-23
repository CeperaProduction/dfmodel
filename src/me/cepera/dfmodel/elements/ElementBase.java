package me.cepera.dfmodel.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;

public abstract class ElementBase<T extends ElementBase> implements IElement{

	protected final IElementFactory<T> factory;
	protected final IElementRenderer<T> renderer;
	
	protected final List<IElementParameter> parameters;
	
	public ElementBase(IElementFactory<T> factory) {
		this.factory = factory;
		this.renderer = factory.createRenderer((T) this);
		this.parameters = new ArrayList<>();
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
		copyState((T) from);
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
	
}
