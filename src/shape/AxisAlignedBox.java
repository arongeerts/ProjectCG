package shape;

import acceleration.BV;
import math.Point;
import math.Ray;
import math.Vector;
import util.Pair;

public class AxisAlignedBox implements Shape {

	
	

	/**
	 * Create a new Unit Cube transformed by the given transformation
	 * @param transformation
	 * 		The transformation to apply on the unit cube.
	 *
	 * 			
	 */
	public AxisAlignedBox() {};
	@Override
	public Intersection getIntersection(Ray ray) {
		if (ray == null) {
			return null;
		}
		
		Vector o = ray.origin.toVector();
		Vector dir = ray.direction;
		double tmin = 0;
		double tmax = Double.MAX_VALUE;
		
		if (dir.x != 0.0) {
			double tx1 = -o.x/dir.x;
			double tx2 = (1.0-o.x)/dir.x;
			
			tmin = Math.min(tx1, tx2);
			tmax = Math.max(tx1, tx2);
		}
		
		if (dir.y != 0.0) {
			double ty1 = -o.y/dir.y;
			double ty2 = (1.0-o.y)/dir.y;
		
			tmin = Math.max(tmin, Math.min(ty1, ty2));
			tmax = Math.min(tmax, Math.max(ty1, ty2));
		}
		if (dir.z != 0.0) {
			double tz1 = -o.z/dir.z;
			double tz2 = (1.0-o.z)/dir.z;
			
			tmin = Math.max(tmin, Math.min(tz1, tz2));
			tmax = Math.min(tmax, Math.max(tz1, tz2));
		}

		if (tmin <= tmax && tmax > 0) {
			Point intersectionPoint;
			if (tmin > 0) {
				intersectionPoint = ray.origin.add(ray.direction.scale(tmin));
			} else {
				intersectionPoint = ray.origin.add(ray.direction.scale(tmax));
			}
			return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint));
		}
		
		return null;
	}

	@Override
	public Pair<Double, Double> getUV(Point p) {
		return new Pair<Double, Double>(p.x, 1-p.y);
	}

	@Override
	public Vector getNormal(Point p) {
		double bias = Math.pow(10, -8);
		if (p.x < bias && p.x > (-bias)) {
			return new Vector(-1, 0, 0);
		}
		if (p.x < (1.0+bias) && p.x > (1.0-bias)) {
			return new Vector(1, 0, 0);
		}
		if (p.y < bias && p.y > (-bias)) {
			return new Vector(0, -1, 0);
		}
		if (p.y < (1.0+bias) && p.y > (1.0-bias)) {
			return new Vector(0, 1, 0);
		}
		if (p.z < bias && p.z > (-bias)) {
			return new Vector(0, 0, -1);
		}
		if (p.z < (1.0+bias) && p.z > (1.0-bias)) {
			return new Vector(0, 0, 1);
		}
		
		return null;
	}
	
	public boolean isTwoSided() {
		return false ;
	}
	

	@Override
	public Point getCentric() {
		return new Point(0.5,0.5,0.5);
	}

	@Override
	public BV createNewBV() {
		/*Point A = transformation.transform(new Point(0,0,0));
		Point B = transformation.transform(new Point(1,1,1));
		double maxdiff = transformation.transform(new Vector(1,0,0)).length();
		double lx = Math.min(A.x, B.x) - maxdiff;
		double ly = Math.min(A.y, B.y) - maxdiff;
		double lz = Math.min(A.z, B.z) - maxdiff;
		double rx = Math.max(A.x, B.x) + maxdiff;
		double ry = Math.max(A.y, B.y) + maxdiff;
		double rz = Math.max(A.z, B.z) + maxdiff;
		Point leftbottom = new Point(lx, ly, lz);
		Point righttop = new Point(rx, ry, rz);
		BV bv = new BV(leftbottom, righttop);
		bv.addShape(new ShapeInstance(this, transformation, texture));*/
		BV bv = new BV(new Point(0,0,0), new Vector(1,0,0),
				new Vector(0,1,0), new Vector(0,0,1));
		bv.addShape(this);
		return bv;
	}
}
