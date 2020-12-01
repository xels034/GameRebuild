package construction;

import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;

import org.newdawn.slick.Color;


public class Line {
  private Vec2D start;
  private Vec2D end;
  private Color col;
  
  private boolean forDeletion;
  public long birth;
  
  public WeakReference<Line> next;
  public WeakReference<Line> previous;
  
  public Line(){
    this(new Vec2D(0,0), new Vec2D(0,0),Color.black);
  }
  
  public Line(Vec2D s, Vec2D e, Color c){
    start=s;
    end=e;
    col=c;
    forDeletion=false;    
    birth = System.currentTimeMillis();
  }
  
  public static boolean intersectsFromRight(Line intersector, Line intersected){
    // a x b -> signum
    
    //(a2*b3) - (a3*b2) -> x
    //-(a1*b3 - a3*b1)  -> y
    //(a1*b2) - (a2*b1) -> z
    
    double ax = intersector.end.x-intersector.start.x;
    double ay = intersector.end.y-intersector.start.y;
    
    double bx = intersected.end.x-intersected.start.x;
    double by = intersected.end.y-intersected.start.y;
    
    double z = ax*by - ay*bx;
    
    System.out.println(z);
    
    if(z<0)return false;
    else return true;
    
    //also, smallest angle is bullshit
    //it goes always along 2nd part of line2
  }
  
  public static double[] getUV(Line v1, Line v2){
    //uses parametric representation: A +x*(B-A)
    double ax = v1.start.x;
    double ay = v1.start.y;//origin
    double bx = v1.end.x-v1.start.x;//vector
    double by = v1.end.y-v1.start.y;
    
    double cx = v2.start.x;
    double cy = v2.start.y;
    double dx = v2.end.x-v2.start.x;
    double dy = v2.end.y-v2.start.y;
    
    // a + u*b = c + v*d
    // u,v=?
    double u,v;
    
    u =  (-cx*dy+ax*dy+(cy-ay)*dx)/(by*dx-bx*dy);
    v =  -(bx*(ay-cy)+by*cx-ax*by)/(by*dx-bx*dy);
    double[] arr = {u,v};
    return arr;
  }
  
  public static boolean isPointOnLine(Vec2D p, Line v){
    double ax = v.start.x;
    double bx = v.end.x-v.start.x;
    double cx = p.x;
    double x = (cx-ax)/bx;
    
    double ay = v.start.y;
    double by = v.end.y-v.start.y;
    double cy = p.y;
    double y = (cy-ay)/by;
    
    if(x>=0 && x<=1 && x==y){
      return true;
    }else{
      return false;
    }
  }
  
  public static boolean isIntersecting(Line v1, Line v2){
    //treats parallel as not intersecting
    double[] uv=getUV(v1,v2);
    
    if(uv[0]>=0 && uv[0]<=1 && uv[1]>=0 && uv[1]<=1){
      return true;
    }else{
      return false;
    }
  }
  
  public static Vec2D getIntersection(Line v1, Line v2){
    //treats parallel as not intersecting
    double ax = v1.start.x;
    double ay = v1.start.y;//origin
    double bx = v1.end.x-v1.start.x;//vector
    double by = v1.end.y-v1.start.y;
    
    double[] uv = getUV(v1,v2);

    if(uv[0]>=0 && uv[0]<=1 && uv[1]>=0 && uv[1]<=1){
      return new Vec2D(ax+uv[0]*bx,ay+uv[0]*by);
    }else{
      return null;
    }
  }
  
  public Vec2D[] getPoints(){
    Vec2D[] a = {start.getCopy(),end.getCopy()};
    return a;
  }
  
  public Color getColor(){
    return col;
  }
  
  public boolean isDeletionMarked(){
    return forDeletion;
  }
  
  public void markDeletion(boolean b){
    forDeletion=b;
  }
  
  public double getAngleDegree(){
    double xLen = end.x-start.x;
    double yLen = end.y-start.y;
    //because the y coordinate in mathematics goes the other way
    yLen*=-1;
    return ((Math.atan2(yLen, xLen)* (180 / Math.PI))+360)%360;
  }
  
  public double getLength(){
    double xLen = end.x-start.x;
    double yLen = end.y-start.y;
    return Math.hypot(xLen, yLen);
  }
  
  public double getLength2(){
    double xLen = end.x-start.x;
    double yLen = end.y-start.y;
    return xLen*xLen+yLen*yLen;
  }
  
  @Override
    public Line clone(){
    Line l = new Line(start,  end, col);
    l.next = next;
    l.previous = previous;
    l.forDeletion = forDeletion;
    
    return l;
  }
  
  public Rectangle2D.Double getBounds(){
    Rectangle2D.Double rec = new Rectangle2D.Double();
    rec.x      = Math.min(start.x,  end.x);
    rec.y      = Math.min(start.y,  end.y);
    rec.width  = Math.abs(start.x - end.x);
    rec.height = Math.abs(start.y - end.y);
    return rec;
  }
  
}
