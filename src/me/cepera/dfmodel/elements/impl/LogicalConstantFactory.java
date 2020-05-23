package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalConstantElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalConstantFactory extends ElementFactoryBase<LogicalConstantElement>{

	@Override
	public String getIdentificator() {
		return "logical_constant";
	}

	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.SOURCES;
	}

	@Override
	public IElementRenderer<LogicalConstantElement> createRenderer(LogicalConstantElement element) {
		return new LogicalConstantElementRenderer(element);
	}

	@Override
	public LogicalConstantElement makeElement() {
		return new LogicalConstantElement(this);
	}

}
