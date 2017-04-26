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
	private static final int nb_shapes = 8;

	private static Splitter splitter = GeometricalSplitter.get();
	private static int currentSplit = 0;

	@SuppressWarnings("unchecked")
	public static List<ShapeInstance> createBVH(List<ShapeInstance> wrappers) {
		List<BVInstance> result = new ArrayList<>();
		for (ShapeInstance wrapper : wrappers) {
			System.out.println("started BVH");
			BV bv = wrapper.shape.createNewBV();
			result.add(new BVInstance(bv, wrapper.transformation, wrapper.texture));
		}

		if (result.size() > nb_shapes) {
			ShapeInstance superbv = buildSuper(result);
			List<ShapeInstance> end_result = new ArrayList<>();
			end_result.add(superbv);
			return end_result;
		}
		return (List<ShapeInstance>) (List<?>) result;
	}

	private static BVInstance buildSuper(List<BVInstance> shapes) {
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		BV superbv = new BV(leftBottom, rightTop);
		for (BVInstance bv_inst : shapes) {
			BV transformed = bv_inst.bv.getTransformedBV(bv_inst.transformation);
			transformed.addShape(bv_inst);
			superbv.expand(transformed);
		}
		List<BV> toSplit = new ArrayList<>();

		toSplit.add(superbv);
		while (!toSplit.isEmpty()) {
			BV parent = toSplit.get(0);

			if (parent.getShapes().size() > nb_shapes) {
				Pair<BV, BV> children = splitter.split(parent);
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
		while (!toSplit.isEmpty()) {
			BV parent = toSplit.get(0);
			if (parent.getShapes().size() > nb_shapes) {
				Pair<BV, BV> children = splitter.split(parent);
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
		double middle = (getCurrentSplitValue(superbv.getLeftBottom()) + getCurrentSplitValue(superbv.getRightTop()))
				/ 2;
		for (Shape shape : superbv.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < middle)
					|| (currentSplit == SPLIT_Y && shape.getCentric().y < middle)
					|| (currentSplit == SPLIT_Z && shape.getCentric().z < middle)) {
				first.expand(shape.createNewBV());
			} else {
				second.expand(shape.createNewBV());
			}
		}
		currentSplit = (currentSplit + 1) % 3;

		return new Pair<BV, BV>(first, second);
	}

	public static Pair<BV, BV> splitSAH(BV superbv) {
		double distance = getCurrentSplitValue(superbv.getRightTop()) - getCurrentSplitValue(superbv.getLeftBottom());
		List<Double> binBoundaries = new ArrayList<>();
		List<BV> bins = new ArrayList<>();
		for (int i = 1; i <= nb_bins_SAH; i++) {
			binBoundaries.add(getCurrentSplitValue(superbv.getLeftBottom()) + distance * i / nb_bins_SAH);
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV binBox = new BV(leftBottom, rightTop);
			bins.add(binBox);
		}
		for (Shape shape : superbv.getShapes()) {
			for (int i = 0; i < nb_bins_SAH; i++) {
				if (getCurrentSplitValue(shape.getCentric()) < binBoundaries.get(i)) {
					bins.get(i).expand(shape.createNewBV());
					break;
				}
			}
		}
		double cost = Double.MAX_VALUE;
		Pair<BV, BV> currentBest = new Pair<>(null, null);

		for (int i = 1; i < nb_bins_SAH; i++) {
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV newFirst = new BV(leftBottom, rightTop);
			BV newSecond = new BV(leftBottom, rightTop);
			for (int j = 0; j < i; j++) {
				newFirst.expand(bins.get(j));
			}
			for (int j = i; j < nb_bins_SAH; j++) {
				newSecond.expand(bins.get(j));
			}
			double costNewFirst = newFirst.getSurfaceArea() * newFirst.getShapes().size() / superbv.getSurfaceArea();
			if (Double.isNaN(costNewFirst)) {
				costNewFirst = 0;
			}
			double costNewSecond = newSecond.getSurfaceArea() * newSecond.getShapes().size() / superbv.getSurfaceArea();
			if (Double.isNaN(costNewSecond)) {
				costNewSecond = 0;
			}
			double newCost = costNewFirst + costNewSecond;

			if (newCost < cost) {
				cost = newCost;
				currentBest = new Pair<>(newFirst, newSecond);
			}
		}
		currentSplit = (currentSplit + 1) % 3;
		return currentBest;

	}

	public static Pair<BV, BV> splitMedian(BV superbv) {
		List<Double> xs = new ArrayList<>();
		for (Shape shape : superbv.getShapes()) {
			xs.add(getCurrentSplitValue(shape.getCentric()));
		}
		Double[] xs2 = new Double[superbv.getShapes().size()];
		xs2 = xs.toArray(xs2);
		Arrays.sort(xs2);
		double median = xs2[xs2.length / 2];
		Point leftbottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point righttop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV first = new BV(leftbottom, righttop);
		BV second = new BV(leftbottom, righttop);
		for (Shape shape : superbv.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < median)
					|| (currentSplit == SPLIT_Y && shape.getCentric().y < median)
					|| (currentSplit == SPLIT_Z && shape.getCentric().z < median)) {
				first.expand(shape.createNewBV());
			} else {
				second.expand(shape.createNewBV());
			}
		}
		currentSplit = (currentSplit + 1) % 3;
		return new Pair<BV, BV>(first, second);
	}

	private static double getCurrentSplitValue(Point p) {
		if (currentSplit == SPLIT_X) {
			return p.x;
		} else if (currentSplit == SPLIT_Y) {
			return p.y;
		} else if (currentSplit == SPLIT_Z) {
			return p.z;
		}
		return 0.0;
	}
}
