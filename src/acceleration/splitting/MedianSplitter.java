package acceleration.splitting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acceleration.BV;
import math.Point;
import math.Transformation;
import shape.BVInstance;
import shape.Shape;
import texture.TransparentTexture;
import util.Pair;

public class MedianSplitter extends Splitter {

	static {
		instance = new MedianSplitter();
	}
	private static SplitMode mode;
	private MedianSplitter(){
		super();
	}
	
	
	@Override
	public Pair<BV, BV> split(BV parent) {
		if (mode.equals(SplitMode.ALTERNATING)) {
			return splitAlternating(parent);
		} else if (mode.equals(SplitMode.ALL_AXIS)) {
			return splitAllAxes(parent);
		} else if (mode.equals(SplitMode.LONGEST_AXIS)) {
			return splitLongest(parent);
		}
		return null;
	}
	
	
	private Pair<BV, BV> splitLongest(BV parent) {
		double x = parent.getRightTop().x - parent.getLeftBottom().x;
		double y = parent.getRightTop().y - parent.getLeftBottom().y;
		double z = parent.getRightTop().z - parent.getLeftBottom().z;
		if (x >= y && x >= z) {
			currentSplit = SPLIT_X;
		} else if (y >= x && y >= z) {
			currentSplit = SPLIT_Y;
		} else if (z >= x && z >= y) {
			currentSplit = SPLIT_Z;
		}
		return splitAlternating(parent);
	}


	private Pair<BV, BV> splitAllAxes(BV parent) {
		currentSplit = SPLIT_X;
		Pair<BV, BV> x = splitAlternating(parent);
		currentSplit = SPLIT_Y;
		Pair<BV, BV> y = splitAlternating(parent);
		currentSplit = SPLIT_Z;
		Pair<BV, BV> z = splitAlternating(parent);
		return getBestSplit(x, y, z);
	}


	public Pair<BV, BV> splitAlternating(BV parent) {
		List<Double> xs = new ArrayList<>();
		for (Shape shape : parent.getShapes()) {
			xs.add(getCurrentSplitValue(shape.getCentric()));
		}
		Double[] xs2 = new Double[parent.getShapes().size()];
		xs2 = xs.toArray(xs2);
		Arrays.sort(xs2);
		double median = xs2[xs2.length / 2];
		Point leftbottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point righttop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV first = new BV(leftbottom, righttop);
		BV second = new BV(leftbottom, righttop);
		for (Shape shape : parent.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < median)
					|| (currentSplit == SPLIT_Y && shape.getCentric().y < median)
					|| (currentSplit == SPLIT_Z && shape.getCentric().z < median)) {
				first.expand(shape.createNewBV());
				if (shape instanceof BVInstance) {
					BV newbv = shape.createNewBV();
					newbv.addShape(shape);
					first.addShape(new BVInstance(newbv, Transformation.IDENTITY, TransparentTexture.get()));
				}
			} else {
				second.expand(shape.createNewBV());
				if (shape instanceof BVInstance) {
					BV newbv = shape.createNewBV();
					newbv.addShape(shape);
					second.addShape(new BVInstance(newbv, Transformation.IDENTITY, TransparentTexture.get()));
				}
			}
		}
		currentSplit = (currentSplit + 1) % 3;
		return new Pair<BV, BV>(first, second);
	}

	public static Splitter get(SplitMode splitmode) {
		mode = splitmode;
		return instance;
	}
}
