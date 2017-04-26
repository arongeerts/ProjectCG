package acceleration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import math.Point;
import shape.Shape;
import util.Pair;

public class MedianSplitter extends Splitter {

	static {
		instance = new MedianSplitter();
	}
	
	private MedianSplitter(){
		super();
	}
	
	@Override
	public Pair<BV, BV> split(BV parent) {
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
			} else {
				second.expand(shape.createNewBV());
			}
		}
		currentSplit = (currentSplit + 1) % 3;
		return new Pair<BV, BV>(first, second);
	}

	public static Splitter get() {
		return instance;
	}
}
