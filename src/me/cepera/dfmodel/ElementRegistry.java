package me.cepera.dfmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.cepera.dfmodel.elements.DefaultElementCategory;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementCategory;
import me.cepera.dfmodel.elements.IElementFactory;
import me.cepera.dfmodel.elements.impl.DTriggerFactory;
import me.cepera.dfmodel.elements.impl.LogicalAndFactory;
import me.cepera.dfmodel.elements.impl.LogicalConstantFactory;
import me.cepera.dfmodel.elements.impl.LogicalDisplayFactory;
import me.cepera.dfmodel.elements.impl.LogicalFunctionFactory;
import me.cepera.dfmodel.elements.impl.LogicalNotFactory;
import me.cepera.dfmodel.elements.impl.LogicalOrFactory;
import me.cepera.dfmodel.elements.impl.LogicalXOrFactory;
import me.cepera.dfmodel.elements.impl.ShiftRegisterElementFactory;

public class ElementRegistry {

	private static List<IElementCategory> categoryRegistry = new ArrayList<IElementCategory>();
	private static List<IElementCategory> protectedCategoryRegistry = Collections.unmodifiableList(categoryRegistry);
	
	private static Map<String, IElementFactory<IElement>> registry = new HashMap<String, IElementFactory<IElement>>();
	private static Map<IElementCategory, List<IElementFactory<IElement>>> registryByCategory = new HashMap<>();
	private static List<IElementFactory<IElement>> registryList = new ArrayList<IElementFactory<IElement>>();
	private static List<IElementFactory<IElement>> protectedRegistryList = Collections.unmodifiableList(registryList);
	
	public static void registerCategory(IElementCategory category) {
		String id = category.getIdentificator();
		for(IElementCategory c : categoryRegistry)
			if(c.getIdentificator().equals(id))
				throw new IllegalArgumentException("Element category with identificator '"+id+"' is already registered");
		registryByCategory.put(category, new ArrayList<IElementFactory<IElement>>());
		categoryRegistry.add(category);
	}
	
	public static void register(IElementFactory<? extends IElement> elementFactory) {
		String id = elementFactory.getIdentificator();
		if(registry.containsKey(id)) 
			throw new IllegalArgumentException("Element with identificator '"+id+"' is already registered");
		List<IElementFactory<IElement>> catElements = registryByCategory.get(elementFactory.getCategory());
		if(catElements == null)
			throw new IllegalStateException("Category of element '"+id+"' is not registered.");
		registry.put(id, (IElementFactory<IElement>) elementFactory);
		registryList.add((IElementFactory<IElement>) elementFactory);
		catElements.add((IElementFactory<IElement>) elementFactory);
	}
	
	public static IElementFactory<IElement> getFactory(String elementId) {
		return registry.get(elementId);
	}
	
	public static List<IElementFactory<IElement>> getFactories(){
		return protectedRegistryList;
	}
	
	public static List<IElementFactory<IElement>> getFactories(IElementCategory category){
		return Collections.unmodifiableList(registryByCategory.get(category));
	}
	
	public static List<IElementCategory> getCategories() {
		return protectedCategoryRegistry;
	}
	
	static void registerDefaultElements() {
		DFModel.LOGGER.info("Registering default elements...");
		for(DefaultElementCategory c : DefaultElementCategory.values())
			registerCategory(c);
		DFModel.LOGGER.info("Registered "+categoryRegistry.size()+" categories");
		register(new LogicalNotFactory());
		register(new LogicalAndFactory());
		register(new LogicalOrFactory());
		register(new LogicalXOrFactory());
		register(new LogicalFunctionFactory());
		register(new LogicalConstantFactory());
		register(new LogicalDisplayFactory());
		register(new DTriggerFactory());
		register(new ShiftRegisterElementFactory());
		
		DFModel.LOGGER.info("Registered "+registryList.size()+" elements");
	}
	
}
