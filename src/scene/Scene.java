package scene;

import java.util.List;

import acceleration.BVH;
import light.LightSource;
import shape.Shape;
import shape.ShapeInstance;

public class Scene {

	private List<LightSource> lightsources;
	private List<ShapeInstance> shapes;

	
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
}
