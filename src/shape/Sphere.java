package shape;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import texture.UniformColorTexture;

/**
 * Represents a three-dimensional sphere with radius one, centered at the
 * origin, which is transformed by a transformation.
 * 
 * @author Niels Billen
 * @version 0.3
 */
public class Sphere implements Shape {
	/**
	 * The transformation which is applied to the sphere to place it in the
	 * scene.
	 */
	public final Transformation transformation;

	public RGBSpectrum color;

	private Texture texture;
	/**
	 * Creates a new unit sphere at the origin, transformed by the given
	 * transformation.
	 * 
	 * @param transformation
	 *            the transformation applied to this sphere.
	 * @throws NullPointerException
	 *             when the transformation is null.
	 */
	public Sphere(Transformation transformation) throws NullPointerException {
		this(transformation, new UniformColorTexture(new RGBSpectrum(255,255,255)));
	}

	public Sphere(Transformation transformation, Texture texture) throws NullPointerException {
		if (transformation == null)
			throw new NullPointerException("the given transformation is null!");
		this.transformation = transformation;
		this.texture = texture;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see shape.Shape#intersect(geometry3d.Ray3D)
	 */
	@Override
	public Intersection getIntersection(Ray ray) {
		if (ray == null)
			return null;
		Ray transformed = transformation.transformInverse(ray);

		Vector o = transformed.origin.toVector();

		double a = transformed.direction.lengthSquared();
		double b = 2.0 * (transformed.direction.dot(o));
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
		return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint), getColor(intersectionPoint));
	}

	@Override
	public RGBSpectrum getColor(Point p) {
		Point p_t = transformation.transformInverse(p);
		return texture.evaluate((Math.atan(p_t.x/p_t.z) + Math.PI/2)/(Math.PI), Math.acos(p_t.y) / Math.PI);
	}

	@Override
	public Vector getNormal(Point p) {
		Point center = transformation.transform(new Point(0,0,0));
		Vector normal = p.subtract(center);
		return normal.divide(normal.length());
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}
	
	@Override
	public Point getCentric() {
		return transformation.transform(new Point(0,0,0));
	}

	@Override
	public BV createNewBV() {
		BV bv = new BV(transformation.transform(new Point(-1,-1,-1))
				, transformation.transform(new Point(1,1,1)));
		bv.addShape(this);
		return bv;		
	}
}
