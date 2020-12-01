package drawable;

import org.newdawn.slick.Graphics;

import construction.Vec2D;

public class Affine {

  public int mode;
  public Vec2D translation;
  public double rotation; //in radiants 
  public double scale;
  public double alpha;
  
  public Affine(){
    mode = Graphics.MODE_NORMAL;
    translation = new Vec2D(0,0);
    rotation=0;
    scale=1;
    alpha=1;
  }
  
  public Affine(int m, Vec2D t, double r, double s, double a){
    mode =m;
    translation=t;
    rotation=r;
    scale = s;
    alpha=a;
  }
  
  public void reset(){
    mode = Graphics.MODE_NORMAL;
    translation = new Vec2D(0,0);
    rotation=0;
    scale = 1;
    alpha=1;
  }
  
  public void setMode(int m){
    mode=m;
  }
  
}
