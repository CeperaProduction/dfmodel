package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.TTriggerElementRenderer;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class TTriggerFactory extends ElementFactoryBase<TTriggerElement>{

	@Override
	public String getIdentificator() {
		return "t_trigger";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.BLOCKS_AND_TRIGGERS;
	}

	@Override
	public IElementRenderer<TTriggerElement> createRenderer(TTriggerElement element) {
		return new TTriggerElementRenderer(element);
	}

	@Override
	public TTriggerElement makeElement() {
		return new TTriggerElement(this);
	}

}
