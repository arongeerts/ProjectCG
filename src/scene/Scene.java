package scene;

import java.util.List;

import acceleration.BVH;
import camera.PerspectiveCamera;
import light.LightSource;
import math.Point;
import math.Vector;
import shape.ShapeInstance;

public class Scene {

	private List<LightSource> lightsources;
	private List<ShapeInstance> shapes;
	private Point origin;
	private Point destination;
	private Vector lookup;
	private int fov = 0;
	private int width = 0;
	private int height = 0;
	

	public Scene(List<LightSource> lightsources, List<ShapeInstance> shapes) {
		this(lightsources, shapes, true);
	}
	
	public Scene(List<LightSource> lightsources, List<ShapeInstance> shapes, boolean accelerated) {
		this.lightsources = lightsources;
		if (! accelerated) {
			this.shapes = shapes;
		} else {
			this.shapes = BVH.createBVH(shapes);
		}
	}

	
	public List<LightSource> getLightsources() {
		return lightsources;
	}

	public List<ShapeInstance> getShapes() {
		return shapes;
	}
	
	
	public void addLightSource(LightSource l) {
		this.lightsources.add(l);
	}
	
	public void addShape(ShapeInstance s) {
		this.shapes.add(s);
	}
	
	public Point getOrigin() {
		return origin;
	}

	public void setOrigin(Point origin) {
		this.origin = origin;
	}

	public Point getDestination() {
		return destination;
	}

	public void setDestination(Point destination) {
		this.destination = destination;
	}

	public Vector getLookup() {
		return lookup;
	}

	public void setLookup(Vector lookup) {
		this.lookup = lookup;
	}

	public int getFov() {
		return fov;
	}

	public void setFov(int fov) {
		this.fov = fov;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
