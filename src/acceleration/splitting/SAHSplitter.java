package acceleration.splitting;

import java.util.ArrayList;
import java.util.List;

import acceleration.BV;
import math.Point;
import math.Transformation;
import shape.BVInstance;
import shape.Shape;
import texture.TransparentTexture;
import util.Pair;

public class SAHSplitter extends Splitter {

	static {
		instance = new SAHSplitter();
	}
	private static SplitMode mode;
	private SAHSplitter(){
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
		// TODO Auto-generated method stub
		return null;
	}

	public Pair<BV, BV> splitAlternating(BV parent) {
		double distance = getCurrentSplitValue(parent.getRightTop()) - getCurrentSplitValue(parent.getLeftBottom());
		List<Double> binBoundaries = new ArrayList<>();
		List<BV> bins = new ArrayList<>();
		for (int i = 1; i <= nb_bins_SAH; i++) {
			binBoundaries.add(getCurrentSplitValue(parent.getLeftBottom()) + distance * i / nb_bins_SAH);
			Point leftBottom = new Point(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
			Point rightTop = new Point(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
			BV binBox = new BV(leftBottom, rightTop);
			bins.add(binBox);
		}
		for (Shape shape : parent.getShapes()) {
			for (int i = 0; i < nb_bins_SAH; i++) {
				if (getCurrentSplitValue(shape.getCentric()) < binBoundaries.get(i)) {
					bins.get(i).expand(shape.createNewBV());
					if (shape instanceof BVInstance) {
						BV newbv = shape.createNewBV();
						newbv.addShape(shape);
						bins.get(i).addShape(new BVInstance(newbv, Transformation.IDENTITY, TransparentTexture.get()));
					}
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
			double costNewFirst = newFirst.getSurfaceArea() * newFirst.getShapes().size() / parent.getSurfaceArea();
			if (Double.isNaN(costNewFirst)) {
				costNewFirst = 0;
			}
			double costNewSecond = newSecond.getSurfaceArea() * newSecond.getShapes().size() / parent.getSurfaceArea();
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
	public static Splitter get(SplitMode splitmode) {
		mode = splitmode;
		return instance;
	}
}
