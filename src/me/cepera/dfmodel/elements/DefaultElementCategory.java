package me.cepera.dfmodel.elements;

import java.util.Optional;

public enum DefaultElementCategory implements IElementCategory {
	LOGICAL,
	DIGITAL_DATA_STORAGES,
	FUNCTIONS,
	SOURCES;
	
	private String id;
	
	private DefaultElementCategory() {
		id = name().toLowerCase();
	}

	@Override
	public String getIdentificator() {
		return id;
	}

	@Override
	public String getNameLocaleKey() {
		return "elements.category."+id;
	}

	@Override
	public Optional<String> getSpecifiedName() {
		return Optional.empty();
	}

}
