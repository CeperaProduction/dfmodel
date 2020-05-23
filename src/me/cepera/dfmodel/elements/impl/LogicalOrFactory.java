package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalOrElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalOrFactory extends ElementFactoryBase<LogicalOrElement>{

	@Override
	public String getIdentificator() {
		return "logical_or";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.LOGICAL;
	}

	@Override
	public IElementRenderer<LogicalOrElement> createRenderer(LogicalOrElement element) {
		return new LogicalOrElementRenderer(element);
	}

	@Override
	public LogicalOrElement makeElement() {
		return new LogicalOrElement(this);
	}

}
