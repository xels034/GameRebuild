package view;

import construction.ValueRamp;
import construction.Vec2D;

public class TZoomManager {
  
  private Vec2D vStart;
  private Vec2D vEnd;
  private Vec2D vDelta;
  
  private double sStart;
  private double sEnd;
  private double sDelta;
  
  private long tStart;
  private long tDelta;
  
  private ValueRamp tRamp;
  //private ValueRamp curvedRamp;
  
  public TZoomManager(){
    tRamp = new ValueRamp();
    //curvedRamp = new ValueRamp();
    //curvedRamp.addHandle(.25, .1);
    //curvedRamp.addHandle(.75, .9);
    tDelta=200;
  }
  
  public void update(Vec2D v, double s){
    putOrder(new Vec2D(), v, 0, s);
    //*1.1 to be sure
    tStart-=tDelta*1.1;
  }
  
  public void setDuration(long d){
    tDelta=d;
  }
  
  public void putOrder(Vec2D vs, Vec2D ve, double ss, double se){
    double state = getRelative();
    if(state<1){
      tStart = (long)(System.currentTimeMillis()-(tDelta*state));
    }else{
      tStart = System.currentTimeMillis();

    }
    vStart=vs.getCopy();
    sStart=ss;
    sEnd=se;
    vEnd=ve.getCopy();
    vDelta = new Vec2D(vEnd.x-vStart.x, vEnd.y-vStart.y);
    sDelta = sEnd-sStart;
  }
  
  public Vec2D getTargetVector(){
    return vEnd.getCopy();
  }
  
  public double getTargetScalar(){
    return sEnd;
  }
  
  public Vec2D getVector(){
    double r = getRelative();
    return new Vec2D(vStart.x+vDelta.x*r, vStart.y+vDelta.y*r);
  }
  
  public double getScalar(){
    return sStart+sDelta*getRelative();
  }
  
  private double getRelative(){
    //total time passed
    double relativity = System.currentTimeMillis()-tStart;
    //percent regarding to time it should take
    relativity/=tDelta;
    //modified by ramp
    return tRamp.getValue(relativity); 
  }
}
