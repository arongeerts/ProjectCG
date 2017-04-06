package texture;

import film.RGBSpectrum;
import util.Pair;

public class TransparentTexture implements Texture {

	@Override
	public RGBSpectrum evaluate(double u, double v) {
		return new RGBSpectrum(0,0,0);
	}

	@Override
	public RGBSpectrum evaluate(Pair<Double, Double> uv) {
		return this.evaluate(uv.getFirst(), uv.getSecond());
	}
}
