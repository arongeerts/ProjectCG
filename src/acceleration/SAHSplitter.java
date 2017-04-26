package acceleration;

import java.util.ArrayList;
import java.util.List;

import math.Point;
import shape.Shape;
import util.Pair;

public class SAHSplitter extends Splitter {

	static {
		instance = new SAHSplitter();
	}
	
	private SAHSplitter(){
		super();
	}
	
	@Override
	public Pair<BV, BV> split(BV parent) {
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

	public static Splitter get() {
		return instance;
	}
}
