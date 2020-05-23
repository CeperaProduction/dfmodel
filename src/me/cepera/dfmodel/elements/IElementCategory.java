package me.cepera.dfmodel.elements;

import java.util.Optional;

public interface IElementCategory {

	public String getIdentificator();

	public String getNameLocaleKey();
	
	public Optional<String> getSpecifiedName();
	
}
