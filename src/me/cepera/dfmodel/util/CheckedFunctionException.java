package me.cepera.dfmodel.util;

public class CheckedFunctionException extends RuntimeException{
	public CheckedFunctionException(Throwable e) {
		super(e);
	}
}