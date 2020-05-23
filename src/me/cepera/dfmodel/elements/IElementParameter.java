package me.cepera.dfmodel.elements;

import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.layout.Pane;
import me.cepera.dfmodel.Scheme;

public interface IElementParameter {

	public String getIdentificator();
	
	public String getNameLocaleKey();
	
	public Optional<String> getSpecifiedName();
	
	public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme);
	
}
