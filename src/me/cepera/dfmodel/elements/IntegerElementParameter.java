package me.cepera.dfmodel.elements;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import me.cepera.dfmodel.Scheme;

public abstract class IntegerElementParameter extends ElementParameterBase<Integer>{

	@Override
	public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
		String name = getSpecifiedName().orElse(resource.getString(getNameLocaleKey()));
		Label label = new Label(name+": ");
		TextField text = new TextField(getElementValue()+"");
		text.textProperty().addListener((obs, oldVal, newVal)->{
			String onv = newVal;
			String sign = signed() && onv.startsWith("-") ? "-" : "";
			newVal = newVal.substring(sign.length());
			if (!newVal.matches("\\d*")) {
				newVal = newVal.replaceAll("[^\\d]", "");
	        }
			if(newVal.length() > 9) {
				newVal = newVal.substring(0, 9);
			}
			newVal = sign + newVal;
			if(!newVal.equals(onv)) {
				final String nv = newVal;
				Platform.runLater(()->text.setText(nv));
			}
			int v = newVal.isEmpty() ? 0 : Integer.parseInt(newVal);
			setElementValue(v);
			scheme.setChanged(true);
		});
		container.getChildren().addAll(label, text);
	}
	
	public abstract boolean signed();

}
