package gui;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.GameContainer;


public interface UIElement {
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data);
  public Rectangle getBounds();
  public void render(GameContainer gc);
  public String getToolTip();
  public LinkedList<UIElement> getChildren();
  public Overlay getOverlay();
  public void press();
  public void release();
  public boolean isPressed();
}
