package construction;


public class CollisionPair {

  private PhPoly pb1,pb2;
  private Vec2D cNormal;
  private Vec2D rSpeed;
  private Vec2D cPos;
  
  public CollisionPair(PhPoly p1, PhPoly p2, Vec2D v, Vec2D cp){
    pb1=p1;
    pb2=p2;
    cNormal = v;
    rSpeed = pb1.getVelocity().getCopy();
    rSpeed.subtract(pb2.getVelocity());
    
    cPos = cp;
  }
  
  public PhPoly getPB1(){
    return pb1;
  }
  
  public PhPoly getPB2(){
    return pb2;
  }
  
  public Vec2D getCNormal(){
    return cNormal.getCopy();
  }
  
  public Vec2D getCPos(){
    return cPos.getCopy();
  }
  
  //as seen by pb1
  public Vec2D getSpeedPB1(){
    Vec2D ret = rSpeed.getCopy();
    ret.x*=-1;
    ret.y*=-1;
    return ret;
  }
  
  //as seen by pb2
  public Vec2D getSpeedPB2(){
    return rSpeed.getCopy();
  }
}
