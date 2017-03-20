package Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;

import acceleration.BV;
import film.RGBSpectrum;
import light.PointLightSource;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import shape.AxisAlignedBox;
import shape.Cylinder;
import shape.Intersection;
import shape.MeshTriangle;
import shape.PolygonMesh;
import shape.Shape;
import shape.Sphere;
import texture.UniformColorTexture;

public class Tests {

	@Test
	public void testBoxIntersection() {
		Point origin = new Point(0.5, 0.5, -1);
		Vector direction = new Vector(0,0,1);
		Ray ray = new Ray(origin, direction);
		AxisAlignedBox box = new AxisAlignedBox(Transformation.IDENTITY);
		Intersection i = box.getIntersection(ray);
		assertFalse(i == null);
		assertEquals(box.getNormal(i.getCoördinate()), new Vector(0,0,-1));
	}
	
	@Test 
	public void testBoxIntersectionTransformed() {
		Transformation t = Transformation.rotateZ(45).append(Transformation.rotateX(20)).append(Transformation.translate(-0.5, -0.5, -0.5)).append(Transformation.scale(4, 4, 4));
		Point origin = new Point(0.1, 0.2, -10);
		Vector direction = new Vector(0,0,1);
		Ray ray = new Ray(origin, direction);
		AxisAlignedBox box = new AxisAlignedBox(t);
		List<Shape> shapes = new ArrayList<>();
		shapes.add(box);
		Intersection i = box.getIntersection(ray);
		assertFalse(i == null);
		assertEquals(box.getNormal(i.getCoördinate()), t.transform(new Vector(0,0,-1)));
		PointLightSource ls = new PointLightSource(new Point(0.1,0.2,-11), new RGBSpectrum(1,1,1));
		assertTrue(ls.isVisibleFrom(i, shapes));
	}
	
	@Test 
	public void testCylinderNormal() {
		Transformation t = Transformation.rotateX(20).append(Transformation.translate(-0.5, -0.5, -0.5)).append(Transformation.scale(4, 4, 4));
		Cylinder c = new Cylinder(t);
		Point p = t.transform(new Point(1, 0.5, 0));
		assertEquals(c.getNormal(p), t.transform(new Vector(1,0,0)));
	}
	
	@Test
	public void testCylinderNormalPlane() {
		Transformation t = Transformation.rotateX(20).append(Transformation.translate(-0.5, -0.5, -0.5)).append(Transformation.scale(4, 4, 4));
		Cylinder c = new Cylinder(t);
		Point p = t.transform(new Point(0.2, 1, 0.2));
		assertEquals(c.getNormal(p), t.transform(new Vector(0,1,0)));
	}
	
	@Test
	public void testMeshTriangleIntersection() {
		Point a = new Point(0,0,0);
		Point b = new Point(1,1,0);
		Point c = new Point(0,2,0);
		Point origin = new Point(0.2,0,0.3);
		Vector direction = new Vector(0,1,-1);
		Ray ray = new Ray(origin, direction);
		Vector normal = new Vector(0,0,1);
		List<Double> tc = new ArrayList<>();
		for (int i = 0; i<6; i++) {
			tc.add(.0);
		}
		MeshTriangle m = new MeshTriangle(a, b, c, normal, normal, normal, new UniformColorTexture(new RGBSpectrum(1,1,1)), tc);
		Intersection i = m.getIntersection(ray);
		assertAboutEquals(i.getCoördinate(), new Point(0.2, 0.3, 0));
	}
	
	@Test
	public void testTokenizing() {
		StringTokenizer st = new StringTokenizer("17/376/371", "/");
		assertEquals(st.nextToken(), "17");
	}
	
