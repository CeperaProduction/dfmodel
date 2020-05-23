package me.cepera.dfmodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import me.cepera.dfmodel.data.ByteDataInputStream;
import me.cepera.dfmodel.data.ByteDataOutputStream;
import me.cepera.dfmodel.data.ByteDataUtils;

public class FileSaveTools {

	public static final int SCHEME_FILE_VERSION = 1;
	
	public static void saveScheme(Scheme scheme, File file) throws IOException {
		ByteDataOutputStream data = ByteDataUtils.makeOutData();
		data.writeInt(SCHEME_FILE_VERSION);
		ByteDataUtils.writeSubData(data, scheme);
		if(!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
		try(FileOutputStream out = new FileOutputStream(file)){
			data.getByteOutputStream().writeTo(out);
		}
		DFModel.LOGGER.info("Scheme saved to "+file.getAbsolutePath());
	}
	
	public static Scheme loadScheme(File file) throws FileNotFoundException, IOException {
		if(!file.isFile()) throw new FileNotFoundException();
		ByteDataInputStream data = ByteDataUtils.makeInData(Files.readAllBytes(file.toPath()));
		int version = data.readInt();
		if(version != SCHEME_FILE_VERSION) {
			DFModel.LOGGER.warning("Current supported sheme version is '"+SCHEME_FILE_VERSION+"'. Target scheme has version '"+version+"'");
		}
		Scheme scheme = ByteDataUtils.readSubData(data, Scheme::new);
		DFModel.LOGGER.info("Scheme loaded from "+file.getAbsolutePath());
		return scheme;
	}
	
}
