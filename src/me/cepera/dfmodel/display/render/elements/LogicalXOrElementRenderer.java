package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.impl.LogicalXOrElement;

public class LogicalXOrElementRenderer extends SquareElementRenderer<LogicalXOrElement>{

	public LogicalXOrElementRenderer(LogicalXOrElement element) {
		super(element);
	}

	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last,
			boolean focused, boolean selected) {
		super.render(gc, rotation, now, last, focused, selected);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.BLACK);
		gc.fillText("XOR", 0, 4);
	}
	
	@Override
	protected double getCornerRadius() {
		return element.hasDelay() ? 20 : 0;
	}

}
