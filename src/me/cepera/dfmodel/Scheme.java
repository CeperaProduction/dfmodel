package me.cepera.dfmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.data.ByteDataUtils;
import me.cepera.dfmodel.data.IByteDataSerializable;
import me.cepera.dfmodel.elements.IElement;
import me.cepera.dfmodel.elements.IElementFactory;

public class Scheme implements IByteDataSerializable{
	
	private static final UUID EMPTY_UUID = new UUID(0, 0);
	
	private List<ElementContainer> elements = new CopyOnWriteArrayList<ElementContainer>();
	private List<ElementContainer> protectedElements = Collections.unmodifiableList(elements);
	private Map<UUID, ElementContainer> elementsByUUID = new HashMap<UUID, Scheme.ElementContainer>();
	
	private List<Wire> wires = new CopyOnWriteArrayList<Wire>();
	private List<Wire> protectedWires = Collections.unmodifiableList(wires);
	private Map<WireConnection, List<Wire>> wiresBySource = new HashMap<WireConnection, List<Wire>>();
	private Map<WireConnection, List<Wire>> wiresByTarget = new HashMap<WireConnection, List<Wire>>();

	private Object selectedObject;
	
	private boolean changed = false;
	
	private Simulation simulation;
	
	private boolean drag = false;
	private boolean temp = false;
	private Wire tempWire;
	private int dragWireCorner = 0;
	private Position dragWireCornerInitPos;
	
	private double[] schemeBounds = new double[4];
	
	public boolean isDrag() {
		return drag;
	}
	
	public void setSimulation(Simulation simulation) {
		this.simulation = simulation;
	}
	
	public Simulation getSimulation() {
		return simulation;
	}
	
	public Optional<ElementContainer> getSelectedElement(){
		if(selectedObject instanceof ElementContainer) return Optional.of((ElementContainer)selectedObject);
		return Optional.empty();
	}
	
	public Optional<Wire> getSelectedWire(){
		if(selectedObject instanceof Wire) return Optional.of((Wire)selectedObject);
		return Optional.empty();
	}
	
	public List<ElementContainer> getElements() {
		return protectedElements;
	}
	
	public ElementContainer createElement(IElementFactory<IElement> factory, double posX, double posY) {
		ElementContainer element = new ElementContainer(factory);
		element.uuid = UUID.randomUUID();
		element.position.set(posX, posY);
		deselect();
		elementsByUUID.put(element.uuid, element);
		elements.add(element);
		selectedObject = element;
		startDragElement();
		temp = true;
		return element;
	}
	
	public Optional<ElementContainer> duplicateSelectedElement(double posX, double posY){
		Optional<ElementContainer> optSelected = getSelectedElement();
		if(!drag && optSelected.isPresent()) {
			ElementContainer selected = optSelected.get();
			ElementContainer duplicated = createElement(selected.factory, posX, posY);
			duplicated.element.copyState(selected.element);
			duplicated.rotation = duplicated.tempRotation = selected.getRotation();
			return Optional.of(duplicated);
		}
		return Optional.empty();
	}
	
	public Optional<Wire> getTempWire(){
		return Optional.ofNullable(tempWire);
	}
	
	public List<Wire> getWires() {
		return protectedWires;
	}
	
	public List<Wire> getInputWires(UUID elementUUID, int input){
		List<Wire> wires = wiresByTarget.get(new WireConnection(elementUUID, input));
		return wires == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(wires);
	}
	
	public List<Wire> getOutputWires(UUID elementUUID, int output){
		List<Wire> wires = wiresBySource.get(new WireConnection(elementUUID, output));
		return wires == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(wires);
	}
	
	public Optional<ElementContainer> getElementByUUID(UUID uuid) {
		return Optional.ofNullable(elementsByUUID.get(uuid));
	}
	
	public Optional<ElementContainer> deleteSelectedElement(){
		if(selectedObject instanceof ElementContainer) {
			ElementContainer element = (ElementContainer) selectedObject;
			deselect();
			elements.remove(element);
			element = elementsByUUID.remove(element.uuid);
			checkAndCleanWires();
			changed = true;
			recalculateSchemeBounds();
			return Optional.ofNullable(element);
		}
		return Optional.empty();
	}

	public void deleteSelectedWire() {
		if(selectedObject instanceof Wire) {
			Wire wire = (Wire) selectedObject;
			deselect();
			deleteWire(wire);
		}
	}
	
