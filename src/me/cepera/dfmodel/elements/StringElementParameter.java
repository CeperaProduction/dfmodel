package me.cepera.dfmodel.elements;

import java.util.ResourceBundle;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import me.cepera.dfmodel.Scheme;

public abstract class StringElementParameter extends ElementParameterBase<String>{

	@Override
	public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
		String name = getSpecifiedName().orElse(resource.getString(getNameLocaleKey()));
		Label label = new Label(name+": ");
		TextField text = new TextField(getElementValue());
		text.textProperty().addListener((obs, oldVal, newVal)->{
			setElementValue(newVal);
			scheme.setChanged(true);
		});
		container.getChildren().addAll(label, text);
	}

}
