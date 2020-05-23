package me.cepera.dfmodel;

public enum Rotation {
	DEG_0(0),
	DEG_90(90),
	DEG_180(180),
	DEG_270(270);
	
	private final double degreesValue;
	
	private Rotation(double degreesValue) {
		this.degreesValue = degreesValue;
	}
	
	public double getDegreesValue() {
		return degreesValue;
	}
	
	public Rotation right() {
		Rotation[] v = values();
		return v[(ordinal()+1) % v.length];
	}
	
	public Rotation left() {
		Rotation[] v = values();
		int o = ordinal() - 1;
		if(o < 0) o = v.length-1;
		return v[o];
	}
	
}
