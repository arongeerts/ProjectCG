package shape;

import acceleration.BV;
import math.Transformation;
import texture.Texture;

public class BVInstance extends ShapeInstance {

	public BV bv = null;
	
	public BVInstance(BV bv, Transformation t, Texture tx) {
		super(bv, t, tx);
		this.bv = bv;
	}
	
	@Override
	public BV createNewBV() {
		BV newbv = bv.getTransformedBV(transformation);
		return newbv;
	}
}
