package shape;

import acceleration.BV;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import util.Pair;

public class ShapeInstance implements Shape {

	
	public Shape shape;
	public Transformation transformation;
	public Texture texture;
	
	public ShapeInstance(Shape shape, Transformation transformation, Texture texture) {
		this.shape = shape;
		this.transformation = transformation;
		this.texture = texture;
	}
	@Override
	public Intersection getIntersection(Ray ray) {
		Ray raytransformed = transformation.transformInverse(ray);
		Intersection i = shape.getIntersection(raytransformed);
		if (i == null) {
			return null;
		} if (! (i.getShape() instanceof BV)) {
			i.setColor(texture.evaluate(i.getShape().getUV(i.getCoördinate())));
		}
		return transformation.transform(i);
	}


	@Override
	public Vector getNormal(Point p) {
		return transformation.transform(shape.getNormal(p));
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}

	@Override
	public Point getCentric() {
		return transformation.transform(shape.getCentric());
	}

	@Override
	public BV createNewBV() {
		return shape.createNewBV();
	}
	
	@Override
	public Pair<Double, Double> getUV(Point p) {
		return shape.getUV(transformation.transformInverse(p));
	}

}
