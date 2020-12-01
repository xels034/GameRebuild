package construction;

import gui.GL_Blender;

import org.newdawn.slick.Image;

public class ParticleEntry {
  
  public String name;
  
  public ValueRamp lifeVR;
  
  public Vec2D startPos;
  public Vec2D direction;
  public double posRand;
  
  public double density;
  
  public Image img;
  public double alpha;
  
  public double speed;
  public double spdRand;
  
  public double angle;
  public double anglRand;
  
  public double rot;
  public double rotRand;
  
  public double scale;
  public double sclRand;
  
  public double emitTime;
  public double emtRand;
  
  public double lifeTime;
  public double lifeRand;
  
  public int drawMode;
  
  public long timeStarted;
  public long lastTouch;
  
  public long pUnqID;
  
  public ParticleEntry(){
    name="Entry";
    lifeVR = new ValueRamp();
    startPos = new Vec2D();
    direction = new Vec2D();
    posRand= 0;
    density=0;
    img=null;
    alpha=0;
    speed=0;
    spdRand=0;
    angle=0;
    anglRand =0;
    scale=0;
    sclRand=0;
    emitTime=0;
    emtRand=0;
    lifeTime=0;
    lifeRand=0;
    drawMode = GL_Blender.MODE_TRUE_ADD;
    rot=0;
    rotRand=0;
  }
  
  public ParticleEntry(String nm, ValueRamp lvr, Vec2D sp, Vec2D dir, double pr, double dns, double a, Image i, double spd, double spdr,
             double angl, double anglr, double rot, double rotRand, double s, double sr, double emt, double emtr, double lt, double ltr, int dm, long pnd){
    name = nm;
    lifeVR=lvr;
    startPos = sp.getCopy();
    direction = dir.getCopy();
    posRand=pr;
    density = dns;
    img=i;
    alpha=a;
    speed=spd;
    spdRand=spdr;
    angle=angl;
    anglRand =anglr;
    scale=s;
    sclRand=sr;
    emitTime=emt;
    emtRand=emtr;
    lifeTime=lt;
    lifeRand=ltr;
    drawMode=dm;
    this.rot=rot;
    this.rotRand=rotRand;
    pUnqID = pnd;
  }
  
  public void touch(){
    timeStarted = System.currentTimeMillis();
    lastTouch = timeStarted;
  }
  
  public ParticleEntry getCopy(){
    return new ParticleEntry(name, lifeVR, startPos, direction, posRand, density, alpha, img, speed, spdRand, angle, anglRand, rot, rotRand, scale,
                         sclRand, emitTime, emtRand, lifeTime, lifeRand, drawMode, pUnqID);
  }
  
}
