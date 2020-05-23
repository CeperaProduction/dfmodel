package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalNotElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalNotFactory extends ElementFactoryBase<LogicalNotElement>{

	@Override
	public String getIdentificator() {
		return "logical_not";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.LOGICAL;
	}

	@Override
	public IElementRenderer<LogicalNotElement> createRenderer(LogicalNotElement element) {
		return new LogicalNotElementRenderer(element);
	}

	@Override
	public LogicalNotElement makeElement() {
		return new LogicalNotElement(this);
	}

}
