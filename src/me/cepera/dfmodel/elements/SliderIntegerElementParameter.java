package me.cepera.dfmodel.elements;

import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import me.cepera.dfmodel.Scheme;

public abstract class SliderIntegerElementParameter implements IElementParameter{

	private TextField valueInput;

	@Override
	public String getIdentificator() {
		return "sized_logical_input";
	}
	
	@Override
	public String getNameLocaleKey() {
		return "elements.parameters."+getIdentificator();
	}

	@Override
	public Optional<String> getSpecifiedName() {
		return Optional.empty();
	}

	@Override
	public void onParametersWindowInit(ResourceBundle resource, Pane container, Scheme scheme) {
		VBox param = new VBox();
		String name = getSpecifiedName().orElse(resource.getString(getNameLocaleKey()));
		Label nameLabel = new Label(name);
		nameLabel.setMaxWidth(Double.MAX_VALUE);
		param.getChildren().add(nameLabel);
		BorderPane line = new BorderPane();
		Label sizeLabel = new Label(resource.getString(getNameLocaleKey()+".size")+": ");
		line.setLeft(sizeLabel);
		Slider sizeSlider = makeSlider(minSize(), maxSize(), getCurrentSize());
		line.setCenter(sizeSlider);
		param.getChildren().add(line);
		valueInput = new TextField(Integer.toBinaryString(getCurrentValue()));
		valueInput.textProperty().addListener((obs, ov, nv)->{
			String onv = nv;
			if (!nv.matches("[01]*")) {
				nv = nv.replaceAll("[^01]", "");
	        }
			if(nv.length() > getCurrentSize()) {
				nv = nv.substring(0, getCurrentSize());
			}
			if(!nv.equals(onv)) {
				final String fnv = nv;
				Platform.runLater(()->valueInput.setText(fnv));
			}
			if(!nv.equals(ov)) {
				int in = nv.isEmpty() ? 0 : Integer.parseUnsignedInt(nv, 2);
				onValueChange(in);
			}
		});
		sizeSlider.valueProperty().addListener((obs, oldVal, newVal)->{
			int n = ((Double) newVal).intValue();
			int o = ((Double) oldVal).intValue();
			if(n != o) {
				String currentText = valueInput.getText();
				if(n < currentText.length()) {
					currentText = currentText.substring(0, n);
					final String fct = currentText;
					Platform.runLater(()->valueInput.setText(fct));
					int in = currentText.isEmpty() ? 0 : Integer.parseUnsignedInt(currentText, 2);
					onValueChange(in);
				}
				onSizeChange(n);
				scheme.setChanged(true);
			}
		});
		Label logicalTableLabel = new Label(resource.getString(getNameLocaleKey()+".value")+": ");
		line = new BorderPane();
		line.setTop(logicalTableLabel);
		line.setCenter(valueInput);
		param.getChildren().add(line);
		container.getChildren().add(param);
		
	}
	
	private Slider makeSlider(int min, int max, int val) {
		Slider slider = new Slider(min, max, val);
        slider.setBlockIncrement(1.0);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setSnapToTicks(true);
        return slider;
	}
	
	public abstract int minSize();
	
	public abstract int maxSize();
	
	public abstract void onSizeChange(int newSize);
	
	public abstract void onValueChange(int newValue);
	
	public abstract int getCurrentSize();
	
	public abstract int getCurrentValue();

}
