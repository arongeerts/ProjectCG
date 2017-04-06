package shape;

import java.util.List;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import texture.Texture;
import texture.UniformColorTexture;
import util.Pair;

public class MeshTriangle implements Shape {

	
	public Point a;
	public Point b;
	public Point c;
	private Vector normalA;
	private Vector normalB;
	private Vector normalC;
	private List<Double> textureCoordinates;

	public MeshTriangle(Point a, Point b, Point c, Vector normalA, Vector normalB, Vector normalC, Texture texture, List<Double> textureCoordinates) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.normalA = normalA;
		this.normalB = normalB;
		this.normalC = normalC;
		this.textureCoordinates = textureCoordinates; 
	}
	
	public MeshTriangle(Point a, Point b, Point c, Vector normalA, Vector normalB, Vector normalC, List<Double> tc) {
			this(a,b,c,normalA,normalB,normalC, new UniformColorTexture(new RGBSpectrum(255,255,255)), tc);
		}
	@Override
	public Intersection getIntersection(Ray ray) {
        Vector u = b.subtract(a);
        Vector v = c.subtract(a);
        Vector n = u.cross(v);
        if (n.length() == 0) {
            return null;
        }
        
        Vector dir = ray.direction;
        Vector v1 = ray.origin.subtract(a);
        double x = - n.dot(v1);
        double y = n.dot(dir);
        
        if ((double)Math.abs(y) < Math.pow(10, -10)) {
            return null;
        }
        
        double r = x / y;
        if (r < Math.pow(10, -6)) {
            return null;
        }
        
        Point I = ray.origin;
        I = I.add(ray.direction.scale(r));
        Vector bc = getBarycentricCoördinates(I);
        if (bc.x >= 0 && bc.x <= 1 && bc.y >= 0 && bc.y <= 1 && bc.z >= 0 && bc.z <= 1) {
        	return new Intersection(I, this, ray, getNormal(I));
        }
        return null;
	}

	@Override
	public Pair<Double, Double> getUV(Point p) {
		Vector bc = getBarycentricCoördinates(p);
		double u = bc.x * textureCoordinates.get(0) + bc.y * textureCoordinates.get(2)
			+ bc.z * textureCoordinates.get(4);
		double v = bc.x * textureCoordinates.get(1) + bc.y * textureCoordinates.get(3)
		+ bc.z * textureCoordinates.get(5);
		return new Pair<Double, Double>(u,1-v);
	}

	@Override
	public Vector getNormal(Point p_t) {
		Vector bc = getBarycentricCoördinates(p_t);
		Vector normal = normalA.scale(bc.x).add(normalB.scale(bc.y)).add(normalC.scale(bc.z));
		return normal.normalize();
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}

	private Vector getBarycentricCoördinates(Point p) {
		Vector v0 = b.subtract(a);
		Vector v1 = c.subtract(a);
		Vector v2 = p.subtract(a);
	    double d00 = v0.dot(v0);
	    double d01 = v0.dot(v1);
	    double d11 = v1.dot(v1);
	    double d20 = v2.dot(v0);
	    double d21 = v2.dot(v1);
	    double denom = d00 * d11 - d01 * d01;
	    double beta = (d11 * d20 - d01 * d21) / denom;
	    double gamma = (d00 * d21 - d01 * d20) / denom;
	    double alpha = 1.0f - beta - gamma;
	    return new Vector(alpha, beta, gamma);
	}
	
	@Override
	public Point getCentric() {
		return a.scale(1.0/3).add(b.scale(1.0/3).toVector()).add(c.scale(1.0/3).toVector());
	}
	
	public BV createNewBV(Transformation transformation) {
		double bias = Math.pow(10, -5);
		double minx = Math.min(Math.min(a.x - bias, b.x - bias), c.x - bias);
		double miny = Math.min(Math.min(a.y - bias, b.y - bias), c.y - bias);
		double minz = Math.min(Math.min(a.z - bias, b.z - bias), c.z - bias);
		Point leftBottom = new Point(minx, miny, minz);
		double maxx = Math.max(Math.max(a.x + bias, b.x + bias), c.x + bias);
		double maxy = Math.max(Math.max(a.y + bias, b.y + bias), c.y + bias);
		double maxz = Math.max(Math.max(a.z + bias, b.z + bias), c.z + bias);
		Point rightTop = new Point(maxx, maxy, maxz);
		BV bv = new BV(leftBottom, rightTop);
		bv.addShape(this);
		return bv;
	}
}
