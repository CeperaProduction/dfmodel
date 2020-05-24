package me.cepera.dfmodel.display;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.ElementRegistry;
import me.cepera.dfmodel.FileSaveTools;
import me.cepera.dfmodel.ResourceHelper;
import me.cepera.dfmodel.Scheme;
import me.cepera.dfmodel.Scheme.ElementContainer;
import me.cepera.dfmodel.Scheme.SchemeClickAction;
import me.cepera.dfmodel.Scheme.SchemeClickType;
import me.cepera.dfmodel.Scheme.Wire;
import me.cepera.dfmodel.Simulation;
import me.cepera.dfmodel.display.render.SchemeCanvasRenderer;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementCategory;
import me.cepera.dfmodel.elements.IElementFactory;
import me.cepera.dfmodel.elements.IElementParameter;

public class MainController {
	
	private Stage stage;
	private AnimationTimer animationTimer;
	
	@FXML
	private ResourceBundle resources;
	
	@FXML
	private Canvas schemeCanvas;
	
	@FXML 
	private ScrollPane schemeCanvasContainer;
	
	public double schemeCanvasMouseX, schemeCanvasMouseY;
	
	@FXML
	private Accordion elements;
	
	public FileChooser dfmFileChooser;
	
	public File editingFile;
	
	@FXML
	private MenuItem editMenuRemove,
					editMenuRotate,
					editMenuRotateLeft,
					editMenuDuplicate,
					editMenuParameters;
	
	private ContextMenu contextMenu;
	
	private Stage simulationStage;
	
	public ResourceBundle getBundle() {
		return resources;
	}
	
	public void initialize() {
		ExtensionFilter dfmsFilter = new ExtensionFilter(resources.getString("format.dfms"), "*.dfms");
		dfmFileChooser = new FileChooser();
		dfmFileChooser.getExtensionFilters().clear();
		dfmFileChooser.getExtensionFilters().add(dfmsFilter);
		dfmFileChooser.setInitialDirectory(new File("."));
		dfmFileChooser.setInitialFileName(resources.getString("window.subtitle.untitled_scheme")+".dfms");
		
		for(IElementCategory category : ElementRegistry.getCategories()) {
			ListView<IElementFactory> list = new ListView<IElementFactory>();
			ObservableList<IElementFactory> contentList = FXCollections.observableArrayList();
			contentList.addAll(ElementRegistry.getFactories(category));
			list.setCellFactory(l->new ElementListCell(this));
			list.focusedProperty().addListener((ob, o, n)->{
				if(!n) list.getSelectionModel().clearSelection();
			});
			list.setItems(contentList);
			Optional<String> optElName = category.getSpecifiedName();
			elements.getPanes().add(new TitledPane(optElName.orElseGet(()->resources.getString(category.getNameLocaleKey())), list));
		}
		
		updateEditMenuState();
		
		contextMenu = new ContextMenu();
        contextMenu.setHideOnEscape(true);
        contextMenu.getItems().addAll(makeBindedMenuItemClone(editMenuParameters),
				new SeparatorMenuItem(),
				makeBindedMenuItemClone(editMenuRotate),
				makeBindedMenuItemClone(editMenuRotateLeft),
				new SeparatorMenuItem(),
				makeBindedMenuItemClone(editMenuDuplicate),
				makeBindedMenuItemClone(editMenuRemove));
        
		schemeCanvas.setFocusTraversable(true);
		schemeCanvas.setOnMouseMoved(this::onMouseMovedOverCanvas);
		schemeCanvas.setOnMouseClicked(this::onCanvasClick);
		schemeCanvas.setOnKeyReleased(this::onKeyTypedInCanvas);
		schemeCanvasContainer.heightProperty().addListener((obs, ov, nv)->{
			double n = DFModel.getCurrentScheme().getSchemeBounds()[3];
			n = Math.max(((Double)nv).doubleValue() * 2, n);
			schemeCanvas.setHeight(n);
		});
		schemeCanvasContainer.widthProperty().addListener((obs, ov, nv)->{
			double n = DFModel.getCurrentScheme().getSchemeBounds()[2];
			n = Math.max(((Double)nv).doubleValue() * 2, n);
			schemeCanvas.setWidth(n);
		});
		DFModel.getShemeRenderer().bindCanvas(schemeCanvas);
		
		DFModel.LOGGER.info("Main window contoller initialized.");
	}
	
