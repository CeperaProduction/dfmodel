package me.cepera.dfmodel.data;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ByteDataOutputStream extends OutputStream implements DataOutput {

	private final ByteArrayOutputStream byteOutput;
	private final DataOutputStream dataOutput;
	
	public ByteDataOutputStream() {
		this(new ByteArrayOutputStream(0));
	}
	
	public ByteDataOutputStream(int length) {
		this(new ByteArrayOutputStream(length));
	}
	
	public ByteDataOutputStream(ByteArrayOutputStream byteOutputStream) {
		byteOutput = byteOutputStream;
		dataOutput = new DataOutputStream(byteOutputStream);
	}
	
	public ByteArrayOutputStream getByteOutputStream() {
		return byteOutput;
	}
	
	@Override
	public void writeBoolean(boolean v) throws IOException {
		dataOutput.writeBoolean(v);
	}

	@Override
	public void writeByte(int v) throws IOException {
		dataOutput.writeByte(v);
	}

	@Override
	public void writeShort(int v) throws IOException {
		dataOutput.writeShort(v);
	}

	@Override
	public void writeChar(int v) throws IOException {
		dataOutput.writeChar(v);
	}

	@Override
	public void writeInt(int v) throws IOException {
		dataOutput.writeInt(v);
	}

	@Override
	public void writeLong(long v) throws IOException {
		dataOutput.writeLong(v);
	}

	@Override
	public void writeFloat(float v) throws IOException {
		dataOutput.writeFloat(v);
	}

	@Override
	public void writeDouble(double v) throws IOException {
		dataOutput.writeDouble(v);
	}

	@Override
	public void writeBytes(String s) throws IOException {
		dataOutput.writeBytes(s);
	}

	@Override
	public void writeChars(String s) throws IOException {
		dataOutput.writeChars(s);
	}

	@Override
	public void writeUTF(String s) throws IOException {
		dataOutput.writeUTF(s);
	}

	@Override
	public void write(int b) throws IOException {
		dataOutput.write(b);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		dataOutput.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		dataOutput.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		dataOutput.close();
	}
	
	@Override
	public void flush() throws IOException {
		dataOutput.flush();
	}

}
