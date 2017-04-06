package shape;

import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Vector;

public class Intersection {
	
	private Point co�rdinate;
	private Shape shape;
	private Ray ray;
	private Vector normal;
	public RGBSpectrum color;

	public Intersection(Point co�rdinate, Shape shape, Ray ray, Vector normal) {
		this(co�rdinate, shape, ray, normal, null);
	}
	
	public Intersection(Point co�rdinate, Shape shape, Ray ray, Vector normal, RGBSpectrum color) {
		this.co�rdinate = co�rdinate;
		this.shape = shape;
		this.ray = ray;
		this.normal = normal;
		this.color = color;
	}

	public Point getCo�rdinate() {
		return co�rdinate;
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
		Vector distanceVector = ray.origin.subtract(co�rdinate);
		return distanceVector.length();
	}

	public Vector getNormal() {
		return normal;
	}

	public RGBSpectrum getColor() {
		return color;
	}
	
	public void setColor(RGBSpectrum color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return "Intersection: Co�rdinate: \n" + this.getCo�rdinate() + ", \nColor: " + this.getColor()
			+ ",\nShape: " + getShape();
				
	}

}
