package com.ggj;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;

public class Player {
	private Color color;
	private int number;
	private Rectangle faceRectangle;
	private int score = 0;
	
	public Color getColor() { return color; }
	public Rectangle getFaceRectangle() { return faceRectangle; }
	
	public Player(int number, Color color) {
		this.number = number;
		this.color = color;
	}
	
	public void update(Rectangle faceRectangle) {
		this.faceRectangle = faceRectangle;
	}
	public String getName() {
		return "Player " + number;
	}
	public Point getCenter() {
		Point p = new Point();
		p.x = faceRectangle.x + faceRectangle.width / 2;
		p.y = faceRectangle.y + faceRectangle.height / 2;
		return p;
	}
	public void incrementScore(int i) {
		score += i;
	}

}
