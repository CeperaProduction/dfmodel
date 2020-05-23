package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import me.cepera.dfmodel.Position;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.display.render.RenderHelper;
import me.cepera.dfmodel.elements.IElement;

public class SquareElementRenderer<T extends IElement> extends ElementRendererBase<T>{

	public SquareElementRenderer(T element) {
		super(element);
	}

	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last,
			boolean focused, boolean selected) {
		double w = getInitWidth();
		double h = getInitHeight();
		if(selected) {
			gc.setStroke(Color.MIDNIGHTBLUE);
			gc.setFill(Color.LIGHTCYAN);
		}else{
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.gray(0.95));
		}
		gc.setLineWidth(1.8);
		gc.save();
		RenderHelper.rotate(gc, rotation.getDegreesValue(), 0, 0);
		double r = getCornerRadius();
		if(r == 0) {
			gc.fillRect(-w/2, -h/2, w, h);
			gc.strokeRect(-w/2, -h/2, w, h);
		}else {
			gc.fillRoundRect(-w/2, -h/2, w, h, r, r);
			gc.strokeRoundRect(-w/2, -h/2, w, h, r, r);
		}
		gc.restore();
	}
	
	protected double getCornerRadius() {
		return 0;
	}
	
	@Override
	public void renderInputsOutputs(GraphicsContext gc, Rotation rotation, long now, long last, 
			boolean[] lastInputs, boolean[] lastOutputs, boolean preparedOutput,
			boolean focused, boolean selected, boolean wiring) {
		if(selected) {
			gc.setStroke(Color.MIDNIGHTBLUE);
			gc.setFill(Color.LIGHTCYAN);
		}else{
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.gray(0.95));
		}
		gc.setLineWidth(1.5);
		for(int i = 0; i < element.getInputCount(); i++) {
			Position p = getInputOffsetPosition(rotation, i);
			gc.fillOval(p.getX()-10, p.getY()-10, 20, 20);
			gc.strokeOval(p.getX()-10, p.getY()-10, 20, 20);
			if(wiring) {
				gc.save();
				gc.setFill(Color.gray(0.5));
				gc.fillText(i+1+"", p.getX()+8, p.getY()-8);
				gc.restore();
			}
			if(i < lastInputs.length) {
				gc.save();
				gc.setFill(Color.BLACK);
				gc.fillText(lastInputs[i] ? "1" : "0", p.getX()-14, p.getY()-10);
				gc.restore();
			}
		}
		for(int i = 0; i < element.getOutputCount(); i++) {
			Position p = getOutputOffsetPosition(rotation, i);
			gc.save();
			RenderHelper.translate(gc, p.getX(), p.getY());
			gc.fillOval(-10, -10, 20, 20);
			gc.strokeOval(-10, -10, 20, 20);
			RenderHelper.rotate(gc, rotation.getDegreesValue(), 0, 0);
			gc.strokeLine(-3, -5, 5, 0);
			gc.strokeLine(5, 0, -3, 5);
			gc.restore();
			if(wiring) {
				gc.save();
				gc.setFill(Color.gray(0.5));
				gc.fillText(i+1+"", p.getX()+8, p.getY()-8);
				gc.restore();
			}
			if(i < lastOutputs.length) {
				gc.save();
				gc.setFill(Color.BLACK);
				gc.fillText(lastOutputs[i] ? "1" : "0", p.getX()-14, p.getY()-10);
				if(preparedOutput) {
					gc.fillText("~", p.getX()-15, p.getY()-19);
				}
				gc.restore();
			}
		}
	}

	@Override
	public Position getInputOffsetPosition(Rotation rotation, int inputIndex) {
		double w = getInitWidth();
		double h = getInitHeight();
		double x = -w/2;
		double y = 20 + (h - element.getInputCount() * 40) / 2 + inputIndex * 40;
		if(rotation.getDegreesValue() > 90 && rotation.getDegreesValue() <= 270) {
			y = h - y;
		}
		y -= h/2;
		double r = Math.toRadians(rotation.getDegreesValue());
		double x1 = x * Math.cos(r) - y * Math.sin(r);
		double y1 = x * Math.sin(r) + y * Math.cos(r);
		return new Position(x1, y1);
	}

	@Override
	public Position getOutputOffsetPosition(Rotation rotation, int outputIndex) {
		double w = getInitWidth();
		double h = getInitHeight();
		double x = w/2;
		double y = 20 + (h - element.getOutputCount() * 40) / 2 + outputIndex * 40;
		if(rotation.getDegreesValue() > 90 && rotation.getDegreesValue() <= 270) {
			y = h - y;
		}
		y -= h/2;
		double r = Math.toRadians(rotation.getDegreesValue());
		double x1 = x * Math.cos(r) - y * Math.sin(r);
		double y1 = x * Math.sin(r) + y * Math.cos(r);
		return new Position(x1, y1);
	}
	
	public double getInitHeight() {
		return Math.max(element.getInputCount(), element.getOutputCount()) * 40;
	}
	
	public double getInitWidth() {
		return getInitHeight() * 0.75d;
	}

	@Override
	public boolean isFocused(Rotation rotation, double posX, double posY, double mouseX, double mouseY) {
		double x = mouseX - posX;
		double y = mouseY - posY;
		double r = Math.toRadians(rotation.getDegreesValue());
		double x1 = x * Math.cos(r) - y * Math.sin(r);
		double y1 = x * Math.sin(r) + y * Math.cos(r);
		double w = getInitWidth() + 20;
		double h = getInitHeight();
		return Math.abs(x1) <= w/2 && Math.abs(y1) <= h/2;
	}

}
