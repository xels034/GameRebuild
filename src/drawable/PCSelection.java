package drawable;

import gui.GL_Blender;
import gui.UITheme;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

import construction.Line;
import construction.ValueRamp;
import construction.Vec2D;


public class PCSelection implements Pathable{

  private Rasterable source;
  
  public PCSelection(Rasterable src){
    source=src;
  }
  
  @Override
  @Deprecated
  public LinkedList<Line> getTransformedLines(Vec2D f, Vec2D screen, double zFactor) {
    
    Rectangle2D.Double r = source.getScreenBounds(f, zFactor, screen);
    LinkedList<Line> returner = new LinkedList<>();
    
    
    returner.add(new Line(new Vec2D(r.x-5,r.y+r.height+5), new Vec2D(r.x-5, r.y+r.height), UITheme.main));
    returner.add(new Line(new Vec2D(r.x-5,r.y+r.height+5), new Vec2D(r.x, r.y+r.height+5), UITheme.main));
    
    returner.add(new Line(new Vec2D(r.x+r.width,r.y-5), new Vec2D(r.x+r.width+5, r.y-5), UITheme.main));
    returner.add(new Line(new Vec2D(r.x+r.width+5,r.y-5), new Vec2D(r.x+r.width+5, r.y), UITheme.main));
    
    returner.add(new Line(new Vec2D(r.x-5, r.y-5), new Vec2D(r.x-5, r.y+r.height),UITheme.main));
    
    return returner;
  }
  
  public void render(GameContainer gc, Vec2D f, double zFactor, Vec2D screen){
    Graphics gx= gc.getGraphics();
    gx.setAntiAlias(false);

    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    gx.setColor(UITheme.main);
    Rectangle2D.Double r = source.getScreenBounds(f, zFactor, screen);
    
    gx.setFont(gc.getDefaultFont());
    gx.drawString(toString(), (int)(r.x)+5, (int)(r.y+r.width+5));
    
    gx.setLineWidth(1f);
    //full color lower left
    gx.drawLine((int)(r.x-5),(int)(r.y+r.height+7), (int)(r.x-5), (int)(r.y+r.height));
    gx.drawLine((int)(r.x-7),(int)(r.y+r.height+5), (int)(r.x), (int)(r.y+r.height+5));
    //full color upper right
    gx.drawLine((int)(r.x+r.width),(int)(r.y-5), (int)(r.x+r.width+7), (int)(r.y-5));
    gx.drawLine((int)(r.x+r.width+5),(int)(r.y-7), (int)(r.x+r.width+5), (int)(r.y));
    //low color upper left
    gx.setColor(UITheme.default_t);
    gx.drawLine((int)(r.x-5), (int)(r.y-7), (int)(r.x-5), (int)(r.y+r.height));
    gx.drawLine((int)(r.x-7), (int)(r.y-5), (int)(r.x+r.width), (int)(r.y-5));
    //low color lower right
    gx.drawLine((int)(r.x),(int)(r.y+r.height+5), (int)(r.x+r.width+7), (int)(r.y+r.height+5));
    gx.drawLine((int)(r.x+r.width+5), (int)(r.y+r.height+7), (int)(r.x+r.width+5), (int)(r.y));
    
    gx.setLineWidth(1f);
    
    //UITheme.addReverb(r.getBounds(), gx, new int[]{0,0,1});
  }

  @Override
  public Line getBounds() {
    return (Line)source.bounds.clone();
  }
  
  @Override
  public String toString(){
    return source.toString();
  }

  @Override
  public void setValueRamp(ValueRamp vr) {
    // TODO Auto-generated method stub
  }

}
