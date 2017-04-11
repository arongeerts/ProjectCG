package acceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import math.Point;
import math.Transformation;
import shape.MeshTriangle;
import shape.Shape;
import shape.ShapeInstance;
import texture.TransparentTexture;
import util.Pair;

public class BVH {

	private static int SPLIT_X = 0;
	private static int SPLIT_Y = 1;
	private static int SPLIT_Z = 2;
	private static int currentSplit = 0;
	
	private static int nb_shapes = 8;
	
	//TODO: fix types + new BV definition with spanning vectors
	public static List<ShapeInstance> createBVH(List<ShapeInstance> wrappers) {
		List<ShapeInstance> result = new ArrayList<>();
		for (ShapeInstance wrapper: wrappers) {
			BV bv = wrapper.shape.createNewBV();
			result.add(new ShapeInstance(bv, wrapper.transformation, wrapper.texture));
		}
		/*if (result.size() > nb_shapes) {
			BV superbv = buildSuper(result);
			List<ShapeInstance> end_result = new ArrayList<>();
			end_result.add(new ShapeInstance(superbv, Transformation.IDENTITY, TransparentTexture.get()));
			return end_result;
		}*/
		return result;
	}
	
	private static BV buildSuper(List<ShapeInstance> mergeQueue) {
		int currentSplit = 0;
		while (mergeQueue.size() != 1) {
			mergeFirstTwo(mergeQueue, currentSplit);
			currentSplit = (currentSplit + 1) % 3;
		}
		return (BV) mergeQueue.get(0).shape;
	}


	@SuppressWarnings("unchecked")
	private static void mergeFirstTwo(List<ShapeInstance> mergeQueue, int currentSplit) {
		quicksort((List<Shape>) (List<?>)mergeQueue, 0, mergeQueue.size() - 1, currentSplit);
		BV first = (BV) mergeQueue.get(0).shape;
		BV second = (BV) mergeQueue.get(1).shape;
		double x = Math.min(first.leftBottom.x, second.getLeftBottom().x);
		double y = Math.min(first.leftBottom.y, second.getLeftBottom().y);
		double z = Math.min(first.leftBottom.z, second.getLeftBottom().z);
		double x2 = Math.max(first.getRightTop().x, second.getRightTop().x);
		double y2 = Math.max(first.getRightTop().y, second.getRightTop().y);
		double z2 = Math.max(first.getRightTop().z, second.getRightTop().z);
		Point leftBottom = new Point(x, y, z);
		Point rightTop = new Point(x2, y2, z2);
		BV parent = new BV(leftBottom, rightTop);
		mergeQueue.remove(0);
		mergeQueue.remove(0);
		parent.addChild(first);
		parent.addChild(second);
		mergeQueue.add(new ShapeInstance(parent, Transformation.IDENTITY, TransparentTexture.get()));
	}

	public static BV buildBVPolygonMesh(List<MeshTriangle> triangles) {
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		BV superbv = new BV(leftBottom, rightTop);
		for (MeshTriangle tri : triangles) {
			BV newBv = tri.createNewBV();
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
		currentSplit = (currentSplit + 1) % 3;
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
