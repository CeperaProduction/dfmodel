package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalAndElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalAndFactory extends ElementFactoryBase<LogicalAndElement>{

	@Override
	public String getIdentificator() {
		return "logical_and";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.LOGICAL;
	}

	@Override
	public IElementRenderer<LogicalAndElement> createRenderer(LogicalAndElement element) {
		return new LogicalAndElementRenderer(element);
	}

	@Override
	public LogicalAndElement makeElement() {
		return new LogicalAndElement(this);
	}

}
