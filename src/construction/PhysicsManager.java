package construction;

import gui.Def;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import drawable.ParticleDraw;

public class PhysicsManager {

  private class Worker extends Thread{
    @Override
    public void run(){
      while(doCycle){
        updateCycle();
      }
      System.out.println("Phm Worker out of cycle");
    }
  }
  
  public static final int LYR_UNCK = 0;
  public static final int LYR_DBRS = 1;
  public static final int LYR_HLL1 = 2;
  public static final int LYR_HLL2 = 3;
  public static final int LYR_MSL1 = 4;
  public static final int LYR_MSL2 = 5;
  public static final int LYR_PRJ1 = 6;
  public static final int LYR_PRJ2 = 7;
  public static final int LYR_RAY1 = 8;
  public static final int LYR_RAY2 = 9;
  
  public static final int[][] LAYER_JT = {{0,0,0,0,0,0,0,0,0,0}, //unchecked
            /*debris*/      {0,1,1,1,1,1,1,1,1,1},
            /*hull1*/      {0,1,1,1,0,1,1,1,1,1},
            /*hull2*/      {0,1,1,1,1,0,1,1,1,1},
            /*mssl1*/      {0,1,0,1,0,0,0,1,0,1},
            /*mssl2*/      {0,1,1,1,0,0,1,0,1,0},
            /*prjc1*/      {0,1,1,1,0,1,0,0,0,0},
            /*prjc2*/      {0,1,1,1,1,0,0,0,0,0},
            /*ray1 */      {0,1,1,1,0,1,0,0,0,0},
            /*ray2 */      {0,1,1,1,1,0,0,0,0,0}};
  
  private static final int QT_MD = 64;
  private static final int QT_MI = 5;
  
  private boolean doCycle;
  private long lastUpdate;
  private double radius;
  
  private LinkedList<PhPoly> polyItems;
  private LinkedList<PhPoly> particleItems;
  private LinkedList<PhRay> rayItems;
  private QuadTree tree;
  private ParticleCreator pcr;
  
  private Worker mThread;
  
  public PhysicsManager(double r){
    radius=r;
    polyItems = new LinkedList<>();
    particleItems = new LinkedList<>();
    rayItems = new LinkedList<>();

    Rectangle2D.Double rct = new Rectangle2D.Double(-radius,-radius,radius*2,radius*2);
    tree = new QuadTree(rct, QT_MD, QT_MI, 0);
    pcr = new ParticleCreator();
    
  }
  
  public void start(){
    doCycle = true;
    mThread = new Worker();
    mThread.start();
  }
  
  public void stop(){
    doCycle=false;
  }
  
  public void updateCycle(){
    //wait til nextCycle
    long now = System.currentTimeMillis();
    long sleepTime =  now - (lastUpdate+Def.PHYS_T_STEP);
    lastUpdate = now;
    sleepTime*=-1;
    sleepTime = Math.max(0, sleepTime);
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      //Nobody fucking cares anyway
      e.printStackTrace();
    }
    
