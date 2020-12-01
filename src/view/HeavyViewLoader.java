package view;

import gui.Container;

import org.newdawn.slick.GameContainer;

import construction.TiledID;
import construction.Vec2D;

public class HeavyViewLoader extends Thread{
  
  private boolean gvFlag;
  private boolean svFlag;
  private Container cnt;
  
  private GameContainer gc;
  private long seed;
  private long radius;
  private double minZF;
  private double maxZF;
  private Vec2D focus;
  
  private TiledID gID;
  
  private boolean finished=true;
  
  public HeavyViewLoader(Container c){
    cnt=c;
    cnt.clear();
    finished=true;
  }
  
  public void setUpGV(GameContainer gc, long s, long radius, double minZF, double maxZF, Vec2D focus){
    if(finished){
      this.gc=gc;
      seed=s;
      this.radius=radius;
      this.minZF=minZF;
      this.maxZF=maxZF;
      this.focus=focus.getCopy();
      
      gvFlag=true;
      finished=false;
    }else{
      throw new IllegalStateException("Loader is already set up. Clear first");
    }
  }
  
  public void setUpSV(GameContainer gc, long s, long radius, double minZF, double maxZF, TiledID gID, Vec2D focus){
    if(finished){
      this.gc=gc;
      seed=s;
      this.radius=radius;
      this.minZF=minZF;
      this.maxZF=maxZF;
      this.gID=gID;
      this.focus=focus.getCopy();
      
      svFlag=true;
      finished=false;
    }else{
      throw new IllegalStateException("Loader is already set up. Clear first");
    }
  }
  
  @Override
  public void run(){
    if(gvFlag){
      GalaxyView gv = new GalaxyView(gc,seed,radius,minZF,maxZF);
      if(focus.getLength2() != 0){
        gv.calibrate(gv.maxZF, focus);
      }
      cnt.setObject(gv);
    }if(svFlag){
      SolView sv = new SolView(gc,seed,radius,minZF,maxZF,gID);
      if(focus.getLength2() != 0){
        sv.calibrate(sv.maxZF, focus);
      }
      cnt.setObject(sv);
    }
  }
  
  public void clear(){
    cnt.setObject(null);
    finished=true;
  }
  
}
