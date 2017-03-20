package shape;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import texture.UniformColorTexture;

public class AxisAlignedBox implements Shape {

	
	public Transformation transformation;
	
	private boolean isTwoSided = false;

	protected Texture texture;

	/**
	 * Create a new Unit Cube transformed by the given transformation
	 * @param transformation
	 * 		The transformation to apply on the unit cube.
	 *
	 * 			
	 */
	public AxisAlignedBox(Transformation transformation) {
		if (transformation == null)
			throw new NullPointerException("the given transformation is null!");
		this.transformation = transformation;
		this.texture = new UniformColorTexture(new RGBSpectrum(255,255,255));
	}
	
	public AxisAlignedBox(Transformation transformation, Texture texture) {
		if (transformation == null)
			throw new NullPointerException("the given transformation is null!");
		this.transformation = transformation;
		this.texture = texture;
	}
	@Override
	public Intersection getIntersection(Ray ray) {
		if (ray == null) {
			return null;
		}
		Ray transformed = transformation.transformInverse(ray);
		Vector o = transformed.origin.toVector();
		
		double tmin = 0;
		double tmax = Double.MAX_VALUE;
		
		if (transformed.direction.x != 0.0) {
			double tx1 = -o.x/transformed.direction.x;
			double tx2 = (1.0-o.x)/transformed.direction.x;
			
			tmin = Math.min(tx1, tx2);
			tmax = Math.max(tx1, tx2);
		}
		
		if (transformed.direction.y != 0.0) {
			double ty1 = -o.y/transformed.direction.y;
			double ty2 = (1.0-o.y)/transformed.direction.y;
		
			tmin = Math.max(tmin, Math.min(ty1, ty2));
			tmax = Math.min(tmax, Math.max(ty1, ty2));
		}
		if (transformed.direction.z != 0.0) {
			double tz1 = -o.z/transformed.direction.z;
			double tz2 = (1.0-o.z)/transformed.direction.z;
			
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
			return new Intersection(intersectionPoint, this, ray, getNormal(intersectionPoint), getColor(intersectionPoint));
		}
		
		return null;
	}

	@Override
	public RGBSpectrum getColor(Point p) {
		Point p_t = transformation.transformInverse(p);
		return texture.evaluate(p_t.x, 1-p_t.y);
	}

	@Override
	public Vector getNormal(Point p) {
		double bias = Math.pow(10, -8);
		Point p_t = transformation.transformInverse(p);
		if (p_t.x < bias && p_t.x > (-bias)) {
			return transformation.transform(new Vector(-1, 0, 0));
		}
		if (p_t.x < (1.0+bias) && p_t.x > (1.0-bias)) {
			return transformation.transform(new Vector(1, 0, 0));
		}
		if (p_t.y < bias && p_t.y > (-bias)) {
			return transformation.transform(new Vector(0, -1, 0));
		}
		if (p_t.y < (1.0+bias) && p_t.y > (1.0-bias)) {
			return transformation.transform(new Vector(0, 1, 0));
		}
		if (p_t.z < bias && p_t.z > (-bias)) {
			return transformation.transform(new Vector(0, 0, -1));
		}
		if (p_t.z < (1.0+bias) && p_t.z > (1.0-bias)) {
			return transformation.transform(new Vector(0, 0, 1));
		}
		
		return null;
	}
	
	public boolean isTwoSided() {
		return isTwoSided ;
	}
	
	public void setTwoSided(boolean b) {
		this.isTwoSided = b;
	}

	@Override
	public Point getCentric() {
		return transformation.transform(new Point(0.5,0.5,0.5));
	}

	@Override
	public BV createNewBV() {
		Point A = transformation.transform(new Point(0,0,0));
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
		bv.addShape(this);
		return bv;
	}
}
