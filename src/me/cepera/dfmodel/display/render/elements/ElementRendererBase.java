package me.cepera.dfmodel.display.render.elements;

import me.cepera.dfmodel.elements.IElement;

public abstract class ElementRendererBase<T extends IElement> implements IElementRenderer<T>{

	protected final T element;
	
	public ElementRendererBase(T element) {
		this.element = element;
	}
	
	@Override
	public T getElement() {
		return element;
	}
	
}
