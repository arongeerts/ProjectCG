package main;

public enum RenderMode {
	NORMAL_MAP, STANDARD, ACCELERATION;

	public static RenderMode parse(String string) {
		if (string.equals("acceleration")) {
			return ACCELERATION;
		} else if (string.equals("normal_map")) {
			return NORMAL_MAP;
		} else if (string.equals("standard")) {
			return STANDARD;
		}
		return null;
	}
}
