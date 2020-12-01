package drawable;

import gui.GL_Blender;
import gui.ImageManager;

import java.util.Random;

import org.newdawn.slick.Image;

import construction.TiledID;
import construction.Vec2D;

public class CSRing extends CBody{

  private Compound comp;
  
  public CSRing(long s, Vec2D p, TiledID pid, CSun cs, double dist) {
    super(s, p, pid);
    //we don't need no automatic addition if tiledID
    tid=pid;
    centerMass = cs;
    
    comp = new Compound();  
    Random r = new Random();
    r.setSeed(cs.seed);
    
    properties.put("Kind", "Asteroid Belt");
    properties.put("Type", "default");
    
    //properties.put("SDistance",centerMass.properties.get("SDistance"));
    //properties.put("SDUnit", "km");
    
    //double diameter = r.nextDouble()+2;
    //diameter*=Double.parseDouble(centerMass.properties.get("Size"));
    
    properties.put("Size", dist*2+"");
    properties.put("SUnit", "km");
    
    //String c = parent.properties.get("Main Component");
    
    //comp = new Compound(parent.getCrustComp());
    
    String c = Compound.fillCompound(comp, CPlanet.solidMatTypes, r);
    
    int bic = 0;
    
    if(c.equals("Rock")){
      bic=1;
    }else if(c.equals("Metal")){
      bic=2;
    }else if(c.equals("Mineral")){
      bic=3;
    }else if(c.equals("Precious Metal")){
      bic=4;
    }
    
    Image i = ImageManager.getImage(ImageManager.OFFS_RING+bic);
        Affine a = new Affine();
        float correction = 1f/i.getWidth();  
        a.scale = correction;
        scale = (float)dist*2;
        //how much it must be shifted due to scaling, so that its in the center again
        float translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_NORMAL);
        addLayer(i, a);
        
        minScale = 4;//applies to rasterAffine, so already 1px convenient
        maxScale = 1000000;//To ensure its not to big for openGL
  }

}
