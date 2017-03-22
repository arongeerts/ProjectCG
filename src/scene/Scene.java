package scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import acceleration.BVH;
import film.RGBSpectrum;
import light.LightSource;
import light.PointLightSource;
import math.Point;
import math.Transformation;
import shape.AxisAlignedBox;
import shape.Cylinder;
import shape.PolygonMesh;
import shape.Shape;
import shape.Sphere;
import texture.RepeatedTextureMap;
import texture.TextureMap;
import texture.UniformColorTexture;

public class Scene {

	private List<LightSource> lightsources;
	private List<Shape> shapes;
	private boolean accelerated = true;

	public Scene(List<LightSource> lightsources, List<Shape> shapes) {
		this.lightsources = lightsources;
		if (! accelerated) {
			this.shapes = shapes;
		} else {
			this.shapes = BVH.createBVH(shapes);
		}
	}

	public List<LightSource> getLightsources() {
		return lightsources;
	}

	public List<Shape> getShapes() {
		return shapes;
	}
	
	public void addLightSource(LightSource l) {
		this.lightsources.add(l);
	}
	
	public void addShape(Shape s) {
		this.shapes.add(s);
	}
	
	public static Scene getExampleScene1() {
		Transformation t1 = Transformation.translate(1, -1, -10).append(
				Transformation.scale(2, 2, 2));
		Transformation t2 = Transformation.translate(-2, -3.5, -10).append(
				Transformation.scale(2, 2, 2)).append(Transformation.rotateY(-10));
		Transformation t3 = Transformation.rotateX(30).append(Transformation.translate(-2, -6, -8)).append(
				Transformation.scale(1, 2, 1));
		final Sphere bol = new Sphere(t1, new UniformColorTexture(new RGBSpectrum(255,0,0)));
		final AxisAlignedBox box= new AxisAlignedBox(t2, new UniformColorTexture(new RGBSpectrum(255,0,255)));
		final Cylinder cyl = new Cylinder(t3, new UniformColorTexture(new RGBSpectrum(0,0,255)));
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		s.add(cyl);
		s.add(bol);
		s.add(box);
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
	}
	
	public static Scene getExampleScene2() {
		Transformation t_table = Transformation.translate(0, -4, -8).append(
				Transformation.scale(0.4, 0.4, 0.4));
		Transformation t_sphere = Transformation.translate(1, -0.7, -7).append(
				Transformation.scale(0.5, 0.5, 0.5));
		Transformation t_2 = Transformation.translate(-1.5, -1.2, -7.5).append(
				Transformation.scale(0.3, 0.3, 0.3)).append(Transformation.rotateY(-10));
		Transformation t_3 = Transformation.translate(0, -1.2, -6.2).append(
				Transformation.scale(1,1,1));
		Transformation t_floor =  Transformation.translate(0,-4, -15).append(Transformation.scale(13, 1, 10));
		Transformation t_wall_behind = Transformation.translate(0, 10, -20).append(Transformation.rotateX(90)).append(Transformation.scale(13, 1, 15));
		final Sphere bol = new Sphere(t_sphere, new TextureMap("worldmap.jpg"));
		final PolygonMesh floor = new PolygonMesh("plane.obj", t_floor);
		final PolygonMesh table = new PolygonMesh("table.obj", t_table, new TextureMap("wood.jpg"));
		final PolygonMesh wallBehind = new PolygonMesh("plane.obj", t_wall_behind, new TextureMap("dambord.jpg"));
		final PolygonMesh bunny = new PolygonMesh("bunny.obj", t_2, new TextureMap("bunny.jpg"));
		final PolygonMesh teapot = new PolygonMesh("teapot.obj", t_3, new RepeatedTextureMap("dambord.jpg", 3, 5));
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		s.add(bunny);
		s.add(bol);
		s.add(teapot);
		s.add(bol);
		s.add(table);
		s.add(floor);
		s.add(wallBehind);
		ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		//ls.add(new PointLightSource(new Point(-5,5,-3), new RGBSpectrum(255,0,0)));
		return new Scene(ls, s);
		
	}
	
	
	public static Scene getExampleScene3() {
		Transformation t1 = Transformation.translate(0,-0.5,-4).append(Transformation.rotateX(90)).append(Transformation.scale(2,2,2));
		Transformation t2 = Transformation.translate(0, -2, -6);
		PolygonMesh p = new PolygonMesh("apple.obj", t1, new TextureMap("apple.jpg"));
		PolygonMesh sphere =new PolygonMesh("sphere.obj", t2, new TextureMap("worldmap.jpg"));
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		s.add(p);
		s.add(sphere);
		ls.add(new PointLightSource(new Point(0,0,5), new RGBSpectrum(255,255,255)));
		//ls.add(new AmbientLight(new RGBSpectrum(0.01,0.01,0.01)));
		return new Scene(ls, s);
	}
	