	@Test
	public void testPhongInterpolation() {
		Vector normalA = new Vector(1,0,0);
		Vector normalB = new Vector(0,1,0);
		Vector normalC = new Vector(0,0,1);
		Vector normalD = new Vector(1,0,0);
		Point a = new Point(0,0,0);
		Point b = new Point(1,0,0);
		Point c = new Point(0,1,0);
		Point d = new Point(-1, 0, 0);
		MeshTriangle tri1 = new MeshTriangle(a, b, c, normalA, normalB, normalC, null, null);
		MeshTriangle tri2 = new MeshTriangle(d, a, c, normalD, normalA, normalC, null, null);
		Point p = new Point(0.5, 0, 0);
		assertEquals(normalA, tri1.getNormal(a));
		assertEquals(normalB, tri1.getNormal(b));
		assertEquals(normalC, tri1.getNormal(c));
		assertEquals(new Vector(0.5,0.5,0).normalize(), tri1.getNormal(p));
		Point p2 = new Point(0,0.5,0);
		assertEquals(tri1.getNormal(p2), tri2.getNormal(p2));
		Point p3a = new Point(0.001,0.5,0);
		Point p3b= new Point(-0.001,0.5,0);
		assertTrue(tri1.getNormal(p3a).subtract(tri2.getNormal(p3b)).length() < 0.01);
	}
	
	@Test
	public void testParseObjFile() {
		PolygonMesh p = new PolygonMesh("test.obj", Transformation.IDENTITY);
		Ray ray = new Ray(new Point(-1, -1, -1), new Vector(1,1,1));
		Intersection i = p.getIntersection(ray);
		assertAboutEquals(i.getNormal(), new Vector(1,1,1).normalize());
	}
	
	@Test
	public void testBVIntersection() {
		Sphere s = new Sphere(Transformation.IDENTITY);
		BV bv = new BV(new Point(-1,-1,-1), new Point(1,1,1));
		bv.addShape(s);
		Point origin = new Point(-5,-5,-5);
		Vector v = new Vector(1,1,1);
		Ray ray = new Ray(origin, v);
		assertAboutEquals(bv.getIntersection(ray).getCoördinate(), new Point(-1/Math.sqrt(3),-1/Math.sqrt(3),-1/Math.sqrt(3)));
	}
	
	
	@Test
	public void testExpand() {
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV bv = new BV(leftBottom, rightTop);
		Point p1 = new Point(0,0,0);
		Point p2 = new Point(1,1,1);
		BV other = new BV(p1, p2);
		bv.expand(other);
		assertEquals(p1, bv.getLeftBottom());
		assertEquals(p2, bv.getRightTop());	
	}
	
	
	@Test
	public void testIntersectionInsideAABB() {
		Point leftbottom = new Point(-1,-1,-1);
		Point rightTop = new Point(1,1,1);
		Point top1 = new Point(0,1,1);
		Point bottom1 = new Point(0, -1, -1);
		BV superbv = new BV(leftbottom, rightTop);
		BV child1 = new BV(leftbottom, top1);
		BV child2 = new BV(bottom1, rightTop);
		Sphere s = new Sphere(Transformation.translate(0.5, 0.5, 0.5).append(Transformation.scale(0.5, 0.5, 0.5)));
		superbv.addChild(child1);
		superbv.addChild(child2);
		child2.addShape(s);
		Ray ray = new Ray(new Point(0.5,0.5, 0.25), new Vector(0,0,1));
		assertAboutEquals(superbv.getIntersection(ray).getCoördinate(), new Point(0.5,0.5,1));
		assertTrue(superbv.getIntersection(ray).getShape() instanceof Sphere);
	}
	
	@Test
	public void testIntersectionFromInsideBox() {
		Ray ray = new Ray(new Point(0,0,0), new Vector(-0.42,-0.25,0.15));
		BV box = new BV(new Point(-1,-1,-1), new Point(1,1,1));
		Sphere s =  new Sphere(Transformation.IDENTITY);
		box.addShape(s);
		Intersection i = box.getIntersection(ray);
		assertEquals(i.getShape(), s);
	}
	
	public void assertAboutEquals(Point point1, Point point2) {
		try {
			assertTrue(point1.subtract(point2).length() < Math.pow(10, -5));
		} catch (AssertionError e) {
			System.out.println("excpected value: " + point1);
			System.out.println("\nreal value: " + point2);
			throw e;
		}
	}

	public void assertAboutEquals(Vector v1, Vector v2) {
		assertTrue(v1.subtract(v2).length() < Math.pow(10, -5));
	}
}

