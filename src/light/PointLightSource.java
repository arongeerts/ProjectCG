package light;

import java.util.List;

import film.RGBSpectrum;
import main.Renderer;
import math.Point;
import math.Ray;
import math.Vector;
import shape.Intersection;
import shape.Shape;

public class PointLightSource implements LightSource {

	public Point position;
	public RGBSpectrum intensity;

	public PointLightSource(Point position, RGBSpectrum intensity){
		this.position = position;
		this.intensity = intensity;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public RGBSpectrum getIntensity(double distance) {
		if (distance == 0) {
			return intensity;
		}
		return intensity.scale(1/(4.0*Math.PI*Math.pow(distance,2)));
	}
	
	@Override
	public boolean isVisibleFrom(Intersection i, List<Shape> shapes) {
		Point p = i.getCoördinate();
		Vector direction = position.subtract(p);
		double selfIntersectionBias = Math.pow(10, -8);
		p.add(direction.scale(selfIntersectionBias));
		Ray shadowRay = new Ray(p, direction);
		Intersection intersect = Renderer.getClosestIntersection(shadowRay, shapes).getFirst();
		if (intersect == null || intersect.getDistance() > direction.length()) {
			return true;
		} else {
			return false;
		}
	}
	/*@Override
	public boolean isVisibleFrom(Intersection i, List<Shape> shapes) {
		Point p = i.getCoördinate();
		Ray shadowRay = new Ray(p, position.subtract(p));
		for (Shape shape : shapes) {
			if (shape instanceof PolygonMesh) {
				for (MeshTriangle tri: ((PolygonMesh) shape).triangles){
					if (! checkVisible(p, shadowRay, tri)) {
						return false;
					}
				}
			}
			if (! checkVisible(p, shadowRay, shape)) {
				return false;
			};
		}
		return true;
	}

	private boolean checkVisible(Point p, Ray shadowRay, Shape shape) {
		Intersection intersect = shape.getIntersection(shadowRay);
		if (intersect != null  && ! (intersect.getShape() instanceof BV)) {
			double distanceToIntersection = intersect.getCoördinate().subtract(p).length();
			double distanceToLS = position.subtract(p).length();
			double selfIntersectBias = Math.pow(10, -8);
			if ( distanceToIntersection <= distanceToLS && distanceToIntersection > selfIntersectBias) {
				return false;
			}
		}
		return true;
	}*/
}
