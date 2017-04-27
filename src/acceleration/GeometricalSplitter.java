package acceleration;

import math.Point;
import math.Transformation;
import shape.BVInstance;
import shape.Shape;
import texture.TransparentTexture;
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
		System.out.println("------------");
		for (Shape shape : parent.getShapes()) {
			if ((currentSplit == SPLIT_X && shape.getCentric().x < middle)
					|| (currentSplit == SPLIT_Y && shape.getCentric().y < middle)
					|| (currentSplit == SPLIT_Z && shape.getCentric().z < middle)) {
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
		
		System.out.println(parent.getShapes().size());
		System.out.println(first.getShapes().size());
		System.out.println(second.getShapes().size());
		
		return new Pair<BV, BV>(first, second);
	}
	
	public static Splitter get() {
		return instance;
	}

}
