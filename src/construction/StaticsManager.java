package construction;

import gui.GL_Blender;
import gui.UITheme;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.newdawn.slick.Image;

import drawable.CMoon;
import drawable.CPlanet;
import drawable.Affine;
import drawable.BGRasterable;
//import drawable.CRing;
import drawable.CSRing;
import drawable.CSun;
import drawable.Rasterable;
import drawable.Pathable;
import drawable.V2DPolygon;

public class StaticsManager {

  private Vec2D focus;
  private Vec2D screenDim;
  private double zFactor;
  private boolean addSel;

  private LinkedList<Rasterable> rasterItems;
  private LinkedList<Rasterable> selectedItems;
  private LinkedList<Pathable> pathItems;
  private LinkedList<Pathable> cigriItems;
  private Rasterable backGround;
  private CircularGrid cigri;
  private Rectangle2D.Double selRect;

  private Boolean updateFlag=false;
  private Boolean selectionFlag=false;
  private ReentrantLock defaultLock,selectionLock;
  private boolean update;
  private ManagerThread mThread;

  private class ManagerThread extends Thread{
    @Override
    public void run(){
      updateCycle();
    }
  }

  public StaticsManager(long s, TiledID id, Vec2D scr, double minZF, long radius){
    focus = new Vec2D(0,0);
    screenDim = scr.getCopy();
    zFactor=1;

    rasterItems = new LinkedList<>();
    selectedItems = new LinkedList<>();
    pathItems = new LinkedList<>();
    cigriItems = new LinkedList<>();

    selRect = new Rectangle2D.Double(0,0,0,0);
    defaultLock = new ReentrantLock();
    selectionLock = new ReentrantLock();
    update = true;
    addSel=false;

    long maxPlanet=buildSystem(s, id);

    backGround = new BGRasterable(new Vec2D(0,0), (100f/radius));
    CloudsNoise cNoise = new CloudsNoise(Long.MAX_VALUE, CloudsNoise.TYPE_BICUBIC);
    //cNoise.setOffset(new Vec2D(maxPlanet, maxPlanet));

    Random r = new Random();
    r.setSeed(s);

    double l = r.nextDouble()*1000;

    cNoise.setOffset(new Vec2D(l, l));

    Image img = cNoise.getTexRB(64, 64, s, 30, 2, .6);
    Affine af = new Affine();
    af.setMode(GL_Blender.MODE_TRUE_ADD);
    backGround.setStraightOpacity(.4f);
    backGround.addLayer(img, af);

    cigri = new CircularGrid((int)(maxPlanet*1.1), UITheme.dark_s);
    mThread = new ManagerThread();
    mThread.start();
  }

  private long buildSystem(long s, TiledID id){
    Random r = new Random();
    r.setSeed(s);

    TiledID sID = new TiledID(id.toString());
                      //sun must add its part seperatley!
    CSun cs = new CSun(s,new Vec2D(0,0),sID,0);

    double temp = Double.parseDouble(cs.properties.get("Temperature"));
    double sysSize = CSun.getRawSize(temp);
    int erg= Integer.parseInt(cs.properties.get("Planets"));

    LinkedList<CPlanet> planets = new LinkedList<>();
    LinkedList<CMoon> moons = new LinkedList<>();
    //LinkedList<CRing> rings = new LinkedList<CRing>();

    ValueRamp vr = new ValueRamp();
    vr.changeEnds(.4, .6);
    vr.addHandle(0.8, 0.5);

    for(int i=0;i<erg;i++){

      double bDist = sysSize/erg;
      double dist=bDist+r.nextDouble()*(bDist*0.5)-(bDist*0.25);
      dist+=bDist*i;



      /*if(i>erg/2 && hasRing){
        csr = new CSRing(r.nextLong(), new Vec2D(0,0), cs.getID(), im, cs, dist);
        hasRing = false;
        //rings.add(new CRing(r.nextLong(), new Vec2D(0,0), cs.getID(), im, cs));System.out.println(dist);
        System.out.println("R: "+dist);
      }else{*/
        //System.out.println("p: "+dist);
        V2DPolygon orb = buildOrbit(dist,r,"Planet "+(i+1), cs.position);
        orb.setValueRamp(vr);
        pathItems.add(orb);
        LinkedList<Vec2D> ll = orb.getVertices();

        TiledID pID = new TiledID(sID.toString());
        pID.addTrabant(i);

        planets.add(new CPlanet(r.nextLong(), ll.getFirst(), pID, cs));
        /*if(planets.getLast().properties.get("Ring").equals("true")){
          rings.add(new CRing(r.nextLong(), ll.getFirst(), new TiledID(pID.toString()+"r"), im, planets.getLast()));
        }*/

        int mErg = Integer.parseInt(planets.getLast().properties.get("Moons"));
        for(int j=0;j<mErg;j++){


          TiledID mID = new TiledID(pID.toString());
          mID.addTrabant(j);
          CMoon moo = new CMoon(r.nextLong(), planets.getLast().position.getCopy(), mID, planets.getLast(),j);

          double pDist = Double.parseDouble(moo.properties.get("PDistance"));
          orb = buildOrbit(pDist,r,"", planets.getLast().position.getCopy());
          orb.setValueRamp(vr);
          //orb.setValueRamp(vr);
          pathItems.add(orb);

          ll = orb.getVertices();

          moo.position.x = ll.getFirst().x;
          moo.position.y = ll.getFirst().y;

          moons.add(moo);
        }
      //}
    }
    rasterItems.add(cs);
    rasterItems.addAll(planets);
    rasterItems.addAll(moons);
    return (long)Math.hypot(planets.getLast().position.x, planets.getLast().position.y);
  }

