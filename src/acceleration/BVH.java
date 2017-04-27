package acceleration;

import java.util.ArrayList;
import java.util.List;

import math.Point;
import math.Transformation;
import shape.BVInstance;
import shape.MeshTriangle;
import shape.ShapeInstance;
import texture.TransparentTexture;
import util.Pair;

public class BVH {

	private static final int nb_shapes = 8;

	private static Splitter splitter = SAHSplitter.get();
	
	@SuppressWarnings("unchecked")
	public static List<ShapeInstance> createBVH(List<ShapeInstance> wrappers) {
		List<BVInstance> result = new ArrayList<>();
		for (ShapeInstance wrapper : wrappers) {
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

}
