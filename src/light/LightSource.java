package light;

import java.util.List;

import film.RGBSpectrum;
import math.Point;
import shape.Intersection;
import shape.Shape;

public interface LightSource {

	public Point getPosition();
	
	public RGBSpectrum getIntensity(double distance);
	
	public boolean isVisibleFrom(Intersection i, List<Shape> shapes);
}
