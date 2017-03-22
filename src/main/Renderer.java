package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import acceleration.BV;
import acceleration.Pair;
import camera.PerspectiveCamera;
import film.FrameBuffer;
import film.RGBSpectrum;
import film.Tile;
import gui.ProgressReporter;
import gui.RenderFrame;
import light.AmbientLight;
import light.LightSource;
import light.PointLightSource;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import sampling.Sample;
import scene.Scene;
import shape.Intersection;
import shape.PolygonMesh;
import shape.Shape;

/**
 * Entry point of your renderer.
 * 
 * @author Niels Billen
 * @version 1.0
 */
public class Renderer {
	/**
	 * Entry point of your renderer.
	 * 
	 * @param arguments
	 *            command line arguments.
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	public static void main(String[] arguments) {
		int width = 640;
		int height = 640;
		double sensitivity = 0.004;
		double gamma = 2.2;
		boolean gui = true;
		boolean quiet = false;
		Point origin = new Point(0,0,0);
		Point destination = new Point(0,0,-1);
		Vector lookup = new Vector(0, 1, 0);
		double fov = 60;
		int nb_samples = 1;
		String filename = "output.png";
		RenderMode mode_if_no_input = RenderMode.NORMAL_MAP;
		
		/**********************************************************************
		 * Parse the command line arguments
		 *********************************************************************/
		Scene scene = null;
		for (int i = 0; i < arguments.length; ++i) {
			if (arguments[i].startsWith("-")) {
				String flag = arguments[i];

				try {
					if (flag.equals("-width"))
						width = Integer.parseInt(arguments[++i]);
					else if (flag.equals("-height"))
						height = Integer.parseInt(arguments[++i]);
					else if (flag.equals("-gui"))
						gui = Boolean.parseBoolean(arguments[++i]);
					else if (flag.equals("-quiet"))
						quiet = Boolean.parseBoolean(arguments[++i]);
					else if (flag.equals("-sensitivity"))
						sensitivity = Double.parseDouble(arguments[++i]);
					else if (flag.equals("-gamma"))
						gamma = Double.parseDouble(arguments[++i]);
					else if (flag.equals("-origin")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						origin = new Point(x, y, z);
					} else if (flag.equals("-destination")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						destination = new Point(x, y, z);
					} else if (flag.equals("-lookup")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						lookup = new Vector(x, y, z);
					} else if (flag.equals("-fov")) {
						fov = Double.parseDouble(arguments[++i]);
					} else if (flag.equals("-output")) {
						filename = arguments[++i];
					} else if (flag.equals("-samples")) {
						nb_samples = Integer.parseInt(arguments[++i]);
					} else if (flag.equals("-input")) {
						PolygonMesh p = new PolygonMesh(arguments[++i], Transformation.IDENTITY);
						List<Shape> shapes = new ArrayList<>();
						shapes.add(p);
						List<LightSource> lightsources = new ArrayList<>();
						lightsources.add(new PointLightSource(new Point(0,0,2), new RGBSpectrum(255,255,225)));
						lightsources.add(new AmbientLight(new RGBSpectrum(20,20,20)));
						scene = new Scene(lightsources, shapes);
					} else if (flag.equals("-mode")) {
						mode_if_no_input = RenderMode.parse(arguments[++i]);
					} else if (flag.equals("-help")) {
						System.out
								.println("usage: java -jar cgpracticum.jar\n"
										+ "  -width <integer>      width of the image\n"
										+ "  -height <integer>     height of the image\n"
										+ "  -sensitivity <double> scaling factor for the radiance\n"
										+ "  -gamma <double>       gamma correction factor\n"
										+ "  -origin <point>       origin for the camera\n"
										+ "  -destination <point>  destination for the camera\n"
										+ "  -lookup <vector>      up direction for the camera\n"
										+ "  -output <string>      filename for the image\n"
										+ "  -gui <boolean>        whether to start a graphical user interface\n"
										+ "  -quiet <boolean>      whether to print the progress bar");
						return;
					} else {
						System.err.format("unknown flag \"%s\" encountered!\n",
								flag);
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					System.err.format("could not find a value for "
							+ "flag \"%s\"\n!", flag);
				}
			} else
				System.err.format("unknown value \"%s\" encountered! "
						+ "This will be skipped!\n", arguments[i]);
		}

		/**********************************************************************
		 * Validate the input
		 *********************************************************************/

		if (width <= 0)
			throw new IllegalArgumentException("the given width cannot be "
					+ "smaller than or equal to zero!");
		if (height <= 0)
			throw new IllegalArgumentException("the given height cannot be "
					+ "smaller than or equal to zero!");
		if (gamma <= 0)
			throw new IllegalArgumentException("the gamma cannot be "
					+ "smaller than or equal to zero!");
		if (sensitivity <= 0)
			throw new IllegalArgumentException("the sensitivity cannot be "
					+ "smaller than or equal to zero!");
		if (fov <= 0)
			throw new IllegalArgumentException("the field of view cannot be "
					+ "smaller than or equal to zero!");
		if (fov >= 180)
			throw new IllegalArgumentException("the field of view cannot be "
					+ "larger than or equal to 180!");
		if (filename.isEmpty())
			throw new IllegalArgumentException("the filename cannot be the "
					+ "empty string!");

		/**********************************************************************
		 * Initialize the camera and graphical user interface
		 *********************************************************************/

		final PerspectiveCamera camera = new PerspectiveCamera(width, height,
				origin, destination, lookup, fov);

		// initialize the frame buffer
		final FrameBuffer buffer = new FrameBuffer(width, height);

		// initialize the progress reporter
		final ProgressReporter reporter = new ProgressReporter("Rendering", 40,
				width * height, quiet);

		// initialize the graphical user interface
		RenderFrame userinterface;
		if (gui) {
			try {
				userinterface = RenderFrame.buildRenderFrame(buffer, gamma,
						sensitivity);
				reporter.addProgressListener(userinterface);
			} catch (Exception e) {
				userinterface = null;
			}
		} else
			userinterface = null;

		final RenderFrame frame = userinterface;

		/**********************************************************************
		 * INITIALIZE THE SCENE
		 *********************************************************************/
		if (scene == null) {
			double start = System.currentTimeMillis();
			scene = Scene.getExampleScene2();
			System.out.println("initialised the scene in: " + (System.currentTimeMillis() - start) +" ms");;
		}
		final int sample_dimension = nb_samples;
		final List<Shape> shapes = scene.getShapes();
		final List<LightSource> lightsources = scene.getLightsources();
		final RenderMode mode = mode_if_no_input;
		/**********************************************************************
		 * Multi-threaded rendering of the scene
		 *********************************************************************/

		final ExecutorService service = Executors.newFixedThreadPool(Runtime
				.getRuntime().availableProcessors());

		// subdivide the buffer in equal sized tiles
		for (final Tile tile : buffer.subdivide(64, 64)) {
			// create a thread which renders the specific tile
			Thread thread = new Thread() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Thread#run()
				 */
				@Override
				public void run() {
					try {
						// iterate over the contents of the tile 
						double stratumWidth = 1.0/sample_dimension;
						for (int y = tile.yStart; y < tile.yEnd; ++y) {
							for (int x = tile.xStart; x < tile.xEnd; ++x) {
								for (int i =0; i < sample_dimension ; i++) {
									for (int j = 0; j < sample_dimension ; j++) {
										Ray ray;
										if (sample_dimension != 1) {
											Random randomX = new Random();
											Random randomY = new Random();
											double jitterX = (randomX.nextDouble() - 0.5) * stratumWidth;
											double jitterY = (randomY.nextDouble() - 0.5) * stratumWidth;
											// create a ray through the center of the pixel.
											double x_co = x + 0.5*stratumWidth + i*stratumWidth + jitterX;
											double y_co = y + 0.5*stratumWidth + j*stratumWidth + jitterY;
											ray = camera.generateRay(new Sample(x_co, y_co));
										} else {
											ray = camera.generateRay(new Sample(x+0.5, y+0.5));
										}
										
										
										
										// test the scene on intersections
										//Intersection currentClosest = null;
										Pair<Intersection, Integer> closestIntersection = getClosestIntersection(ray, shapes);
										Intersection currentClosest = closestIntersection.getFirst();
										int nb = closestIntersection.getSecond();
										/*int nb = 1;
										for (Shape shape : shapes) {
											nb += 1;
											Intersection currentInt = shape.getIntersection(ray);
											if (currentInt != null) {
												nb += currentInt.getDepth();
												if (currentClosest == null || currentClosest.getDistance() > currentInt.getDistance()) {
													if (mode.equals(RenderMode.ACCELERATION) || !(currentInt.getShape() instanceof BV))
													currentClosest = currentInt;	
												}
											}
										}*/
										//Intersection currentClosest = getClosestIntersection(ray, shapes);
										// add a color contribution to the pixel
										
										if (currentClosest != null) {
											RGBSpectrum totalColor = new RGBSpectrum(0,0,0);
											if (mode.equals(RenderMode.STANDARD)) {
												for (LightSource ls : lightsources) {
													if (ls.isVisibleFrom(currentClosest, shapes)) {
														RGBSpectrum colorContribution = getColorContribution(ray, currentClosest, ls);
														totalColor = totalColor.add(colorContribution);
													}
												}
												buffer.getPixel(x, y).add(totalColor);
											} else if (mode.equals(RenderMode.NORMAL_MAP)) {
												buffer.getPixel(x, y).add(getFalseColor(currentClosest));
											} 
										}
										if (mode.equals(RenderMode.ACCELERATION)) {
											buffer.getPixel(x, y).add(new RGBSpectrum(0, 0, nb));
										}
									}
								}
								
							}
						}
						

						// update the graphical user interface
						if (frame != null)
							frame.panel.finished(tile);
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(1);
					} catch (StackOverflowError e) {
						e.printStackTrace();
						System.exit(1);
					} catch (OutOfMemoryError e) {
						e.printStackTrace();
						System.exit(1);
					}

					// update the progress reporter
					reporter.update(tile.getWidth() * tile.getHeight());

				}
				
				
				private RGBSpectrum getFalseColor(Intersection currentClosest) {
					Vector normal = currentClosest.getNormal().scale(0.5);
					return new RGBSpectrum(0.5 + normal.x, 0.5 + normal.y, 0.5 + normal.z).scale(255.0);
				}

				private RGBSpectrum getColorContribution(Ray ray, Intersection currentClosest, LightSource ls) {
					if (ls instanceof AmbientLight) {
						return ls.getIntensity(0.0).scale(1/255.0).multiply(currentClosest.getColor());
					}
					Point p = currentClosest.getCoördinate();
					Vector omegaO = ray.direction.scale(-1);
					Vector omegaI = ls.getPosition().subtract(p);
					Vector normal = currentClosest.getNormal();
					if (normal.length() * omegaI.length() == 0) {
						return new RGBSpectrum(0,0,0);
					}
					double cosTheta = normal.dot(omegaI)/(normal.length() * omegaI.length());
					if (cosTheta < 0 && currentClosest.getShape().isTwoSided()) {
						cosTheta = Math.abs(cosTheta);
					}
					RGBSpectrum power = ls.getIntensity(omegaI.length());
					double brdf = math.BRDF.evaluate(omegaI, omegaO);
					double red = power.red * brdf * cosTheta * currentClosest.getColor().red;
					double green = power.green * brdf * cosTheta * currentClosest.getColor().green;
					double blue = power.blue * brdf * cosTheta * currentClosest.getColor().blue;
					return new RGBSpectrum(red, green, blue);
				}
			};
			
			service.submit(thread);
		}

