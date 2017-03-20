package texture;

import film.RGBSpectrum;

public class RepeatedTextureMap extends TextureMap {

	private int nbX;
	private int nbY;
	
	public RepeatedTextureMap(String filename, int nbX, int nbY) {
		super(filename);
		this.nbX = nbX;
		this.nbY = nbY;
	}

	@Override
	public RGBSpectrum evaluate(double u, double v) {
		double u_new = nbX * u % 1.0;
		double v_new = nbY * v % 1.0;
		
		return super.evaluate(u_new, v_new);
	}
}

