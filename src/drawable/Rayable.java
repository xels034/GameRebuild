package drawable;

import org.newdawn.slick.Image;

import construction.Vec2D;

public class Rayable extends Rasterable{

  private double length;
  private Vec2D baseDim;
  
  public Rayable(Vec2D p) {
    super(p);
    length=0;
    baseDim = new Vec2D(0,0);
  }
  
  public void setLength(double l){
    length=l;

    double scl = layerAffine.getFirst().scale;
    bounds.width=(length/scl);
  }
  
  public double getLength(){
    return length;
  }
  
  @Override
  public void addLayer(Image im, Affine af){
    super.addLayer(im, af);
    baseDim = new Vec2D(im.getWidth()*af.scale, im.getHeight()*af.scale);
  }
  
  public Vec2D getBaseDim(){
    return baseDim.getCopy();
  }

}
