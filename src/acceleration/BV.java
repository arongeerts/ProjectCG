package acceleration;

import java.util.ArrayList;
import java.util.List;

import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import shape.AxisAlignedBox;
import shape.Intersection;
import shape.Shape;

public class BV extends AxisAlignedBox {

	private List<BV> children = new ArrayList<>();
	private List<Shape> shapes = new ArrayList<>();
	protected Vector x;
	protected Vector y;
	protected Vector z;
	protected Point leftBottom;
	
	public BV(Point leftBottom, Vector x, Vector y, Vector z) {
		this.leftBottom = leftBottom;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BV(Point leftBottom, Point rightTop) {
		this(leftBottom, new Vector(rightTop.subtract(leftBottom).x,0,0),
				new Vector(0,rightTop.subtract(leftBottom).y, 0), new Vector(0,0,rightTop.subtract(leftBottom).z));
	}

	public List<BV> getChildren() {
		return children;
	}
	
	public void addChild(BV child) {
		this.children.add(child);
	}
	
	public void setChildren(List<BV> children) {
		this.children = children;
	}
	
	public List<Shape> getShapes() {
		return this.shapes ;
	}

	public void addShape(Shape s) {
		this.shapes.add(s);
	}
	
	public void setShape(List<Shape> shapes) {
		this.shapes = shapes;
	}
	
	public void addAllShapes(List<Shape> shapes) {
		this.shapes.addAll(shapes);
	}
	
	public void addAllChildren(List<BV> children) {
		this.children.addAll(children);
	}
	
	public void clearChildren() {
		this.children = new ArrayList<>();
	}
	
	public void clearShapes() {
		this.shapes = new ArrayList<Shape>();
	}

	public Point getLeftBottom() {
		return leftBottom;
	}
	
	public Point getRightTop() {
		return leftBottom.add(x).add(y).add(z);
	}
	
	@Override
	public BV createNewBV() {
		return this;
	}
	
	@Override 
	public Point getCentric() {
		return leftBottom.add(getRightTop().subtract(leftBottom).scale(1.0/2));
	}
	
	public void expand(BV other) {
		double x = Math.min(leftBottom.x, other.getLeftBottom().x);
		double y = Math.min(leftBottom.y, other.getLeftBottom().y);
		double z = Math.min(leftBottom.z, other.getLeftBottom().z);
		double x2 = Math.max(getRightTop().x, other.getRightTop().x);
		double y2 = Math.max(getRightTop().y, other.getRightTop().y);
		double z2 = Math.max(getRightTop().z, other.getRightTop().z);
		this.leftBottom = new Point(x, y, z);
		this.x = new Vector(x2-x, 0, 0);
		this.y = new Vector(0, y2-y, 0);
		this.z = new Vector(0, 0, z2-z);
		this.addAllShapes(other.getShapes());
	}
	
	public double getSurfaceArea() {
		double sides = x.length() * y.length() * 2;
		double sides2 = z.length() * y.length() * 2;
		double sides3 = x.length() * z.length() * 2;
		double result = 0.25*(sides + sides2 + sides3);
		if (result == 0 || Double.isNaN(result)) {
			System.out.println("surface area is 0");
		}
		return result;
	}
	
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
			double tx1 = (leftBottom.x-o.x)/dir.x;
			double tx2 = (getRightTop().x-o.x)/dir.x;
			
			tmin = Math.min(tx1, tx2);
			tmax = Math.max(tx1, tx2);
		}
		
		if (dir.y != 0.0) {
			double ty1 = (leftBottom.y-o.y)/dir.y;
			double ty2 = (getRightTop().y-o.y)/dir.y;
		
			tmin = Math.max(tmin, Math.min(ty1, ty2));
			tmax = Math.min(tmax, Math.max(ty1, ty2));
		}
		if (dir.z != 0.0) {
			double tz1 = (leftBottom.z-o.z)/dir.z;
			double tz2 = (getRightTop().z-o.z)/dir.z;
			
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
	
	public BV getTransformedBV(Transformation transformation) {
		Point lb = transformation.transform(getLeftBottom());
		Vector newx = transformation.transform(x);
		Vector newy = transformation.transform(y);
		Vector newz = transformation.transform(z);
		List<Point> points = new ArrayList<>();
		points.add(lb);
		points.add(lb.add(newx));
		points.add(lb.add(newy));
		points.add(lb.add(newz));
		points.add(lb.add(newx).add(newy));
		points.add(lb.add(newz).add(newy));
		points.add(lb.add(newx).add(newz));
		points.add(lb.add(newx).add(newy).add(newz));
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double minz = Double.MAX_VALUE;
		double maxx = -Double.MAX_VALUE;
		double maxy = -Double.MAX_VALUE;
		double maxz = -Double.MAX_VALUE;
		for (Point point : points) {
			minx = Math.min(minx,  point.x);
			miny = Math.min(miny,  point.y);
			minz = Math.min(minz,  point.z);
			maxx = Math.max(maxx,  point.x);
			maxy = Math.max(maxy,  point.y);
			maxz = Math.max(maxz,  point.z);	
		}
		BV bv = new BV(new Point(minx, miny, minz),
				new Point(maxx, maxy, maxz));
		
		return bv;
	}
}
