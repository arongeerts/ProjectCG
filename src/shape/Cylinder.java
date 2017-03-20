package shape;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import texture.UniformColorTexture;

public class Cylinder implements Shape {

	private Transformation transformation;
	private Texture texture;
	
	public Cylinder(Transformation transformation, Texture texture) {
		this.transformation = transformation;
		this.texture = texture;
	}
	
	public Cylinder(Transformation transformation) {
		this(transformation, new UniformColorTexture(new RGBSpectrum(255,255,255)));
	}
	
	@Override
	public Intersection getIntersection(Ray ray) {
		Ray transformed = transformation.transformInverse(ray);
		Vector o = transformed.origin.toVector();
		Vector dir = transformed.direction;
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
		Point IPTransformed = transformed.origin.add(transformed.direction.scale(tSolmin));
		if (IPTransformed.y > 0 && IPTransformed.y < 1.0) {
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
		
		Point IPPlaneTransformed = transformed.origin.add(transformed.direction.scale(tymin));
		double t2 = Double.MAX_VALUE;
		if (Math.pow(IPPlaneTransformed.x, 2) + Math.pow(IPPlaneTransformed.z, 2) < 1.0) {
			t2 = tymin;
		}
		double closest = Math.min(t1, t2);
		if (closest == Double.MAX_VALUE) {
			return null;
		}
		Point intersectionPoint = ray.origin.add(ray.direction.scale(closest));
		
		return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint), getColor(intersectionPoint));
		
	}

	@Override
	public RGBSpectrum getColor(Point p) {
		Point p_t = transformation.transformInverse(p);
		return texture.evaluate((Math.atan(p_t.x/p_t.z)+Math.PI/2)/Math.PI, 1-p_t.y);
	}

	@Override
	public Vector getNormal(Point p) {
		Point p_t = transformation.transformInverse(p);
		double bias = Math.pow(10,  -10);
		if (p_t.y < bias && p_t.y > (-bias)) {
			return transformation.transform(new Vector(0,-1,0));
		}
		if (p_t.y < 1.0+bias && p_t.y > (1.0-bias)) {
			return transformation.transform(new Vector(0,1,0));
		}
		return transformation.transform(new Vector(p_t.x, 0, p_t.z));
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}

	@Override
	public Point getCentric() {
		return transformation.transform(new Point(0,0.5,0));
	}

	@Override
	public BV createNewBV() {
		Point A = transformation.transform(new Point(0,0,0));
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
		bv.addShape(this);
		return bv;
				
	}
}
