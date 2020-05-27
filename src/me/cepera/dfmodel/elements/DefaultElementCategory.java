package me.cepera.dfmodel.elements;

import java.util.Optional;

public enum DefaultElementCategory implements IElementCategory {
	FUNCTIONS,
	DIGITAL_DATA_STORAGES,
	BLOCKS_AND_TRIGGERS,
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
