package construction;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import drawable.Rasterable;
import drawable.Rayable;

public class PhRay {
  
  private Vec2D position;
  private double length;
  private double rotation;
  
  private Line l;
  private double coef;
  public PhPoly target;
  public final int DMG_T;
  public final int LYR_T;
  public final double DMG_V;
  
  public long unqID;
  public long pUnqID;
  
  private Rayable effigy;
  
  public LinkedList<ParticleEntry> pBirthList, pCollList, pDeathList;
  //public ParticleEntry peBirth,peCollision,peDeath;
  
  public PhRay(Vec2D strt,double len,double rot, int dmgt, double dmgv, int lyr, Rayable r, long unqid){
    pBirthList = new LinkedList<>();
    pCollList = new LinkedList<>();
    pDeathList = new LinkedList<>();
    
    position = strt.getCopy();
    length = len;
    rotation=rot;
    buildLine();

    coef=1;
    DMG_T = dmgt;
    DMG_V = dmgv;
    LYR_T = lyr;
    
    effigy = r;
    effigy.rasterAffine.rotation = -rot;
    
    unqID = unqid;
    pUnqID = UniqueID.newID();
  }
  
  private void buildLine(){
    Vec2D end = new Vec2D(length*Math.cos(rotation), -length*Math.sin(rotation));
    end.add(position);
    
    l = new Line(position.getCopy(),end,Color.black);
  }
  
  public double getCoef(){
    return coef;
  }
  
  public void setCoef(double d){
    coef = d;
    if(effigy != null)
      effigy.setLength(coef*l.getLength());
  }
  
  public Line getLine(){
    return l;
  }
  
  public Rasterable getEffigy(){
    return effigy;
  }
  
  public void setPosition(Vec2D v){
    position=v.getCopy();
    effigy.position=position.getCopy();
    buildLine();
  }
  
  public void translate(Vec2D v){
    position.add(v);
    effigy.position=position.getCopy();
    buildLine();
  }
  
  public Vec2D getPosition(){
    return position.getCopy();
  }
  
  public void setRotation(double r){
    rotation=r;
    //effigy.getAffine(0).rotation=-rotation;
    effigy.rasterAffine.rotation = rotation;
    buildLine();
  }
  
  public void rotate(double r){
    rotation+=r;
    //effigy.getAffine(0).rotation=-rotation;
    effigy.rasterAffine.rotation= rotation;
    buildLine();
  }
  
  public double getRotation(){
    return rotation;
  }
}
