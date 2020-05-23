package me.cepera.dfmodel.display;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;
import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.ResourceHelper;
import me.cepera.dfmodel.elements.IElementFactory;

public class ElementListCell extends ListCell<IElementFactory>{
	
	private MainController mainController;
	
	public ElementListCell(MainController mainController) {
		this.mainController = mainController;
	}
	
	@Override
	protected void updateItem(IElementFactory item, boolean empty) {
		super.updateItem(item, empty);
		setText(null);
		setContextMenu(null);
		Node root = null;
		if(!empty) {
			try {
				FXMLLoader loader = new FXMLLoader(ResourceHelper.getResourceURL("fxml/ElementListCell.fxml"),
						mainController.getBundle());
				root = loader.load();
				((ElementListCellController)loader.getController()).postInitialize(item);
				this.setOnMouseClicked(e->onClicked(e, item));
			} catch (IOException e) {
				DFModel.logException("Failed to load element list cell", e);
			}
		}
		setGraphic(root);
	}
	
	public void onClicked(MouseEvent e, IElementFactory factory) {
		if(mainController.handleNoSimulation()) {
			DFModel.getCurrentScheme().createElement(factory, 
					mainController.schemeCanvasMouseX,
					mainController.schemeCanvasMouseY);
			mainController.updateEditMenuState();
			mainController.getSchemeCanvas().requestFocus();
		}
	}
	
}
