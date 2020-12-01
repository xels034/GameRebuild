package view;

import gui.Def;
import gui.UIButton;
import gui.UITheme;

import org.newdawn.slick.GameContainer;

import construction.ProxyManager;
import construction.StaticsManager;
import construction.TiledID;
import construction.Vec2D;


public class SolView extends GalaxyView{

  public SolView(GameContainer gc, long s, long radius, double minZF, double maxZF, TiledID gID) {
    super(gc, s, radius, minZF, maxZF);
    zStep=1.3f;
    
    UIButton b = new UIButton("Return to Galaxy",
          "Returns to the\nGalaxy Map",
          110, 20,
          UITheme.font.getWidth("Return to Galaxy")+10, 20,
          Def.A_ENTER_GALAXY_FROM_SOL, null);
    children.add(b);

    StaticsManager sm = new StaticsManager(s,gID, new Vec2D(gc.getWidth(), gc.getHeight()), minZF, radius);
    pm = new ProxyManager(sm);
    tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
    

  }
}
