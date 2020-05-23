package me.cepera.dfmodel;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import me.cepera.dfmodel.display.MainWindow;
import me.cepera.dfmodel.display.render.SchemeCanvasRenderer;

public class DFModel {

	public static final String PROGRAM_NAME = "DFModel";
	public static final String PROGRAM_VERSION = "1.0.0";

	public static final Logger LOGGER;
	
	private static SchemeCanvasRenderer schemeRenderer;
	
	private static Scheme currentScheme;
	private static Simulation currentSimulation;

	static {
		try (InputStream in = DFModel.class.getClassLoader().getResourceAsStream("logging.properties")){
			LogManager.getLogManager().readConfiguration(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER = Logger.getLogger(PROGRAM_NAME);
	}

	public static void main(String[] args) {
		LOGGER.info("Starting "+PROGRAM_NAME+" version "+PROGRAM_VERSION);
		ElementRegistry.registerDefaultElements();
		createNewScheme();
		schemeRenderer = new SchemeCanvasRenderer();
		MainWindow.launch(MainWindow.class, args);
	}
	
	public static SchemeCanvasRenderer getShemeRenderer() {
		return schemeRenderer;
	}
	
	public static Scheme getCurrentScheme() {
		return currentScheme;
	}
	
	public static Optional<Simulation> getCurrentSimulation() {
		return Optional.ofNullable(currentSimulation);
	}
	
	public static Scheme createNewScheme() {
		endSimulation();
		LOGGER.info("Creating new scheme");
		return currentScheme = new Scheme();
	}
	
	public static void setScheme(Scheme scheme) {
		if(scheme == null) throw new NullPointerException();
		endSimulation();
		currentScheme = scheme;
		LOGGER.info("Current scheme changed");
	}
	
	public static Simulation createNewSimulation() {
		endSimulation();
		LOGGER.info("Creating new simulation");
		return currentSimulation = new Simulation(currentScheme);
	}
	
	public static void endSimulation() {
		if(currentSimulation != null) {
			currentSimulation.stop();
			LOGGER.info("Simulation ended");
		}
		currentSimulation = null;
		if(currentScheme != null) {
			currentScheme.setSimulation(null);
		}
		System.gc();
	}
	
	public static void logException(String message, Throwable throwable) {
		ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
		LOGGER.log(Level.SEVERE, message);
		try (PrintStream ps = new PrintStream(byteBuff, true, ResourceHelper.CHARSET.name())) {
	        throwable.printStackTrace(ps);
			LOGGER.log(Level.SEVERE, byteBuff.toString(ResourceHelper.CHARSET.name()), throwable);
	    } catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error while display another error.");
			e.printStackTrace();
		}
	}

}
