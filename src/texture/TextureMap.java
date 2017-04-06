package texture;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import film.RGBSpectrum;
import util.Pair;

public class TextureMap implements Texture {

	protected BufferedImage map = null;
	
	public TextureMap(String filename) {
		try {
				this.map = ImageIO.read(new File("src/texture/maps/" + filename));
			} catch (IOException e1) {
				try {
					this.map = ImageIO.read(new File("Project/src/texture/maps/" + filename));
				} catch (IOException e2) {
					File f = new File("test");
					System.out.println("could not find texturemap at " + f.getAbsolutePath());
				}
			
		}
	}
	@Override
	public RGBSpectrum evaluate(double u, double v) {
		if (map == null) {
			return new RGBSpectrum(255,255,255);
		}
		
		else {
			
			int x = (int) Math.abs((map.getWidth() * u));
			int y = (int) (map.getHeight() * v);
			if (x == map.getWidth()) {
				x -= 1;
			}
			if (y == map.getHeight()) {
				y -= 1;
			}
			/*System.out.println("----------");
			System.out.println(x);
			System.out.println(y);
			System.out.println(map.getWidth());
			System.out.println(map.getHeight());*/
			Color color = new Color(map.getRGB(x, y));

			int red = color.getRed();
			int green = color.getGreen();
			int blue = color.getBlue();
			return new RGBSpectrum(red, green, blue);
		}
	}
	
	@Override
	public RGBSpectrum evaluate(Pair<Double, Double> uv) {
		return this.evaluate(uv.getFirst(), uv.getSecond());
	}

}
