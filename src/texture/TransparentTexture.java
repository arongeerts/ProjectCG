package texture;

import film.RGBSpectrum;

public class TransparentTexture implements Texture {

	@Override
	public RGBSpectrum evaluate(double u, double v) {
		return new RGBSpectrum(0,0,0);
	}

}
