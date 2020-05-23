package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.impl.LogicalConstantElement;

public class LogicalConstantElementRenderer extends SquareElementRenderer<LogicalConstantElement>{

	public LogicalConstantElementRenderer(LogicalConstantElement element) {
		super(element);
	}
	
	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last, boolean focused, boolean selected) {
		super.render(gc, rotation, now, last, focused, selected);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.BLACK);
		gc.fillText(element.getValue() ? "1" : "0", 0, 4);
	}
	
	@Override
	public double getInitWidth() {
		return super.getInitWidth() * 1.3;
	}
	
	@Override
	protected double getCornerRadius() {
		return 20;
	}

}
