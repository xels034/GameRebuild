package drawable;

import construction.ValueRamp;
import construction.Vec2D;

public class ParticleDraw extends Rasterable{

  private double lifeTime;
  private ValueRamp opacRamp;
  
  public ParticleDraw(Vec2D p, double lt, ValueRamp vr) {
    super(p);
    lifeTime = lt;
    opacRamp = vr;
  }
  
  @Override
  public float getOpacity(int idx, double zFactor){
    long gap = System.currentTimeMillis()-birth;
    double f = gap/(lifeTime+.0);

    return (float)(opacRamp.getValue(f)*rasterAffine.alpha);
  }
  
  public boolean isDead(){
    return(System.currentTimeMillis()-lifeTime > birth);
  }

}
