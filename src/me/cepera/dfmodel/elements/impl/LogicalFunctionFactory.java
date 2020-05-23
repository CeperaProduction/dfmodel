package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalFunctionElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalFunctionFactory extends ElementFactoryBase<LogicalFunctionElement>{

	@Override
	public String getIdentificator() {
		return "logical_function";
	}

	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.FUNCTIONS;
	}

	@Override
	public IElementRenderer<LogicalFunctionElement> createRenderer(LogicalFunctionElement element) {
		return new LogicalFunctionElementRenderer(element);
	}

	@Override
	public LogicalFunctionElement makeElement() {
		return new LogicalFunctionElement(this);
	}

}
