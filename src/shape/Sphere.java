package shape;

import acceleration.BV;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import util.Pair;

/**
 * Represents a three-dimensional sphere with radius one, centered at the
 * origin, which is transformed by a transformation.
 * 
 * @author Niels Billen
 * @version 0.3
 */
public class Sphere implements Shape {


	/**
	 * Creates a new unit sphere at the origin, transformed by the given
	 * transformation.
	 * 
	 * @param transformation
	 *            the transformation applied to this sphere.
	 * @throws NullPointerException
	 *             when the transformation is null.
	 */
	public Sphere() {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see shape.Shape#intersect(geometry3d.Ray3D)
	 */
	@Override
	public Intersection getIntersection(Ray ray) {
		if (ray == null)
			return null;

		Vector o = ray.origin.toVector();

		double a = ray.direction.lengthSquared();
		double b = 2.0 * (ray.direction.dot(o));
		double c = o.dot(o) - 1.0;

		double d = b * b - 4.0 * a * c;

		if (d < 0)
			return null;
		double dr = Math.sqrt(d);

		// numerically solve the equation a*t^2 + b * t + c = 0
		double q = -0.5 * (b < 0 ? (b - dr) : (b + dr));

		double t0 = q / a;
		double t1 = c / q;
		double closest = 0.0;
		double bias = Math.pow(10, -6);
		if (t0 < bias && t1 < bias) {
			return null;
		} else if (t0 < bias) {
			closest = t1;
		} else if (t1 < bias) {
			closest = t0;
		} else {
			closest = Math.min(t0, t1);
		}
		Point intersectionPoint = ray.origin.add(ray.direction.scale(closest));
		return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint));
	}

	@Override
	public Pair<Double, Double> getUV(Point p) {
		return new Pair<Double, Double>((Math.atan(p.x/p.z) + Math.PI/2)/(Math.PI), Math.acos(p.y) / Math.PI);
	}

	@Override
	public Vector getNormal(Point p) {
		return p.toVector().normalize();
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}
	
	@Override
	public Point getCentric() {
		return new Point(0,0,0);
	}

	@Override
	public BV createNewBV(Transformation transformation) {
		BV bv = new BV(transformation.transform(new Point(-1,-1,-1))
				, transformation.transform(new Point(1,1,1)));
		bv.addShape(this);
		return bv;		
	}
}
