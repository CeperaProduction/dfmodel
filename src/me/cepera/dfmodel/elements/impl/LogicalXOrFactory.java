package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.LogicalXOrElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class LogicalXOrFactory extends ElementFactoryBase<LogicalXOrElement>{

	@Override
	public String getIdentificator() {
		return "logical_xor";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.LOGICAL;
	}

	@Override
	public IElementRenderer<LogicalXOrElement> createRenderer(LogicalXOrElement element) {
		return new LogicalXOrElementRenderer(element);
	}

	@Override
	public LogicalXOrElement makeElement() {
		return new LogicalXOrElement(this);
	}

}