	public void checkAndCleanWires() {
		ArrayList<Wire> toRemove = new ArrayList<Wire>();
		for(Wire wire : wires) {
			ElementContainer source = wire.getSourceElement();
			ElementContainer target = wire.getTargetElement();
			if(source == null 
					|| source.getElement().getOutputCount() <= wire.getSourceOutputNumber()
					|| target == null
					|| target.getElement().getInputCount() <= wire.getTargetInputNumber()) {
				toRemove.add(wire);
			}
		}
		for(Wire wire : toRemove) {
			deleteWire(wire);
		}
	}
	
	private void deleteWire(Wire wire) {
		wiresBySource.get(wire.source).remove(wire);
		wiresByTarget.get(wire.target).remove(wire);
		wires.remove(wire);
		changed = true;
	}
	
	public SchemeClickAction getMouseClickAction(double posX, double posY, int mouseButton) {
		if(drag) {
			return new SchemeClickAction(SchemeClickType.DRAG_STOP, selectedObject, ()->stopDrag(false));
		}
		Position mousePos = new Position(posX, posY);
		WireClick wireClick = tryClickWire(mousePos);
		loop: for(ElementContainer el : elements) {
			if(el.element.getRenderer().isFocused(el.rotation, el.position.getX(), el.position.getY(), posX, posY)) {
				for(int i = 0; i < el.element.getInputCount(); i++) {
					Position offset = el.element.getRenderer().getInputOffsetPosition(el.rotation, i);
					Position pos = offset.plus(el.position);
					if(pos.distance(mousePos) <= 10) {
						Wire wire;
						if(tempWire == null) wire = new Wire();
						else wire = tempWire;
						final int index = i;
						return new SchemeClickAction(SchemeClickType.WIRING, wire, ()->{
							wire.target = new WireConnection(el.uuid, index);
							if(wire == tempWire) {
								onTempWireComplete();
							}else {
								this.tempWire = wire;
							}
						});
					}
				}
				for(int i = 0; i < el.element.getOutputCount(); i++) {
					Position offset = el.element.getRenderer().getOutputOffsetPosition(el.rotation, i);
					Position pos = offset.plus(el.position);
					if(pos.distance(mousePos) <= 10) {
						Wire wire;
						if(tempWire == null) wire = new Wire();
						else wire = tempWire;
						final int index = i;
						return new SchemeClickAction(SchemeClickType.WIRING, wire, ()->{
							wire.source = new WireConnection(el.uuid, index);
							if(wire == tempWire) {
								onTempWireComplete();
							}else{
								this.tempWire = wire;
							}
						});
					}
				}
				if(tempWire != null || wireClick.wire != null) {
					break loop;
				}
				if(selectedObject == el && mouseButton == 0) {
					return new SchemeClickAction(SchemeClickType.DRAG_START, el, this::startDragElement);
				}else{
					return new SchemeClickAction(SchemeClickType.SELECT, el, ()->{selectedObject = el;});
				}
			}
		}
		if(tempWire != null) {
			return new SchemeClickAction(SchemeClickType.WIRING, tempWire, ()->{tempWire = null;});
		}
		if(wireClick.wire != null) {
			if(selectedObject == wireClick.wire) {
				return new SchemeClickAction(SchemeClickType.DRAG_START, selectedObject, ()->startDragWire(mousePos, wireClick.cornerId, wireClick.newCorner));
			}
			return new SchemeClickAction(SchemeClickType.SELECT, wireClick.wire, ()->{selectedObject = wireClick.wire;});
		}
		if(selectedObject != null) {
			return new SchemeClickAction(SchemeClickType.DESELECT, selectedObject, ()->{selectedObject = null;});
		}
		return new SchemeClickAction(SchemeClickType.NONE, null, ()->{});
	}
	
	private WireClick tryClickWire(Position mousePos){
		if(selectedObject instanceof Wire) {
			Wire wire = (Wire) selectedObject;
			ElementContainer source = wire.getSourceElement();
			ElementContainer target = wire.getTargetElement();
			if(source != null && target != null) {
				List<Position> points = wire.calculateAllPoints();
				for(int i = 1; i < points.size(); i++) {
					Position pos1 = points.get(i-1);
					Position pos2 = points.get(i);
					double a = pos1.distance(mousePos);
					double b = pos2.distance(mousePos);
					double c = pos1.distance(pos2);
					if(a < 5) {
						return new WireClick(wire, Math.max(i-2, 0), false);
					}
					if(b < 5) {
						return new WireClick(wire, i-1, false);
					}
					if(a+b-c < 2) {
						return new WireClick(wire, i-1, true);
					}
				}
			}
		}
		for(Wire wire : wires) {
			if(selectedObject == wire) continue;
			ElementContainer source = wire.getSourceElement();
			ElementContainer target = wire.getTargetElement();
			if(source != null && target != null) {
				List<Position> points = wire.calculateAllPoints();
				for(int i = 1; i < points.size(); i++) {
					Position pos1 = points.get(i-1);
					Position pos2 = points.get(i);
					double a = pos1.distance(mousePos);
					double b = pos2.distance(mousePos);
					double c = pos1.distance(pos2);
					if(a < 5) {
						return new WireClick(wire, Math.max(i-2, 0), false);
					}
					if(b < 5) {
						return new WireClick(wire, i-1, false);
					}
					if(a+b-c < 2) {
						return new WireClick(wire, i-1, true);
					}
				}
				
			}
		}
		return new WireClick(null, 0, false);
	}
	
