package me.cepera.dfmodel.util;

import java.util.function.Function;

public interface CheckedFunction<T, R, E extends Throwable> extends Function<T, R>{
	
	@Override
	default R apply(T t) {
		try {
			return applyChecked(t);
		}catch (Throwable e) {
			throw new CheckedFunctionException(e);
		}
	}
	
	public R applyChecked(T t) throws E;
	
}