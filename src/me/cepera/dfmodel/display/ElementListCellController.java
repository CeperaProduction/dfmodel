package me.cepera.dfmodel.display;

import java.util.Optional;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import me.cepera.dfmodel.elements.IElementFactory;

public class ElementListCellController {

	@FXML
	private ResourceBundle resources;
	
	@FXML
	private Label label;
	
	@FXML
	private ImageView image;
	
	private IElementFactory element;
	
	public IElementFactory getElement() {
		return element;
	}
	
	public void initialize() {
		
	}
	
	public void postInitialize(IElementFactory element) {
		this.element = element;
		Optional<String> optElName = element.getSpecifiedName();
		label.setText(optElName.orElseGet(()->resources.getString(element.getNameLocaleKey())));
		image.setImage(element.getElementImage());
		
	}
	
}
