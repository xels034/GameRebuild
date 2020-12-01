package drawable;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.newdawn.slick.Image;

import construction.Vec2D;

public class Rasterable {

  public float minScale,maxScale;

  protected float scale;
  public Vec2D position;
  protected boolean useable;
  public long birth;
  
  protected float opacity;
  
  protected boolean rasterable;
  protected Rectangle2D.Double bounds; //raw bounds, -width/2, -height/2
  
  protected LinkedList<Image> layer;
  protected LinkedList<Affine> layerAffine;
  
  public Affine rasterAffine; //the one storing viewPort dependant information. Position must be calculated into the affine!
  
  public Rasterable(Vec2D p){
    
    position = p;
    layer = new LinkedList<>();
    layerAffine = new LinkedList<>();
    
    birth = System.currentTimeMillis();
    resetRasterAffine();
    
    rasterable=false;
    
    minScale = 0;
    maxScale = Float.POSITIVE_INFINITY;
    
    scale = 1;
    
    opacity=1;
  }
  
    public Rectangle2D.Double getRawBounds(){
      return (Rectangle2D.Double)bounds.clone();
    }
    
    public boolean isRasterable(){
      return rasterable;
    }
    
    public Rectangle2D.Double getScreenBounds(Vec2D f, double zFactor, Vec2D screen){
      return getScreenBounds(f, zFactor, screen, 0);
    }
    
    public Rectangle2D.Double getScreenBounds(Vec2D f, double zFactor, Vec2D screen, int i){ //how big would it be on the screen
      Rectangle2D.Double retu = new Rectangle2D.Double(bounds.x, bounds.y, bounds.getWidth(), bounds.getHeight());
      
      double effectiveScale = zFactor*scale;
      
      effectiveScale = Math.max(minScale, effectiveScale);
      effectiveScale = Math.min(maxScale, effectiveScale);
      
      effectiveScale *= layerAffine.get(i).scale;
      
      
      
      retu.x *= effectiveScale;
      retu.y *= effectiveScale;
      retu.width *= effectiveScale;
      retu.height *= effectiveScale;
      
      Vec2D shift = new Vec2D(0,0);
      shift.add(position);
      shift.subtract(f);
      
      shift.x*=zFactor;
      shift.y*=zFactor;
      
      shift.x+=screen.x/2;
      shift.y+=screen.y/2;
      
      retu.x+=shift.x;
      retu.y+=shift.y;
      
      
      
      return retu;
    }
  
  public void addLayer(Image im, Affine af){
    layer.add(im);
    layerAffine.add(af);
    
    if(!rasterable){
      Image ref = layer.getFirst();
      bounds = new Rectangle2D.Double(-ref.getWidth()/2, -ref.getHeight()/2, ref.getWidth(), ref.getHeight());
      rasterable=true;
    }
  }
  
  public int getLayerCount(){
    return layer.size();
  }
  
  public Image getLayerImage(int idx){
    return layer.get(idx);
  }
  
  public Affine getLayerAffine(int idx){
    return layerAffine.get(idx);
  }
  
  public float getLayerAlpha(Vec2D f, double zFactor){
    //TODO Blending via ramp and so on;
    return 1;
  }
  
  public void setAffine(Affine a, int idx) throws IndexOutOfBoundsException{
    if(idx<layerAffine.size()){
      layerAffine.remove(idx);
      layerAffine.add(idx, a);
    }else{
      throw new IndexOutOfBoundsException("Affine Index o.o.b. Idx="+idx+", size="+layerAffine.size());
    }
  }
  
  public void setStraightOpacity(float o){
    opacity=o;
  }
  
  public float getOpacity(int idx, double zFactor){
    return opacity*(float)rasterAffine.alpha;
  }
  
  public Affine getRasterAffine(){
    return rasterAffine;
  }
  
  public void resetRasterAffine(){
    rasterAffine = new Affine();
    rasterAffine.scale = scale;
  }
  
  public Affine getAffine(int idx){
    return layerAffine.get(idx);
  }
  
  
  public Vec2D getVectorHandle(){
    return position;
  }
  
  public Vec2D getVector(){
    return position.getCopy();
  }
  public boolean useable(){
    return useable;
  }
}
