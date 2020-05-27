package me.cepera.dfmodel;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import me.cepera.dfmodel.elements.IElement;

public class Simulation {

	private static UncaughtExceptionHandler exceptionHandler;
	
	private final Scheme scheme;
	private List<ElementSimulation> elementSimulations = new ArrayList<ElementSimulation>();
	private Map<UUID, ElementSimulation> elementSimulationsByUUID = new HashMap<UUID, ElementSimulation>();
	
	private int ticks;
	
	private Thread simulationThread;
	private boolean running;
	private int targetTick;
	private int lastStartTick;
	private long stepDelay;
	
	public Simulation(Scheme scheme) {
		this.scheme = scheme;
		scheme.setSimulation(this);
		for(Scheme.ElementContainer c : scheme.getElements()) {
			ElementSimulation s = new ElementSimulation(c);
			elementSimulations.add(s);
			elementSimulationsByUUID.put(s.uuid, s);
		}
	}
	
	/**
	 * Установка стандартного обработчика ошибок
	 * @param exceptionHandler - Обработчик ошибок
	 */
	public static void setExceptionHandler(UncaughtExceptionHandler exceptionHandler) {
		Simulation.exceptionHandler = exceptionHandler;
	}
	
	public Optional<ElementSimulation> getSimulation(UUID elementUUID) {
		return Optional.ofNullable(elementSimulationsByUUID.get(elementUUID));
	}
	
	public Scheme getScheme() {
		return scheme;
	}
	
	private void threadTick() {
		try {
			while(running && targetTick > ticks) {
				if(stepDelay > 0) {
					long t = System.currentTimeMillis();
					tick();
					t = System.currentTimeMillis() - t;
					if(t < stepDelay && targetTick > ticks) {
						try {
							Thread.sleep(stepDelay - t);
						} catch (InterruptedException e1) {
							break;
						}
					}
				}else {
					tick();
				}
			}
		}finally {
			simulationThread = null;
			running = false;
		}
	}
	
	/**
	 * Запуск асинхронного процесса моделирования.
	 * @param ticks - Количество тактов моделирования, которые необходимо произвести
	 * @param stepDelayMillis - Временная задержка между тактами моделирования
	 * @throws IllegalStateException В случае, если процесс уже запущен и не был завершен или остановлен
	 * @throws IllegalArgumentException В случае, если ticks < 1 или stepDelayMillis < 0
	 */
	public synchronized void start(int ticks, long stepDelayMillis){
		if(running) throw new IllegalStateException("Simulation is already running");
		if(ticks < 1) throw new IllegalArgumentException("Simulation must be started at least on 1 tick");
		if(stepDelayMillis < 0) throw new IllegalArgumentException("Step delay can't be negative");
		lastStartTick = this.ticks;
		targetTick = this.ticks + ticks;
		stepDelay = stepDelayMillis;
		running = true;
		simulationThread = new Thread(this::threadTick, "simulation");
		if(exceptionHandler != null)
			simulationThread.setUncaughtExceptionHandler(exceptionHandler);
		simulationThread.start();
	}
	
	/**
	 * Остановка асинхронного процесса моделирования.
	 */
	public void stop() {
		if(simulationThread != null && !simulationThread.isInterrupted()) simulationThread.interrupt();
		simulationThread = null;
		running = false;
	}
	
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * @return Количество произведенных тактов моделирования
	 */
	public int getTicks() {
		return ticks;
	}
	
	/**
	 * @return Конечный такт, на котором моделирование будет завершено
	 */
	public int getTargetTick() {
		return targetTick;
	}
	
	/**
	 * @return Такт, на котором последний раз запускался процесс моделирования
	 */
	public int getLastStartTick() {
		return lastStartTick;
	}
	
	/**
	 * @return Количество тактов моделирования, которое осталось произвести по последней запущенной задаче моделирования.
	 */
	public int getRemainedTicks() {
		return targetTick - ticks;
	}
	
