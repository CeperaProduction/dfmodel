package me.cepera.dfmodel.elements;

import java.util.Optional;

public abstract class ElementParameterBase<T> implements IElementParameter{

	@Override
	public String getNameLocaleKey() {
		return "elements.parameters."+getIdentificator();
	}

	@Override
	public Optional<String> getSpecifiedName() {
		return Optional.empty();
	}
	
	public abstract T getElementValue();
	
	public abstract void setElementValue(T value);
	
}
