package me.cepera.dfmodel.elements;

import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;
import me.cepera.dfmodel.Scheme;

public abstract class BooleanElementParameter extends ElementParameterBase<Boolean>{

	private ChangeListener<Boolean> disableChangeListener;
	private ChangeListener<Boolean> checkedChangeListener;
	
	protected CheckBox check;
	
	@Override
	public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
		String name = getSpecifiedName().orElse(resource.getString(getNameLocaleKey()));
		check = new CheckBox(name);
		check.setSelected(getElementValue());
		check.selectedProperty().addListener((obs, oldVal, newVal)->{
			setElementValue(newVal);
			scheme.setChanged(true);
		});
		if(disabled()) check.setDisable(true);
		if(disableChangeListener != null) check.disableProperty().addListener(disableChangeListener);
		if(checkedChangeListener != null) check.selectedProperty().addListener(checkedChangeListener);
		container.getChildren().add(check);
	}
	
	protected boolean disabled() {
		return false;
	}
	
	public void setDisableChangeListener(ChangeListener<Boolean> disableChangeListener) {
		this.disableChangeListener = disableChangeListener;
	}
	
	public void setCheckedChangeListener(ChangeListener<Boolean> checkedChangeListener) {
		this.checkedChangeListener = checkedChangeListener;
	}

}
