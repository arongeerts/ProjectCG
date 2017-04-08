package acceleration;

import java.util.ArrayList;
import java.util.List;

import math.Point;
import math.Ray;
import math.Vector;
import shape.AxisAlignedBox;
import shape.Intersection;
import shape.Shape;

public class BV extends AxisAlignedBox {

	private List<BV> children = new ArrayList<>();
	private List<Shape> shapes = new ArrayList<>();
	Vector x;
	Vector y;
	Vector z;
	Point leftBottom;
	
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
	
	/*public Intersection getIntersection(Ray ray) {
		Intersection i = super.getIntersection(ray);
		if (i != null) {
			Intersection currentClosest = null;
			int depth = 1;
			if (children.size() == 0) {
				for (Shape s : getShapes()) {	
					Intersection childIntersect = s.getIntersection(ray);
					if (childIntersect != null) {
						depth += childIntersect.getDepth() + 1;
						if (currentClosest == null || childIntersect.getDistance() < currentClosest.getDistance()) {
							currentClosest = childIntersect;
						}	
					}
				}
				if (currentClosest != null) {
					currentClosest.setDepth(depth);
					return currentClosest;
				} else {
					i.setDepth(depth);
					return i;
				}
				
			} else {
				Intersection i1 = getChildren().get(0).getIntersection(ray);
				Intersection i2 = getChildren().get(1).getIntersection(ray);
				depth += 2;
				
				if (i1 == null && i2 == null) {
					i.setDepth(depth);
					return i;
				} else if (i1 == null) {
					i2.setDepth(i2.getDepth() + depth);
					return i2;
				} else if (i2 == null) {
					i1.setDepth(i1.getDepth() + depth);
					return i1;
				} 
				depth = i1.getDepth() + i2.getDepth();
				if (i1.getShape() instanceof BV && ! (i2.getShape() instanceof BV)) {
					i2.setDepth(depth);
					return i2;
				} else if (i2.getShape() instanceof BV && ! (i1.getShape() instanceof BV)) {
					i1.setDepth(depth);
					return i1;
				} else if (i1.getDistance() < i2.getDistance()) {
					i1.setDepth(depth);
					return i1;
				} else {
					i2.setDepth(depth);
					return i2;
				}
			}
		}
		return null;
	}*/
}
