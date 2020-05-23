package me.cepera.dfmodel.elements.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import me.cepera.dfmodel.Scheme;
import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.elements.ElementBase;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementFactory;
import me.cepera.dfmodel.elements.IElementParameter;
import me.cepera.dfmodel.elements.StringElementParameter;

public class LogicalFunctionElement extends ElementBase<LogicalFunctionElement>{

	private String functionName = "Func";
	private int inputs = 1;
	private int outputs = 1;
	private byte[] table = new byte[256];
	
	public LogicalFunctionElement(IElementFactory<LogicalFunctionElement> factory) {
		super(factory);
		IElementParameter nameParam = new StringElementParameter() {
			
			@Override
			public String getIdentificator() {
				return "function_name";
			}
			
			@Override
			public void setElementValue(String value) {
				functionName = value;
			}
			
			@Override
			public String getElementValue() {
				return functionName;
			}
		};
		parameters.add(nameParam);
		parameters.add(new FunctionParameters());
	}
	
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public int getInputCount() {
		return inputs;
	}

	@Override
	public int getOutputCount() {
		return outputs;
	}

	@Override
	public boolean[] process(boolean[] input, int tick) {
		int key = input[0] ? 1 : 0;
		for(int i = 1; i < input.length && i < inputs; i++) {
			key = key << 1;
			if(input[i]) key = key | 1;
		}
		System.out.println("\n"+Integer.toBinaryString(key));
		boolean[] result = new boolean[outputs];
		int val = table[key];
		int firstBitMask = 1 << (outputs-1);
		System.out.println(Integer.toBinaryString(val));
		for(int i = 0; i < result.length; i++) {
			result[i] = (val & firstBitMask) != 0;
			val = val << 1;
		}
		return result;
	}

	@Override
	public boolean statesEquals(IElement another) {
		if(another instanceof LogicalFunctionElement) {
			LogicalFunctionElement target = (LogicalFunctionElement) another;
			if(!target.functionName.equals(functionName)) return false;
			return true;
		}
		return false;
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		functionName = data.readUTF();
		inputs = data.readInt();
		outputs = data.readInt();
		byte[] table = new byte[1 << inputs];
		data.read(table);
		this.table = Arrays.copyOf(table, 256);
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		data.writeUTF(functionName);
		data.writeInt(inputs);
		data.writeInt(outputs);
		data.write(Arrays.copyOf(table, 1 << inputs));
	}

	@Override
	protected void copyState(LogicalFunctionElement from) {
		functionName = from.functionName;
		inputs = from.inputs;
		outputs = from.outputs;
		table = new byte[256];
		for(int i = 0; i < table.length && i < from.table.length; i++)
			table[i] = from.table[i];
	}
	
	private class FunctionParameters implements IElementParameter{				
		
		private BorderPane logicalTableContainer;
		
