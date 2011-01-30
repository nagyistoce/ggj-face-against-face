package com.ggj;
import processing.core.*;



public class SampleProcessingApplet extends PApplet {

	private static final long serialVersionUID = -1879972470130769375L;

public void setup() {
    size(200,200);
    background(0);
  }

  public void draw() {
    stroke(255);
    if (mousePressed) {
      line(mouseX,mouseY,pmouseX,pmouseY);
    }
  }
  
  
  
}