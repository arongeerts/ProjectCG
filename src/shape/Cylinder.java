package shape;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import texture.UniformColorTexture;
import util.Pair;

public class Cylinder implements Shape {

	
	public Cylinder(){}
	
	@Override
	public Intersection getIntersection(Ray ray) {
		Vector o = ray.origin.toVector();
		Vector dir = ray.direction;
		Vector va = new Vector(0,1,0);
		double a = dir.subtract(va.scale(dir.dot(va))).lengthSquared();
		double b = 2.0*(dir.subtract(va.scale(dir.dot(va)))).dot(o.subtract(va.scale(o.dot(va))));
		double c = (o.subtract(va.scale(o.dot(va)))).lengthSquared() - 1.0;
		double d = b * b - 4.0 * a * c;

		if (d < 0)
			return null;
		double dr = Math.sqrt(d);

		// numerically solve the equation a*t^2 + b * t + c = 0
		double q = -0.5 * (b < 0 ? (b - dr) : (b + dr));

		double tSol1 = q / a;
		double tSol2 = c / q;
		double tSolmin = Double.MAX_VALUE;
		if (tSol1 < 0 && tSol2 < 0) {
			return null;
		} else if (tSol1 < 0) {
			tSolmin = tSol2;
		} else if (tSol2 < 0) {
			tSolmin = tSol1;
		} else {
			tSolmin = Math.min(tSol1, tSol2);
		}
		double t1 = Double.MAX_VALUE;
		Point p = o.toPoint().add(dir.scale(tSolmin));
		if (p.y > 0 && p.y < 1.0) {
			t1 = tSolmin;
		}

		double ty1 = (-o.y)/dir.y;
		double ty2 = (1.0-o.y)/dir.y;
		
		double tymin = Double.MAX_VALUE;
		if (ty1 < 0 && ty2 < 0) {
			return null;
		} else if (ty1 < 0) {
			tymin = ty2;
		} else if (ty2 < 0) {
			tymin = ty1;
		} else {
			tymin = Math.min(ty1, ty2);
		}
		
		Point IPPlaneTransformed = o.toPoint().add(dir.scale(tymin));
		double t2 = Double.MAX_VALUE;
		if (Math.pow(IPPlaneTransformed.x, 2) + Math.pow(IPPlaneTransformed.z, 2) < 1.0) {
			t2 = tymin;
		}
		double closest = Math.min(t1, t2);
		if (closest == Double.MAX_VALUE) {
			return null;
		}
		Point intersectionPoint = ray.origin.add(ray.direction.scale(closest));
		
		return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint));
		
	}

	@Override
	public Vector getNormal(Point p) {
		double bias = Math.pow(10,  -10);
		if (p.y < bias && p.y > (-bias)) {
			return new Vector(0,-1,0);
		}
		if (p.y < 1.0+bias && p.y > (1.0-bias)) {
			return new Vector(0,1,0);
		}
		return new Vector(p.x, 0, p.z);
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}

	@Override
	public Point getCentric() {
		return new Point(0,0.5,0);
	}

	@Override
	public Pair<Double, Double> getUV(Point p) {
		return new Pair<Double, Double>((Math.atan(p.x/p.z)+Math.PI/2)/Math.PI, 1-p.y);
	}
	
	@Override
	public BV createNewBV() {
		/*Point A = transformation.transform(new Point(0,0,0));
		Point B = transformation.transform(new Point(0,1,0));
		double radius = transformation.transform(new Vector(1,0,0)).length();
		double lx = Math.min(A.x, B.x) - radius;
		double ly = Math.min(A.y, B.y) - radius;
		double lz = Math.min(A.z, B.z) - radius;
		double rx = Math.max(A.x, B.x) + radius;
		double ry = Math.max(A.y, B.y) + radius;
		double rz = Math.max(A.z, B.z) + radius;
		Point leftbottom = new Point(lx, ly, lz);
		Point righttop = new Point(rx, ry, rz);
		BV bv = new BV(leftbottom, righttop);
		bv.addShape(new ShapeInstance(this, transformation, texture));
		return bv;*/
		BV bv = new BV(new Point(-1,-1,-1), new Vector(2, 0, 0),
				new Vector(0, 1, 0), new Vector(0, 0, 2));
		bv.addShape(this);
		return bv;
	}
}
