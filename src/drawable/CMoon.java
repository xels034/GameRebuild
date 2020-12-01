package drawable;

import java.util.Random;

import construction.TiledID;
import construction.ValueRamp;
import construction.Vec2D;

public class CMoon extends CPlanet{

  public static final int MIN_SIZE_M=250;
  public static final int MAX_SIZE_M=7500;
  
  public CMoon(long s, Vec2D pos, TiledID pid, CPlanet parent, int relPos) {
    super(s, pos, pid, parent.centerMass);
    this.centerMass = parent;
    
    Random r = new Random();
    r.setSeed(s);
    clearAllInfo();
    
    properties.remove("Kind");
    properties.put("Kind", "Moon");
    //properties.put("SDistance",dist+"");
    //properties.put("SDUnit", "km");
    double atmoThresh = buildOwnSize(r, centerMass);
    buildType(r, atmoThresh/2);
    if(Double.parseDouble(properties.get("Size"))<4000){
      properties.remove("Atmosphere");
      properties.put("Atmosphere", false+"");
    }
    buildWater(r, parent.centerMass);
    buildDetails(r);
    properties.put("Ring", "false");
    buildLayers(r);
    
    double pSize = Double.parseDouble(parent.properties.get("Size"));
    //double pDist = (r.nextDouble()*pSize)+pSize;
    //pDist*=relPos+1;
    double pDist = pSize*relPos*2;
    pDist += r.nextDouble()*pSize;
    pDist/=4;
    pDist+=pSize*2;
    properties.put("PDistance",pDist+"");
    properties.put("PUnit", "km");
    
    properties.remove("Moons");
    //scale = size;
  }
  
  protected double buildOwnSize(Random r, CBody parent){
    properties.remove("Temperature");
    properties.remove("TUnit");
    properties.remove("Size");
    properties.remove("SUnit");
    
    double dist = Math.hypot(position.x, position.y);
    
    ValueRamp sizeRamp = new ValueRamp();
    ValueRamp gaussRamp = new ValueRamp();
    ValueRamp gasGRamp = new ValueRamp();
    
    sizeRamp.changeEnds(0, 0.01);
    
    sizeRamp.addHandle(0.1, 0.3);
    sizeRamp.addHandle(0.25,0.5);
    sizeRamp.addHandle(0.45,1);
    sizeRamp.addHandle(0.5, 1.2);
    sizeRamp.addHandle(0.55,1);
    sizeRamp.addHandle(0.75,0.3);
    sizeRamp.addHandle(0.85,0.15);
    
    gaussRamp.addHandle(0.25, 0.4);
    gaussRamp.addHandle(0.75, 0.6);
    
    gasGRamp.addHandle(0.85, 0);
    gasGRamp.addHandle(0.925, 0.2);
    int sunTemp=Integer.parseInt(parent.centerMass.properties.get("Temperature"));
    double temperature = CSun.fTemp(dist,sunTemp)*1.2;
    
    temperature += (r.nextDouble()*50)+50;
    
    properties.put("Temperature", (int)temperature+"");
    properties.put("TUnit", "k");
    
    double relativeSizeMedian = sizeRamp.getValue(dist/CSun.getRawSize(sunTemp));
    double deviation = gaussRamp.getValue(r.nextDouble())*0.2;
    
    int size = MIN_SIZE_M+(int)(r.nextDouble()*Double.parseDouble((parent.properties.get("Size")))/10);
    if(size>MAX_SIZE_M){
      size = MAX_SIZE_M-(int)(r.nextDouble()*1500)+750;
    }
    
    properties.put("Size", size+"");
    properties.put("SUnit", "km");
    
    String sd = ""+(int)Math.hypot(position.x, position.y);
    
    properties.put("SDistance", sd);
    properties.put("SDUnit", "km");
    
    return relativeSizeMedian+deviation;
  }

}
