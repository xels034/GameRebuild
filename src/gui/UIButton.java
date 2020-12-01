package gui;



import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;



public class UIButton implements UIElement{

  private String name;
  private Image icon;
  private boolean useText;
  
  private UIPopup pp;
  
  private final int action;
  private String tooltip;
  private Rectangle bounds;
  private Overlay ovrly;
  private boolean pressed;
  private Object data;
  private long lastInput;
  
  private int[] mask;
  
  private UIButton(String t, int x, int y, int w, int h, int action, Object data){
    tooltip=t;
    bounds = new Rectangle(x,y,w,h);
    ovrly = new Overlay(tooltip, x+5, y+h+5, bounds);
    this.action=action;
    pressed=false;
    this.data=data;
    mask = new int[]{1,1,1};
    pp = null;
  }
  
  public UIButton(String n, String t, int x, int y, int w, int h, int action, Object data){
    this(t,x,y,w,h,action,data);
    name=n;
    useText=true;
    pp = null;
  }
  
  public UIButton(Image img, String t, int x, int y, int w, int h, int action, Object data){
    this(t,x,y,w,h,action,data);
    icon=img;
    useText = false;
    pp = null;
  }
  
  @SuppressWarnings("hiding")
    @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    if(timeStamp > lastInput){
      lastInput = timeStamp;
      switch(event){
      case Def.EVENT_M_PRESSED:
        press();
        break;
      case Def.EVENT_M_RELEASED:
        release();
        if(pp != null){
          return new AbstractMap.SimpleEntry<>(Def.A_OPOP, pp);
        }else{
          return new AbstractMap.SimpleEntry<>(action, this.data);
        }
      }
    }
    return new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
  }

  @Override
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public void render(GameContainer gc) {
    Graphics gx = gc.getGraphics();
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    UITheme.readGLFont();

    gx.setColor(new Color(0,0,0,.7f));
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    
    gx.setColor(UITheme.default_t);
    
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.setColor(UITheme.main);
    gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    
    if(useText){
      gx.setColor(UITheme.main);
      gx.setFont(gc.getDefaultFont());
      gx.drawString(name, bounds.x+4, bounds.y+4);
    }else{
      gx.drawImage(icon, bounds.x+2, bounds.y+2);
    }
    
    UITheme.addReverb(bounds, gx, mask);
  }
  
  public void setMask(int[] m){
    mask = m.clone();
  }
  
  @Override
  public void press(){
    if(!pressed){
      bounds.x++;
      bounds.y++;
      pressed=true;
    }
  }
  
  @Override
  public void release(){
    if(pressed){
      bounds.y--;
      bounds.x--;
      pressed=false;
    }
  }
  
  @Override
  public boolean isPressed(){
    return pressed;
  }
  
  public void setToolTip(String n){
    tooltip = n;
  }
  
  @Override
    public String getToolTip(){
    return tooltip;
  }
  
  @Override
    public LinkedList<UIElement> getChildren(){
    return new LinkedList<>();
  }

  @Override
  public Overlay getOverlay() {
    return ovrly;
  }
  
  public void setPopup(UIPopup pp){
    this.pp = pp;
  }

}
