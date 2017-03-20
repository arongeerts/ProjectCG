package texture;

import film.RGBSpectrum;

public interface Texture {
	public RGBSpectrum evaluate(double u, double v);
}
