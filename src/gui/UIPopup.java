package gui;

import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class UIPopup implements UIElement{
  
  private LinkedList<UIElement> children;
  private Entry<Integer, Object> response;
  private Rectangle bounds;
  private boolean pressed;
  private String[] message;
  
  public UIPopup(int x, int y, String desc, LinkedList<UIButton> choices){
    children = new LinkedList<>();
    children.addAll(choices);
    response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
    pressed = false;
    
    int w = 0;
    for(UIElement e : children){
      w = Math.max(w, e.getBounds().x+e.getBounds().width);
    }
    w+=30;
    w-= x;
    
    bounds = new Rectangle(x,y,w,80);
    
    message = desc.split("\n");
  }
  
  public Entry<Integer, Object> getResponse(){
    return response;
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> resp = new AbstractMap.SimpleEntry<>(Def.A_IGNR_PRS,null);
    switch(event){
    case Def.EVENT_M_PRESSED:
      press();
      if(!bounds.contains(data[1], data[2])){
        response = resp;
        resp =  new AbstractMap.SimpleEntry<>(Def.A_CPOP,this);
      }else{
        for(UIElement e : children){
          if(e.getBounds().contains(data[1], data[2])){
            e.handleInput(timeStamp, event, data);
            
          }  
        }
      }
      break;
    case Def.EVENT_M_RELEASED:
      release();
      for(UIElement e : children){
        if(e.getBounds().contains(data[1], data[2])){
          response = e.handleInput(timeStamp, event, data);
          resp = new AbstractMap.SimpleEntry<>(Def.A_CPOP,this);
        }
        
      }
      break;
    }
    return resp;
  }

  @Override
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public void render(GameContainer gc) {
    Graphics gx = gc.getGraphics();
    gx.setColor(UITheme.dark_s);
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.setColor(UITheme.main);
    gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    int hShift = 20;
    for(int i=0;i<message.length;i++){
      gx.drawString(message[i], bounds.x+20, bounds.y+15+hShift*i);
    }
    
    for(UIElement e : children){
      e.render(gc);
    }
    
    UITheme.addReverb(bounds, gx, new int[]{1,1,1});
  }

  @Override
  public String getToolTip() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedList<UIElement> getChildren() {
    return children;
  }

  @Override
  public Overlay getOverlay() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void press() {
    pressed = true;
  }

  @Override
  public void release() {
    pressed = false;
  }

  @Override
  public boolean isPressed() {
    return pressed;
  }

}
