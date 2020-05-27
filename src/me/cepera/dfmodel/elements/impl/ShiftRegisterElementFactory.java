package me.cepera.dfmodel.elements.impl;

import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.display.render.elements.ShiftRegisterElementRenderer;
import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.ElementFactoryBase;
import me.cepera.dfmodel.elements.IElementCategory;

public class ShiftRegisterElementFactory extends ElementFactoryBase<ShiftRegisterElement>{

	@Override
	public String getIdentificator() {
		return "shift_register";
	}
	
	@Override
	public IElementCategory getCategory() {
		return DefaultElementCategory.BLOCKS_AND_TRIGGERS;
	}

	@Override
	public IElementRenderer<ShiftRegisterElement> createRenderer(ShiftRegisterElement element) {
		return new ShiftRegisterElementRenderer(element);
	}

	@Override
	public ShiftRegisterElement makeElement() {
		return new ShiftRegisterElement(this);
	}

}
