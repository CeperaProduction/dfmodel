package me.cepera.dfmodel.data;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.sun.istack.internal.Nullable;

import me.cepera.dfmodel.util.CheckedBiConsumer;
import me.cepera.dfmodel.util.CheckedFunction;

public class ByteDataUtils {

	public static <T extends IByteDataSerializable> T readSubData(ByteDataInputStream data,
			Supplier<T> instanceFactory) throws IOException {
		return readSubData(data, b->{
			T obj = instanceFactory.get();
			obj.readData(b);
			return obj;
		});
	}
	
	public static <T> T readSubData(ByteDataInputStream data, 
			CheckedFunction<ByteDataInputStream, T, IOException> serializator) throws IOException{
		return serializator.applyChecked(ByteDataUtils.readSubData(data));
	}
	
	public static ByteDataInputStream readSubData(ByteDataInputStream data) throws IOException {
		try {
			return readSubData(data, data.readInt());
		}catch (EOFException e) {
			return null;
		}
	}
	
	@Nullable
	public static ByteDataInputStream readSubData(ByteDataInputStream data, int size) throws IOException {
		if(size < 0) return null;
		byte[] bytes = new byte[size];
		if(data.read(bytes) < 0) return null;
		return new ByteDataInputStream(bytes);
	}
	
	public static void writeSubData(ByteDataOutputStream data, ByteDataOutputStream subData) throws IOException {
		data.writeInt(subData.getByteOutputStream().size());
		subData.getByteOutputStream().writeTo(data);
	}
	
	public static void writeSubData(ByteDataOutputStream data, IByteDataSerializable subData) throws IOException {
		ByteDataOutputStream content = makeOutData();
		subData.writeData(content);
		writeSubData(data, content);
	}
	
	public static ByteDataOutputStream makeOutData() {
		return new ByteDataOutputStream();
	}
	
	public static ByteDataInputStream makeInData(byte[] bytes) {
		return new ByteDataInputStream(bytes);
	}
	
	public static <T> void writeSubDataList(ByteDataOutputStream data, List<T> list, 
			CheckedBiConsumer<T, ByteDataOutputStream, IOException> serializator) throws IOException {
		ByteDataOutputStream content = makeOutData();
		for(T e : list) {
			ByteDataOutputStream d = makeOutData();
			serializator.acceptChecked(e, d);
			writeSubData(content, d);
		}
		writeSubData(data, content);
	}
	
	public static void writeSubDataList(ByteDataOutputStream data, List<? extends IByteDataSerializable> list) throws IOException{
		writeSubDataList(data, list, IByteDataSerializable::writeData);
	}
	
	public static <T> List<T> readSubDataList(ByteDataInputStream data, 
			CheckedFunction<ByteDataInputStream, T, IOException> deserializator) throws IOException{
		List<T> list = new ArrayList<T>();
		ByteDataInputStream content = readSubData(data);
		while(content != null) {
			ByteDataInputStream d = readSubData(content);
			if(d == null) break;
			T obj = deserializator.applyChecked(d);
			if(obj != null) list.add(obj);
		}
		return list;
	}
	
	public static <T extends IByteDataSerializable> List<T> readSubDataList(ByteDataInputStream data,
			Supplier<T> instanceFactory) throws IOException{
		return readSubDataList(data, (CheckedFunction<ByteDataInputStream, T, IOException>)b->{
			T obj = instanceFactory.get();
			if(obj == null) return null;
			obj.readData(b);
			return obj;
		});
	}
	
}
