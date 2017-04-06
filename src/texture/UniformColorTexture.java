package texture;

import film.RGBSpectrum;
import util.Pair;

public class UniformColorTexture implements Texture {

	private final RGBSpectrum color;
	public UniformColorTexture(RGBSpectrum color) {
		this.color = color;
	}
	
	public UniformColorTexture(int i, int j, int k) {
		this(new RGBSpectrum(i, j, k));
	}

	@Override
	public RGBSpectrum evaluate(double u, double v) {
		return color;
	}

	@Override
	public RGBSpectrum evaluate(Pair<Double, Double> uv) {
		return color;
	}
}