    synchronized(this){
      //update
      for(PhPoly pb: polyItems){
        pb.update();
      }for(PhPoly pb : particleItems){
        pb.update();
      }
      
      //rebuild quadTree
      tree.clear();
      for(PhPoly pb : polyItems){
        tree.insert(pb);
      }
      
      LinkedList<CollisionPair> candidates = new LinkedList<>();
      LinkedList<PhPoly> treeCandidates;
      LinkedList<PhPoly> forDeletion = new LinkedList<>();
      
      //lines
      LinkedList<Line> lineCands = new LinkedList<>();
      //though all line objects
      for(PhRay l : rayItems){
        l.target=null;
        l.setCoef(1);
        treeCandidates = tree.getCandidates(l.getLine().getBounds());
        //through all candidates that could be touching
        for(PhPoly pb : treeCandidates){
          if(LAYER_JT[pb.getLayerID()][l.LYR_T]==1 && l.unqID != pb.unqID){
            lineCands = pb.getShape().getFastPlot();
            //through all their poly lines
            for(Line lc : lineCands){
              double[] uv = Line.getUV(l.getLine(), lc);
              //if there's a valid intersection ...
              if(uv[0] >= 0 && uv[0] <= 1 && uv[1] >= 0 && uv[1] <= 1){
                //...and its nearer than the current, make this the new hit target
                if(uv[0] < l.getCoef()){
                  l.setCoef(uv[0]);
                  l.target = pb;
                }
              }
            }
          }
        }

        //line resolving
        if(l.target != null){
          l.target.collide(l.DMG_T, l.DMG_V);
          
          Vec2D[] vecs = l.getLine().getPoints();
          double co = l.getCoef();
          Vec2D cCenter = vecs[1].getCopy();
          cCenter.subtract(vecs[0]);
          cCenter.multiply(co);
          cCenter.add(vecs[0]);

          for(ParticleEntry pe : l.pCollList){
            pe.startPos = cCenter;
            pcr.addSystem(pe);
          }for(ParticleEntry pe : l.target.pCollList){
            pe.startPos = cCenter;
            pcr.addSystem(pe);
          }
          
          if(l.target.life <= 0){
            forDeletion.add(l.target);
            pcr.addAllSystems(l.target.pDeathList);
          }
        }
        
      }

      //broadTest

      for(PhPoly pb : polyItems){
        treeCandidates = tree.getCandidates(pb.getSquareBounds());
        for(PhPoly cpb: treeCandidates){
          int d = LAYER_JT[pb.getLayerID()][cpb.getLayerID()];
          if(cpb.lastChecked!= now && d == 1 && PhPoly.SATCollision(pb, cpb)){
            candidates.add(new CollisionPair(pb,cpb, new Vec2D(0,0), null));
          }
        }
        pb.lastChecked=now;
      }

      //narrow test
      LinkedList<CollisionPair> collisions = new LinkedList<>();
      
      PhPoly pb1,pb2;
      Vec2D v, center = new Vec2D();
      for(CollisionPair cp : candidates){
        pb1 = cp.getPB1();
        pb2 = cp.getPB2();
        v = PhPoly.getCollisionNormal(pb1, pb2, center);
        if(v != null){
          collisions.add(new CollisionPair(pb1,pb2,v, center));
        }  
      }
      //resolving

      for(CollisionPair cp : collisions){
        pb1 = cp.getPB1();
        pb2 = cp.getPB2();
        
        pb1.collide(pb2.DMG_T, pb2.DMG_V);
        pb2.collide(pb1.DMG_T, pb1.DMG_V);
        
        for(ParticleEntry pe : pb1.pCollList){
          pe.startPos = cp.getCPos();
          pcr.addSystem(pe);
        }for(ParticleEntry pe : pb2.pCollList){
          pe.startPos = cp.getCPos();
          pcr.addSystem(pe);
        }
        
        if(pb1.life <= 0){
          forDeletion.add(pb1);
          pcr.addAllSystems(pb1.pDeathList);
        }if(pb2.life <= 0){
          forDeletion.add(pb2);
          pcr.addAllSystems(pb2.pDeathList);
        }
        
        Vec2D cNormal = cp.getCNormal();
        Vec2D velTo1 = cp.getSpeedPB1();
        
        if(velTo1.getLength2() < 2){
          velTo1 = cNormal.getCopy();
          double x = pb2.getSquareBounds().width;
          double y = pb2.getSquareBounds().height;
          
          x = Math.max(x, pb1.getSquareBounds().width);
          y = Math.max(y,  pb1.getSquareBounds().height);

          velTo1.multiply(.001*Math.hypot(x, y));
        }
        
        double absorb = Math.min(pb1.getRestitution(), pb2.getRestitution());
        absorb+=1;
        
        //factor of inverse mass. between 1 and 2 for stillstand to total 
        double m = Math.min(pb1.getMass(), pb2.getMass());
        velTo1.multiply(.05*m*absorb);
        //rest is no real restitution. more like a correction factor
        //to get objects apart fast enough, but not shooting out
        pb1.applyForce(new Vec2D(velTo1.x, velTo1.y));
        pb2.applyForce(new Vec2D(-velTo1.x, -velTo1.y));
      }
      
      //escapers
      forDeletion.addAll(tree.getEscapers());
      polyItems.removeAll(forDeletion);
      
      
      forDeletion.clear();
      for(PhPoly pp : particleItems){
        ParticleDraw pd = (ParticleDraw)pp.getEffigy();
        if(pd.isDead()) forDeletion.add(pp);
      }
      particleItems.removeAll(forDeletion);
      particleItems.addAll(pcr.generate());
    }
  }
  
  public synchronized void addPoly(PhPoly pb){
    polyItems.add(pb);
    pcr.addAllSystems(pb.pBirthList);
  }
  
  public synchronized void addRay(PhRay r){
    rayItems.add(r);
    pcr.addAllSystems(r.pBirthList);
  }
  
  public synchronized PhPoly removePoly(PhPoly pb){
    int idx = polyItems.indexOf(pb);
    if(idx > -1){
      pcr.addAllSystems(pb.pDeathList);
      return polyItems.remove(polyItems.indexOf(pb));
    }
    return null;
  }
  
  public synchronized PhRay removeRay(PhRay r){
    int idx =rayItems.indexOf(r);
    if(idx > -1){
      pcr.addAllSystems(r.pDeathList);
      return rayItems.remove(rayItems.indexOf(r));
    }
    return null;
  }
  
  public synchronized void removeEffects(long id){
    pcr.abortSystems(id);
  }
  
  public synchronized LinkedList<PhPoly> getPolyList(){
    LinkedList<PhPoly> ret = new LinkedList<>();
    ret.addAll(polyItems);
    ret.addAll(particleItems);
    return ret;
  }
  
  @SuppressWarnings("unchecked")
  public synchronized LinkedList<PhRay> getRayList(){
    return (LinkedList<PhRay>)rayItems.clone();
  }
}
