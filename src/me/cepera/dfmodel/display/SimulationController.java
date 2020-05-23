package me.cepera.dfmodel.display;

import java.util.Optional;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.Simulation;

public class SimulationController {

	@FXML
	private TextField ticksField;
	
	@FXML
	private TextField delaysField;
	
	@FXML
	private ProgressBar currentTaskBar;
	
	@FXML
	private ProgressBar allTasksBar;
	
	@FXML
	private Label currentTaskLabel;
	
	@FXML
	private Label allTasksLabel;
	
	@FXML
	private Button startButton;
	
	@FXML
	private Button stopButton;
	
	private MainController mainController;
	private AnimationTimer timer;
	
	public void initialize() {
		ticksField.textProperty().addListener((obs, oldVal, newVal)->{
			String onv = newVal;
			if (!newVal.matches("\\d*")) {
				newVal = newVal.replaceAll("[^\\d]", "");
	        }
			if(newVal.length() > 9) {
				newVal = newVal.substring(0, 9);
			}
			if(!newVal.equals(onv)) {
				final String nv = newVal;
				Platform.runLater(()->ticksField.setText(nv));
			}
		});
		delaysField.textProperty().addListener((obs, oldVal, newVal)->{
			String onv = newVal;
			if (!newVal.matches("\\d*\\.?(\\d){0,3}")) {
				newVal = newVal.replaceAll("[^\\d.]", "");
				int i = newVal.indexOf('.');
				if(i != -1) {
					String suffix = newVal.substring(i);
					if(suffix.length() > 4) suffix = suffix.substring(0, 4);
					newVal = newVal.substring(0, i)+suffix;
				}
				while(i != -1) {
					newVal = newVal.substring(0, i);
					if(i < newVal.length()-1) {
						newVal+=newVal.substring(i+1);
					}
					i = newVal.indexOf('.', i);
				}
	        }
			if(newVal.length() > 20) {
				newVal = newVal.substring(0, 10);
			}
			if(!newVal.equals(onv)) {
				final String nv = newVal;
				Platform.runLater(()->delaysField.setText(nv));
			}
		});
		stopButton.setDisable(true);
		
	}
	
	public void postInitialize(Stage stage, MainController mainController) {
		this.mainController = mainController;
		timer = new AnimationTimer() {
			boolean lastRunning;
			long lastUpdate = 0;
			@Override
			public void handle(long now) {
				Optional<Simulation> optSimulation = DFModel.getCurrentSimulation();
				if(optSimulation.isPresent()) {
					Simulation simulation = optSimulation.get();
					if(simulation.isRunning() != lastRunning) {
						lastRunning = simulation.isRunning();
						if(simulation.isRunning()) {
							startButton.setDisable(true);
							stopButton.setDisable(false);
							ticksField.setDisable(true);
							delaysField.setDisable(true);
						}else {
							startButton.setDisable(false);
							stopButton.setDisable(true);
							ticksField.setDisable(false);
							delaysField.setDisable(false);
						}
					}
					if(now - lastUpdate < 500000000) return;
					lastUpdate = now;
					int a = simulation.getTicks()-simulation.getLastStartTick();
					int b = simulation.getTargetTick()-simulation.getLastStartTick();
					String current = new StringBuilder().append('(').append(a)
							.append('/').append(b).append(')').toString();
					currentTaskLabel.setText(current);
					currentTaskBar.setProgress(b != 0 ? 1.0*a/b : 0);
					a = simulation.getTicks();
					b = simulation.getTargetTick();
					String all = new StringBuilder().append('(').append(a)
							.append('/').append(b).append(')').toString();
					allTasksLabel.setText(all);
					allTasksBar.setProgress(b != 0 ? 1.0*a/b : 0); 
				}else {
					mainController.closeSimulationWindow();
				}
			}
		};
		timer.start();
	}
	
	public void onStartClick(ActionEvent e) {
		String tickStr = ticksField.getText();
		int ticks = tickStr.isEmpty() ? 0 : Integer.parseInt(tickStr);
		String delayStr = delaysField.getText();
		long delay = delayStr.isEmpty() ? 0 : ((Double)(Double.parseDouble(delayStr)*1000)).longValue();
		DFModel.getCurrentSimulation().ifPresent(s->{
			s.start(ticks, delay);
		});
	}
	
	public void onStopClick(ActionEvent e) {
		DFModel.getCurrentSimulation().ifPresent(s->{
			s.stop();
		});
	}
	
	public void onEndClick(ActionEvent e) {
		mainController.closeSimulationWindow();
		DFModel.endSimulation();
	}
	
}
