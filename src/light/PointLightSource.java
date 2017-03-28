package light;

import java.util.List;

import film.RGBSpectrum;
import main.Renderer;
import math.Point;
import math.Ray;
import math.Vector;
import shape.Intersection;
import shape.Shape;

public class PointLightSource extends LightSource {

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
	public RGBSpectrum getColorContribution(Intersection currentClosest, List<Shape> shapes) {
		if (! isVisibleFrom(currentClosest, shapes)) {
			return new RGBSpectrum(0,0,0);
		}
		Ray ray = currentClosest.getRay();
		Point p = currentClosest.getCoördinate();
		Vector omegaO = ray.direction.scale(-1);
		Vector omegaI = getPosition().subtract(p);
		Vector normal = currentClosest.getNormal();
		if (normal.length() * omegaI.length() == 0) {
			return new RGBSpectrum(0,0,0);
		}
		double cosTheta = normal.dot(omegaI)/(normal.length() * omegaI.length());
		if (cosTheta < 0 && currentClosest.getShape().isTwoSided()) {
			cosTheta = Math.abs(cosTheta);
		}
		RGBSpectrum power = getIntensity(omegaI.length());
		double brdf = math.BRDF.evaluate(omegaI, omegaO);
		double red = power.red * brdf * cosTheta * currentClosest.getColor().red;
		double green = power.green * brdf * cosTheta * currentClosest.getColor().green;
		double blue = power.blue * brdf * cosTheta * currentClosest.getColor().blue;
		return new RGBSpectrum(red, green, blue);
		
	}
	
	public boolean isVisibleFrom(Intersection i, List<Shape> shapes) {
		Point p = i.getCoördinate();
		return isPointVisibleFrom(position, p, shapes);
	}
	
	@Override
	public String toString() {
		return position.toString() + "\n" + intensity.toString();
	}
	
	
}
