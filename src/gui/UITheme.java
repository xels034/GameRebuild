package gui;



import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.newdawn.slick.AngelCodeFont;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import construction.Vec2D;

public class UITheme {

  public static Color main;
  public static Color dark_s;
  public static Color dimmed_s;
  public static Color strong_t;
  public static Color default_t;
  public static Color faint_t;
  public static Color toolTip;
  
  public static Vec2D dimension;// = new Vec2D(1920-100,1080-100);
  
  public static int REVERB_COUNT = 2;
  public static float REVERB_DIST = 3;
  public static float REVERB_COL_BASE = .6f;
  public static float REVERB_FALLOF = 3;
  public static float REVERB_SHRINK = -.5f;
  
  private static float r;//=1f;
  private static float g;//=(150f/255);
  private static float b;//=0;
  
  
  
  public static AngelCodeFont font;
  
  public static boolean loaded = false;
  
  public static void readNonGL(){
    try(BufferedReader br = new BufferedReader(new FileReader("config/ui.ini"));){
      
      String l = br.readLine();
      while(l.charAt(0) == ';') l = br.readLine();
      int x = Integer.parseInt(l.split("=")[1]);
      int y = Integer.parseInt(br.readLine().split("=")[1]);
      l = br.readLine();
      while(l.charAt(0) == ';') l = br.readLine();
      r = Float.parseFloat(l.split("=")[1]);
      g = Float.parseFloat(br.readLine().split("=")[1]);
      b = Float.parseFloat(br.readLine().split("=")[1]);
      br.close();
      dimension = new Vec2D(x,y);
      
      main        = new Color(r,g,b);
      dark_s      = new Color(r*.075f,g*.075f,b*.075f,1f);
      dimmed_s    = new Color(r*.3f,g*.3f,b*.3f);
      strong_t    = new Color(r,g,b,.55f);
      default_t   = new Color(r,g,b,.1f);
      faint_t     = new Color(r,g,b,.015f);
      toolTip     = new Color(r*.1f,g*.1f,b*.1f,.85f);
    } catch (IOException x){
      x.printStackTrace();
    }
  }
  
  public static boolean readGLFont(){
    if(!loaded){
      try {
        loaded = true;
        font = new AngelCodeFont("assets/fonts/OCRA_E.fnt", new Image("assets/fonts/OCRA_E_0.png")); //10px
        //font = new AngelCodeFont("assets/fonts/ORA_STD.fnt", new Image("assets/fonts/ORA_STD_0.png")); //15px
      } catch (SlickException e) {
        e.printStackTrace();
        loaded = false;
      }
      
    }
    return loaded;
  }
  
  public static boolean reload(){
    loaded=false;
    return readGLFont();
  }
  
  @SuppressWarnings("hiding")
    public static void addReverb(Rectangle r, Graphics gx, int[] mask){
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.setColor(UITheme.default_t);
    for(int i=0;i<r.height && mask[1]==1;i+=5){
      gx.drawLine(r.x, r.y+i+1, r.x+r.width, r.y+i+1);
    }
    
    //reverb effect
    for(int i=0;i<UITheme.REVERB_COUNT && mask[0]==1 ;i++){
      // 1 - (base / (i+Fallof))
      gx.setColor(UITheme.main.darker(1-(UITheme.REVERB_COL_BASE/(i+UITheme.REVERB_FALLOF))));
      float xMod = UITheme.REVERB_DIST*(i+1);
      float yMod = i*UITheme.REVERB_SHRINK;
      
      //LR
      gx.drawLine(r.x-xMod, r.y+yMod, r.x-xMod, r.y+r.height-yMod);
      gx.drawLine(r.x+r.width+xMod, r.y+yMod, r.x+r.width+xMod, r.y+r.height-yMod);
      
      //UD
      gx.drawLine(r.x+yMod, r.y-xMod, r.x+r.width-yMod, r.y-xMod);
      gx.drawLine(r.x+yMod, r.y+r.height+xMod, r.x+r.width-yMod, r.y+r.height+xMod);  
    }
    
    if(mask[2]==1){
      GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_SCREEN);
      Color end = new Color(0,0,0,0);
      Color start = UITheme.default_t.scaleCopy(1.5f);
      //onkly found a screen method for premultiplied
      start.r*=start.a;
      start.g*=start.a;
      start.b*=start.a;
      
      int dist = (int)(UITheme.REVERB_COUNT*UITheme.REVERB_DIST*4);
      gx.drawGradientLine(r.x, r.y, start, r.x, r.y-dist, end);
      gx.drawGradientLine(r.x, r.y, start, r.x-dist, r.y, end);
      
      gx.drawGradientLine(r.x, r.y+r.height, start, r.x-dist, r.y+r.height, end);
      gx.drawGradientLine(r.x, r.y+r.height, start, r.x, r.y+r.height+dist, end);
      
      gx.drawGradientLine(r.x+r.width, r.y+r.height, start, r.x+r.width, r.y+r.height+dist, end);
      gx.drawGradientLine(r.x+r.width, r.y+r.height, start, r.x+r.width+dist, r.y+r.height, end);
      
      gx.drawGradientLine(r.x+r.width, r.y, start, r.x+r.width+dist, r.y, end);
      gx.drawGradientLine(r.x+r.width, r.y, start, r.x+r.width, r.y-dist, end);
    }
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
  }
}
