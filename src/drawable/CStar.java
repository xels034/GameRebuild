package drawable;

import gui.GL_Blender;
import gui.ImageManager;

import java.util.Random;

import org.newdawn.slick.Image;

import construction.TiledID;
import construction.ValueRamp;
import construction.Vec2D;

public class CStar extends CBody{
  

  
  public CStar(long s, Vec2D pos, TiledID pid, float sca) {
    super(s, pos, pid);

    ValueRamp oChance = new ValueRamp();
    ValueRamp bChance = new ValueRamp();
    
    oChance.changeEnds(.001, .4);
    oChance.addHandle(.8,.1);
    
    bChance.changeEnds(.001, .9);
    bChance.addHandle(.6, .05);
    
    Random r = new Random();
        r.setSeed(seed);
        properties.put("Kind", "Star");
        
        double d = r.nextDouble();
        d*=15;
        d++;
        properties.put("Age", (long)d+"");//in billion years, 10^9
        
        double sr = r.nextDouble();
        
        //d*=ImageManager.SIZE_STAR;
        
        String t="";
        d=r.nextDouble();
        int gen = TiledID.getGeneration(tid.toString());
        
        //what value in the ramp should be considered as the threshold
        double vIdx = (1-(gen/10f));
        if(d<oChance.getValue(vIdx)){
          t="Class O";
          sr*=10;
          sr+=6.6;
          //used for indexing the texture
          d=4;
        }else if(d<bChance.getValue(vIdx)){
          t="Class B";
          sr*=4.8;
          sr+=1.8;
          d=3;
          //when none of the two were chosen, pick an equal random of the
          //remaining three
        }else if(d< 1/2f){
          t="Class K";
          sr*=0.2;
          sr+=1;
          d=1;
        }else if(d< 3/4f){
          t="Class F";
          sr*=0.25;
          sr+=1.15;
          d=2;
        }else{
          t="Class M";
          sr*=0.2;
          sr+=0.5;
          d=0;
        }
        
        properties.put("Size",String.format("%.3f", sr).replace(',', '.'));//in solar radii
        properties.put("SUnit", "R");
        
        properties.put("Type", t);
        
        Image i = ImageManager.getImage(ImageManager.OFFS_STAR+(int)d);
        Affine a = new Affine();
        //1/i.getWidth would be normalization to get 1px convention
        //*scale to get it up where the game wants it. so its scale/i.getWidth
        float correction = 1f/i.getWidth();  
        a.scale = correction;
        scale=sca*(float)(d/4);//Modify the tile-given scale by startype
        scale+=.005;//so that the smallest stars are still not tiny
        //how much it must be shifted due to scaling, so that its in the center again
        float translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_TRUE_ADD);
        addLayer(i, a);
        
        minScale = 4;//applies to rasterAffine, so already 1px convenient
        maxScale = 10+(int)(d/2);//to give the even more random variation. d is already in range of 0-4
  }
}