		@Override
		public String getIdentificator() {
			return "function_parameters";
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
			logicalTableContainer = new BorderPane();
			VBox param = new VBox();
			String name = getSpecifiedName().orElse(resource.getString(getNameLocaleKey()));
			Label nameLabel = new Label(name);
			nameLabel.setMaxWidth(Double.MAX_VALUE);
			param.getChildren().add(nameLabel);
			BorderPane line = new BorderPane();
			Label inputsLabel = new Label(resource.getString(getNameLocaleKey()+".inputs")+": ");
			line.setLeft(inputsLabel);
			Slider inputsSlider = makeSlider(1, 8, inputs);
			line.setCenter(inputsSlider);
			param.getChildren().add(line);
			line = new BorderPane();
			Label outputsLabel = new Label(resource.getString(getNameLocaleKey()+".outputs")+": ");
			line.setLeft(outputsLabel);
			Slider outputsSlider = makeSlider(1, 8, outputs);
			line.setCenter(outputsSlider);
			param.getChildren().add(line);
			inputsSlider.valueProperty().addListener((obs, oldVal, newVal)->{
				int n = ((Double) newVal).intValue();
				int o = ((Double) oldVal).intValue();
				if(n != o) {
					inputs = ((Double) newVal).intValue();
					if(n < o) {
						for(int i = 1 << n; i < (1 << o); i++) {
							table[i] = 0;
						}
					}
					initLogicalTable(scheme);
					scheme.checkAndCleanWires();
					scheme.setChanged(true);
				}
			});
			outputsSlider.valueProperty().addListener((obs, oldVal, newVal)->{
				int n = ((Double) newVal).intValue();
				int o = ((Double) oldVal).intValue();
				if(n != o) {
					outputs = ((Double) newVal).intValue();
					if(n > o) {
						for(int i = 0; i < table.length; i++) {
							table[i] = (byte) (table[i] << 1);
						}
					}else {
						for(int i = 0; i < table.length; i++) {
							table[i] = (byte) (table[i] >>> 1);
						}
					}
					initLogicalTable(scheme);
					scheme.checkAndCleanWires();
					scheme.setChanged(true);
				}
			});
			Label logicalTableLabel = new Label(resource.getString(getNameLocaleKey()+".logical_table")+": ");
			line = new BorderPane();
			line.setTop(logicalTableLabel);
			line.setCenter(logicalTableContainer);
			param.getChildren().add(line);
			initLogicalTable(scheme);
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
		
		private void initLogicalTable(Scheme scheme) {
			if(logicalTableContainer != null) {
				logicalTableContainer.getChildren().clear();
			}
			logicalTableContainer.setCenter(makeLogicalTable(scheme));
		}
		
		private TableView<LogicalTableElement> makeLogicalTable(Scheme scheme) {
			int inputs = LogicalFunctionElement.this.inputs;
			int outputs = LogicalFunctionElement.this.outputs;
			TableView<LogicalTableElement> table = new TableView<>();
			ObservableList<LogicalTableElement> rows = FXCollections.observableArrayList();
			for(int i = 0; i < (1 << inputs); i++) {
				rows.add(new LogicalTableElement(i, inputs, outputs, scheme));
			}
			ObservableList<TableColumn<LogicalTableElement, Boolean>> columns = FXCollections.observableArrayList();
			{
				TableColumn<LogicalTableElement, Boolean> col = new TableColumn<>("IN:");
				columns.add(col);
			}
			for(int i = 0; i < inputs; i++) {
				final int index = i;
				TableColumn<LogicalTableElement, Boolean> col = new TableColumn<>(index+1+"");
				col.setCellFactory(c ->{
					CheckBoxTableCell<LogicalTableElement, Boolean> cell = 
							new CheckBoxTableCell<LogicalTableElement, Boolean>();
					cell.setEditable(false);
					return cell;
				});
				col.setCellValueFactory(df -> df.getValue().inputValProps[index]);
				columns.add(col);
			}
			{
				TableColumn<LogicalTableElement, Boolean> col = new TableColumn<>("OUT:");
				columns.add(col);
			}
			for(int i = 0; i < outputs; i++) {
				final int index = i;
				TableColumn<LogicalTableElement, Boolean> col = new TableColumn<>(index+1+"");
				col.setCellFactory(c ->new CheckBoxTableCell<LogicalTableElement, Boolean>());
				col.setCellValueFactory(df -> {
					SimpleBooleanProperty prop = df.getValue().outputValProps[index];
					prop.addListener((obs, ov, nv)->{
						table.requestFocus();
						table.getSelectionModel().select(df.getValue().row);
						table.getFocusModel().focus(df.getValue().row);
					});
					return prop;
				});
				columns.add(col);
			}
			table.getColumns().addAll(columns);
			table.getItems().addAll(rows);
			table.setPrefWidth(400);
			table.setMaxWidth(400);
			table.setEditable(true);
			return table;
		}
		
		private class LogicalTableElement{
			int row, inputs, outputs;
			SimpleBooleanProperty[] inputValProps;
			SimpleBooleanProperty[] outputValProps;
			
			LogicalTableElement(int row, int inputs, int outputs, Scheme scheme){
				this.row = row;
				this.inputs = inputs;
				this.outputs = outputs;
				inputValProps = new SimpleBooleanProperty[inputs];
				outputValProps = new SimpleBooleanProperty[outputs];
				for(int i = 0; i < inputs; i++) {
					inputValProps[i] = new SimpleBooleanProperty(getCurrentInputValue(i));
				}
				for(int i = 0; i < outputs; i++) {
					outputValProps[i] = new SimpleBooleanProperty(getCurrentOutputValue(i));
					final int index = i;
					outputValProps[i].addListener((obs, oldVal, newVal)->{
						setOutputValue(index, newVal);
						scheme.setChanged(true);
					});
				}
			}
			
			boolean getCurrentInputValue(int index) {
				int key = row;
				int mask = 1 << (inputs - index - 1);
				return (key & mask) > 0;
			}
			
			boolean getCurrentOutputValue(int index) {
				int line = table[row];
				int mask = 1 << (outputs - index - 1);
				return (line & mask) > 0;
			}
			
			void setOutputValue(int index, boolean value) {
				int line = table[row];
				int mask = 1 << (outputs - index - 1);
				if(value) table[row] = (byte) (line | mask);
				else table[row] = (byte) (line & ~mask);
			}
		}
		
	}

}
