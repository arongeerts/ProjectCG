package acceleration.splitting;

import acceleration.BV;
import math.Point;
import util.Pair;

public abstract class Splitter {
	protected static final int SPLIT_X = 0;
	protected static final int SPLIT_Y = 1;
	protected static final int SPLIT_Z = 2;
	protected static int currentSplit = 0;

	protected static final int nb_shapes = 8;
	protected static final int nb_bins_SAH = 4;

	
	static Splitter instance;
	
	public abstract Pair<BV, BV> split(BV parent);
	
	public static Splitter get(SplitMode mode) {
		return instance;
	}
	
	protected static double getCurrentSplitValue(Point p) {
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
