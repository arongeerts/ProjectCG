package shape;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import acceleration.BV;
import acceleration.BVH;
import math.Point;
import math.Ray;
import math.Transformation;
import math.Vector;
import util.Pair;

public class PolygonMesh implements Shape {
	

	public List<Point> vertices = new ArrayList<>();
	public List<Vector> normals = new ArrayList<>();
	public List<Double> us = new ArrayList<>();
	public List<Double> vs = new ArrayList<>();
	public List<MeshTriangle> triangles = new ArrayList<>();
	public BV bv = null;
	

	public PolygonMesh(String filename) {
		this.parseObjFile(filename);
		bv = BVH.buildBVPolygonMesh(triangles);
	}

	protected void parseObjFile(String filename) {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("src/shape/meshes/" + filename));
		} catch (FileNotFoundException e1) {
			try {
				reader = new BufferedReader(new FileReader("Project/src/shape/meshes/" + filename));
			} catch (FileNotFoundException e2) {
				try {
					reader = new BufferedReader(new FileReader(filename));
				} catch (FileNotFoundException e3) {
					System.out.println("could not find file: " + filename);
				}
			}
			
		}
		try {
			String CurrentLine = null;
			while ((CurrentLine = reader.readLine()) != null) {
				try {
					parseLine(CurrentLine);
				} catch (NoSuchElementException e) {
					System.out.println("parsing error in object file");
					System.out.println(CurrentLine);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void parseLine(String currentLine) throws NoSuchElementException {
		if (currentLine.equals("")) {
			//do nothing
		}
		else {
			StringTokenizer st = new StringTokenizer(currentLine);
			List<String> tokens = new ArrayList<>();
			while (st.hasMoreTokens()) {
				tokens.add(st.nextToken());
			}
			if (tokens.get(0).equals("v")) {
				//new vertex
				vertices.add(new Point(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2)), Double.parseDouble(tokens.get(3))));
			}
			else if (tokens.get(0).equals("vn")) {
				normals.add(new Vector(Double.parseDouble(tokens.get(1)), Double.parseDouble(tokens.get(2)), Double.parseDouble(tokens.get(3))));
			}
			else if (tokens.get(0).equals("vt")) {
				us.add(Double.parseDouble(tokens.get(1)));
				vs.add(Double.parseDouble(tokens.get(2)));
			}
			else if (tokens.get(0).equals("f")) {
				StringTokenizer st1 = new StringTokenizer(tokens.get(1), "/");
				StringTokenizer st2 = new StringTokenizer(tokens.get(2), "/");
				StringTokenizer st3 = new StringTokenizer(tokens.get(3), "/");
				Point a = vertices.get(Integer.parseInt(st1.nextToken())-1);
				Point b = vertices.get(Integer.parseInt(st2.nextToken())-1);
				Point c = vertices.get(Integer.parseInt(st3.nextToken())-1);
				int i1 = Integer.parseInt(st1.nextToken());
				int i2 = Integer.parseInt(st2.nextToken());
				int i3 = Integer.parseInt(st3.nextToken());
				List<Double> textureCoordinates = new ArrayList<>();
				textureCoordinates.add(us.get(i1-1));
				textureCoordinates.add(vs.get(i1-1));
				textureCoordinates.add(us.get(i2-1));
				textureCoordinates.add(vs.get(i2-1));
				textureCoordinates.add(us.get(i3-1));
				textureCoordinates.add(vs.get(i3-1));
				Vector na = normals.get(Integer.parseInt(st1.nextToken())-1).normalize();
				Vector nb = normals.get(Integer.parseInt(st2.nextToken())-1).normalize();
				Vector nc = normals.get(Integer.parseInt(st3.nextToken())-1).normalize();
				triangles.add(new MeshTriangle(a, b, c, na, nb, nc, textureCoordinates));
				
			}
		}
		
	}

	@Override
	public Intersection getIntersection(Ray ray) {
		Intersection closest = null;
		double closestDistance = Double.MAX_VALUE;
		Point origin = ray.origin;
		for (MeshTriangle tri : triangles) {
			Intersection i = tri.getIntersection(ray);
			if (i != null) {
				double distance = origin.subtract(i.getCoördinate()).length();
				if (distance < closestDistance) {
					closestDistance = distance;
					closest = i;
				}
			}
		}
		return closest;
	}



	@Override
	public Vector getNormal(Point p) {
		return null;
	}

	@Override
	public boolean isTwoSided() {
		return false;
	}

	@Override
	public Point getCentric() {
		return null;
	}

	@Override
	public BV createNewBV() {
		return bv;
	}

	@Override
	public Pair<Double, Double> getUV(Point p) {
		return null;
	}

}
