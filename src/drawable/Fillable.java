package drawable;


import java.awt.Polygon;
import java.util.LinkedList;

import org.newdawn.slick.Color;

import construction.Line;
import construction.Vec2D;

public interface Fillable {
  
  public Vec2D getCenter();
  
  public Color getFillColor();
  
  public Polygon getPolygon();
  //1 true, 0 unknown, -1 false
  public int isCCW();
  
  public LinkedList<Line> getPlot();
  
  public LinkedList<Line> getFastPlot();
  
  public boolean intersects(Line v);
  
  public boolean intersects(Fillable f);
  
  public boolean contains(Vec2D p);
  
  public boolean contains(Fillable f);
  
  public Line intersectedLine(Fillable f, Vec2D pos);
  
  public LinkedList<Vec2D> getVertices();
  
  public double getArea();
}
