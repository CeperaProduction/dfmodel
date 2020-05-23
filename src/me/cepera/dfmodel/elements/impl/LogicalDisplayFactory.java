package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalDisplayElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalDisplayFactory extends ElementFactoryBase<LogicalDisplayElement>{

	@Override
	public String getIdentificator() {
		return "logical_display";
	}

	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.DIGITAL_DATA_STORAGES;
	}

	@Override
	public IElementRenderer<LogicalDisplayElement> createRenderer(LogicalDisplayElement element) {
		return new LogicalDisplayElementRenderer(element);
	}

	@Override
	public LogicalDisplayElement makeElement() {
		return new LogicalDisplayElement(this);
	}

}
