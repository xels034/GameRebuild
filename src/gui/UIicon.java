package gui;

import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

public class UIicon implements UIElement{

  private Image icn;
  private String txt;
  private Rectangle bounds;
  
  public UIicon(Image img, int x, int y, String tt){
    icn=img;
    bounds = new Rectangle(x,y,icn.getWidth()+10,icn.getHeight()+10);
  }
  
  public UIicon(int x, int y, String tt){
    String[]msg = tt.split("\n");
    int maxLen=0;
    for(int i=0;i<msg.length;i++){
      maxLen = Math.max(maxLen, UITheme.font.getWidth(msg[i]));
    }
    int width = maxLen+13;
    int height = msg.length*(UITheme.font.getLineHeight()+3)+10;
    
    bounds = new Rectangle(x, y, width, height);
    
    txt=tt;
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    return new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
  }

  @Override
  public Rectangle getBounds() {
    return (Rectangle)bounds.clone();
  }

  @Override
  public void render(GameContainer gc) {
    Graphics gx = gc.getGraphics();
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    
    gx.setColor(UITheme.faint_t);
    
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    
    if(icn != null){
      gx.drawImage(icn, bounds.x+5, bounds.y+5);
      UITheme.addReverb(bounds, gx, new int[]{0,0,1});
    }else{
      gx.setColor(UITheme.main);
      gx.drawString(txt, bounds.x+5, bounds.y+7);
      gx.drawLine(bounds.x+5, bounds.y+bounds.height, bounds.x+bounds.width-5, bounds.y+bounds.height);
    }
    
    
  }
  
  public void setText(String t){
    String[]msg = t.split("\n");
    int maxLen=0;
    for(int i=0;i<msg.length;i++){
      maxLen = Math.max(maxLen, UITheme.font.getWidth(msg[i]));
    }
    int width = maxLen+13;
    int height = msg.length*(UITheme.font.getLineHeight()+3)+10;
    
    bounds = new Rectangle(bounds.x, bounds.y, width, height);
    
    txt=t;
  }

  @Override
  public String getToolTip() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LinkedList<UIElement> getChildren() {
    // TODO Auto-generated method stub
    return new LinkedList<>();
  }

  @Override
  public Overlay getOverlay() {
    return null;
  }

  @Override
  public void press() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void release() {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isPressed() {
    // TODO Auto-generated method stub
    return false;
  }

}
