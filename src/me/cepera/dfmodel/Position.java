package me.cepera.dfmodel;

import java.io.IOException;

import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.data.IByteDataSerializable;

public class Position implements IByteDataSerializable{

	private double x, y;
	
	public Position(){}
	
	public Position(double x, double y) {
		set(x, y);
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void change(double x, double y) {
		this.x += x;
		this.y += y;
	}
	
	public Position plus(Position other) {
		Position result = copy();
		result.change(other.x, other.y);
		return result;
	}
	
	public Position minus(Position other) {
		Position result = copy();
		result.change(-other.x, -other.y);
		return result;
	}
	
	public double distance(Position other) {
		double dx = other.x - x;
		double dy = other.y - y;
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public Position copy() {
		return new Position(x, y);
	}

	@Override
	public void readData(ByteDataInputStream data) throws IOException{
		x = data.readDouble();
		y = data.readDouble();
	}

	@Override
	public void writeData(ByteDataOutputStream data) throws IOException{
		data.writeDouble(x);
		data.writeDouble(y);
	}
	
}	
