package view;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import gui.GL_Blender;
import gui.ImageManager;
import gui.Def;
import gui.UIButton;
import gui.UISelectionList;
import gui.UITheme;
import gui.UIinfoPanel;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import construction.Line;
import construction.ProxyManager;
import construction.TileManager;
import construction.Vec2D;
import drawable.PCSelection;
import drawable.Pathable;
import drawable.Rasterable;

public class GalaxyView extends AbstractView{

  protected long seed;
  protected boolean loadFlag;
  
  protected UISelectionList sl;
  protected UIinfoPanel ip;
  protected Image offScreen;
  
  public GalaxyView(GameContainer gc, long s, long radius, double minZF, double maxZF) {
    super(gc);
    zStep = 1.2f;
    MAX = radius;
    this.minZF = minZF;
    this.maxZF = maxZF;
    seed = s;
    speed=10;
    debugFlag=true;
    selectionFlag = true;
    zFactor = minZF;
                                                  //density
    TileManager tm = new TileManager(seed,1_000_000_000,new Vec2D(gc.getWidth(),gc.getHeight()), 15);
    pm = new ProxyManager(tm);
    
    UIButton b = new UIButton("Exit to Menu",
          "Exits to the main menu\nof the game",
          20, 20,
          UITheme.font.getWidth("Exit to Menu")+10, 20,
          Def.A_ENTER_MAIN_MENU, null);
    children.add(b);
    
    sl = new UISelectionList("Current Selection", 10, 100, gc.getHeight()-200, 2);
    children.add(sl);
    
    ip = new UIinfoPanel("Information", 10, 100);
    children.add(ip);

    offScreen = ImageManager.getNewImage(gc.getWidth(), gc.getHeight());
    tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
    ready=true;
  }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    //### Preparations ###//
    Graphics gx = gc.getGraphics();
    //gx.setAntiAlias(false);
    gx.copyArea(offScreen, 0, 0);
    LinkedList<PCSelection> pcsLst = updateLists();
    //pathItems.addAll(updateRasterables());

    //### Offscreen Operations###//
    renderOffScreen(gx, offScreen);
    gx.clear();
    
    //### Mainscreen Operations###//
    //renderPaths(gx, prepLines);
    applyGrid(gx);
    renderRasterItems(gx);
    for(PCSelection pcs : pcsLst){
      pcs.render(gc, focus, zFactor, new Vec2D(gc.getWidth(), gc.getHeight()));
    }
    super.render(gc);
  }
  
  private LinkedList<PCSelection> updateLists(){
    
    rasterItems = pm.getRasterItems();
    //if(!forcedSelection){
      selectedItems = pm.getMarkedItems();
    //}
    
    LinkedList<PCSelection> pcs = new LinkedList<>();
    for(Rasterable r: selectedItems){
      pcs.add(new PCSelection(r));
    }
    
    sl.updateSource(selectedItems);
    ip.updateSource(selectedItems);
    
    return pcs;
  }
  
  @Override
  protected void renderOffScreen(Graphics gx, Image img){
    LinkedList<Line> lines = null;
    pathItems = pm.getPathItems();
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    for(Pathable cgt: pathItems){
      lines = cgt.getTransformedLines(focus, new Vec2D(gc.getWidth(), gc.getHeight()),zFactor);
      for(Line l: lines){
        gx.setColor(l.getColor());
        gx.drawLine((float)l.getPoints()[0].x, (float)l.getPoints()[0].y, (float)l.getPoints()[1].x, (float)l.getPoints()[1].y);
      }
    }
    gx.copyArea(img, 0, 0);
  }
  
  protected void applyGrid(Graphics gx){
    GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
    gx.drawImage(offScreen, 0, 0);
  }
  
  @Override
  protected void renderDebug(Graphics gx){
    //### Debug info if flag is set ###//
    Vec2D anchor = new Vec2D(gc.getWidth()/2,gc.getHeight()-30);
    gx.setColor(UITheme.main);
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    if(debugFlag){
      gx.drawString("Zoom: "+zFactor, (int)anchor.x, (int)anchor.y);
      gx.drawString("Focus: "+focus.toString(), (int)anchor.x, (int)anchor.y-30);
      gx.drawString("rasterItems.size(): "+rasterItems.size()+"", (int)anchor.x, (int)anchor.y-60);
      gx.drawString("FPS: "+gc.getFPS(),(int)anchor.x, (int)anchor.y-90);
      if(loadFlag){
        gx.fillRect(gc.getWidth()/2+12, gc.getHeight()/2-5, 3, 7);
      }
    }
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = super.handleInput(timeStamp, event, data);
    
    switch(response.getKey()){
    case Def.A_CNTR_FCS:
      Rasterable rast = (Rasterable)response.getValue();
      Vec2D old = focus.getCopy();
      focus = rast.position.getCopy();
      tzm.putOrder(old, focus, Math.log(zFactor)/Math.log(zStep), Math.log(zFactor)/Math.log(zStep));
      response = new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
      break;
    case Def.A_SINGLE:
      pm.proxyForceSelection((Rasterable)response.getValue());
      response = new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
      break;
    }
    return response;
  }
  
  @Override
  public Entry<Integer, Object> proc_K_PRS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){
    response = super.proc_K_PRS(timeStamp, event, data, response);
    
    switch(data[0]){
    case Input.KEY_BACK:
      focus = new Vec2D(0,0);
      break;
    case Input.KEY_C:
      zFactor/=zStep;
      break;
    case Input.KEY_V:
      zFactor*=zStep;
      break;
    case Input.KEY_W:
    case Input.KEY_S:
    case Input.KEY_A:
    case Input.KEY_D:    
      activeKeys.add(data[0]);
      break;
    case Input.KEY_F12:
      if(debugFlag)debugFlag=false;else debugFlag=true;
      break;
    }
    return response;
  }
  
  @Override
  public Entry<Integer, Object> proc_MW_MVD(long timeStamp, int event, int[] data, Entry<Integer, Object> response){
    if(sl.getBounds().contains(cursor.x, cursor.y)){
      response = sl.handleInput(timeStamp, event, data);
    }else{
      response = super.proc_MW_MVD(timeStamp, event, data, response);
    }
    return response;
  }
  
  @Override
  public long getSeed(){
    return seed;
  }
}
