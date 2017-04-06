package texture;

import film.RGBSpectrum;
import util.Pair;

public interface Texture {
	public RGBSpectrum evaluate(double u, double v);

	public RGBSpectrum evaluate(Pair<Double, Double> uv);
}
