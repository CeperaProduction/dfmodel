package me.cepera.dfmodel.data;

import java.io.IOException;

public interface IByteDataSerializable{

	public void readData(ByteDataInputStream data) throws IOException;
	
	public void writeData(ByteDataOutputStream data) throws IOException;
	
}