  private V2DPolygon buildOrbit(double d, Random r, String t, Vec2D center){
    LinkedList<Vec2D> verts = new LinkedList<>();

    int resolution=128;

    int offset = (int)(r.nextDouble()*resolution);

    double rad=0;
    double stepSize = (2*Math.PI)/resolution;
    rad+=(offset*stepSize);
    Vec2D v;

    //+1 because V2DPolygon closes itself
    //this producec a double vertex but whatever
    for(int i=0;i<resolution+1;i++){
      v = new Vec2D(d*Math.cos(rad),d*Math.sin(rad));
      v.add(center);
      verts.add(v);
      rad+=stepSize;
    }
    //System.out.println(rad);
    return new V2DPolygon(verts,new Vec2D(0,0),UITheme.main,t);
  }

  public void shutDown(){
    update=false;
    mThread.interrupt();
    System.out.println("shutdown the tm, update is "+update);
  }

  public void translate(double factor,double x, double y){
    if(defaultLock.tryLock()){
      if(focus.x!=x || focus.y!=y || zFactor!=factor){
        focus.x=x;
        focus.y=y;
        zFactor=factor;
        synchronized(cigri){
          cigri.notifyAll();
        }
        synchronized(updateFlag){
          updateFlag=true;
        }
      }
      defaultLock.unlock();
    }
  }

  public void setSelection(Rectangle r, boolean add){
    if(selectionLock.tryLock()){
      selRect=new Rectangle2D.Double(r.getX(),r.getY(),r.getWidth(),r.getHeight());
      synchronized(selectionFlag){
        selectionFlag=true;
        addSel=add;
      }
      synchronized(cigri){
        cigri.notifyAll();
      }
      selectionLock.unlock();
    }
  }

  private void updateCycle(){
    while(update){
      try{
        if(updateFlag){
          synchronized(updateFlag){
            updateFlag=false;
          }
          defaultLock.lock();
          updateCigri();
          defaultLock.unlock();
        }

        if(selectionFlag){
          synchronized(selectionFlag){
            selectionFlag=false;
          }
          selectionLock.lock();
          updateSelection();
          selectionLock.unlock();
        }
        Thread.sleep(100);
        if(!updateFlag && !selectionFlag){
          synchronized(cigri){
            cigri.wait();
          }
        }
      } catch (InterruptedException exc){
        System.out.println("Manager interrupted in update method");
      }
    }
  }

  private void updateCigri(){
    cigri.updateTiles(focus, zFactor, screenDim);
    LinkedList<CGTile> cigris = cigri.getAllTiles();
    cigriItems.clear();

    for(CGTile cgt: cigris){
      if(!cgt.hasChildren()){
        cigriItems.add(cgt);
      }
    }
  }

  private void updateSelection(){
    Rectangle2D.Double bodyBounds;
    if(!addSel){
      selectedItems.clear();
    }
    addSel=false;
    for(Rasterable b:rasterItems){
      bodyBounds = b.getScreenBounds(focus, zFactor, screenDim);
      if(selRect.intersects(bodyBounds) && !(b instanceof CSRing) && !selectedItems.contains(b)){
        selectedItems.add(b);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public LinkedList<Rasterable> getRasterItems(){
    if(defaultLock.tryLock()){
      try{
        LinkedList<Rasterable> ret = (LinkedList<Rasterable>)rasterItems.clone();
        ret.addFirst(backGround);
        return ret;
      } finally{
        defaultLock.unlock();
      }
    }else{
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public LinkedList<Rasterable> getSelectedItems(){
    if(selectionLock.tryLock()){
      try{
        return (LinkedList<Rasterable>)selectedItems.clone();
      }finally{
        selectionLock.unlock();
      }
    }else{
      return null;
    }
  }

  public LinkedList<Pathable> pathItems(){
    if(defaultLock.tryLock()){
      try{
        LinkedList<Pathable> returner = new LinkedList<>();
        returner.addAll(cigriItems);
        returner.addAll(pathItems);
        return returner;
      }finally{
        defaultLock.unlock();
      }
    }else{
      return null;
    }
  }

  public void forceSelection(Rasterable r){
    selectionLock.lock();
    selectedItems.clear();
    selectedItems.add(r);
    selectionLock.unlock();
  }
}
