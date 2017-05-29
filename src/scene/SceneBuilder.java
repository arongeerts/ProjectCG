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
import texture.RepeatedTextureMap;
import texture.TextureMap;
import texture.UniformColorTexture;

public class SceneBuilder {

	public static Scene buildSimpleAnalyticObjectsScene() {
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
		ls.add(new AreaLight(new RGBSpectrum(255,255,255), new Point(0,0,0), new Vector(2,0,0), new Vector(0,2,0)));
		return new Scene(ls, s);
	}

	public static Scene getTableScene() {
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
		s.add(new ShapeInstance(wallBehind, t_wall_behind, new RepeatedTextureMap("dambord.jpg", 5, 5)));
		//ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		ls.add(new AreaLight(new RGBSpectrum(255,255,255), new Point(0,0,0), new Vector(2,0,0), new Vector(0,2,0)));
	
		//ls.add(new PointLightSource(new Point(-5,5,-3), new RGBSpectrum(255,0,0)));
		return new Scene(ls, s);
		
	}

	public static Scene getTeapotsScene() {
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		PolygonMesh teapot = new PolygonMesh("teapot.obj");
		int n = 8;
		for (int i=0; i < n; i++) {
			for (int j=0; j <n; j++) {
				for (int k=0; k<n; k++) {
					
					RGBSpectrum color = new RGBSpectrum(new Random().nextInt(50), new Random().nextInt(50), new Random().nextInt(50));
					Transformation t = Transformation.translate(-2.0 + 4*i * 1.0/n,
							-2.0 + 4*j * 1.0/n,
							-2.0 + 4 *k * 1.0/n)
							.append(Transformation.scale(0.1,0.1,0.1));
					s.add(new ShapeInstance(teapot, t, new UniformColorTexture(color)));
				}
				
			}
			
		}
		ls.add(new PointLightSource(new Point(2,2,5), new RGBSpectrum(255,255,255)));
		Scene scene = new Scene(ls, s);
		scene.setOrigin(new Point(2,2,5));
		scene.setDestination(new Point(0,0,-1));
		return scene;
	}
	
	public static Scene getTwoBunnys() {
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

	public static Scene getDragon() {
		Transformation t1 = Transformation.translate(0, -1, -6).append(Transformation.scale(2.5, 2.5, 2.5).append(Transformation.rotateY(-50)));
		PolygonMesh trex = new PolygonMesh("dragon.obj");
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		s.add(new ShapeInstance(trex, t1, new UniformColorTexture(255, 150, 0)));
		ls.add(new PointLightSource(new Point(1,1,0), new RGBSpectrum(255,255,255)));
		Scene scene = new Scene(ls, s);
		scene.setOrigin(new Point(3, 3, 0));
		scene.setDestination(new Point(0, 0, -6));
		return scene;
	}

	
	public static Scene generateDragons(double amount) {
		PolygonMesh dragon = new PolygonMesh("dragon.obj");
		amount = Math.round(amount);
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		for (double i = 0.0 ; i <= amount ; i+= 1) {
			Transformation t = Transformation.translate(-2 + (amount - i)*4/amount, -1, -4)
					.append(Transformation.scale(0.3, 0.3, 0.3));
			Random r = new Random();
			UniformColorTexture tex = new UniformColorTexture(r.nextInt(256), r.nextInt(256), r.nextInt(256));
			s.add(new ShapeInstance(dragon, t, tex));
		}
		Transformation t_floor =  Transformation.translate(0,-1, -4).append(Transformation.scale(13, 1, 10));
		Transformation t_wall_behind = Transformation.translate(0, 0, -8).append(Transformation.rotateX(90)).append(Transformation.scale(13, 1, 15));
		PolygonMesh floor = new PolygonMesh("plane.obj");
		PolygonMesh wallBehind = new PolygonMesh("plane.obj");
		s.add(new ShapeInstance(floor, t_floor, new UniformColorTexture(new RGBSpectrum(0,255,0))));
		s.add(new ShapeInstance(wallBehind, t_wall_behind, new UniformColorTexture(255,255,255)));
		ls.add(new AreaLight(new RGBSpectrum(255,255,255),new Point(2,2,0), new Vector(2,0,0), new Vector(0,0,2)));
		return new Scene(ls, s);
	}

	public static Scene getTestScene() {
		PolygonMesh teapot = new PolygonMesh("teapot.obj");
		Transformation t1 = Transformation.translate(-2, 0, -8);
		Transformation t2 = Transformation.translate(2, 0, -8);
		List<LightSource> ls = new ArrayList<>();
		List<ShapeInstance> s = new ArrayList<>();
		s.add(new ShapeInstance(teapot, t1, new UniformColorTexture(255, 150, 0)));
		s.add(new ShapeInstance(teapot, t2, new UniformColorTexture(255, 150, 255)));
		ls.add(new PointLightSource(new Point(0,0,0), new RGBSpectrum(255,255,255)));
		return new Scene(ls,s);
	}


}