	public static Scene getExampleScene4() {
		Transformation t1 = Transformation.translate(-1, -1, -4).append(Transformation.scale(-0.3, 0.3, 0.3));
		PolygonMesh bunny = new PolygonMesh("bunny.obj", t1, new TextureMap("wood.jpg"));
		Transformation t2 = Transformation.translate(1, -1, -4).append(Transformation.scale(0.3, 0.3, 0.3));
		PolygonMesh bunny2 = new PolygonMesh("bunny.obj", t2, new TextureMap("bunny.jpg"));
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		s.add(bunny);
		s.add(bunny2);
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
		
	}
	
	public static Scene getExampleScene5() {
		Transformation t1 = Transformation.translate(0, -1, -6).append(Transformation.scale(3, 3, 3).append(Transformation.rotateY(-50)));
		PolygonMesh object = new PolygonMesh("trex.obj", t1);
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		s.add(object);
		ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
	}
	
	public static Scene getExampleScene6() {
		List<LightSource> ls = new ArrayList<>();
		List<Shape> s = new ArrayList<>();
		for (int i=1; i < 20000; i++) {
			RGBSpectrum color = new RGBSpectrum(new Random().nextInt(50), new Random().nextInt(50), new Random().nextInt(50));
			Transformation t = Transformation.translate(-2.0 + 4.0 * new Random().nextDouble(), 4*new Random().nextDouble() - 2.0, -4 - 4*new Random().nextDouble()).append(Transformation.scale(0.02,  0.02,  0.02));
			s.add(new Sphere(t, new UniformColorTexture(color)));
		}
		ls.add(new PointLightSource(new Point(4,4,0), new RGBSpectrum(255,255,255)));
		// place origin of camera at (4,4,0) and destination at (0,0,-4) for good view
		return new Scene(ls, s);
	}
	
	public static Scene getTestScene() {
		PolygonMesh table = new PolygonMesh("table.obj", Transformation.translate(0, -4, -8).append(
				Transformation.scale(0.4, 0.4, 0.4)));
		List<Shape> s = new ArrayList<>();
		s.add(table);
		List<LightSource> ls = new ArrayList<>();
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
	}
	public static Scene getMuseumScene() {
		PolygonMesh bones = new PolygonMesh("bone.obj", Transformation.IDENTITY);
		PolygonMesh brushedMetals = new PolygonMesh("brushedmetals.obj", Transformation.IDENTITY);
		PolygonMesh glasstranslucent = new PolygonMesh("glasstranslucent.obj", Transformation.IDENTITY);
		PolygonMesh painted = new PolygonMesh("painted.obj", Transformation.IDENTITY);
		PolygonMesh glass = new PolygonMesh("glass.obj", Transformation.IDENTITY);
		PolygonMesh stone = new PolygonMesh("stone.obj", Transformation.IDENTITY);
		PolygonMesh stoneUnfinished = new PolygonMesh("stoneunfinished.obj", Transformation.IDENTITY);
		PolygonMesh woodpainted = new PolygonMesh("woodpainted.obj", Transformation.IDENTITY);
		PolygonMesh woodvarnished = new PolygonMesh("woodvarnished.obj", Transformation.IDENTITY);
		List<LightSource> ls = new ArrayList<>();
		ls.add(new PointLightSource(new Point(0,0,-1), new RGBSpectrum(255,255,255)));
		ls.add(new PointLightSource(new Point(0,0,1), new RGBSpectrum(255,255,255)));
		ls.add(new PointLightSource(new Point(0,1,0), new RGBSpectrum(255,255,255)));
		List<Shape> s = new ArrayList<>();
		s.add(bones);
		s.add(brushedMetals);
		s.add(glasstranslucent);
		s.add(glass);
		s.add(stoneUnfinished);
		s.add(stone);
		s.add(woodpainted);
		s.add(woodvarnished);
		s.add(painted);
		return new Scene(ls, s);
	}
}
