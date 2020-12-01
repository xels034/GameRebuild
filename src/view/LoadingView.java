package view;

import gui.UITheme;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;

public class LoadingView extends AbstractView{

  int asd;
  
  public LoadingView(GameContainer gc) {
    super(gc);
    asd=0;
  }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc) {
    Graphics gx = gc.getGraphics();

    asd++;
    gx.clear();
    gx.setColor(UITheme.main);
    gx.drawString("LOADING..."+asd, 500, 500);
    asd%=5000;
  }
}