	/**
	 * Синхронное исполнение одного такта моделирования. 
	 * Не рекомендуется использовать этот метод, используйте {@link Simulation#start(int)} для асинхронного исполнения.
	 */
	@Deprecated
	public synchronized void tick() {
		List<ElementSimulation> elements = new ArrayList<>(this.elementSimulations);
		Collections.sort(elements, (e1, e2)->{
			return e2.element.getPriority() - e1.element.getPriority();
		});
		for(ElementSimulation s : elements) {
			s.prepareTick();
		}
		Iterator<ElementSimulation> it = elements.iterator();
		l: while(it.hasNext()) {
			ElementSimulation s = it.next();
			for(int i = 0; i < s.element.getInputCount(); i++) {
				if(scheme.getInputWires(s.uuid, i).size() > 0) continue l;
			}
			s.tick(new boolean[s.element.getInputCount()]);
			it.remove();
		}
		int tscc = 1;
		while(tscc != 0) {
			tscc = calculateCompletedElements(elements.iterator());
			if(tscc == 0) {
				for(ElementSimulation s : this.elementSimulations) s.prepareStep();
				tscc = calculateNonCompletedElements(elements.iterator());
			}
		}
		ticks++;
	}
	
	private int calculateCompletedElements(Iterator<ElementSimulation> it) {
		int tscc = 0;
		m0: while(it.hasNext()) {
			ElementSimulation s = it.next();
			boolean[] inputs = new boolean[s.element.getInputCount()];
			for(int i = 0; i < inputs.length; i++) {
				List<Scheme.Wire> wires = scheme.getInputWires(s.uuid, i);
				if(wires.size() == 0) {
					inputs[i] = false;
				}else {
					boolean found = false;
					for(Scheme.Wire wire : wires) {
						ElementSimulation source = elementSimulationsByUUID.get(wire.getSourceUUID());
						if(source.prepared != null) {
							inputs[i] = inputs[i] | source.prepared[wire.getSourceOutputNumber()];
							found = true;
						}else if(source.calculatedThisTick) {
							inputs[i] = inputs[i] | source.lastTickOutputs[wire.getSourceOutputNumber()];
							found = true;
						}
					}
					if(!found) continue m0;
				}
			}
			s.tick(inputs);
			it.remove();
			tscc++;
		}
		return tscc;
	}
	
	private int calculateNonCompletedElements(Iterator<ElementSimulation> it) {
		int tscc = 0;
		m0: while(it.hasNext()) {
			ElementSimulation s = it.next();
			boolean[] inputs = new boolean[s.element.getInputCount()];
			boolean hasCalculated = false;
			for(int i = 0; i < inputs.length; i++) {
				List<Scheme.Wire> wires = scheme.getInputWires(s.uuid, i);
				boolean nf = true;
				for(Scheme.Wire wire : wires) {
					ElementSimulation source = elementSimulationsByUUID.get(wire.getSourceUUID());
					if(source.calculatedThisStep) continue m0;
					if(source.calculatedThisTick) {
						inputs[i] = inputs[i] | source.lastTickOutputs[wire.getSourceOutputNumber()];
						hasCalculated = true;
						nf = false;
					}
				}
				if(nf) inputs[i] = false;
			}
			if(hasCalculated) {
				s.tick(inputs);
				it.remove();
				tscc++;
			}
		}
		return tscc;
	}
	
	public class ElementSimulation{
		
		private final UUID uuid;
		private final IElement element;
		
		private boolean[] lastTickInputs;
		private boolean[] lastTickOutputs;
		private boolean[] prepared;
		private boolean[] nextPrepared;
		
		private boolean calculatedThisTick = false;
		private boolean calculatedThisStep = false;
		
		private ElementSimulation(Scheme.ElementContainer container) {
			uuid = container.getUUID();
			element = container.getFactory().makeElement();
			element.copyState(container.getElement());
			element.onSimulationStart();
			nextPrepared = element.preprocessed(ticks).orElse(null);
			prepareTick();
		}
		
		private void prepareTick() {
			calculatedThisTick = false;
			lastTickInputs = new boolean[element.getInputCount()];
			lastTickOutputs = new boolean[element.getOutputCount()];
			prepared = nextPrepared;
			prepareStep();
		}
		
		private void prepareStep() {
			calculatedThisStep = false;
		}
		
		private boolean[] tick(boolean[] inputs) {
			lastTickOutputs = element.process(inputs, ticks);
			lastTickInputs = inputs;
			calculatedThisTick = true;
			calculatedThisStep = true;
			nextPrepared = element.preprocessed(ticks+1).orElse(null);
			return lastTickOutputs;
		}
		
		public UUID getUUID() {
			return uuid;
		}
		
		public IElement getElement() {
			return element;
		}
		
		public boolean[] getLastTickInputs() {
			return lastTickInputs;
		}
		
		public boolean[] getLastTickOutputs() {
			return lastTickOutputs;
		}
		
		public boolean[] getPrepared() {
			return prepared;
		}
		
	}
	
}