	private class WireClick{
		final Wire wire;
		final int cornerId;
		final boolean newCorner;
		
		public WireClick(Wire wire, int cornerId, boolean newCorner) {
			this.wire = wire;
			this.cornerId = cornerId;
			this.newCorner = newCorner;
		}
		
		
	}
	
	private boolean onTempWireComplete() {
		if(tempWire == null) return false;
		boolean r = onWireComplete(tempWire);
		if(r) changed = true;
		tempWire = null;
		return r;
	}
	
	private boolean onWireComplete(Wire wire) {
		if(!wire.isCompleted()) return false;
		List l1 = wiresBySource.get(wire.source);
		if(l1 == null) {
			l1 = new ArrayList<Wire>();
			wiresBySource.put(wire.source, l1);
		}
		List l2 = wiresByTarget.get(wire.target);
		if(l2 == null) {
			l2 = new ArrayList<Wire>();
			wiresByTarget.put(wire.target, l2);
		}
		l1.add(wire);
		l2.add(wire);
		this.wires.add(wire);
		return true;
	}
	
	public void handleMouseMove(double posX, double posY) {
		if(drag) {
			if(selectedObject instanceof ElementContainer) {
				((ElementContainer)selectedObject).tempPosition.set(posX, posY);
				recalculateSchemeBounds();
			}
			if(selectedObject instanceof Wire) {
				((Wire)selectedObject).corners.get(dragWireCorner).set(posX, posY);
			}
		}
	}
	
	public void handleRotate(boolean left) {
		if(selectedObject instanceof ElementContainer) {
			((ElementContainer)selectedObject).rotate(left);
		}
	}
	
	public boolean deselect() {
		stopDrag(true);
		if(selectedObject != null) {
			selectedObject = null;
			return true;
		}
		return false;
	}
	
	public void stopDrag(boolean cancel) {
		tempWire = null;
		if(drag) {
			drag = false;
			if(selectedObject instanceof ElementContainer) {
				ElementContainer selected = (ElementContainer) selectedObject;
				if(!cancel) {
					selected.position = selected.tempPosition;
					selected.rotation = selected.tempRotation;
					temp = false;
					changed = true;
				}else if(temp) {
					temp = false;
					deleteSelectedElement();
					return;
				}
				selected.tempPosition = null;
				selected.tempRotation = null;
				return;
			}
			if(selectedObject instanceof Wire) {
				Wire selected = (Wire) selectedObject;
				if(cancel) {
					if(temp) {
						selected.corners.remove(dragWireCorner);
					}else {
						selected.corners.set(dragWireCorner, dragWireCornerInitPos);
					}
				}else {
					List<Position> points = selected.calculateAllPoints();
					Position pos1 = points.get(dragWireCorner);
					Position current = points.get(dragWireCorner+1);
					Position pos2 = points.get(dragWireCorner+2);
					double a = pos1.distance(current);
					double b = current.distance(pos2);
					double c = pos1.distance(pos2);
					if(a+b-c < 1.5) {
						selected.corners.remove(dragWireCorner);
					}else {
						double d = 3;
						if(Math.abs(pos1.getX() - current.getX()) < d) {
							current.set(pos1.getX(), current.getY());
						}
						if(Math.abs(pos2.getX() - current.getX()) < d) {
							current.set(pos2.getX(), current.getY());
						}
						if(Math.abs(pos1.getY() - current.getY()) < d) {
							current.set(current.getX(), pos1.getY());
						}
						if(Math.abs(pos2.getY() - current.getY()) < d) {
							current.set(current.getX(), pos2.getY());
						}
					}
						
					changed = true;
				}
				temp = false;
				dragWireCorner = 0;
				dragWireCornerInitPos = null;
			}
		}
		
	}
	
	public boolean startDragElement() {
		if(selectedObject instanceof ElementContainer) {
			ElementContainer selected = (ElementContainer) selectedObject;
			selected.tempPosition = selected.position.copy();
			selected.tempRotation = selected.rotation;
			drag = true;
			return true;
		}
		return false;
	}
	
