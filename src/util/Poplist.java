package util;

import java.util.ArrayList;
import java.util.Random;

public class Poplist<E> extends ArrayList<E> {

	
	private static final long serialVersionUID = 1L;
	
	public E pop() {
		Random r = new Random();
		int index = r.nextInt(this.size());
		E elem = get(index);
		remove(index);
		return elem;
	}
	
}
