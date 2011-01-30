package com.ggj;
import processing.core.*;
import hypermedia.video.*;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GameApplet extends PApplet {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 786;
    private static final int MARGIN = 20;
    
    //On Mac I need to pass -d32 to as a VM argument to java or I get a UnsatisfiedLinkError for the OpenCV library.
    //see http://processing.org/discourse/yabb2/YaBB.pl?num=1251771520
    
    //2. Automated Development - All game assets (art, sound, levels, etc.) are procedurally generated.
    //5. Bits and Pieces - The game has both physical and digital elements.
    //6. Both Hands Tied Behind My Back - The game is meant to be played without the use of a player's hands.

    private static final long serialVersionUID = -5328979700956585581L;

    private boolean DRAW_FACE_RECTANGLE = false;
    
    private static final List<Ball> balls = new ArrayList<Ball>();
    static {
        for (int i = 0; i < 25; i++) {
            Ball b = new Ball();
            resetBall(b);
            balls.add(b);
        }
    }

    private static void resetBall(Ball b) {
        b.trapped = false;
        b.x = (int) (Math.random() * WIDTH);
        b.y = (int) (Math.random() * HEIGHT);
        b.dx = 100 - (int) (Math.random() * 200);
        b.dy = 100 - (int)  (Math.random() * 200);
        b.color = hashColor(b);
    }
    
    
        

    
    private static final List<Player> PLAYERS = new ArrayList<Player>();
    static {
        int num = 1;
        PLAYERS.add(new Player(num++, Color.RED.brighter()));
        PLAYERS.add(new Player(num++, Color.YELLOW.brighter()));
        PLAYERS.add(new Player(num++, Color.GREEN.brighter()));
        PLAYERS.add(new Player(num++, Color.CYAN.brighter()));
        PLAYERS.add(new Player(num++, Color.BLUE.brighter()));
        PLAYERS.add(new Player(num++, Color.MAGENTA.brighter()));
        PLAYERS.add(new Player(num++, Color.PINK.brighter()));
        
        PLAYERS.add(new Player(num++, Color.RED.darker()));
        PLAYERS.add(new Player(num++, Color.YELLOW.darker()));
        PLAYERS.add(new Player(num++, Color.GREEN.darker()));
        PLAYERS.add(new Player(num++, Color.CYAN.darker()));
        PLAYERS.add(new Player(num++, Color.BLUE.darker()));
        PLAYERS.add(new Player(num++, Color.MAGENTA.darker()));
        PLAYERS.add(new Player(num++, Color.PINK.darker()));

    }
    
    private OpenCV opencv;
    double elapsedF = 0f;
    long elapsedI = 0;
    long previousTime = (new Date()).getTime();
    long deltaTime = 1;
    
    // contrast/brightness values
    private int contrast_value    = 0;
    private int brightness_value  = 0;    
    private PFont font; 
    
    private long ticks = 0;
    private Date startTime = new Date();

    @Override
    public void setup() {
        size( WIDTH, HEIGHT );
        opencv = new OpenCV( this );
        opencv.capture( width, height );                   // open video stream
        opencv.cascade( OpenCV.CASCADE_FRONTALFACE_ALT );  // load detection description, here-> front face detection : "haarcascade_frontalface_alt.xml"
        font = loadFont("ScalaSans-Caps-32.vlw");
        textFont(font);
    }

    @Override
    public void stop() {
        opencv.stop();
        super.stop();
    }


    @Override
    public void draw() {

        Date currentTime = new Date();
        elapsedF = (currentTime.getTime() - startTime.getTime()) / 1000L;
        elapsedI = (int) elapsedF;
        deltaTime = currentTime.getTime() - previousTime; 
        previousTime = currentTime.getTime();
        ticks++;

        // grab a new frame
        opencv.read();
        opencv.flip(OpenCV.FLIP_HORIZONTAL);

        List<Player> players = findPlayers();

        boolean hasCollisions = handleCollisions(players);
        if (hasCollisions) {
            opencv.convert(GRAY);
        }

        //if (elapsedI % 20 == 0) { opencv.invert(); }
        //if (elapsedI % 21 == 0) { opencv.convert( GRAY ); }
        //if (elapsedI % 22 > 0) { opencv.contrast( contrast_value ); }
        //if (elapsedI % 23 > 0) { opencv.brightness( brightness_value ); }

        // display the image
        image( opencv.image(), 0, 0 );
        
        drawPlayers(players);
        //if (players.size() > 0 && players.get(0) != null && players.get(0).getCenter() != null) {
        //    drawTriangle(new Point(10,310), new Point(210,10), players.get(0).getCenter());
        //}
        drawArena();
        drawBalls();

        resetBallsIfNeeded();

        if (elapsedI < 10) { drawAbout(); }
      
    }
    
    private void resetBallsIfNeeded() {
        int trappedCount = 0;
        for (Ball ball : balls) {
            if (ball.trapped) {trappedCount++;}   
        }

        if (trappedCount > 20) {
            for (Ball ball : balls) {
                resetBall(ball);
            }
        }
    }
    
    private void drawAbout() {
        stroke(Color.GRAY.brighter().getRGB());
        text("Global Game Jam 2011 - San Francisco", 30, 30);
        //text("Processing, OpenCV", 30, 60);
    }
    
    private void drawBalls() {
        for (Ball b : balls) {
            stroke(b.color.darker().getRGB());
            this.fill(b.color.brighter().getRGB());
            b.x = (int) (b.x + (b.dx * deltaTime / 1000));
            b.y = (int) (b.y + (b.dy * deltaTime / 1000));
            constrain(b);
            ellipse(b.x, b.y, 20, 20);
        }
        
    }

    private void constrain(Ball b) {
        if (false) {
            //WRAP
            if (b.x < 0)      {b.x = b.x + HEIGHT;}
            if (b.x > HEIGHT) {b.x = b.x - HEIGHT; }
            if (b.y < 0)      {b.y = b.y + WIDTH;}
            if (b.y > WIDTH)  {b.y = b.y - WIDTH; }
        } else {
            //BOUNCE
            if (b.x < MARGIN)   {b.dx = Math.abs(b.dx);}
            if (b.y < MARGIN)   {b.dy = Math.abs(b.dy);}
            if (b.x > HEIGHT - MARGIN)  {b.dx = -Math.abs(b.dx);}
            if (b.y > WIDTH - MARGIN)   {b.dy = -Math.abs(b.dy);}
        }
    }
    
    private void copyFace(Player p1, Player p2) {        
        copyFace(p1.getFaceRectangle(), p2.getFaceRectangle());
    }

    private void copyFace(Rectangle face1, Rectangle face2) {        
        copy(face1.x, face1.y, face1.width, face1.height, 
                  face2.x, face2.y, face2.width, face2.height);        
    }

    
    private void drawArena() {
        noFill();
        smooth();
        strokeWeight(4f);
        strokeJoin(ROUND);
        stroke(Color.GRAY.brighter().getRGB());
        rect(MARGIN, MARGIN, width - 2 * MARGIN, height - 2 * MARGIN);
        text("" + elapsedI, MARGIN, MARGIN);
        text("" + deltaTime, MARGIN, MARGIN * 2);
    }

    private List<Player> findPlayers() {
        List<Player> players = new ArrayList<Player>();
        
        Rectangle[] faces = opencv.detect( 1.2f, 2, OpenCV.HAAR_DO_CANNY_PRUNING, 40, 40 );
        //Because open cv returns the face list in a different order every time, sort it by X location.
        List<Rectangle> sortedFaces = new ArrayList<Rectangle>();
        for (Rectangle r : faces) { sortedFaces.add(r); }
        Collections.sort(sortedFaces, RectangleComparator.INSTANCE);
        int i = 0;
        for(Rectangle faceRectange : sortedFaces) {
            Player player = PLAYERS.get(i);
            players.add(player);
            player.update(faceRectange);
            i++;
        }

        return players;
    }
    
      private void drawPlayers(List<Player> players) {
        // draw faces
        noFill();
        for (Player player : players) {
            stroke(player.getColor().getRGB());
            Rectangle faceRectangle = player.getFaceRectangle();
            if (DRAW_FACE_RECTANGLE) {
                rect(faceRectangle.x, faceRectangle.y, faceRectangle.width, faceRectangle.height); 
            }
            fill(player.getColor().getRGB());
            text(player.getName(), faceRectangle.x, faceRectangle.y);
            noFill();
        }

        

        if (players.size() == 1) {
            drawInvitation(players.get(0));
            flipFace(players.get(0).getFaceRectangle());
            
        } else if (players.size() == 2) {
            copyFace(players.get(0), players.get(1));
            drawManifestation(players.get(0).getCenter(), players.get(1).getCenter());
        } else if (players.size() == 3) {
            copyFace(players.get(1), players.get(0));
            copyFace(players.get(2), players.get(1));
            drawTriangle(players.get(0).getCenter(), players.get(1).getCenter(), players.get(2).getCenter());
        } else if (players.size() == 4) {
            copyFace(players.get(1), players.get(0));
            copyFace(players.get(2), players.get(1));
            copyFace(players.get(3), players.get(2));

            drawQuad(players.get(0).getCenter(), players.get(1).getCenter(), 
                     players.get(2).getCenter(), players.get(3).getCenter());
        }

        for (Player p : players) { makeClown(p); }
      
      }


      
      private void flipFace(Rectangle face) {
          copy(face.x + face.width, face.y, -face.width, face.height,
               face.width, face.y, face.width, face.height);
     }

    private void drawInvitation(Player p) {
          Rectangle r = p.getFaceRectangle();
          
//          if (elapsedI % 3 == 0) {
//              fill(PLAYERS.get(1).getColor().getRGB());
//              text("???", width - r.x - r.width, r.y);
//          } else if (elapsedI % 5 == 0) {
//              stroke(PLAYERS.get(1).getColor().getRGB());
//              rect(width - r.x - r.width, r.y, r.width, r.height);
//          }
      }

      private void makeClown(Player p) {
          Rectangle r = p.getFaceRectangle();
          Point c = p.getCenter();
          stroke(p.getColor().getRGB());
          fill(p.getColor().brighter().getRGB());
          ellipse(c.x, c.y, r.width / 6, r.height / 6);
          noFill();
      }
      
      private void drawManifestation(Point p1, Point p2) {          
          stroke(PLAYERS.get(2).getColor().getRGB());
          strokeWeight(2);
          line(p1.x, p1.y, p2.x, p2.y);
          
      }
      
      private void drawTriangle(Point p1, Point p2, Point p3) {          
          strokeWeight(8f);
          stroke(PLAYERS.get(0).getColor().getRGB());
          line(p1.x, p1.y, p2.x, p2.y);
          stroke(PLAYERS.get(1).getColor().getRGB());
          line(p2.x, p2.y, p3.x, p3.y);
          stroke(PLAYERS.get(2).getColor().getRGB());
          line(p3.x, p3.y, p1.x, p1.y);
          
          if (max(p1.x, p2.x, p3.x) - min(p1.x, p2.x, p3.x) > 4) { 
              drawTriangle(
                      new Point(avg(p1.x, p2.x), avg(p1.y, p2.y)),
                      new Point(avg(p2.x, p3.x), avg(p2.y, p3.y)),
                      new Point(avg(p3.x, p1.x), avg(p3.y, p1.y)));
          }
          
      }

      private void drawQuad(Point p1, Point p2, Point p3, Point p4) {          
          strokeWeight(8f);
          stroke(PLAYERS.get(0).getColor().getRGB());
          line(p1.x, p1.y, p2.x, p2.y);
          line(p1.x, p1.y, p3.x, p3.y);
          stroke(PLAYERS.get(1).getColor().getRGB());
          line(p2.x, p2.y, p3.x, p3.y);
          line(p2.x, p2.y, p4.x, p4.y);
          stroke(PLAYERS.get(2).getColor().getRGB());
          line(p3.x, p3.y, p4.x, p4.y);
          stroke(PLAYERS.get(3).getColor().getRGB());
          line(p4.x, p4.y, p1.x, p1.y);
          
          if (max(p1.x, p2.x, p3.x) - min(p1.x, p2.x, p3.x) > 5) { 
              drawQuad(
                      new Point(avg(p1.x, p2.x), avg(p1.y, p2.y)),
                      new Point(avg(p2.x, p3.x), avg(p2.y, p3.y)),
                      new Point(avg(p3.x, p4.x), avg(p3.y, p4.y)),
                      new Point(avg(p4.x, p1.x), avg(p4.y, p1.y)));
          }
          
      }

      
      private boolean handleCollisions(List<Player> players) {
          boolean hasCollisions = false;

          for (Player player : players) {
              for (Ball ball : balls) {
                  if (!ball.trapped && player.getFaceRectangle().contains(ball.toPoint())) {
                      ball.dx = 0;
                      ball.dy = 0;
                      ball.trapped = true;
                      ball.color = player.getColor();
                      player.incrementScore(1);
                      hasCollisions = true;
                  }
              }
          }
          return hasCollisions;
      }
      
      private int avg(int a, int b) { return (a + b) / 2; }
      
    /**
     * Changes contrast/brightness values
     */
      public void mouseDragged() {
        contrast_value   = (int) map( mouseX, 0, width, -128, 128 );
        brightness_value = (int) map( mouseY, 0, width, -128, 128 );
    }

    public static Color hashColor(Object value) {
        if (value == null) {
            return Color.WHITE.darker();
        } else {
            int r = 0xff - (Math.abs(1 + value.hashCode()) % 0xce);
            int g = 0xff - (Math.abs(1 + value.hashCode()) % 0xdd);
            int b = 0xff - (Math.abs(1 + value.hashCode()) % 0xec);
            return new Color(r, g, b);
        }
    }
}