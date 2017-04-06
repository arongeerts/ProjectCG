package light;

import java.util.List;
import java.util.Random;

import film.RGBSpectrum;
import math.Point;
import math.Vector;
import shape.Intersection;
import shape.ShapeInstance;
import util.Poplist;

public class AreaLight extends LightSource {

	private RGBSpectrum intensity;
	private Point leftbottom;
	private Vector spanningVector1;
	private Vector spanningVector2; 
	
	private static int DEFAULT_SAMPLE_DIMENSION = 4;
	
	
	public AreaLight(RGBSpectrum intensity, Point leftbottom, Vector spanningVector1, Vector spanningVector2) {
		this.intensity = intensity;
		this.leftbottom = leftbottom;
		this.spanningVector1 = spanningVector1;
		this.spanningVector2 = spanningVector2;
	}
	
	@Override
	public RGBSpectrum getColorContribution(Intersection currentClosest, List<ShapeInstance> shapes) {
		Poplist<PointLightSource> samples = sample(DEFAULT_SAMPLE_DIMENSION);
		RGBSpectrum total = RGBSpectrum.BLACK;
		for (PointLightSource sample : samples) {
			total = total.add(sample.getColorContribution(currentClosest, shapes));
		}
		return total;
	}

	public Poplist<PointLightSource> sample(int sample_dimension) {
		if (sample_dimension != 1) {
			DEFAULT_SAMPLE_DIMENSION = sample_dimension;
		}
		Poplist<PointLightSource> samples = new Poplist<>();
		for (int i = 0 ; i < DEFAULT_SAMPLE_DIMENSION ; i++) {
			for (int j = 0 ; j < DEFAULT_SAMPLE_DIMENSION ; j++) {
				double stratumWidthX = spanningVector1.length()/DEFAULT_SAMPLE_DIMENSION;
				double stratumWidthY = spanningVector2.length()/DEFAULT_SAMPLE_DIMENSION;
				Random randomX = new Random();
				Random randomY = new Random();
				double jitterX = (randomX.nextDouble() - 0.5) * stratumWidthX;
				double jitterY = (randomY.nextDouble() - 0.5) * stratumWidthY;
				// create a ray through the center of the pixel.
				double x_co = 0.5*stratumWidthX + i*stratumWidthX + jitterX;
				double y_co = 0.5*stratumWidthX + j*stratumWidthY + jitterY;
				samples.add(new PointLightSource(leftbottom.add(spanningVector1.scale(x_co).add(spanningVector2).scale(y_co)), intensity.divide(DEFAULT_SAMPLE_DIMENSION)));
			}
		}
		return samples;
	}

}


