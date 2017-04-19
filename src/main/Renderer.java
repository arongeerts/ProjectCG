package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import acceleration.BV;
import camera.PerspectiveCamera;
import film.FrameBuffer;
import film.RGBSpectrum;
import film.Tile;
import gui.ProgressReporter;
import gui.RenderFrame;
import light.AmbientLight;
import light.AreaLight;
import light.LightSource;
import light.PointLightSource;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import sampling.Sample;
import scene.Scene;
import scene.SceneBuilder;
import shape.BVInstance;
import shape.Intersection;
import shape.PolygonMesh;
import shape.Shape;
import shape.ShapeInstance;
import texture.UniformColorTexture;
import util.Pair;
import util.Poplist;

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
		Map<String, Object> options = new HashMap<>();
		options.put("width", 		RenderConstants.DEFAULT_WIDTH);
		options.put("height", 		RenderConstants.DEFAULT_HEIGHT);
		options.put("origin", 		RenderConstants.DEFAULT_ORIGIN);
		options.put("destination", 	RenderConstants.DEFAULT_DESTINATION);
		options.put("lookup", 		RenderConstants.DEFAULT_LOOKUP);
		options.put("fov", 			RenderConstants.DEFAULT_FOV);
		options.put("sensitivity", 	RenderConstants.DEFAULT_SENSITIVITY);
		options.put("gamma", 		RenderConstants.DEFAULT_GAMMA);
		options.put("mode", 		RenderMode.STANDARD);
		options.put("sample_dim", 1);
		options.put("scene", 		SceneBuilder.getExampleScene1());
		options.put("filename", 	"output.png");
		options.put("gui", 			true);
		options.put("quiet", 		false);
		
		
		/**********************************************************************
		 * Parse the command line arguments
		 *********************************************************************/
		for (int i = 0; i < arguments.length; ++i) {
			if (arguments[i].startsWith("-")) {
				String flag = arguments[i];

				try {
					if (flag.equals("-width"))
						options.put("width", Integer.parseInt(arguments[++i]));
					else if (flag.equals("-height"))
						options.put("height", Integer.parseInt(arguments[++i]));
					else if (flag.equals("-gui"))
						options.put("gui", Boolean.parseBoolean(arguments[++i]));
					else if (flag.equals("-quiet"))
						options.put("quiet", Boolean.parseBoolean(arguments[++i]));
					else if (flag.equals("-sensitivity"))
						options.put("sensitivity", Double.parseDouble(arguments[++i]));
					else if (flag.equals("-gamma"))
						options.put("gamma", Double.parseDouble(arguments[++i]));
					else if (flag.equals("-origin")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						options.put("origin", new Point(x, y, z));
					} else if (flag.equals("-destination")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						options.put("destination", new Point(x, y, z));
					} else if (flag.equals("-lookup")) {
						double x = Double.parseDouble(arguments[++i]);
						double y = Double.parseDouble(arguments[++i]);
						double z = Double.parseDouble(arguments[++i]);
						options.put("lookup", new Vector(x, y, z));
					} else if (flag.equals("-fov")) {
						options.put("fov", Double.parseDouble(arguments[++i]));
					} else if (flag.equals("-output")) {
						options.put("filename", arguments[++i]);
					} else if (flag.equals("-samples")) {
						options.put("sample_dimension", Integer.parseInt(arguments[++i]));
					} else if (flag.equals("-input")) {
						PolygonMesh p = new PolygonMesh(arguments[++i]);
						ShapeInstance inst = new ShapeInstance(p,
								Transformation.IDENTITY, new UniformColorTexture(new RGBSpectrum(255,255,255)));
						List<ShapeInstance> shapes = new ArrayList<>();
						shapes.add(inst);
						List<LightSource> lightsources = new ArrayList<>();
						lightsources.add(new PointLightSource(new Point(0,0,2), new RGBSpectrum(255,255,225)));
						lightsources.add(new AmbientLight(new RGBSpectrum(20,20,20)));
						options.put("scene", new Scene(lightsources, shapes));
					} else if (flag.equals("-mode")) {
						options.put("mode", RenderMode.parse(arguments[++i]));
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

		final Scene scene 				= (Scene) options.get("scene");
		if (scene.getWidth() != 0) {
			options.put("width", scene.getWidth());
		} if (scene.getHeight() != 0) {
			options.put("height", scene.getHeight());
		} if (scene.getFov() != 0) {
			options.put("fov", scene.getFov());
		} if (scene.getOrigin() != null) {
			options.put("origin", scene.getOrigin());
		} if (scene.getDestination() != null) {
			options.put("destination", scene.getDestination());
		} if (scene.getLookup() != null) {
			options.put("lookup", scene.getLookup());
		} 
		
		final int width 				= (int) options.get("width");
		final int height 				= (int) options.get("height");
		final double gamma 				= (double) options.get("gamma");
		final double sensitivity 		= (double) options.get("sensitivity");
		final int fov 					= (int) options.get("fov");
		final String filename 			= (String) options.get("filename");
		final Point origin 				= (Point) options.get("origin");
		final Point destination 		= (Point) options.get("destination");
		final Vector lookup 			= (Vector) options.get("lookup");
		final int sample_dimension 		= (int) options.get("sample_dim");
		final RenderMode mode 			= (RenderMode) options.get("mode");
		final PerspectiveCamera camera 	= new PerspectiveCamera(width, height, origin, destination, lookup, fov);
		final boolean gui 				= (boolean) options.get("gui");
		final boolean quiet 			= (boolean) options.get("quiet");
		final List<ShapeInstance> shapes = scene.getShapes();
		final List<LightSource> lightsources = scene.getLightsources();
		
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
								Map<AreaLight, Poplist<PointLightSource>> lightsourceSamples = new HashMap<>();
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
										Pair<Intersection, Integer> closestIntersection = getClosestIntersection(ray, shapes);
										Intersection currentClosest = closestIntersection.getFirst();
										int nb_of_calculated_intersections = closestIntersection.getSecond();
										//Intersection currentClosest = getClosestIntersection(ray, shapes);
										// add a color contribution to the pixel
										
										if (currentClosest != null) {
											RGBSpectrum totalColor = new RGBSpectrum(0,0,0);
											if (mode.equals(RenderMode.STANDARD)) {
												
												for (LightSource ls : lightsources) {
													if (ls instanceof AreaLight && sample_dimension != 1) {
														Poplist<PointLightSource> ps = lightsourceSamples.get((AreaLight) ls);
														if (ps == null || ps.size() == 0 ) {
															ps = ((AreaLight) ls).sample(sample_dimension);
															lightsourceSamples.put((AreaLight) ls, ps) ;
														}
														
														PointLightSource p = ps.pop();
														totalColor = totalColor.add(p.getColorContribution(currentClosest, shapes)).scale(sample_dimension);
														
													} else {
														
														RGBSpectrum colorContribution = ls.getColorContribution(currentClosest, shapes);
														totalColor = totalColor.add(colorContribution);
													}
												}
												buffer.getPixel(x, y).add(totalColor);
											} else if (mode.equals(RenderMode.NORMAL_MAP)) {
												buffer.getPixel(x, y).add(getFalseColor(currentClosest));
											} 
										}
										if (mode.equals(RenderMode.ACCELERATION)) {
											buffer.getPixel(x, y).add(new RGBSpectrum(0, 0, nb_of_calculated_intersections));
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
	
	public static Pair<Intersection, Integer> getClosestIntersection(Ray ray, List<ShapeInstance> shapes) {
		Intersection currentClosest = null;
		int nb = 0;
		for (ShapeInstance shape : shapes) {
			Intersection i = shape.getIntersection(ray);
			nb += 1;
			if (i != null) {
				if (i.getShape() instanceof BV) {
					List<Pair<BVInstance, Intersection>> children = new ArrayList<>();
					children.add(new Pair<>(new BVInstance((BV) i.getShape(),
							shape.transformation, shape.texture), i));
					while (! children.isEmpty()) {
						Pair<BVInstance, Intersection> pair = children.get(0);
						BVInstance bv_wrapper = pair.getFirst();
						Intersection bv_intersection = pair.getSecond();
						
						if (bv_intersection != null && (currentClosest == null || bv_intersection.getDistance() <= currentClosest.getDistance())) {
							
							if (bv_wrapper.bv.getChildren().size() != 0) {
								BVInstance child1 = new BVInstance(bv_wrapper.bv.getChildren().get(0),
										bv_wrapper.transformation, bv_wrapper.texture);
								BVInstance child2 = new BVInstance(bv_wrapper.bv.getChildren().get(1),
										bv_wrapper.transformation, bv_wrapper.texture);
								nb += 2;
								Intersection int1 = child1.getIntersection(ray);
								Intersection int2 = child2.getIntersection(ray);
								
								if (int1 == null) {
									if (int2 != null) {
										
										children.add(1,new Pair<>(child2, int2));
										
									}
								} else if (int2 == null) {
									children.add(1,new Pair<>(child1, int1));
								} else {
									if (int1.getDistance() < int2.getDistance()) {
										children.add(1,new Pair<>(child1, int1));
										children.add(2,new Pair<>(child2, int2));
									} else {
										children.add(1,new Pair<>(child2, int2));
										children.add(2,new Pair<>(child1, int1));
									}
								}
							} 
							
							for (Shape s : bv_wrapper.bv.getShapes()) {
								nb += 1;
								Intersection currentInt = new ShapeInstance(s, bv_wrapper.transformation, bv_wrapper.texture).getIntersection(ray);
								if (currentInt != null) {
									if (currentClosest == null || currentClosest.getDistance() > currentInt.getDistance()) {
										currentClosest = currentInt;	
										currentClosest.setColor(bv_wrapper.texture.evaluate(s.getUV(bv_wrapper.transformation.transformInverse(currentClosest.getCoördinate()))));
									}
								}
							}
						}
						children.remove(0);
					}
				}
				else {
					if (i != null) {
						if (currentClosest == null || currentClosest.getDistance() > i.getDistance()) {
							currentClosest = i;	
							currentClosest.setColor(shape.texture.evaluate(i.getShape().getUV(i.getCoördinate())));
						}
					}
				}
			}
			
		}
		
		return new Pair<Intersection, Integer>(currentClosest, nb);
	}
}
