package gui;


import java.awt.Rectangle;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;


public class Overlay {
  
  //TODO make it fancy with formatting and all
  String[] msg;
  Rectangle bounds;
  Rectangle UIEBounds;
  
  public Overlay(String n, int x, int y, Rectangle r){
    UITheme.readGLFont();
    msg = n.split("\n");
    int maxLen=0;
    for(int i=0;i<msg.length;i++){
      maxLen = Math.max(maxLen, UITheme.font.getWidth(msg[i]));
    }
    UIEBounds = r;
    int width = maxLen+13;
    int height = msg.length*(UITheme.font.getLineHeight()+3)+10;
    
    bounds = new Rectangle(x, y, width, height);
  }
  
  public void render(GameContainer gc){
    UITheme.readGLFont();
    Graphics gx = gc.getGraphics();

    UITheme.addReverb(bounds, gx, new int[]{1,1,1});
    
    gx.setColor(UITheme.toolTip);
    
    bounds.x = Math.min(gc.getWidth(),  bounds.x+bounds.width) - bounds.width;
    bounds.y = Math.min(gc.getHeight(), bounds.y+bounds.height) - bounds.height;
    
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.setColor(UITheme.strong_t);
    gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.setColor(UITheme.main);
    for(int i=0;i<msg.length;i++){
      gx.drawString(msg[i], bounds.x+5, bounds.y+5+(UITheme.font.getLineHeight()+3)*i);
    }

    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.setColor(UITheme.dark_s);
    gx.fillRect(UIEBounds.x, UIEBounds.y, UIEBounds.width, UIEBounds.height);
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    for(int i=0;i<msg.length;i++){
      sb.append(msg[i]);
    }
    return sb.toString();
  }
  
}
