package texture;

import film.RGBSpectrum;

public class UniformColorTexture implements Texture {

	private final RGBSpectrum color;
	public UniformColorTexture(RGBSpectrum color) {
		this.color = color;
	}
	
	@Override
	public RGBSpectrum evaluate(double u, double v) {
		return color;
	}

}
