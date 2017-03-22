package acceleration;

import java.util.ArrayList;
import java.util.List;

import math.Point;
import math.Ray;
import math.Transformation;
import shape.AxisAlignedBox;
import shape.Intersection;
import shape.Shape;
import texture.TransparentTexture;

public class BV extends AxisAlignedBox {

	private List<BV> children = new ArrayList<>();
	private Point leftBottom;
	private Point rightTop;
	private List<Shape> shapes = new ArrayList<>();
	
	public BV(Point leftBottom, Point rightTop) {
		super(Transformation.translate(leftBottom.x, leftBottom.y, leftBottom.z)
				.append(Transformation.scale(rightTop.subtract(leftBottom).x, rightTop.subtract(leftBottom).y, rightTop.subtract(leftBottom).z)));
		this.leftBottom = leftBottom;
		this.rightTop = rightTop;
		this.texture = new TransparentTexture();
	}
	
	public List<BV> getChildren() {
		return children;
	}
	
	public void addChild(BV child) {
		this.children.add(child);
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
	
	public Point getLeftBottom() {
		return leftBottom;
	}
	
	public Point getRightTop() {
		return rightTop;
	}
	
	@Override 
	public Point getCentric() {
		return leftBottom.add(getRightTop().subtract(leftBottom).scale(1.0/2));
	}
	
	public void expand(BV other) {
		double x = Math.min(leftBottom.x, other.getLeftBottom().x);
		double y = Math.min(leftBottom.y, other.getLeftBottom().y);
		double z = Math.min(leftBottom.z, other.getLeftBottom().z);
		double x2 = Math.max(rightTop.x, other.getRightTop().x);
		double y2 = Math.max(rightTop.y, other.getRightTop().y);
		double z2 = Math.max(rightTop.z, other.getRightTop().z);
		this.leftBottom = new Point(x, y, z);
		this.rightTop = new Point(x2, y2, z2);
		this.transformation = Transformation.translate(leftBottom.x, leftBottom.y, leftBottom.z)
				.append(Transformation.scale(rightTop.subtract(leftBottom).x, rightTop.subtract(leftBottom).y, rightTop.subtract(leftBottom).z));
		this.addAllShapes(other.getShapes());
	}
	
	/*@Override
	public Intersection getIntersection(Ray ray) {
		return super.getIntersection(ray);
	}*/
	
	public Intersection getIntersection(Ray ray) {
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
	}
}
