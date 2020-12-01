package drawable;

import java.util.HashMap;

import construction.TiledID;
import construction.Vec2D;

public class CBody extends Rasterable{

  protected TiledID tid;
  protected long seed;
  protected CBody centerMass;
  
  public HashMap<String,String> properties;
  
  public CBody(long s, Vec2D p, TiledID pid) {
    super(p);
    seed=s;
    tid=new TiledID(pid.toString());
    //tid.addPosInTile(x);
    properties = new HashMap<>();
  }
  
  @Override
  public float getOpacity(int idx, double zFactor){
    return Math.min(System.currentTimeMillis()-birth,500)/500f;
  }
  public long getSeed(){
    return seed;
  }
  
  public TiledID getID(){
    return tid;
  }
  @Override
  public String toString(){
    return this.tid.toString();
  }
}
