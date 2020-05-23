package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.impl.LogicalFunctionElement;

public class LogicalFunctionElementRenderer extends SquareElementRenderer<LogicalFunctionElement>{

	public LogicalFunctionElementRenderer(LogicalFunctionElement element) {
		super(element);
	}
	
	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last,
			boolean focused, boolean selected) {
		super.render(gc, rotation, now, last, focused, selected);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.BLACK);
		gc.fillText(element.getFunctionName(), 0, 4);
	}
	
	@Override
	public double getInitWidth() {
		double init = super.getInitWidth();
		if(init < element.getFunctionName().length() * 8 + 20) {
			init = element.getFunctionName().length() * 8 + 20;
		}
		return init;
	}
	
	@Override
	public double getInitHeight() {
		double init = super.getInitHeight();
		if(init < element.getFunctionName().length() * 8 + 20) {
			init = element.getFunctionName().length() * 8 + 20;
		}
		return init;
	}

}
