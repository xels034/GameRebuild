package drawable;

import gui.GL_Blender;
import gui.ImageManager;

import java.util.Random;

import org.newdawn.slick.Image;

import construction.TiledID;
import construction.Vec2D;

public class CBlackHole extends CBody{

  public CBlackHole(long s, Vec2D p, TiledID pid) {
    super(s, p, pid);

    Random r = new Random();
        r.setSeed(seed);
        double d = r.nextDouble();
        d*=10;
        properties.put("Kind", "Supermassive Black Hole");
        properties.put("Size",(long)d+"");
        properties.put("SUnit", "AU");
        d = r.nextDouble();
        d*=15;
        d++;
        properties.put("Age", (long)d+"");//in billion years, 10^9
        properties.put("Type", "Supermassive");
        
        int idx = (int)(r.nextDouble()*ImageManager.SIZE_HOLE);
        
        Image i = ImageManager.getImage(ImageManager.OFFS_HOLE+idx);
        Affine a = new Affine();
        //1/i.getWidth would be normalization to get 1px convention
        //*scale to get it up where the game wats it. so its scale/i.getWidth
        float correction = 1f/i.getWidth();  
        a.scale = correction;
        //TODO scale okay?
        scale = .1f;
        //how much it must be shifted due to scaling, so that its in the center again
        float translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_NORMAL);
        addLayer(i, a);
        
        minScale = 2;//applies to rasterAffine, so already 1px convenient
        maxScale = Float.POSITIVE_INFINITY;
  }

}
