package light;

import java.util.List;

import film.RGBSpectrum;
import math.Point;
import shape.Intersection;
import shape.Shape;

public class AmbientLight implements LightSource{

	
	private RGBSpectrum intensity;

	public AmbientLight(RGBSpectrum intensity) {
		this.intensity = intensity;
	}
	@Override
	public Point getPosition() {
		return null;
	}

	@Override
	public RGBSpectrum getIntensity(double distance) {
		return intensity;
	}

	@Override
	public boolean isVisibleFrom(Intersection i, List<Shape> shapes) {
		return true;
	}

}
