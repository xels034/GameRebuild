package view;

import gui.GL_Blender;
import gui.ImageManager;
import gui.Def;
import gui.UIButton;
import gui.UITheme;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import construction.TiledID;

public class SandboxView extends AbstractView{

  private Image planet,atmo,shadow,light,water,ring,clouds;
  private float rotP, rotS;  
  
  public SandboxView(GameContainer gc) {
    super(gc);

    planet = ImageManager.getImage("assets/planet/solid/sur_Mineral.png");
    atmo = ImageManager.getImage("assets/planet/atmosphere/atmo_Oxy.png");
    shadow = ImageManager.getImage("assets/planet/modifier/shadow_noA.png");
    light = ImageManager.getImage("assets/planet/modifier/nightLights.png");
    //light = im.getImage("assets/planet/solid/sur_Molt_g.png");
    water = ImageManager.getImage("assets/planet/modifier/water_med.png");
    ring = ImageManager.getImage("assets/planet/ring/ring_Prec.png");
    ring = ImageManager.getImage(ImageManager.OFFS_RING+3);
    
    clouds = ImageManager.getImage("assets/planet/modifier/clouds.png");
    
    rotP = .3f;
    rotS= -.1f;

    ring = ring.getScaledCopy(2f);
    
    UIButton b = new UIButton("Exit to Menu",
          "Exits to the main menu\nof the game",
          20, 20,
          UITheme.font.getWidth("Exit to Menu")+10, 20,
          Def.A_ENTER_MAIN_MENU, null);
    children.add(b);
    
    
    TiledID t1 = new TiledID("1000|0999:123456789a:0050 || 0:5:5");
    TiledID t2 = new TiledID("1000|0999:123456789a:0050 || 0:5:6");
    
    System.out.println(t1.toString()+" cTo() "+t2.toString()+" -> "+t1.compareTo(t2));
  }
  
   @Override
   public void update(int delta){
    super.update(delta);
    ready=true;
   }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    Graphics gx = gc.getGraphics();
    
    planet.rotate(rotP);
    light.rotate(rotP);
    water.rotate(rotP);
    shadow.rotate(rotS);
    ring.rotate(rotS);
    clouds.rotate(rotP*.9f);
    
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    
    int ANC=200;
    
    
    gx.drawImage(planet, ANC,ANC);
    
    gx.drawImage(water, ANC, ANC);
    gx.drawImage(clouds, ANC, ANC);
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawImage(atmo, ANC, ANC);
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_MULTIPLY);
    gx.drawImage(shadow, ANC,ANC);
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawImage(light, ANC,ANC);
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    
    //gx.drawImage(processed, 50, 50);
    //gx.drawImage(light,50,50);
    
    
    int center = (512/2);
    int bigCenter = ((512*2)/2);
    
    int draw = ANC - (bigCenter-center);
    gx.drawImage(ring, draw,draw);
    
    
    gx.setColor(Color.white);
    gx.fillRect(1500, 500, 150, 150);
    //####
    //####
    
    //rotation by matrix multiplication:
    
    // [cos -sin]   [x]    [x*cos - y*sin]
    // [sin  cos] x [y] -> [x*sin + y*cos]
    super.render(gc);
  }

}
