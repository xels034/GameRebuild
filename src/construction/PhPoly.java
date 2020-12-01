package construction;

import gui.Def;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import drawable.Rasterable;
import drawable.V2DPolygon;

public class PhPoly{

  private static final double TRQ_THRSH = 0.01;
  
  private double rotation;
  private double mass;
  private double inv_mass;
  private double restitution;
  
  private Vec2D position;
  private V2DPolygon boundry;
  private Rasterable effigy;
  
  private Vec2D vel;
  private double torque;
  private long lastUpdate;
  private int layerID;
  public long unqID;
  
  public long lastChecked;
  public double life;
  
  public final int DMG_T;
  public final double DMG_V;
  public final int HLL_T;
  
  public LinkedList<ParticleEntry> pBirthList, pCollList, pDeathList;
  public long pUnqID;
  
  public PhPoly(Vec2D p, V2DPolygon b, Rasterable r, int lid, double lf, double dmgv, PhMaterial mat, long unqid) {
    pBirthList = new LinkedList<>();
    pCollList = new LinkedList<>();
    pDeathList = new LinkedList<>();
    
    position = p.getCopy();
    boundry=b;
    effigy=r;
    rotation=0;
    mass=mat.getMass(boundry.getArea());
    inv_mass = 1/mass;
    
    lastUpdate = System.currentTimeMillis();
    torque=0;
    vel = new Vec2D(0,0);
    layerID = lid;
    
    restitution = mat.absorbtion;
    
    life = lf;
    
    DMG_T = mat.DMG_T;
    DMG_V = dmgv;
    HLL_T = mat.HLL_T;
    unqID = unqid;
    pUnqID = UniqueID.newID();
  }
  
  public static boolean SATCollision(PhPoly pb1, PhPoly pb2){
    if(pb1 == pb2) return false;
    if(pb1.unqID == pb2.unqID) return false;
    //means they're on the same "team"
    Rectangle2D.Double rec1 = pb1.getSquareBounds();
    Rectangle2D.Double rec2 = pb2.getSquareBounds();

    /*System.out.println("---");
    System.out.println(rec1);
    System.out.println(rec2);*/
    
    double minWidth = rec1.width+rec2.width;
    double minHeight = rec1.height+rec2.height;
    
    double minX = Math.min(rec1.x, rec2.x);
    double minY = Math.min(rec1.y, rec2.y);
    
    double maxX = Math.max(rec1.x+rec1.width, rec2.x+rec2.width);
    double maxY = Math.max(rec1.y+rec1.height, rec2.y+rec2.height);
    
    double xLen = maxX-minX;
    double yLen = maxY-minY;
    
    if(xLen > minWidth){
      return false;
    }if(yLen > minHeight){
      return false;
    }
    return true;
  }
  
  public static Vec2D getCollisionNormal(PhPoly pb1, PhPoly pb2, Vec2D pos){
    //pos as a by-ref parameter
    Line l = pb1.getShape().intersectedLine(pb2.getShape(), pos);
    if(l != null && pb1 != pb2){
      Vec2D dir = l.getPoints()[1].getCopy();
      dir.subtract(l.getPoints()[0]);
      
      //right hand normal, pointing outwards in a regular v2dp
      double tmp = dir.x;
      dir.x = dir.y;
      dir.y = -tmp;

      tmp = Math.hypot(dir.x, dir.y);
      dir.x/=tmp;
      dir.y/=tmp;
      return dir;
    }
    if(pb1.getShape().contains(pb2.getShape())){
      pos.x = pb2.getPosition().x;
      pos.y = pb2.getPosition().y;
      return new Vec2D(pb2.position.x-pb1.position.x, pb2.position.y-pb1.position.y);
    }if(pb2.getShape().contains(pb1.getShape())){
      //pos = pb1.getPosition();
      pos.x = pb2.getPosition().x;
      pos.y = pb2.getPosition().y;
      return new Vec2D(pb1.position.x-pb2.position.x, pb1.position.y-pb2.position.y);
    }
    return null;
  }
  
  public Rectangle2D.Double getSquareBounds(){
    Vec2D[] points = boundry.getBounds().getPoints();
    
    return new Rectangle2D.Double(points[0].x, points[0].y, points[1].x-points[0].x, points[1].y-points[0].y);
  }
  
  public V2DPolygon getShape(){
    return boundry;
  }
  
  public synchronized void collide(int dmgt, double dmgv){
    double c = PhMaterial.getCoeff(HLL_T, dmgt);
    life-=dmgv*c;
  }
  
  public synchronized void update(){
    lastUpdate=System.currentTimeMillis();
    
    position.add(vel);
    rotation+=torque;
    
    effigy.position = position.getCopy();
    effigy.rasterAffine.rotation = rotation;
    
    boundry.setTranslation(position);
    boundry.setRotation(rotation);
    
    //if(peDeath != null) peDeath.startPos = position.getCopy();
    for(ParticleEntry pe : pDeathList){
      pe.startPos = position;
    }for(ParticleEntry pe : pCollList){
      pe.startPos = position;
    }
  }
  
  public void applyExtrapolation(){
    long now = System.currentTimeMillis();
    double gap = now-lastUpdate;
    gap = Math.min(Def.PHYS_T_STEP, gap);

    Vec2D adder = vel.getCopy();    
    adder.multiply(gap/Def.PHYS_T_STEP);
    adder.multiply(2);
    
    adder.add(position);
    effigy.position = adder;
    effigy.rasterAffine.rotation += torque*(gap/Def.PHYS_T_STEP);
  }
  
  public Rasterable getEffigy(){
    return effigy;
  }
  
  public synchronized void setVelocity(Vec2D v){
    vel = v.getCopy();
  }
  
  public synchronized void setTorque(double t){
    torque = t;
  }
  
  public synchronized void setRotation(Double r){
    rotation = r;
  }
  
  public synchronized void applyForce(Vec2D v){
    Vec2D f = v.getCopy();
    f.x*=inv_mass;
    f.y*=inv_mass;
    vel.add(f);
  }
  
  public synchronized void applyTorque(double t){
    torque+=(t*inv_mass);
  }
  
  public synchronized void applyTorque(double t, boolean c){
    torque+=(t*inv_mass);
    if(c && torque < TRQ_THRSH){
      torque=0;
    }
  }
  
  public synchronized void setPosition(Vec2D v){
    position=v.getCopy();
  }
  
  public synchronized Vec2D getPosition(){
    return position.getCopy();
  }
  
  public synchronized Vec2D getVelocity(){
    return vel;
  }
  
  public synchronized double getRotation(){
    return rotation;
  }
  
  public synchronized double getTorque(){
    return torque;
  }
  
  public double getMass(){
    return mass;
  }
  
  public double getRestitution(){
    return restitution;
  }
  
  public int getLayerID(){
    return layerID;
  }

}
