package view;

import gui.UIElement;

import java.net.URL;
import construction.Vec2D;


public interface View extends UIElement{
  public void calibrate(double zFactor, Vec2D focus);

  public void update(int delta);
  public URL getMusicURL();
  public boolean isReady();
  public long getSeed();
}
