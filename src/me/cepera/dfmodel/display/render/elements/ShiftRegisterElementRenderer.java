package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import me.cepera.dfmodel.Position;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.impl.ShiftRegisterElement;

public class ShiftRegisterElementRenderer extends SquareElementRenderer<ShiftRegisterElement>{

	public ShiftRegisterElementRenderer(ShiftRegisterElement element) {
		super(element);
	}
	
	@Override
	public void render(GraphicsContext gc, Rotation rotation, long now, long last, boolean focused, boolean selected) {
		super.render(gc, rotation, now, last, focused, selected);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.setFill(Color.BLACK);
		gc.fillText("Reg", 0, -4);
		StringBuilder sb = new StringBuilder();
		int v = element.getValue();
		int m = 1 << element.getSize();
		sb.append('[');
		for(int i = 0; i < element.getSize(); i++) {
			v<<=1;
			sb.append((v & m) == 0 ? '0' : '1');
		}
		sb.append(']');
		gc.fillText(sb.toString(), 0, 12);
		if(element.hasSync()) {
			Position p = getInputOffsetPosition(rotation, 1);
			gc.fillText("c", p.getX()+12, p.getY()+14);
		}
	}
	
	@Override
	public double getInitWidth() {
		double init = super.getInitWidth();
		int w = Math.max(element.getSize()+2, 4) * 8 + 20;
		if(init < w) {
			init = w;
		}
		return init;
	}
	
	@Override
	protected double getCornerRadius() {
		return element.hasDelay() ? 20 : 0;
	}

}