	public void updateEditMenuState() {
		Optional<ElementContainer> selectedElement = DFModel.getCurrentScheme().getSelectedElement();
		Optional<Wire> selectedWire = DFModel.getCurrentScheme().getSelectedWire();
		boolean elementPresent = selectedElement.isPresent();
		editMenuRemove.setDisable(!elementPresent && !selectedWire.isPresent());
		editMenuRotate.setDisable(!elementPresent);
		editMenuRotateLeft.setDisable(!elementPresent);
		editMenuParameters.setDisable(!elementPresent || selectedElement.get().getElement().getParameters().isEmpty());
		editMenuDuplicate.setDisable(!elementPresent || DFModel.getCurrentScheme().isDrag());
	}
	
	public void postInitialize(Stage stage) {
		this.stage = stage;
		Platform.setImplicitExit(true);
		stage.setOnCloseRequest(e->{
			if(!handleUnsaved()) {
				e.consume();
			}
		});
		stage.setTitle(makeWindowTitle());
		stage.getIcons().add(ResourceHelper.ICON);
		stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::onKeyTypedInScene);
		animationTimer = new AnimationTimer() {
			long last;
			SchemeCanvasRenderer renderer = DFModel.getShemeRenderer();
			boolean lastChangedState = false;
			File lastEditFile = null;
			@Override
			public void handle(long now) {
				long prev = last;
				last = now;
				if(!stage.isIconified()) {
					Platform.runLater(()->{
						renderer.render(stage, schemeCanvasMouseX, schemeCanvasMouseY, now, prev);
					});
				}
				boolean changed = DFModel.getCurrentScheme().isChanged();
				if(lastEditFile != editingFile || changed != lastChangedState) {
					stage.setTitle(makeWindowTitle());
					lastChangedState = changed;
					lastEditFile = editingFile;
				}
			}
		};
		animationTimer.start();
		
