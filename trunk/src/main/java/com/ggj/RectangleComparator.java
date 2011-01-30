package com.ggj;

import java.awt.Rectangle;
import java.util.Comparator;

public class RectangleComparator implements Comparator<Rectangle> {
	static final public RectangleComparator INSTANCE = new RectangleComparator();
	
	@Override /** Josh Bloch would smack me for this */
	public int compare(Rectangle o1, Rectangle o2) { return o1.x - o2.x; }
}