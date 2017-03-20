package shape;

import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Vector;

public class Intersection {
	
	private Point coördinate;
	private Shape shape;
	private Ray ray;
	private Vector normal;
	public RGBSpectrum color;
	private int depth = 1;

	public Intersection(Point coördinate, Shape shape, Ray ray, Vector normal, RGBSpectrum color, int depth) {
		this.coördinate = coördinate;
		this.shape = shape;
		this.ray = ray;
		this.normal = normal;
		this.color = color;
		this.depth = depth;
	}

	public Intersection(Point coördinate, Shape shape, Ray ray, Vector normal, RGBSpectrum color) {
		this(coördinate, shape, ray, normal, color, 1);
	}
	public Point getCoördinate() {
		return coördinate;
	}

	public Shape getShape() {
		return shape;
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
	}
	
	public Ray getRay() {
		return ray;
	}
	public double getDistance() {
		Vector distanceVector = ray.origin.subtract(coördinate);
		return distanceVector.length();
	}

	public Vector getNormal() {
		return normal;
	}

	public RGBSpectrum getColor() {
		return color;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public void incrementDepth() {
		depth += 1;
	}
	
	@Override
	public String toString() {
		return "Intersection: Coördinate: \n" + this.getCoördinate() + ", \nColor: " + this.getColor()
			+ ",\nShape: " + getShape();
				
	}

}