		DFModel.LOGGER.info("Scheme render loop started");
	}
	
	
	
	public Canvas getSchemeCanvas() {
		return schemeCanvas;
	}
	
	public String makeWindowTitle() {
		String title = resources.getString("window.title");
		String subtitle = editingFile != null ? editingFile.getName() : resources.getString("window.subtitle.untitled_scheme");
		if(DFModel.getCurrentScheme().isChanged()) subtitle = "*"+subtitle;
		return subtitle+" - "+title;
	}
	
	public void loadFromFile(File file) {
		if(handleUnsaved()) {
			try {
				DFModel.setScheme(FileSaveTools.loadScheme(file));
				updateEditMenuState();
			} catch (IOException e) {
				DFModel.logException("File load failed.", e);
				MainWindow.showError(e);
			}
		}
	}
	
	public void saveToFile(File file) {
		Scheme scheme = DFModel.getCurrentScheme();
		try {
			FileSaveTools.saveScheme(scheme, file);
			scheme.setChanged(false);
		} catch (IOException e) {
			DFModel.logException("File save failed.", e);
			MainWindow.showError(e);
		}
	}
	
	public void onNewFileClick(ActionEvent e) {
		if(handleUnsaved()) {
			editingFile = null;
			DFModel.createNewScheme();
			updateEditMenuState();
		}
	}
	
	public Stage getSimulationStage() {
		return simulationStage;
	}
	
	public void setSimulationStage(Stage simulationStage) {
		this.simulationStage = simulationStage;
	}
	
	public void onFileOpenClick(ActionEvent e) {
		requestSchemeFile(this::loadFromFile, false);
	}
	
	public void onFileSaveClick(ActionEvent e) {
		saveScheme();
	}
	
	public boolean saveScheme() {
		if(editingFile == null) return requestSchemeFile(this::saveToFile, true);
		saveToFile(editingFile);
		return true;
	}
	
	public void onFileSaveAsClick(ActionEvent e) {
		requestSchemeFile(this::saveToFile, true);
	}
	
	public void onExitClick(ActionEvent e) {
		if(handleUnsaved()) {
			Platform.exit();
		}
	}
	
	public void onMouseMovedOverCanvas(MouseEvent e) {
		schemeCanvasMouseX = e.getX();
		schemeCanvasMouseY = e.getY();
		DFModel.getCurrentScheme().handleMouseMove(e.getX(), e.getY());
	}
	
	public void onCanvasClick(MouseEvent e) {
		MouseButton button = e.getButton();
		if(button == MouseButton.NONE) return;
		if (contextMenu.isShowing()) {
            contextMenu.hide();
        }
		schemeCanvas.requestFocus();
		SchemeClickAction action = DFModel.getCurrentScheme().getMouseClickAction(e.getX(), e.getY(), button.ordinal()-1);
		switch(action.getActionType()) {
		case DRAG_START: case DRAG_STOP: case WIRING:
			if(!handleNoSimulation()) break;
		default:
			action.run();
			updateEditMenuState();
			if(action.getActionType() == SchemeClickType.SELECT && button == MouseButton.SECONDARY) {
				contextMenu.show(schemeCanvas, e.getScreenX()+2, e.getScreenY()+2);
			}
		}
	}
	
	public void onKeyTypedInCanvas(KeyEvent e) {
		Scheme scheme = DFModel.getCurrentScheme();
		switch(e.getCode()) {
		case ESCAPE:
			scheme.deselect();
			updateEditMenuState();
			break;
		case R:
			if(handleNoSimulation()) scheme.handleRotate(e.isControlDown());
			break;
		case DELETE:
			if(handleNoSimulation()) {
				scheme.deleteSelectedElement();
				scheme.deleteSelectedWire();
				updateEditMenuState();
			}
			break;
		case C:
			if(e.isControlDown()
					&& !scheme.isDrag()
					&& scheme.getSelectedElement().isPresent()
					&& handleNoSimulation()) {
				scheme.duplicateSelectedElement(schemeCanvasMouseX, schemeCanvasMouseY);
			}
			break;
		default:
		}
	}
	
	public void onKeyTypedInScene(KeyEvent e) {
		switch(e.getCode()) {
		case F3:
			doSimulationStep();
			break;
		case F2:
			showSimulationWindow();
			break;
		case F5:
			if(DFModel.getCurrentSimulation().isPresent()) {
				DFModel.endSimulation();
			}
		case S:
			if(e.isControlDown()) {
				if(e.isShiftDown()) {
					requestSchemeFile(this::saveToFile, true);
				}else saveScheme();
			}
			break;
		case O:
			if(e.isControlDown() && handleUnsaved()) {
				requestSchemeFile(this::loadFromFile, false);
			}
			break;
		case N:
			if(e.isControlDown() && handleUnsaved()) {
				editingFile = null;
				DFModel.createNewScheme();
				updateEditMenuState();
			}
			break;
		default:
		}
	}
	
	public void onClickEditMenuParameters(ActionEvent e) {
		if(!handleNoSimulation()) return;
		Optional<ElementContainer> optContainer = DFModel.getCurrentScheme().getSelectedElement();
		if(!optContainer.isPresent()) return;
		ElementContainer container = optContainer.get();
		IElement element = container.getElement();
		List<IElementParameter> parameters = element.getParameters();
		if(parameters.isEmpty()) return;
		
		Stage window = new Stage();
		window.initOwner(stage);
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(resources.getString("window.parameters.title"));
		window.getIcons().add(ResourceHelper.ICON);
		window.setResizable(false);
		BorderPane root = new BorderPane();
		VBox content = new VBox();
		content.setPadding(new Insets(10));
		Scene scene = new Scene(root);
		window.setScene(scene);
		boolean f = true;
		for(IElementParameter p : parameters) {
			if(!f) {
				Separator sep = new Separator();
				sep.setPadding(new Insets(5, 0, 5, 0));
				content.getChildren().add(sep);
			}
			else f = false;
			FlowPane flow = new FlowPane();
			p.onParametersWindowInit(resources, flow, DFModel.getCurrentScheme());
			content.getChildren().add(flow);
		}
		Button close = new Button(resources.getString("buttons.close"));
		close.setOnAction(ae->{
			window.close();
		});
		close.setMaxWidth(Double.MAX_VALUE);
		root.setBottom(close);
		root.setCenter(content);
		window.showAndWait();
	}
	
	public void onClickEditMenuRotate(ActionEvent e) {
		if(handleNoSimulation()) {
			DFModel.getCurrentScheme().getSelectedElement().ifPresent(el->el.rotate(false));
		}
	}
	
	public void onClickEditMenuRotateLeft(ActionEvent e) {
		if(handleNoSimulation()) {
			DFModel.getCurrentScheme().getSelectedElement().ifPresent(el->el.rotate(true));
		}
	}
	
	public void onClickEditMenuRemove(ActionEvent e) {
		if(handleNoSimulation()) {
			DFModel.getCurrentScheme().deleteSelectedElement();
			DFModel.getCurrentScheme().deleteSelectedWire();
			updateEditMenuState();
		}
	}
	
	public void onClickEditMenuDuplicate(ActionEvent e) {
		Scheme scheme = DFModel.getCurrentScheme();
		if(!scheme.isDrag()
				&& scheme.getSelectedElement().isPresent()
				&& handleNoSimulation()) {
			scheme.duplicateSelectedElement(schemeCanvasMouseX, schemeCanvasMouseY);
		}
	}
	
	public void onClickSimulationMenuSimulation(ActionEvent e) {
		showSimulationWindow();
	}
	
	public void showSimulationWindow() {
		if(!DFModel.getCurrentSimulation().isPresent()) {
			DFModel.createNewSimulation();
		}
		if(simulationStage == null) {
			try {
				Stage stage = new Stage();
				FXMLLoader loader = new FXMLLoader(ResourceHelper.getResourceURL("fxml/SimulationWindow.fxml"), resources);
				loader.setCharset(ResourceHelper.CHARSET);
				BorderPane root = (BorderPane)loader.load();
				Scene scene = new Scene(root);
				scene.getStylesheets().add(ResourceHelper.getResourceURL("css/application.css").toExternalForm());
				stage.setScene(scene);
				stage.setTitle(resources.getString("window.simulation.title"));
				stage.getIcons().add(ResourceHelper.ICON);
				stage.setResizable(false);
				((SimulationController)loader.getController()).postInitialize(stage, this);
				stage.sizeToScene();
				stage.initOwner(this.stage);
				simulationStage = stage;
			} catch(Exception ex) {
				DFModel.logException("Simulation window initialization failed", ex);
			}
		}
		simulationStage.show();
		simulationStage.toFront();
	}
	
	public void onClickSimulationMenuOneStep(ActionEvent e) {
		doSimulationStep();
	}
	
	public void onClickSimulationMenuEnd(ActionEvent e) {
		if(DFModel.getCurrentSimulation().isPresent()) {
			DFModel.endSimulation();
		}
	}
	
	private void doSimulationStep() {
		if(!DFModel.getCurrentSimulation().isPresent()) {
			DFModel.createNewSimulation();
		}
		Simulation s = DFModel.getCurrentSimulation().get();
		if(s.isRunning()) showSimulationWindow();
		else DFModel.getCurrentSimulation().get().start(1, 0);
	}
	
	public void closeSimulationWindow() {
		simulationStage.hide();
	}
	
	private boolean requestSchemeFile(Consumer<File> callBack, boolean save) {
		try {
    		File file = save ? dfmFileChooser.showSaveDialog(stage) : dfmFileChooser.showOpenDialog(stage);
			if(file != null) {
				if(save || file.isFile()) {
					dfmFileChooser.setInitialDirectory(file.getParentFile());
					dfmFileChooser.setInitialFileName(file.getName());
					callBack.accept(file);
					editingFile = file;
					return true;
				}else {
					DFModel.LOGGER.warning("Target is not a file.");
				}
			}
		} catch (Exception ex) {
			Exception ex2 = new Exception("Can't "+(save ? "save" : "open")+" file.", ex);
			DFModel.logException(ex2.getMessage(), ex);
			MainWindow.showError(ex2);
		}
		return false;
	}
	
	boolean handleUnsaved() {
		if(DFModel.getCurrentScheme().isChanged()) {
			ButtonType buttonYes = new ButtonType(resources.getString("buttons.yes"), ButtonData.YES);
			ButtonType buttonNo = new ButtonType(resources.getString("buttons.no"), ButtonData.NO);
			ButtonType buttonCancel = new ButtonType(resources.getString("buttons.cancel"), ButtonData.CANCEL_CLOSE);
			Alert question = new Alert(AlertType.WARNING, resources.getString("messages.save_on_close"),
					buttonYes, buttonNo, buttonCancel);
			question.setTitle(resources.getString("window.dialog.title.confirm_on_close"));
			question.setHeaderText(null);
			question.showAndWait();
			if(question.getResult() == buttonYes) {
				return saveScheme();
			}
			if(question.getResult() == buttonNo) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	boolean handleNoSimulation() {
		if(DFModel.getCurrentSimulation().isPresent()) {
			showSimulationWindow();
			ButtonType buttonStop = new ButtonType(resources.getString("buttons.simulation.end"), ButtonData.YES);
			ButtonType buttonCancel = new ButtonType(resources.getString("buttons.cancel"), ButtonData.CANCEL_CLOSE);
			Alert question = new Alert(AlertType.WARNING, resources.getString("messages.change_on_simulation"),
					buttonStop, buttonCancel);
			question.setTitle(resources.getString("window.dialog.title.change_on_simulation"));
			question.setHeaderText(null);
			question.showAndWait();
			if(question.getResult() == buttonStop) {
				DFModel.endSimulation();
				return true;
			}
			return false;
		}
		return true;
	}
	
	private MenuItem makeBindedMenuItemClone(MenuItem item) {
		MenuItem newItem = new MenuItem(item.getText());
		newItem.setOnAction(item.getOnAction());
		newItem.disableProperty().bind(item.disableProperty());
		return newItem;
	}
	
}
