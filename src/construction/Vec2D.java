package construction;

import java.util.Objects;

public class Vec2D implements Comparable<Vec2D>{
    public double x;
    public double y;

    public Vec2D(){
        x=0f;
        y=0f;
    }
    
  public static void viewTransform(Vec2D v, Vec2D f, Vec2D s, double zF){
    v.subtract(f);
    v.x*=zF;
    v.y*=zF;
    v.x+=s.x/2;
    v.y+=s.y/2;
  }

    public Vec2D(double x, double y){
        this.x=x;
        this.y=y;
    }
    
    public double getLength(){
      return Math.hypot(x, y);
    }
    
    public double getLength2(){
      return x*x+y*y;
    }
    
    public void add(Vec2D p){
      x+=p.x;
      y+=p.y;
    }
    
    public void subtract(Vec2D p){
      x-=p.x;
      y-=p.y;
    }
    
    public void multiply(double s){
      x*=s;
      y*=s;
    }
    
    public double dot(Vec2D v){
      return x*v.x + y*v.y;
    }
    
    public void normalize(){
      double l = getLength();
      x/=l;
      y/=l;
    }

    public void clear(){
        x=0;
        y=0;
    }
    
    public Vec2D getCopy(){
      return new Vec2D(x,y);
    }
    
    @Override
    public int compareTo(Vec2D o){
      //sorts after x then after y
      if(o.x<x)return 1;
      else if(o.x>x)return -1;
      else if(o.y<y)return 1;
      else if(o.y>y)return -1;
      return 0;
    }

  @Override
  public boolean equals(Object arg0) {
    Vec2D c=(Vec2D)arg0;
    return (x==c.x && y==c.y);
  }

  @Override
  public int hashCode(){
    return Objects.hash(x, y);
  }
  
  @Override
  public String toString(){
    return String.format("%.4f:%.4f", x,y);
  }
}