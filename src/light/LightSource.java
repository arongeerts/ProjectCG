package light;

import java.util.ArrayList;
import java.util.List;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Vector;
import shape.Intersection;
import shape.Shape;
import shape.ShapeInstance;
import util.Pair;

public abstract class LightSource {

	public abstract RGBSpectrum getColorContribution(Intersection currentClosest, List<ShapeInstance> shapes);
	
	public static boolean isPointVisibleFrom(Point my_pos, Point target, List<ShapeInstance> shapes) {
		Vector direction = my_pos.subtract(target);
		double selfIntersectionBias = Math.pow(10, -8);
		target.add(direction.scale(selfIntersectionBias));
		Ray shadowRay = new Ray(target, direction);
		for (ShapeInstance shape : shapes) {
			Intersection i = shape.getIntersection(shadowRay);
			if (i != null) {
				if (i.getShape() instanceof BV) {
					List<Pair<BV, Intersection>> children = new ArrayList<>();
					children.add(new Pair<>((BV) i.getShape(), i));
					while (! children.isEmpty()) {
						BV bv = children.get(0).getFirst();
						Intersection bv_int = children.get(0).getSecond();
						if (bv_int != null) {
							if (bv.getChildren().size() != 0) {
								BV child1 = bv.getChildren().get(0);
								BV child2 = bv.getChildren().get(1);
								Intersection int1 = child1.getIntersection(shadowRay);
								Intersection int2 = child2.getIntersection(shadowRay);
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
							for (Shape s : bv.getShapes()) {
								Intersection currentInt = s.getIntersection(shadowRay);
								if (currentInt != null) {
									return false;
								}
							}
						}
						children.remove(0);
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
}
