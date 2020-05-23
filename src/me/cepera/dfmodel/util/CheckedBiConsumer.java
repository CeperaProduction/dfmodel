package me.cepera.dfmodel.util;

import java.util.function.BiConsumer;

public interface CheckedBiConsumer<T, U, E extends Throwable> extends BiConsumer<T, U>{
	
	@Override
	default void accept(T t, U u) {
		try {
			acceptChecked(t, u);
		}catch (Throwable e) {
			throw new CheckedFunctionException(e);
		}
	}
	
	public void acceptChecked(T t, U u) throws E;
	
}