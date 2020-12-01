package gui;

import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import drawable.CBody;
import drawable.CMoon;
import drawable.CPlanet;
import drawable.CStar;
import drawable.CSun;
import drawable.Rasterable;

public class UIinfoPanel implements UIElement{

  private static final int CSTAR = 0;
  private static final int CSUN = 1;
  private static final int CPLANET = 2;
  private static final int CMOON = 3;
  
  private boolean pressed;
  private Rectangle bounds;
  //private String headLine;
  private CBody source;
  private LinkedList<UIElement> children;
  private int sourceType;
  
  public UIinfoPanel(String n, int x, int y){
    bounds = new Rectangle(x,y,0,0);
    //headLine = n;
    children = new LinkedList<>();
  }
  
  public void updateSource(LinkedList<Rasterable> ll){
    if(ll.size() == 1){
      updateSource(ll.getFirst(), false);
    }else{
      source = null;
      bounds.width=0;
      bounds.height=0;
      children.clear();
    }
  }
  
  private void updateSource(Rasterable r, boolean updateFlag){
    if(r != source){
      sourceType = -1;
      if(r instanceof CSun){
        sourceType = CSUN;
        bounds.width = 170;
        bounds.height = 145;
      }else if(r instanceof CStar){
        sourceType = CSTAR;
        bounds.width = 170;
        bounds.height = 110;
      }else if(r instanceof CMoon){
        sourceType = CMOON;
        bounds.width = 220;
        bounds.height = 165;
      }else if(r instanceof CPlanet){
        sourceType= CPLANET;
        bounds.width = 220;
        bounds.height = 175;
      }
      
      bounds.width = Math.max(bounds.width, UITheme.font.getWidth(r.toString())+15);
      
      children.clear();
      source = (CBody)r;
      String kind = source.properties.get("Kind");
      //bounds.width=Math.max(170, UITheme.font.getWidth(source.getID().toString()));
      //bounds.height = 120;
      
      UIButton b = new UIButton("Focus view","Scroll to\n"+source.getID().toString(),
            bounds.x+5, bounds.y+bounds.height-30,
            UITheme.font.getWidth("Focus View")+10,20,
            Def.A_CNTR_FCS, source);
      children.add(b);
      
      if(kind.equals("Star")){
        
        b = new UIButton("Enter System","Enter the selected\nSolar System",
                   bounds.x+80, bounds.y+bounds.height-30,
                   UITheme.font.getWidth("Enter System")+10,20,
                   Def.A_ENTER_SOL_FROM_GALAXY, source);
        children.add(b);
      }
    }
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
    if(event == Def.EVENT_M_PRESSED || event == Def.EVENT_M_RELEASED){
      if(event == Def.EVENT_M_PRESSED){
        press();
      }else{
        release();
      }
      for(UIElement e: children){
        if(e.getBounds().contains(data[1],data[2])){
          response = e.handleInput(timeStamp, event, data);
        }
      }
    }
    return response;
  }

  @Override
  public Rectangle getBounds() {
    return (Rectangle)bounds.clone();
  }

  @Override
  public void render(GameContainer gc) {
    if(source != null){
      Graphics gx = gc.getGraphics();
      GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
      gx.setColor(new Color(0,0,0,.7f));
      gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
      gx.setColor(UITheme.main);
      gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
      
      switch(sourceType){
      case CSTAR:
        renderStarInfo(gx);break;
      case CSUN:
        renderSunInfo(gx);break;
      case CPLANET:
        renderPlanetInfoBase(gx);
        renderPlanetInfoAppendix(gx);
        break;
      case CMOON:
        renderPlanetInfoBase(gx);
        renderMoonInfoAppendix(gx);
        break;
      default:
        break;
      }
      
      for(UIElement e: children){
        e.render(gc);
      }
      
      UITheme.addReverb(bounds, gx, new int[]{1,1,1});
    }
  }
  
  private void renderStarInfo(Graphics gx){
    int mid = 50;

    gx.drawString(source.getID().toString(), bounds.x+5, bounds.y+7);
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawGradientLine(bounds.x+5, bounds.y+18, Color.black, bounds.x+mid, bounds.y+18, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+18, UITheme.main, bounds.x+bounds.width-5, bounds.y+18, Color.black);
    
    gx.drawGradientLine(bounds.x+5, bounds.y+68, Color.black, bounds.x+mid, bounds.y+68, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+68, UITheme.main, bounds.x+bounds.width-5, bounds.y+68, Color.black);
    
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    gx.setColor(UITheme.strong_t);
    gx.drawLine(bounds.x+mid, bounds.y+18+1, bounds.x+50, bounds.y+68);
    
    gx.setColor(UITheme.main);
    gx.drawString("Type:", bounds.x+5, bounds.y+25);
    gx.drawString("Age:", bounds.x+5, bounds.y+40);//+15
    gx.drawString("Size:", bounds.x+5, bounds.y+55);
    
    gx.drawString(source.properties.get("Type"), bounds.x+mid+5, bounds.y+25);
    gx.drawString(source.properties.get("Age")+" billion years", bounds.x+mid+5, bounds.y+40);
    gx.drawString(source.properties.get("Size")+source.properties.get("SUnit"), bounds.x+mid+5, bounds.y+55);
  }

