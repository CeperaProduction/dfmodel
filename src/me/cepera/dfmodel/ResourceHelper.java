package me.cepera.dfmodel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class ResourceHelper {

	public final static Charset CHARSET = StandardCharsets.UTF_8;
	
	private static Map<String, Image> imageCache = new HashMap<String, Image>();
	
	private static final Image IMAGE_NOT_FOUND;
	
	public static final Image ICON;
	
	static {
		try(InputStream is = DFModel.class.getResourceAsStream("/assets/dfmodel/images/texture_not_found.png")){
			IMAGE_NOT_FOUND = new Image(is);
		} catch (IOException | NullPointerException e) {
			DFModel.logException("Error while load default image. Looks like program is corrupted.", e);
			throw new Error(e);
		}
		ICON = getOrLoadImage("icon.png");
	}
	
	public static URL getResourceURL(String path) {
		return DFModel.class.getResource("/assets/dfmodel/"+path);
	}
	
	public static Image getOrLoadImage(String imageFileName) {
		Image img = imageCache.get(imageFileName);
		if(img == null) {
			try(InputStream is = DFModel.class.getResourceAsStream("/assets/dfmodel/images/"+imageFileName)){
				img = new Image(is);
			} catch (IOException | NullPointerException e) {
				DFModel.logException("Error while load image '"+imageFileName+"'", e);
				img = IMAGE_NOT_FOUND;
			}
			imageCache.put(imageFileName, img);
		}
		return img;
	}
	
	public static void clearCache() {
		imageCache.clear();
	}
	
}
