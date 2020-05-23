package me.cepera.dfmodel.elements;

import java.util.Optional;
import java.util.function.Supplier;

import javafx.scene.image.Image;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;

public interface IElementFactory<T extends IElement> extends Supplier<T>{
	
	public String getIdentificator();
	
	public IElementCategory getCategory();

	public String getNameLocaleKey();
	
	public Optional<String> getSpecifiedName();
	
	public Image getElementImage();
	
	public IElementRenderer<T> createRenderer(T element);
	
	public T makeElement();
	
	@Override
	@Deprecated
	default T get() {
		return makeElement();
	}
	
}
