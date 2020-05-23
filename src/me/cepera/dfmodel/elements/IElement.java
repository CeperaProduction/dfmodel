package me.cepera.dfmodel.elements;

import java.util.List;
import java.util.Optional;

import me.cepera.dfmodel.data.IByteDataSerializable;
import me.cepera.dfmodel.display.render.elements.IElementRenderer;

public interface IElement extends IByteDataSerializable{

	public IElementFactory<? extends IElement> getFactory();
	
	public IElementRenderer<? extends IElement> getRenderer();
	
	public int getInputCount();
	
	public int getOutputCount();
	
	public boolean[] process(boolean[] input, int tick);
	
	public Optional<boolean[]> preprocessed(int tick);
	
	public void copyState(IElement from);
	
	public boolean statesEquals(IElement another);
	
	public List<IElementParameter> getParameters();
	
	public void onSimulationStart();
	
}