	public boolean startDragWire(Position initMousePos, int cornerId, boolean newCorner) {
		if(selectedObject instanceof Wire) {
			Wire selected = (Wire) selectedObject;
			if(newCorner) {
				selected.corners.add(cornerId, initMousePos);
				temp = true;
			}
			dragWireCorner = cornerId;
			dragWireCornerInitPos = selected.corners.get(cornerId).copy();
			drag = true;
			return true;
		}
		return false;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
	public double[] getSchemeBounds() {
		return schemeBounds;
	}
	
	public double[] recalculateSchemeBounds() {
		if(elements.isEmpty()) return new double[4];
		Iterator<ElementContainer> it = elements.iterator();
		double[] r = elementBounds(it.next());
		while(it.hasNext()) {
			ElementContainer el = it.next();
			double[] b = elementBounds(el);
			if(b[0] < r[0]) r[0] = b[0];
			if(b[1] < r[1]) r[1] = b[1];
			if(b[2] > r[2]) r[2] = b[2];
			if(b[3] > r[3]) r[3] = b[3];
		}
		schemeBounds = r;
		return r;
	}
	
	private double[] elementBounds(ElementContainer el) {
		double dw = Math.max(el.element.getRenderer().getInitHeight(), el.element.getRenderer().getInitWidth());
		return new double[] {el.position.getX()-dw, el.position.getY()-dw, el.position.getX()+dw, el.position.getY()+dw};
		
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException {
		List<ElementContainer> elements = ByteDataUtils.readSubDataList(data, d->{
			String elementId = d.readUTF();
			IElementFactory<IElement> factory = ElementRegistry.getFactory(elementId);
			if(factory != null) {
				ByteDataInputStream content = ByteDataUtils.readSubData(d);
				if(content != null) {
					ElementContainer element = new ElementContainer(factory);
					element.readData(content);
					return element;
				}
			}else {
				DFModel.LOGGER.warning("Element of type '"+elementId+"' is not registered.");
			}
			return null;
		});
		List<Wire> wires = ByteDataUtils.readSubDataList(data, Wire::new);
		this.wires.clear();
		this.elements.clear();
		elementsByUUID.clear();
		wiresBySource.clear();
		wiresByTarget.clear();
		for(ElementContainer e : elements) {
			elementsByUUID.put(e.uuid, e);
		}
		for(Wire w : wires) {
			onWireComplete(w);
		}
		checkAndCleanWires();
		this.elements.addAll(elements);
		recalculateSchemeBounds();
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException {
		ByteDataUtils.writeSubDataList(data, elements, (e,b)->{
			b.writeUTF(e.element.getFactory().getIdentificator());
			ByteDataUtils.writeSubData(b, e);
		});
		ByteDataUtils.writeSubDataList(data, wires);
	}

	public class ElementContainer implements IByteDataSerializable{
		
		private UUID uuid;

		private IElementFactory<IElement> factory;
		private IElement element;
		private Position position;
		private Rotation rotation;
		
		private Position tempPosition;
		private Rotation tempRotation;
		
		private ElementContainer(IElementFactory<IElement> factory) {
			uuid = EMPTY_UUID;
			this.factory = factory;
			element = factory.makeElement();
			position = new Position(0, 0);
			rotation = Rotation.values()[0];
		}
		
		public IElementFactory<IElement> getFactory() {
			return factory;
		}
		
		public UUID getUUID() {
			return uuid;
		}
		
		public IElement getElement() {
			return element;
		}
		
		public Position getPosition() {
			return tempPosition == null ? position : tempPosition;
		}
		
		public Rotation getRotation() {
			return tempRotation == null ? rotation : tempRotation;
		}
		
		public void rotate(boolean left) {
			if(tempRotation == null) {
				if(left) rotation = rotation.left();
				else rotation = rotation.right();
				changed = true;
			}else {
				if(left) tempRotation = tempRotation.left();
				else tempRotation = tempRotation.right();
			}
		}
		
		public void rotate() {
			rotate(false);
		}

		@Override
		public void readData(ByteDataInputStream data) throws IOException {
			long um = data.readLong();
			long ul = data.readLong();
			uuid = new UUID(um, ul);
			rotation = Rotation.values()[data.readInt()];
			position.readData(ByteDataUtils.readSubData(data));
			element.readData(ByteDataUtils.readSubData(data));
		}

		@Override
		public void writeData(ByteDataOutputStream data) throws IOException {
			data.writeLong(uuid.getMostSignificantBits());
			data.writeLong(uuid.getLeastSignificantBits());
			data.writeInt(rotation.ordinal());
			ByteDataUtils.writeSubData(data, position);
			ByteDataUtils.writeSubData(data, element);
		}

	}
	
	public class Wire implements IByteDataSerializable{
		
		private WireConnection source;
		private WireConnection target;
		
		private LinkedList<Position> corners = new LinkedList<Position>();
		private List<Position> protectedCorners = Collections.unmodifiableList(corners);;
		
		private Wire() {}
		
		private Wire(UUID sourceElementId, int outputNumber, UUID targetElementId, int inputNumber) {
			source = new WireConnection(sourceElementId, outputNumber);
			target = new WireConnection(targetElementId, inputNumber);
		}
		
		public List<Position> getCorners() {
			return protectedCorners;
		}
		
		public List<Position> calculateAllPoints(){
			ElementContainer source = getSourceElement();
			ElementContainer target = getTargetElement();
			Position offset1 = source.getElement().getRenderer().getOutputOffsetPosition(
					source.getRotation(), getSourceOutputNumber());
			Position startPos = source.getPosition().plus(offset1);
			Position offset2 = target.getElement().getRenderer().getInputOffsetPosition(
					target.getRotation(), getTargetInputNumber());
			Position endPos = target.getPosition().plus(offset2);
			List<Position> points = new ArrayList<Position>();
			points.add(startPos);
			points.addAll(corners);
			points.add(endPos);
			return points;
		}
		
		public boolean isCompleted() {
			return source != null && target != null;
		}
		
		public ElementContainer getSourceElement() {
			if(source == null) return null;
			return elementsByUUID.get(source.elementId);
		}
		
		public ElementContainer getTargetElement() {
			if(target == null) return null;
			return elementsByUUID.get(target.elementId);
		}
		
		public UUID getSourceUUID() {
			return source == null ? null : source.elementId;
		}
		
		public UUID getTargetUUID() {
			return source == null ? null : source.elementId;
		}
		
		public int getSourceOutputNumber() {
			if(source == null) return -1;
			return source.number;
		}
		
		public int getTargetInputNumber() {
			if(target == null) return -1;
			return target.number;
		}
		
		@Override
		public void readData(ByteDataInputStream data) throws IOException {
			UUID sourceElementId = new UUID(data.readLong(), data.readLong());
			UUID targetElementId = new UUID(data.readLong(), data.readLong());
			int sourceOutputNumber = data.readInt();
			int targetInputNumber = data.readInt();
			source = new WireConnection(sourceElementId, sourceOutputNumber);
			target = new WireConnection(targetElementId, targetInputNumber);
			List<Position> corners = ByteDataUtils.readSubDataList(data, Position::new);
			this.corners.clear();
			this.corners.addAll(corners);
		}

		@Override
		public void writeData(ByteDataOutputStream data) throws IOException {
			data.writeLong(source.elementId.getMostSignificantBits());
			data.writeLong(source.elementId.getLeastSignificantBits());
			data.writeLong(target.elementId.getMostSignificantBits());
			data.writeLong(target.elementId.getLeastSignificantBits());
			data.writeInt(source.number);
			data.writeInt(target.number);
			ByteDataUtils.writeSubDataList(data, corners);
		}
		
	}
	
	private static class WireConnection{
		final UUID elementId;
		final int number;
		WireConnection(UUID elementId, int number){
			this.elementId = elementId;
			this.number = number;
		}
		
		@Override
		public int hashCode() {
			long n = ((long)number) << 16;
			return (int) (elementId.hashCode() ^ n);
		}
		
		@Override
		public boolean equals(Object obj) {
			if ((null == obj) || (obj.getClass() != WireConnection.class))
	            return false;
			WireConnection c = (WireConnection)obj;
			return elementId.equals(c.elementId) && number == c.number;
		}
	}
	
	public class SchemeClickAction implements Runnable{

		private final Runnable action;
		private final SchemeClickType type;
		private final Object target;
		
		private SchemeClickAction(SchemeClickType type, Object target, Runnable action) {
			this.action = action;
			this.target = target;
			this.type = type;
		}
		
		@Override
		public void run() {
			action.run();
		}
		
		public SchemeClickType getActionType() {
			return type;
		}
		
		public Scheme getScheme() {
			return Scheme.this;
		}
		
		public Object getTarget() {
			return target;
		}
		
	}
	
	public enum SchemeClickType{
		NONE,
		SELECT,
		DESELECT,
		DRAG_START,
		DRAG_STOP,
		WIRING;
		
	}
	
}
