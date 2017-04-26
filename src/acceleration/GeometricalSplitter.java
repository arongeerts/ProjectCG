package acceleration;

import math.Point;
import shape.Shape;
import util.Pair;

public class GeometricalSplitter extends Splitter {
	
	static {
		instance = new GeometricalSplitter();
	}

	private GeometricalSplitter(){
		super();
	};
	
	@Override
	public Pair<BV, BV> split(BV parent) {
		Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
		BV first = new BV(leftBottom, rightTop);
		BV second = new BV(leftBottom, rightTop);
		double middle = (getCurrentSplitValue(parent.getLeftBottom()) + getCurrentSplitValue(parent.getRightTop()))
				/ 2;
		for (Shape shape : parent.getShapes()) {
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
	
	public static Splitter get() {
		return instance;
	}

}
