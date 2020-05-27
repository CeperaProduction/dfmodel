package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.DTriggerElementRenderer;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class DTriggerFactory extends ElementFactoryBase<DTriggerElement>{

	@Override
	public String getIdentificator() {
		return "d_trigger";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.BLOCKS_AND_TRIGGERS;
	}

	@Override
	public IElementRenderer<DTriggerElement> createRenderer(DTriggerElement element) {
		return new DTriggerElementRenderer(element);
	}

	@Override
	public DTriggerElement makeElement() {
		return new DTriggerElement(this);
	}

}
