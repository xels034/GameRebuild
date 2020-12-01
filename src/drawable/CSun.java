package drawable;

import java.util.Random;

import org.newdawn.slick.Image;

import construction.ValueRamp;
import construction.Vec2D;
import gui.GL_Blender;
import gui.ImageManager;
import construction.TiledID;

public class CSun extends CStar{

  private static final double SLOPE = 0.0000001; // 2 null is grad dazu
  private ValueRamp vr;
  
  public CSun(long s, Vec2D pos, TiledID pid, int x) {
    super(s, pos, pid, 1);
    //CBody would add a tileCount, not needed
    //TODO use X for multi-Sun Systems
    tid=pid;
    tid.addCenterMass(0);
    int temp = (int)(Double.parseDouble(properties.get("Size"))*5000);
    
    properties.remove("Kind");
    properties.put("Kind", "Sun");
    
    properties.put("Temperature", temp+"");
    properties.put("TUnit", "k");
    properties.put("System Size", fSize(temp)+"");
    
    ValueRamp pAmt = new ValueRamp();
    pAmt.changeEnds(2, 20);
    pAmt.addHandle(0.3, 15);
    
    double ix = (temp/5000.0)/15.0;
    
    Random r = new Random();
    r.setSeed(seed);
    double pAmtBase= r.nextDouble();
    ix = pAmt.getValue(ix);
    pAmtBase*=0.7*ix;
    pAmtBase-=0.35*ix;
    
    int erg=(int)(ix+pAmtBase+0.5);
    
    properties.put("Planets", erg+"");
    
    int d=0;
    float begin=0,end=0;
    begin = 2e-6f;  //< 1
    end = 8e-5f;    // > 0
    String type = properties.get("Type");
    if(type.equals("Class M")){
      begin = 4.5e-4f;
      end = 0.003f;
    }else if(type.equals("Class K")){
      d=1;
      
      begin=2e-4f;
      end = 0.002f;
    }else if(type.equals("Class F")){
      d=2;
      
      begin=9e-5f;
      end=0.001f;
    }else if(type.equals("Class B")){
      d=3;
      
      begin = 4e-5f;
      end = 3.5e-4f;
    }else if(type.equals("Class O")){
      d=4;
      
      begin = 2e-6f;  // < 1
      end = 8e-5f;    // > 0
    }
    
    vr = new ValueRamp();
    vr.changeEnds(1, 0.1);
    vr.addHandle(begin, 1);
    vr.addHandle(end, 0.1);
    
    layer.clear();
    layerAffine.clear();
    rasterable = false;
    
    Image i = ImageManager.getImage(ImageManager.OFFS_SUN+d);
        Affine a = new Affine();
        //1/i.getWidth would be normalization to get 1px convention
        //*scale to get it up where the game wats it. so its scale/i.getWidth
        float correction = 1f/i.getWidth();  
        a.scale = correction;
        scale = Float.parseFloat(properties.get("Size"))*700000;//1px=1m 1R=700km
        //scale=sca;//mopdify the tile-given scale by startype
        //how much it must be shifted due to scaling, so that its in the center again
        float translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_TRUE_ADD);
        addLayer(i, a);
        
        i = ImageManager.getImage(ImageManager.OFFS_SFLR+d);
        a = new Affine();
        correction = 1f/i.getWidth();
        a.scale = correction*100;
        translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate, translate));
        a.setMode(GL_Blender.MODE_TRUE_ADD);
        addLayer(i,a);
        addLayer(i, new Affine(a.mode,a.translation,a.rotation,a.scale,a.alpha));
        
        minScale = 2;//applies to rasterAffine, so already 1px convenient
        //maxScale = 1000000;//To ensure its not to big for openGL
        maxScale = Float.POSITIVE_INFINITY;
  }
  
  @Override
  public float getOpacity(int idx, double zFactor){
    if(idx > 0){
      return (float)vr.getValue(zFactor);
    }
    return 1;
  }
  
  public static double fTemp(double x, double temp){
    //returns absolute temperature
    return  ((1/(x + 1/SLOPE))*(1/SLOPE))*(temp/5);
  }
  
  public static double fSize(double temp){
    //returns absolute recommended SolSys size
    return Math.pow(10, Math.floor(Math.log10(getRawSize(temp))+1));
  }
  
  public static double fSizeRound(double temp){
    //same as fSize, but rounded inseat of strict upgoing;
    return Math.pow(10, Math.round(Math.log10(getRawSize(temp))));
  }
  
  public static double getRawSize(double temp){
    return (temp-500)/(500*SLOPE);
  }
  
  public static double fFluid (double x, double temp){
    //returns chance of water in 0-1 interval
    return (1500-fTemp(x,temp))/1500;
  }

}

