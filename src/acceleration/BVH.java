package acceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import math.Point;
import math.Transformation;
import shape.BVInstance;
import shape.MeshTriangle;
import shape.Shape;
import shape.ShapeInstance;
import texture.TransparentTexture;
import util.Pair;

public class BVH {

	private static final int SPLIT_X = 0;
	private static final int SPLIT_Y = 1;
	private static final int SPLIT_Z = 2;
	private static final int nb_bins_SAH = 4;
	private static final int nb_shapes = 1;
	
	private static int currentSplit = 0;
	
	public static List<ShapeInstance> createBVH(List<ShapeInstance> wrappers) {
		List<BVInstance> result = new ArrayList<>();
		for (ShapeInstance wrapper: wrappers) {
			BV bv = wrapper.shape.createNewBV();
			result.add(new BVInstance(bv, wrapper.transformation, wrapper.texture));
		}
		/*if (result.size() > nb_shapes) {
			BV superbv = buildSuper(result);
			List<ShapeInstance> end_result = new ArrayList<>();
			end_result.add(new ShapeInstance(superbv, Transformation.IDENTITY, TransparentTexture.get()));
			return end_result;
		}*/
		if (result.size() > nb_shapes) {
			ShapeInstance superbv = buildSuper3(result);
			List<ShapeInstance> end_result = new ArrayList<>();
			end_result.add(superbv);
			return end_result;
		}
		return (List<ShapeInstance>) (List<?>) result;
	}
	
	private static BVInstance buildSuper3(List<BVInstance> shapes) {
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		BV superbv = new BV(leftBottom, rightTop);
		for (BVInstance bv_inst : shapes) {
			BV transformed = bv_inst.bv.getTransformedBV(bv_inst.transformation);
			transformed.addShape(bv_inst);
			superbv.expand(transformed);
		}
		System.out.println("initially: ");
		System.out.println(superbv.getShapes().size());
		List<BV> toSplit = new ArrayList<>();
		
		toSplit.add(superbv);
		while (! toSplit.isEmpty()) {
			BV parent = toSplit.get(0);
			
			if (parent.getShapes().size() > nb_shapes) {
				Pair<BV, BV> children = splitGeometrically(parent);
				BV first = children.getFirst();
				BV second = children.getSecond();
				
				if (first.getShapes().size() != 0 && second.getShapes().size() != 0) {
					parent.clearShapes();
					parent.addChild(first);
					parent.addChild(second);
					toSplit.add(1, first);
					toSplit.add(1, second);
				}
				
			}
			toSplit.remove(0);
		}
		return new BVInstance(superbv, Transformation.IDENTITY, TransparentTexture.get());
		
	}
	
