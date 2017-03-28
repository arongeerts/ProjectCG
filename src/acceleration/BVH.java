package acceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import math.Point;
import math.Transformation;
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
			@SuppressWarnings("unchecked")
			BV superbv = buildSuper((List<BV>)(List<?>)result);
			List<Shape> end_result = new ArrayList<>();
			end_result.add(superbv);
			return end_result;
		}
		return result;
	}
	private static BV buildSuper(List<BV> mergeQueue) {
		int currentSplit = 0;
		while (mergeQueue.size() != 1) {
			mergeFirstTwo(mergeQueue, currentSplit);
			currentSplit = (currentSplit + 1) % 3;
		}
		return mergeQueue.get(0);
	}


	@SuppressWarnings("unchecked")
	private static void mergeFirstTwo(List<BV> mergeQueue, int currentSplit) {
		quicksort((List<Shape>) (List<?>)mergeQueue, 0, mergeQueue.size() - 1, currentSplit);
		BV first = mergeQueue.get(0);
		BV second = mergeQueue.get(1);
		double x = Math.min(first.leftBottom.x, second.getLeftBottom().x);
		double y = Math.min(first.leftBottom.y, second.getLeftBottom().y);
		double z = Math.min(first.leftBottom.z, second.getLeftBottom().z);
		double x2 = Math.max(first.rightTop.x, second.getRightTop().x);
		double y2 = Math.max(first.rightTop.y, second.getRightTop().y);
		double z2 = Math.max(first.rightTop.z, second.getRightTop().z);
		Point leftBottom = new Point(x, y, z);
		Point rightTop = new Point(x2, y2, z2);
		BV parent = new BV(leftBottom, rightTop);
		parent.transformation = Transformation.translate(leftBottom.x, leftBottom.y, leftBottom.z)
				.append(Transformation.scale(rightTop.subtract(leftBottom).x, rightTop.subtract(leftBottom).y, rightTop.subtract(leftBottom).z));
		mergeQueue.remove(0);
		mergeQueue.remove(0);
		parent.addChild(first);
		parent.addChild(second);
		mergeQueue.add(parent);
	}

	public static BV buildBVPolygonMesh(List<Shape> shapes) {
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
		superbv.clearShapes();
		return new Pair<BV, BV>(first, second);
	}

	
	public static void quicksort(List<Shape> list, int low, int high, int currentSplit) {
		if (low >= high) {
			return;
		}
		int middle = low + (high - low)/2;
		Shape pivotshape = list.get(middle);
		double pivot = 0.0;
		if (currentSplit == SPLIT_X) {
			pivot = pivotshape.getCentric().x;
		} else if (currentSplit == SPLIT_Y) {
			pivot = pivotshape.getCentric().y;
		} else if (currentSplit == SPLIT_Z){
			pivot = pivotshape.getCentric().z;
		}
		int i = low;
		int j = high;
		while (i <= j) {
			double value_i = 0.0;
			if (currentSplit == SPLIT_X) {
				value_i = list.get(i).getCentric().x;
			} else if (currentSplit == SPLIT_Y) {
				value_i = list.get(i).getCentric().y;
			} else if (currentSplit == SPLIT_Z){
				value_i = list.get(i).getCentric().z;
			}
			while (value_i < pivot) {
				
				i++;
				if (currentSplit == SPLIT_X) {
					value_i = list.get(i).getCentric().x;
				} else if (currentSplit == SPLIT_Y) {
					value_i = list.get(i).getCentric().y;
				} else if (currentSplit == SPLIT_Z){
					value_i = list.get(i).getCentric().z;
				}
			}
			double value_j = 0.0;
			if (currentSplit == SPLIT_X) {
				value_j = list.get(j).getCentric().x;
			} else if (currentSplit == SPLIT_Y) {
				value_j = list.get(j).getCentric().y;
			} else if (currentSplit == SPLIT_Z){
				value_j = list.get(j).getCentric().z;
			}
			while (value_j > pivot) {
				j--;
				if (currentSplit == SPLIT_X) {
					value_j = list.get(j).getCentric().x;
				} else if (currentSplit == SPLIT_Y) {
					value_j = list.get(j).getCentric().y;
				} else if (currentSplit == SPLIT_Z){
					value_j = list.get(j).getCentric().z;
				}
			}
			if (i <= j){
				Shape at_i = list.get(i);
				list.set(i, list.get(j));
				list.set(j, at_i);
				i++;
				j--;
			}
			
		}
		if (low < j) {
			quicksort(list, low, j, currentSplit);
			
		}
		if (high > i) {
			quicksort(list, i, high, currentSplit);
			
		}
		
	}
}
