package light;

import java.util.ArrayList;
import java.util.List;

import acceleration.BV;
import film.RGBSpectrum;
import math.Point;
import math.Ray;
import math.Vector;
import shape.BVInstance;
import shape.Intersection;
import shape.Shape;
import shape.ShapeInstance;
import util.Pair;

public abstract class LightSource {

	public abstract RGBSpectrum getColorContribution(Intersection currentClosest, List<ShapeInstance> shapes);
	
	public static boolean isPointVisibleFrom(Point my_pos, Point target, List<ShapeInstance> shapes) {
		Vector direction = my_pos.subtract(target);
		double selfIntersectionBias = Math.pow(10, -8);
		target = target.add(direction.scale(selfIntersectionBias));
		Ray shadowRay = new Ray(target, direction);
		for (ShapeInstance shape : shapes) {
			Intersection i = shape.getIntersection(shadowRay);
			if (i != null) {
				if (i.getShape() instanceof BV) {
					List<Pair<BVInstance, Intersection>> children = new ArrayList<>();
					children.add(new Pair<>(new BVInstance((BV) i.getShape(),
							shape.transformation, shape.texture), i));
					while (! children.isEmpty()) {
						Pair<BVInstance, Intersection> pair = children.get(0);
						BVInstance bv_wrapper = pair.getFirst();
						Intersection bv_intersection = pair.getSecond();
						
						if (bv_intersection != null) {
							if (bv_wrapper.bv.getChildren().size() != 0) {
								BVInstance child1 = new BVInstance(bv_wrapper.bv.getChildren().get(0),
										bv_wrapper.transformation, bv_wrapper.texture);
								BVInstance child2 = new BVInstance(bv_wrapper.bv.getChildren().get(1),
										bv_wrapper.transformation, bv_wrapper.texture);
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
							for (Shape s : bv_wrapper.bv.getShapes()) {
								if (s instanceof BVInstance) {
									Intersection intersect = s.getIntersection(shadowRay);
									if (intersect != null) {
										children.add(1,new Pair<BVInstance, Intersection>((BVInstance) s, intersect));
										
									}
									continue;
								}
								Intersection currentInt = new ShapeInstance(s, bv_wrapper.transformation, bv_wrapper.texture).getIntersection(shadowRay);
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