		// signal the reporter that rendering has started
		reporter.start();

		// execute the threads
		service.shutdown();

		// wait until the threads have finished
		try {
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// signal the reporter that the task is done
		reporter.done();

		/**********************************************************************
		 * Export the result
		 *********************************************************************/

		BufferedImage result = buffer.toBufferedImage(sensitivity, gamma);
		try {
			ImageIO.write(result, "png", new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Pair<Intersection, Integer> getClosestIntersection(Ray ray, List<Shape> shapes) {
		Intersection currentClosest = null;
		int nb = 0;
		for (Shape shape : shapes) {
			Intersection i = shape.getIntersection(ray);
			nb += 1;
			if (i != null) {
				if (i.getShape() instanceof BV) {
					List<BV> children = new ArrayList<>();
					children.add((BV) i.getShape());
					while (! children.isEmpty()) {
						BV bv = children.get(0);
						nb += 1;
						Intersection bv_int = bv.getIntersection(ray);
						if (bv_int != null && (currentClosest == null || bv_int.getDistance() < currentClosest.getDistance())) {
							children.addAll(bv.getChildren());
							for (Shape s : bv.getShapes()) {
								nb += 1;
								Intersection currentInt = s.getIntersection(ray);
								if (currentInt != null) {
									if (currentClosest == null || currentClosest.getDistance() > currentInt.getDistance()) {
										currentClosest = currentInt;	
									}
								}
							}
						}
						
						children.remove(bv);
					}
				}
				else {
					if (i != null) {
						if (currentClosest == null || currentClosest.getDistance() > i.getDistance()) {
							currentClosest = i;	
						}
					}
				}
			}
			
		}
		return new Pair<Intersection, Integer>(currentClosest, nb);
	}
}
