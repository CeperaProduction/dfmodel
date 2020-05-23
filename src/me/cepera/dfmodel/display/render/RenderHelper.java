package me.cepera.dfmodel.display.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class RenderHelper {
	
	public static void rotate(GraphicsContext gc, double angle, double rotationPointX, double rotationPointY) {
        Rotate r = new Rotate(angle, rotationPointX, rotationPointY);
        Affine a = gc.getTransform();
        a.append(r);
        gc.setTransform(a);
    }
	
	public static void translate(GraphicsContext gc, double x, double y) {
		translate(gc, x, y, 0);
	}
	
	public static void translate(GraphicsContext gc, double x, double y, double z) {
        Translate t = new Translate(x, y, z);
        Affine a = gc.getTransform();
        a.append(t);
        gc.setTransform(a);
    }
	
}
