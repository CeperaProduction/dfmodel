package me.cepera.dfmodel.display.render;

import static me.cepera.dfmodel.display.render.RenderHelper.translate;

import java.util.List;
import java.util.Optional;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import me.cepera.dfmodel.DFModel;
import me.cepera.dfmodel.Position;
import me.cepera.dfmodel.Scheme;
import me.cepera.dfmodel.Scheme.ElementContainer;
import me.cepera.dfmodel.Scheme.Wire;
import me.cepera.dfmodel.Simulation;
import me.cepera.dfmodel.Simulation.ElementSimulation;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;
import me.cepera.dfmodel.elements.IElement;

public class SchemeCanvasRenderer {
	
	private Canvas canvas;
	private GraphicsContext gc;
	
	public void bindCanvas(Canvas canvas) {
		this.canvas = canvas;
		this.gc = canvas.getGraphicsContext2D();
	}
	
	public void render(Stage stage, double canvasMouseX, double canvasMouseY, long now, long last) {
		double width = canvas.getWidth();
		double height = canvas.getHeight();
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, width, height);
		gc.setFill(Color.BLACK);
		/*gc.save();
		translate(gc, 50, 50);
		rotate(gc, counter+=1, 25, 25);
		gc.strokeRect(0, 0, 50, 50);
		gc.save();
		translate(gc, 40, 40);
		gc.strokeRect(0, 0, 10, 10);
		gc.restore();
		rotate(gc, -(counter+1)*2, 25, 25);
		translate(gc, 15, 15);
		gc.strokeRect(0, 0, 20, 20);
		gc.restore();
		gc.fillText("TEST: "+((int)counter/60)+" | "+(int)counter+" | "+canvasMouseX+" "+canvasMouseY, 100, 150);
		*/
		Scheme scheme = DFModel.getCurrentScheme();
		Simulation simulation = DFModel.getCurrentSimulation().orElse(null);
		for(ElementContainer el : scheme.getElements()) {
			gc.save();
			Position pos = el.getPosition();
			translate(gc, pos.getX(), pos.getY());
			IElement element = null;
			if(simulation != null) {
				Optional<ElementSimulation> optSim = simulation.getSimulation(el.getUUID());
				if(optSim.isPresent()) {
					element = optSim.get().getElement();
				}
			}
			if(element == null) element = el.getElement();
			IElementRenderer render = element.getRenderer();
			render.render(gc, el.getRotation(), now, last, 
					render.isFocused(el.getRotation(), pos.getX(), pos.getY(), canvasMouseX, canvasMouseY), 
					scheme.getSelectedElement().filter(e->e==el).isPresent());
			gc.restore();
		}
		Optional<Wire> optTempWire = scheme.getTempWire();
		if(optTempWire.isPresent()) {
			Wire tempWire = optTempWire.get();
			gc.save();
			gc.setLineDashes(5);
			ElementContainer el = tempWire.getSourceElement();
			Position offset;
			if(el == null) {
				el = tempWire.getTargetElement();
				offset = el.getElement().getRenderer().getInputOffsetPosition(
						el.getRotation(), tempWire.getTargetInputNumber());
			}else {
				offset = el.getElement().getRenderer().getOutputOffsetPosition(
						el.getRotation(), tempWire.getSourceOutputNumber());
			}
			Position pos1 = el.getPosition().plus(offset);
			Position pos2 = new Position(canvasMouseX, canvasMouseY);
			gc.strokeLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
			gc.restore();
		}
		Wire selectedWire = scheme.getSelectedWire().orElse(null);
		gc.save();
		gc.setLineWidth(1.6);
		for(Wire wire : scheme.getWires()) {
			ElementContainer source = wire.getSourceElement();
			ElementContainer target = wire.getTargetElement();
			if(source != null && target != null) {
				List<Position> points = wire.calculateAllPoints();
				for(int i = 1; i < points.size(); i++) {
					Position pos1 = points.get(i-1);
					Position pos2 = points.get(i);
					if(wire == selectedWire) {
						gc.save();
						gc.setStroke(Color.MIDNIGHTBLUE);
						gc.setLineWidth(5);
						gc.strokeLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
						gc.setStroke(Color.LIGHTCYAN);
						gc.setLineWidth(3);
						gc.strokeLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
						gc.restore();
					}else {
						gc.strokeLine(pos1.getX(), pos1.getY(), pos2.getX(), pos2.getY());
					}
				}
				if(wire == selectedWire) {
					for(int i = 1; i < points.size()-1; i++) {
						Position pos = points.get(i);
						gc.save();
						gc.setStroke(Color.MIDNIGHTBLUE);
						gc.setFill(Color.LIGHTCYAN);
						gc.fillOval(pos.getX()-5, pos.getY()-5, 10, 10);
						gc.strokeOval(pos.getX()-5, pos.getY()-5, 10, 10);
						gc.restore();
					}
				}
			}
		}
		gc.restore();
		for(ElementContainer el : scheme.getElements()) {
			gc.save();
			Position pos = el.getPosition();
			translate(gc, pos.getX(), pos.getY());
			IElement element = null;
			boolean[] lastInputs = new boolean[0];
			boolean[] lastOutputs = new boolean[0];
			boolean preparedOutput = false;
			if(simulation != null && simulation.getTicks() != 0) {
				Optional<ElementSimulation> optSim = simulation.getSimulation(el.getUUID());
				if(optSim.isPresent()) {
					ElementSimulation sim = optSim.get();
					element = sim.getElement();
					lastInputs = sim.getLastTickInputs();
					lastOutputs = sim.getLastTickOutputs();
					preparedOutput = sim.getPrepared() != null;
				}
			}
			if(element == null) element = el.getElement();
			IElementRenderer render = element.getRenderer();
			render.renderInputsOutputs(gc, el.getRotation(), now, last,
					lastInputs, lastOutputs, preparedOutput,
					render.isFocused(el.getRotation(), pos.getX(), pos.getY(), canvasMouseX, canvasMouseY), 
					scheme.getSelectedElement().filter(e->e==el).isPresent(),
					scheme.getTempWire().isPresent());
			gc.restore();
		}
	}
	
	
	
}