  private void renderSunInfo(Graphics gx){
    int mid = 60;
    int height = 103;
    gx.drawString(source.getID().toString(), bounds.x+5, bounds.y+7);
    
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawGradientLine(bounds.x+5, bounds.y+18, Color.black, bounds.x+mid, bounds.y+18, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+18, UITheme.main, bounds.x+bounds.width-5, bounds.y+18, Color.black);
    gx.drawGradientLine(bounds.x+5, bounds.y+height, Color.black, bounds.x+mid, bounds.y+height, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+height, UITheme.main, bounds.x+bounds.width-5, bounds.y+height, Color.black);
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    gx.setColor(UITheme.strong_t);
    gx.drawLine(bounds.x+mid, bounds.y+18+1, bounds.x+60, bounds.y+height);
    
    gx.setColor(UITheme.main);
    gx.drawString("Type:", bounds.x+5, bounds.y+25);
    gx.drawString("Age:", bounds.x+5, bounds.y+40);//+15
    gx.drawString("Size:", bounds.x+5, bounds.y+55);
    gx.drawString("Temp.:", bounds.x+5, bounds.y+70);
    gx.drawString("Planets:", bounds.x+5, bounds.y+85);
    
    gx.drawString(source.properties.get("Type"), bounds.x+mid+5, bounds.y+25);
    gx.drawString(source.properties.get("Age")+" billion years", bounds.x+mid+5, bounds.y+40);
    gx.drawString(source.properties.get("Size")+source.properties.get("SUnit"), bounds.x+mid+5, bounds.y+55);
    gx.drawString(source.properties.get("Temperature")+source.properties.get("TUnit"), bounds.x+mid+5, bounds.y+70);
    gx.drawString(source.properties.get("Planets"), bounds.x+mid+5, bounds.y+85);
  }
  
  private void renderPlanetInfoBase(Graphics gx){
    int mid = 140;
    int height = 128;
    gx.drawString(source.getID().toString(), bounds.x+5, bounds.y+7);
    
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawGradientLine(bounds.x+5, bounds.y+18, Color.black, bounds.x+mid, bounds.y+18, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+18, UITheme.main, bounds.x+bounds.width-5, bounds.y+18, Color.black);
    gx.drawGradientLine(bounds.x+5, bounds.y+height, Color.black, bounds.x+mid, bounds.y+height, UITheme.main);
    gx.drawGradientLine(bounds.x+mid, bounds.y+height, UITheme.main, bounds.x+bounds.width-5, bounds.y+height, Color.black);
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    gx.setColor(UITheme.strong_t);
    gx.drawLine(bounds.x+mid, bounds.y+18+1, bounds.x+mid, bounds.y+height);
    
    gx.setColor(UITheme.main);
    gx.drawString("Tpye:", bounds.x+5, bounds.y+25);
    gx.drawString("Surface Temperature:" , bounds.x+5, bounds.y+40);
    gx.drawString("Has Atmosphere:", bounds.x+5, bounds.y+55);
    gx.drawString("Amount of Water:", bounds.x+5, bounds.y+70);
    gx.drawString("Is Habitable:", bounds.x+5, bounds.y+85);
    
    gx.drawString(source.properties.get("Type"), bounds.x+mid+5, bounds.y+25);
    gx.drawString(source.properties.get("Temperature")+source.properties.get("TUnit"), bounds.x+mid+5, bounds.y+40);
    gx.drawString(source.properties.get("AType"), bounds.x+mid+5, bounds.y+55);
    gx.drawString(source.properties.get("Fluid"), bounds.x+mid+5, bounds.y+70);
    if(source.properties.get("Habitable").equals("true"))gx.setColor(new Color(.5f,1f,0f));
    gx.drawString(source.properties.get("Habitable"), bounds.x+mid+5, bounds.y+85);
    
  }
  
  private void renderPlanetInfoAppendix(Graphics gx){
    gx.setColor(UITheme.main);
    int mid = 140;
    
    gx.drawString("Number of Moons:", bounds.x+5, bounds.y+100);
    gx.drawString("Size:", bounds.x+5, bounds.y+115);
    gx.drawString("Distance from Sun:", bounds.x+5, bounds.y+130);
    
    gx.drawString(source.properties.get("Moons"), bounds.x+mid+5, bounds.y+100);
    gx.drawString(source.properties.get("Size")+source.properties.get("SUnit"), bounds.x+mid+5, bounds.y+115);
    gx.drawString(source.properties.get("SDistance")+source.properties.get("SDUnit"), bounds.x+mid+5, bounds.y+130);
  }
  
  private void renderMoonInfoAppendix(Graphics gx){
    gx.setColor(UITheme.main);
    int mid = 140;
    
    gx.drawString("Size:", bounds.x+5, bounds.y+100);
    gx.drawString("Distance from Sun:", bounds.x+5, bounds.y+115);
    
    gx.drawString(source.properties.get("Size")+source.properties.get("SUnit"), bounds.x+mid+5, bounds.y+100);
    gx.drawString(source.properties.get("SDistance")+source.properties.get("SDUnit"), bounds.x+mid+5, bounds.y+115);
  }
  
  @Override
  public String getToolTip() {
    // TODO Auto-generated method stub
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public LinkedList<UIElement> getChildren() {
    return (LinkedList<UIElement>)children.clone();
  }

  @Override
  public Overlay getOverlay() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void press() {
    pressed=true;
  }

  @Override
  public void release() {
    pressed=false;
  }

  @Override
  public boolean isPressed() {
    return pressed;
  }

}
