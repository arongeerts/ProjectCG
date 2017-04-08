package scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import film.RGBSpectrum;
import light.AreaLight;
import light.LightSource;
import light.PointLightSource;
import math.Point;
import math.Transformation;
import math.Vector;
import shape.AxisAlignedBox;
import shape.Cylinder;
import shape.PolygonMesh;
import shape.ShapeInstance;
import shape.Sphere;
import texture.TextureMap;
import texture.UniformColorTexture;

public class SceneBuilder {

	public static Scene getExampleScene1() {
		Transformation t1 = Transformation.translate(1, -1, -10).append(
				Transformation.scale(2, 2, 2));
		Transformation t2 = Transformation.translate(-2, -3.5, -10).append(
				Transformation.scale(2, 2, 2)).append(Transformation.rotateY(-10));
		Transformation t3 = Transformation.rotateX(30).append(Transformation.translate(-2, -6, -8)).append(
				Transformation.scale(1, 2, 1));
		final Sphere bol = new Sphere();
		final AxisAlignedBox box= new AxisAlignedBox();
		final Cylinder cyl = new Cylinder();
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		s.add(new ShapeInstance(cyl, t3, new UniformColorTexture(0,0,255)));
		s.add(new ShapeInstance(bol, t2, new UniformColorTexture(255,0,0)));
		s.add(new ShapeInstance(box, t1, new UniformColorTexture(0,255,0)));
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s, false);
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
		final Sphere bol = new Sphere();
		final PolygonMesh floor = new PolygonMesh("plane.obj");
		final PolygonMesh table = new PolygonMesh("table.obj");
		final PolygonMesh wallBehind = new PolygonMesh("plane.obj");
		final PolygonMesh bunny = new PolygonMesh("bunny.obj");
		final PolygonMesh teapot = new PolygonMesh("teapot.obj");
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		s.add(new ShapeInstance(bunny, t_2, new TextureMap("bunny.jpg")));
		s.add(new ShapeInstance(bol, t_sphere, new TextureMap("worldmap.jpg")));
		s.add(new ShapeInstance(teapot, t_3, new TextureMap("dambord.jpg")));
		s.add(new ShapeInstance(table, t_table, new TextureMap("wood.jpg")));
		s.add(new ShapeInstance(floor, t_floor, new UniformColorTexture(new RGBSpectrum(255,255,255))));
		s.add(new ShapeInstance(wallBehind, t_wall_behind, new TextureMap("dambord.jpg")));
		ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		//ls.add(new AreaLight(new RGBSpectrum(255,255,255), new Point(0,0,0), new Vector(2,0,0), new Vector(0,2,0)));
	
		//ls.add(new PointLightSource(new Point(-5,5,-3), new RGBSpectrum(255,0,0)));
		return new Scene(ls, s);
		
	}

	public static Scene getExampleScene3() {
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		Sphere sphere = new Sphere();
		for (int i=1; i < 20000; i++) {
			RGBSpectrum color = new RGBSpectrum(new Random().nextInt(50), new Random().nextInt(50), new Random().nextInt(50));
			Transformation t = Transformation.translate(-2.0 + 4.0 * new Random().nextDouble(), 4*new Random().nextDouble() - 2.0, -4 - 4*new Random().nextDouble()).append(Transformation.scale(0.02,  0.02,  0.02));
			s.add(new ShapeInstance(sphere, t, new UniformColorTexture(color)));
		}
		ls.add(new PointLightSource(new Point(4,4,0), new RGBSpectrum(255,255,255)));
		// place origin of camera at (4,4,0) and destination at (0,0,-4) for good view
		return new Scene(ls, s);
	}
	
	public static Scene getExampleScene4() {
		PolygonMesh bunny = new PolygonMesh("bunny.obj");
		Transformation t1 = Transformation.translate(-1, -1, -4).append(Transformation.scale(-0.3, 0.3, 0.3));
		Transformation t2 = Transformation.translate(1, -1, -4).append(Transformation.scale(0.3, 0.3, 0.3));
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		//s.add(new ShapeInstance(bunny, t1, new UniformColorTexture(255,0,0)));
		//s.add(new ShapeInstance(bunny, t2, new UniformColorTexture(0,255,0)));
		s.add(new ShapeInstance(bunny, t1, new TextureMap("bunny.jpg")));
		s.add(new ShapeInstance(bunny, t2, new TextureMap("wood.jpg")));
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
		
	}

	public static Scene getExampleScene5() {
		Transformation t1 = Transformation.translate(0, -1, -6).append(Transformation.scale(3, 3, 3).append(Transformation.rotateY(-50)));
		PolygonMesh trex = new PolygonMesh("trex.obj");
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		s.add(new ShapeInstance(trex, t1, new UniformColorTexture(255,255,255)));
		ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls, s);
	}

	
	

	public static Scene getTestScene() {
		Cylinder cyl = new Cylinder();
		TextureMap text = new TextureMap("wood.jpg");
		ShapeInstance inst = new ShapeInstance(cyl, Transformation.translate(0,0,-6), text);
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		s.add(inst);
		return new Scene(ls, s, false);
	}


}
