package me.cepera.dfmodel.elements;

import java.util.Optional;

import javafx.scene.image.Image;
import me.cepera.dfmodel.ResourceHelper;

public abstract class ElementFactoryBase<T extends IElement> implements IElementFactory<T>{


	@Override
	public String getNameLocaleKey() {
		return "elements."+getIdentificator();
	}

	@Override
	public Optional<String> getSpecifiedName() {
		return Optional.empty();
	}
	
	@Override
	public Image getElementImage() {
		return ResourceHelper.getOrLoadImage(getIdentificator()+".png");
	}

}
