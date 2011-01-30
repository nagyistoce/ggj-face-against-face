package com.ggj;

import java.awt.Color;
import java.awt.Point;

public class Ball {
	  public long x = 10;
	  public long y = 10;
	  public long dx = 1; //Change per second;
	  public long dy = 1;
	  public boolean trapped = false;
	  public Color color = Color.GRAY;

	  public Point toPoint() {
		  Point p = new Point();
		  p.setLocation(x,y);
		  return p;
	  }
}