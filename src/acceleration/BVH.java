package acceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import math.Point;
import shape.PolygonMesh;
import shape.Shape;

public class BVH {

	private static int SPLIT_X = 0;
	private static int SPLIT_Y = 1;
	private static int SPLIT_Z = 2;
	private static int currentSplit = 0;
	
	private static int nb_shapes = 8;
	
	public static List<Shape> createBVH(List<Shape> shapes) {
		List<Shape> result = new ArrayList<>();
		for (Shape shape: shapes) {
			if (shape instanceof PolygonMesh) {
				result.add(shape.createNewBV());
			} else {
				result.add(shape);
			}
			
		}
		if (result.size() > nb_shapes) {
			BV superbv = buildBV(result);
			List<Shape> end_result = new ArrayList<>();
			end_result.add(superbv);
			return end_result;
		}
		return result;
	}
	

	public static BV buildBV(List<Shape> shapes) {
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		BV superbv = new BV(leftBottom, rightTop);
		for (Shape shape : shapes) {
			BV newBv = shape.createNewBV();
			superbv.expand(newBv);
		}
		List<BV> toSplit = new ArrayList<>();
		toSplit.add(superbv);
		while (! toSplit.isEmpty()) {
			BV parent = toSplit.get(0);
			if (parent.getShapes().size() > nb_shapes) {
				Pair<BV, BV> children = split(parent);
				BV first = children.getFirst();
				BV second = children.getSecond();
				parent.addChild(first);
				parent.addChild(second);
				toSplit.add(first);
				toSplit.add(second);
			}
			toSplit.remove(parent);
		}
		return superbv;
	}

	public static Pair<BV, BV> split(BV superbv) {
		List<Double> xs = new ArrayList<>();
		for (Shape shape : superbv.getShapes()) {
			if (currentSplit == SPLIT_X) {
				xs.add(shape.getCentric().x);
			} else if (currentSplit == SPLIT_Y) {
				xs.add(shape.getCentric().y);
			} else if (currentSplit == SPLIT_Z){
				xs.add(shape.getCentric().z);
			}
		}
		Double[] xs2 = new Double[superbv.getShapes().size()];
		xs2 = xs.toArray(xs2);
		Arrays.sort(xs2);
		double median = xs2[xs2.length/2];
		Point leftbottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point righttop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV first = new BV(leftbottom, righttop);
		BV second = new BV(leftbottom, righttop);
		for (Shape shape : superbv.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < median) || (currentSplit == SPLIT_Y && shape.getCentric().y < median) || (currentSplit == SPLIT_Z && shape.getCentric().z < median)) {
				first.expand(shape.createNewBV());
			} else {
				second.expand(shape.createNewBV());
			}
		}
		currentSplit = (currentSplit + 1) % 2;
		return new Pair<BV, BV>(first, second);
	}

	
	
}