	private static ShapeInstance buildSuper2(List<ShapeInstance> shapes) {
		int currentSplit = 0;
		while (shapes.size() > 1) {
			mergeFirstTwo2(shapes, currentSplit);
			currentSplit = (currentSplit + 1) % 3;
		}
		return shapes.get(0);
		
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
	
	@SuppressWarnings("unchecked")
	private static void mergeFirstTwo2(List<ShapeInstance> mergeQueue, int currentSplit) {
		quicksort((List<Shape>) (List<?>)mergeQueue, 0, mergeQueue.size() - 1, currentSplit);
		MultiLayeredBVInstance firstInstance = new MultiLayeredBVInstance((BV) mergeQueue.get(0).shape, mergeQueue.get(0).transformation, mergeQueue.get(0).texture);
		MultiLayeredBVInstance secondInstance = new MultiLayeredBVInstance((BV) mergeQueue.get(1).shape, mergeQueue.get(1).transformation, mergeQueue.get(1).texture);
		BV first = firstInstance.bv;
		BV second = secondInstance.bv;
		double x = Math.min(first.getLeftBottom().x, second.getLeftBottom().x);
		double y = Math.min(first.getLeftBottom().y, second.getLeftBottom().y);
		double z = Math.min(first.getLeftBottom().z, second.getLeftBottom().z);
		double x2 = Math.max(first.getRightTop().x, second.getRightTop().x);
		double y2 = Math.max(first.getRightTop().y, second.getRightTop().y);
		double z2 = Math.max(first.getRightTop().z, second.getRightTop().z);
		Point leftBottom = new Point(x, y, z);
		Point rightTop = new Point(x2, y2, z2);
		MultiLayeredBVInstance parent = new MultiLayeredBVInstance(new BV(leftBottom, rightTop), Transformation.IDENTITY, TransparentTexture.get());
		mergeQueue.remove(0);
		mergeQueue.remove(0);
		parent.addChild(firstInstance);
		parent.addChild(secondInstance);
		mergeQueue.add(parent);
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
				Pair<BV, BV> children = splitGeometrically(parent);
				BV first = children.getFirst();
				BV second = children.getSecond();
				
				if (first.getShapes().size() != 0 && second.getShapes().size() != 0) {
					parent.clearShapes();
					parent.addChild(first);
					parent.addChild(second);
					toSplit.add(1, first);
					toSplit.add(1, second);
				}
				
			}
			toSplit.remove(0);
		}
		return superbv;
	}

	public static Pair<BV, BV> splitGeometrically(BV superbv) {
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV first = new BV(leftBottom, rightTop);
		BV second = new BV(leftBottom, rightTop);
		double middle = (getCurrentSplitValue(superbv.getLeftBottom()) + getCurrentSplitValue(superbv.getRightTop()))/2;
		for (Shape shape : superbv.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < middle) || (currentSplit == SPLIT_Y && shape.getCentric().y < middle) || (currentSplit == SPLIT_Z && shape.getCentric().z < middle)) {
				first.expand(shape.createNewBV());
			} else {
				second.expand(shape.createNewBV());
			}
		}
		currentSplit = (currentSplit + 1) % 3;
		
		return new Pair<BV, BV>(first, second);
	}
	
	public static Pair<BV, BV> splitSAH(BV superbv) {
		double distance = getCurrentSplitValue(superbv.getRightTop())
				- getCurrentSplitValue(superbv.getLeftBottom());
		List<Double> binBoundaries = new ArrayList<>();
		List<BV> bins = new ArrayList<>();
		for (int i = 1; i <= nb_bins_SAH ; i++) {
			binBoundaries.add(getCurrentSplitValue(superbv.getLeftBottom()) + distance * i / nb_bins_SAH);
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV binBox = new BV(leftBottom, rightTop);
			bins.add(binBox);
		}
		for (Shape shape : superbv.getShapes()) {
			for (int i = 0 ; i < nb_bins_SAH ; i++) {
				if (getCurrentSplitValue(shape.getCentric()) < binBoundaries.get(i)) {
					bins.get(i).expand(shape.createNewBV());
					break;
				} 
			}
		}
		double cost = Double.MAX_VALUE;
		Pair<BV, BV> currentBest = new Pair<>(null, null);
		
		for (int i = 1 ; i < nb_bins_SAH; i++) {
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV newFirst = new BV(leftBottom, rightTop);
			BV newSecond = new BV(leftBottom, rightTop);
			for (int j = 0 ; j < i; j++) {
				newFirst.expand(bins.get(j));
			}
			for (int j = i; j < nb_bins_SAH; j++) {
				newSecond.expand(bins.get(j));
			}
			double costNewFirst = newFirst.getSurfaceArea() * newFirst.getShapes().size()/superbv.getSurfaceArea();
			if (Double.isNaN(costNewFirst)) {
				costNewFirst = 0;
			}
			double costNewSecond = newSecond.getSurfaceArea() * newSecond.getShapes().size()/superbv.getSurfaceArea();
			if (Double.isNaN(costNewSecond)) {
				costNewSecond = 0;
			}
			double newCost = costNewFirst  + costNewSecond ;
			if (Double.isNaN(newCost)) {
				System.out.println("-----------");
				System.out.println(String.format("newFirst: %f\nnewSecond: %f\nsuperbv: %f\n", newFirst.getSurfaceArea(), newSecond.getSurfaceArea(), superbv.getSurfaceArea()));
				System.out.println(bins.get(1).getLeftBottom());
				System.out.println(bins.get(1).getRightTop());
				System.out.println(bins.get(2).getLeftBottom());
				System.out.println(bins.get(2).getRightTop());
				System.out.println(bins.get(3).getLeftBottom());
				System.out.println(bins.get(3).getRightTop());
			}
			
			if (newCost < cost) {
				cost = newCost;
				currentBest = new Pair<>(newFirst, newSecond);
			} 
		}
		currentSplit = (currentSplit + 1) % 3;
		return currentBest;
		/*// work with geometrical splitting, not in numbers
		int nb_shapes = superbv.getShapes().size();
		quicksort(superbv.getShapes(), 0, nb_shapes - 1, currentSplit);
		List<BV> binBoundingBoxes = new ArrayList<>();
		for (int i = 1; i <= nb_bins_SAH ; i++) {
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV binBox = new BV(leftBottom, rightTop);
			for (int j = 0; j < nb_shapes * i / nb_bins_SAH ; j++) {
				// java get a linear operation? otherwise, create poplist
				binBox.expand(superbv.getShapes().get(j).createNewBV());
				binBoundingBoxes.add(binBox);
			}
		}
		for (int i = 0; i <= nb_bins_SAH ; i++) {
			
		}*/
		
	}
	public static Pair<BV, BV> splitMedian(BV superbv) {
		List<Double> xs = new ArrayList<>();
		for (Shape shape : superbv.getShapes()) {
			xs.add(getCurrentSplitValue(shape.getCentric()));
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
	
	private static double getCurrentSplitValue(Point p) {
		if (currentSplit == SPLIT_X) {
			return p.x;
		} else if (currentSplit == SPLIT_Y) {
			return p.y;
		} else if (currentSplit == SPLIT_Z){
			return p.z;
		}
		return 0.0;
	}
}
