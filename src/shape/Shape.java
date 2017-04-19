package shape;

import acceleration.BV;
import math.Point;
import math.Ray;
import math.Vector;
import util.Pair;

/**
 * Interface which should be implemented by all shapes.
 * 
 * @author Niels Billen
 * @version 0.3
 */
public interface Shape {
	/**
	 * Returns whether the given ray intersects this shape. Returns false when
	 * the given ray is null.
	 * 
	 * @param ray
	 *            the ray to intersect with.
	 * @return true when the given ray intersects this shape.
	 */
	public Intersection getIntersection(Ray ray);
	
	public Vector getNormal(Point p);
	
	public boolean isTwoSided();
	
	public Point getCentric();

	public BV createNewBV();
	
	public Pair<Double, Double> getUV(Point p);
}
