package me.cepera.dfmodel.display.render.elements;

import javafx.scene.canvas.GraphicsContext;
import me.cepera.dfmodel.Position;
import me.cepera.dfmodel.Rotation;
import me.cepera.dfmodel.elements.IElement;

public interface IElementRenderer<T extends IElement> {

	public IElement getElement();
	
	public void render(GraphicsContext gc, Rotation rotation, long now, long last,
			boolean focused, boolean selected);
	
	public void renderInputsOutputs(GraphicsContext gc, Rotation rotation, long now, long last,
			boolean[] lastInputs, boolean[] lastOutputs, boolean preparedOutput,
			boolean focused, boolean selected, boolean wiring);
	
	public Position getInputOffsetPosition(Rotation rotation, int inputIndex);
	
	public Position getOutputOffsetPosition(Rotation rotation, int outputIndex);
	
	public boolean isFocused(Rotation rotation, double posX, double posY, double mouseX, double mouseY);
	
	public double getInitWidth();
	
	public double getInitHeight();
	
}
