package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import me.cepera.dfmodel.Position;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.impl.DTriggerElement;

public class DTriggerElementRenderer extends SquareElementRenderer<DTriggerElement>{

	public DTriggerElementRenderer(DTriggerElement element) {
		super(element);
	}
	
	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last, boolean focused, boolean selected) {
		super.render(gc, rotation, now, last, focused, selected);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.BLACK);
		gc.fillText("D-tr", 0, -4);
		gc.fillText(element.getValue() ? "[1]" : "[0]", 0, 12);
		Position p = getInputOffsetPosition(rotation, 1);
		gc.fillText("c", p.getX()+12, p.getY()+14);
	}
	
	@Override
	protected double getCornerRadius() {
		return element.hasDelay() ? 20 : 0;
	}

}
