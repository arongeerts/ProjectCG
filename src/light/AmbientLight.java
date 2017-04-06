package light;

import java.util.List;

import film.RGBSpectrum;
import shape.Intersection;
import shape.ShapeInstance;

public class AmbientLight extends LightSource{

	
	private RGBSpectrum intensity;

	public AmbientLight(RGBSpectrum intensity) {
		this.intensity = intensity;
	}
	

	public RGBSpectrum getIntensity(double distance) {
		return intensity;
	}

	
	@Override
	public RGBSpectrum getColorContribution(Intersection currentClosest, List<ShapeInstance> shapes) {
		return getIntensity(0.0);
	}

}
