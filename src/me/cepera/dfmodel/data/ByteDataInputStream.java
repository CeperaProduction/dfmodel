package me.cepera.dfmodel.data;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteDataInputStream extends InputStream implements DataInput{

	private final ByteArrayInputStream byteInput;
	private final DataInputStream dataInput;
	
	public ByteDataInputStream(byte[] bytes) {
		this(new ByteArrayInputStream(bytes));
	}
	
	public ByteDataInputStream(ByteArrayInputStream byteInputStream) {
		byteInput = byteInputStream;
		dataInput = new DataInputStream(byteInputStream);
	}
	
	public ByteArrayInputStream getByteInputStream() {
		return byteInput;
	}

	@Override
	public void readFully(byte[] b) throws IOException {
		dataInput.readFully(b);
	}

	@Override
	public void readFully(byte[] b, int off, int len) throws IOException {
		dataInput.readFully(b, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return dataInput.skipBytes(n);
	}

	@Override
	public boolean readBoolean() throws IOException {
		return dataInput.readBoolean();
	}

	@Override
	public byte readByte() throws IOException {
		return dataInput.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return dataInput.readUnsignedByte();
	}

	@Override
	public short readShort() throws IOException {
		return dataInput.readShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		return dataInput.readUnsignedShort();
	}

	@Override
	public char readChar() throws IOException {
		return dataInput.readChar();
	}

	@Override
	public int readInt() throws IOException {
		return dataInput.readInt();
	}

	@Override
	public long readLong() throws IOException {
		return dataInput.readLong();
	}

	@Override
	public float readFloat() throws IOException {
		return dataInput.readFloat();
	}

	@Override
	public double readDouble() throws IOException {
		return dataInput.readDouble();
	}

	@Override
	@Deprecated
	public String readLine() throws IOException {
		return dataInput.readLine();
	}

	@Override
	public String readUTF() throws IOException {
		return dataInput.readUTF();
	}

	@Override
	public int read() throws IOException {
		return dataInput.read();
	}
	
	@Override
	public int available() throws IOException {
		return dataInput.available();
	}
	
	@Override
	public void close() throws IOException {
		dataInput.close();
	}
	
	@Override
	public synchronized void mark(int readlimit) {
		dataInput.mark(readlimit);
	}
	
	@Override
	public boolean markSupported() {
		return dataInput.markSupported();
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		return dataInput.read(b);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return dataInput.read(b, off, len);
	}
	
	@Override
	public void reset() throws IOException {
		dataInput.reset();
	}
	
	@Override
	public long skip(long n) throws IOException {
		return dataInput.skip(n);
	}

}
