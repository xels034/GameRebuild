package view;

import gui.GL_Blender;
import gui.ImageManager;
import gui.Def;
import gui.Overlay;
import gui.UIElement;
import gui.UIPopup;
import gui.UITheme;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import construction.Line;
import construction.ProxyManager;
import construction.Vec2D;

import drawable.Pathable;
import drawable.Rasterable;
import drawable.Rayable;

public class AbstractView implements View{
  //finals
  protected double speed;
  protected double zStep;
  protected double maxZF;
  protected double minZF;
  protected long MAX;
  //
  
  protected Vec2D focus;
  protected Vec2D cursor;
  protected Vec2D sStart;
  protected double zFactor;
  protected boolean debugFlag;
  protected boolean selectionFlag;
  protected int mDown;
  protected boolean ready;
  
  protected UIElement inClicked;
  protected UIPopup popup;
  protected Overlay activeO;
  protected LinkedList<Integer> activeKeys;
  protected LinkedList<UIElement> children;
  protected LinkedList<UIElement> focusedUI;
  protected LinkedList<Rasterable> rasterItems;
    protected LinkedList<Rasterable> selectedItems;
    protected LinkedList<Pathable> pathItems;

  protected GameContainer gc;
  protected ProxyManager pm;
  protected TZoomManager tzm;
  

  public AbstractView(GameContainer gc){
    focus = new Vec2D(0,0);
    cursor = new Vec2D(0,0);
    zFactor = 1;
    speed=0;
    zStep=2;//if 1, log doesn't work anymore
    maxZF=1;
    minZF=1;
    //forcedSelection = false;
    MAX = 1;
    debugFlag = false;
    selectionFlag = false;
    mDown=-1;
    
    activeKeys = new LinkedList<>();
    children = new LinkedList<>();
    focusedUI = new LinkedList<>();
    rasterItems = new LinkedList<>();
    selectedItems = new LinkedList<>();
    pathItems = new LinkedList<>();

    this.gc=gc;
    
    pm = new ProxyManager();
    tzm = new TZoomManager();
    tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
    ready = false;
  }
  
  @SuppressWarnings("hiding")
    @Override
  public void calibrate(double zFactor, Vec2D focus) {
    this.zFactor=zFactor;
    this.focus=focus.getCopy();
    
    pm.proxyTranslate(zFactor, focus);
    tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
  }
  
  @Override
  public void update(int delta) {
    float dConst = .1f*delta;
    
    focus = tzm.getVector();
    zFactor = Math.pow(zStep, tzm.getScalar());
    //zFactor = tzm.getScalar();
    
    //All downed keys
    for(int i: activeKeys){
      switch(i){
      case Input.KEY_W:
        focus.y-=(speed/zFactor)*dConst;
        break;
      case Input.KEY_S:
        focus.y+=(speed/zFactor)*dConst;
        break;
      case Input.KEY_A:
        focus.x-=(speed/zFactor)*dConst;
        break;
      case Input.KEY_D:
        focus.x+=(speed/zFactor)*dConst;
        break;
      }
    }
    if(!activeKeys.isEmpty()){
      tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
    }
    checkBounds();
    pm.proxyTranslate(zFactor, focus);
  }
  
  @SuppressWarnings("hiding")
    @Override
  public void render(GameContainer gc) {
    Graphics gx = gc.getGraphics();
    renderUI(gx);
  }
  
  protected void renderUI(Graphics gx){
    if(selectionFlag){
      renderSelectionRect(gx);
    }
    //### UITree ###//
    for(UIElement e:children){
      e.render(gc);
    }
    if(activeO != null){
      activeO.render(gc);
    }
    if(debugFlag){
      renderDebug(gx);
    }
  }
  
  protected void renderRasterItems(Graphics gx){
    long renderTimeStamp = System.currentTimeMillis();
    for(Rasterable r:rasterItems){
      for(int i=0;i<r.getLayerCount();i++){

        Rectangle2D.Double rec = r.getScreenBounds(focus, zFactor, new Vec2D(gc.getWidth(),gc.getHeight()),i);
        
        //if the image is deemed too big for float-precision,
        //a single px sample is derived and drawn on the screen
        if(!(rec.x > -gc.getWidth()*200 &&
            rec.x < gc.getWidth()*200 &&
            rec.y > -gc.getHeight()*200 &&
            rec.y < gc.getHeight()*200)){
          if(rec.contains(new Rectangle2D.Double(0,0,gc.getWidth(), gc.getHeight()))){
            Color c = ImageManager.getSample(r.getLayerImage(i), rec, r.getAffine(i).rotation);
            float alpha = Math.min(renderTimeStamp-r.birth,500)/500f;
            float imgA = r.getOpacity(i, zFactor);
            c.a = alpha*imgA;
            gx.setColor(c);
            GL_Blender.setDrawMode(r.getAffine(i).mode);
            gx.fillRect(0, 0, gc.getWidth(), gc.getHeight());
          }
          continue;
        }

        Image img = r.getLayerImage(i);
        img = img.getScaledCopy((int)rec.width, (int)rec.height);
        
        if(r instanceof Rayable){
          Rayable ray = (Rayable)r;
          Vec2D c = ray.getBaseDim();
          img.setCenterOfRotation((int)((c.x/2)*zFactor), (int)((c.y/2)*zFactor));
        }
        
        double rot = r.getAffine(i).rotation*(180/Math.PI);
        rot -= r.rasterAffine.rotation*(180/Math.PI);
        //rot+=90;

        img.setRotation((float)rot);
        img.setAlpha(r.getOpacity(i, zFactor));
        GL_Blender.setDrawMode(r.getAffine(i).mode);
        gx.drawImage(img, (float)rec.x, (float)rec.y);
      }
    }
  }
  
  protected void renderPaths(Graphics gx, LinkedList<Line> allPaths){
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    for(Line l:allPaths){
      gx.setColor(UITheme.main);
      gx.drawLine((int)l.getPoints()[0].x,
            (int)l.getPoints()[0].y,
            (int)l.getPoints()[1].x,
            (int)l.getPoints()[1].y);
    }
  }
  
  
  protected void renderOffScreen(Graphics gx, Image img){
    
    gx.copyArea(img, 0, 0);
  }
  
  protected void renderSelectionRect(Graphics gx){
    if(sStart != null){
      Rectangle r = new Rectangle((int)Math.min(sStart.x, cursor.x),
          (int)Math.min(sStart.y, cursor.y),
          (int)Math.abs(sStart.x-cursor.x),
          (int)Math.abs(sStart.y-cursor.y));
      
      UITheme.addReverb(r, gx, new int[]{0,0,1});
      gx.setColor(UITheme.faint_t);
      gx.fillRect(r.x, r.y, r.width, r.height);
      gx.setColor(UITheme.dimmed_s);
      GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
      gx.drawRect(r.x, r.y, r.width, r.height);
    }
  }
  
  protected void renderDebug(Graphics gx){

  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
    
    //a rls could be overridden so deal with it
    LinkedList<UIElement> releaser = new LinkedList<>();
    boolean ignoreInput = false;
    boolean ignorePress = false;
    for(UIElement e: focusedUI){
      response = e.handleInput(timeStamp, event, data);
      if(response.getKey() == Def.A_RLS_FCS) releaser.add((UIElement)response.getValue());
      else if(response.getKey() == Def.A_IGNR_INPT) ignoreInput = true;
      else if(response.getKey() == Def.A_IGNR_PRS) ignorePress = true;
    }
    
    focusedUI.removeAll(releaser);
    boolean responseAltered;
    do{
      responseAltered = false;
      if(!ignoreInput){
        switch(event){
        case Def.EVENT_K_PRESSED:
          if(!ignorePress) response = proc_K_PRS(timeStamp, event, data, response);
          break;
        case Def.EVENT_K_RELEASED:
          if(!ignorePress) response = proc_K_RLS(timeStamp, event, data, response);
          break;
        case Def.EVENT_M_PRESSED:
          if(!ignorePress) response = proc_M_PRS(timeStamp, event, data, response);
          break;
        case Def.EVENT_M_RELEASED:
          if(!ignorePress) response = proc_M_RLS(timeStamp, event, data, response);
          break;
        case Def.EVENT_M_MOVED:
          response = proc_M_MVD(timeStamp, event, data, response);
          break;
        case Def.EVENT_M_DRAGGED:
          response = proc_M_DRG(timeStamp, event, data, response);
          break;
        case Def.EVENT_MW_MOVED:
          response = proc_MW_MVD(timeStamp, event, data, response);
          break;
        }
        
        switch(response.getKey()){
        case Def.A_RQST_FCS:
          focusedUI.add((UIElement)response.getValue());
          break;
        case Def.A_OPOP:
          popup = (UIPopup)response.getValue();
          children.add(popup);
          focusedUI.add(popup);
          break;
        case Def.A_CPOP:
          children.remove(popup);
          focusedUI.remove(popup);
          response = popup.getResponse();
          responseAltered=true;
          popup=null;
          break;
        case Def.A_ENTER_MAIN_MENU:
        case Def.A_ENTER_SANDBOX:
        case Def.A_ENTER_SOL_FROM_GALAXY:
        case Def.A_ENTER_UNIVERSE_FROM_GALAXY:
        case Def.A_ENTER_GALAXY_FROM_MENU:
        case Def.A_ENTER_GALAXY_FROM_SOL:
        case Def.A_ENTER_GALAXY_FROM_UNIVERSE:
        case Def.A_EXIT:
          pm.proxyShutDown();
          break;
        }
      }
    } while (responseAltered);
    return response;
  }
  
  protected void checkBounds(){
    zFactor=Math.max(zFactor, minZF);
    zFactor=Math.min(zFactor, maxZF);
    
    focus.x=Math.max(focus.x, -MAX);
    focus.y=Math.max(focus.y, -MAX);
    
    focus.x=Math.min(focus.x, MAX);
    focus.y=Math.min(focus.y, MAX);
  }
  
  protected void buildSelectionRect(){
      Rectangle selRect = new Rectangle((int)Math.min(sStart.x, cursor.x),
                      (int)Math.min(sStart.y, cursor.y),
                      (int)Math.abs(sStart.x-cursor.x),
                      (int)Math.abs(sStart.y-cursor.y));
    //if its small enough, it sould be a click
    //however, a click is represented by a rect (you dont have to klick
    //EXACTLY in an item. but in that case its shifted so that the mouse-
    //position is in the center
    if(selRect.getWidth()*selRect.getHeight()<100){
      selRect.width=18;
      selRect.height=18;
      selRect.x=(int)cursor.x-9;
      selRect.y=(int)cursor.y-9;
    }

    pm.proxySelection(selRect, activeKeys.contains(Input.KEY_LSHIFT));
  }
  
  protected void updateOverlay(){
    UIElement e = getOverlay(this, (int)cursor.x, (int)cursor.y);
    if(e != this){
      activeO = e.getOverlay();
    }else{
      activeO= null;
    }
  }
  
  protected UIElement getOverlay(UIElement p, int x, int y){
    for(UIElement e:p.getChildren()){
      if(e.getBounds().contains(x, y)){
        return getOverlay(e, x, y);
      }
    }
    return p;
  }
  
  //##### #####
  //##### #####
  
  protected Entry<Integer, Object>proc_K_PRS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){
    if(data[0] == Input.KEY_LSHIFT) activeKeys.add(data[0]);
    else if(data[0] == Input.KEY_LALT) activeKeys.add(data[0]);
    else if(data[0] == Input.KEY_LCONTROL) activeKeys.add(data[0]);
    return response;
  }
  
  protected Entry<Integer, Object>proc_K_RLS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){
    int idx = activeKeys.indexOf(data[0]);
    if(idx>=0) activeKeys.remove(activeKeys.indexOf(data[0]));
    return response;
  }
  
  protected Entry<Integer, Object>proc_M_PRS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){

    switch(data[0]){
    case Input.MOUSE_LEFT_BUTTON:
      mDown = 0;
      for(UIElement e:children){
        if(e.getBounds().contains(data[1],data[2])){
          inClicked = e;
          response = e.handleInput(timeStamp, event, data);
          break;
        }
      }
      if(inClicked == null && sStart == null && selectionFlag){
        sStart = new Vec2D(data[1],data[2]);
      }
      break;
    case Input.MOUSE_RIGHT_BUTTON:
      mDown = 1;
      //None yet
      break;
    case Input.MOUSE_MIDDLE_BUTTON:
      mDown = 2;
      //none yet
      break;
    }

    return response;
  }
  
  protected Entry<Integer, Object>proc_M_RLS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){

    mDown = -1;
    switch(data[0]){
    case Input.MOUSE_LEFT_BUTTON:
      if(sStart == null){
        for(UIElement e:children){
          if(e.getBounds().contains(data[1],data[2]) && e.isPressed()){
              response = e.handleInput(timeStamp, event, data);
              e.release();
              break;
          }
        }
        if(inClicked != null){
          inClicked.release();
          inClicked = null;
        }
      }else if(selectionFlag){
        buildSelectionRect();
        sStart=null;
      }
      break;
    case Input.MOUSE_RIGHT_BUTTON:
      break;
    case Input.MOUSE_MIDDLE_BUTTON:
      break;
    }

    return response;
  }
  
  protected Entry<Integer, Object>proc_M_MVD(long timeStamp, int event, int[] data, Entry<Integer, Object> response){

    cursor.x=data[2];
    cursor.y=data[3];
    updateOverlay();

    return response;
  }
  
  protected Entry<Integer, Object>proc_M_DRG(long timeStamp, int event, int[] data, Entry<Integer, Object> response){

    cursor.x=data[2];
    cursor.y=data[3];

    if(mDown == 1){
      focus.x += (data[0]-data[2])/zFactor;
      focus.y += (data[1]-data[3])/zFactor;
      checkBounds();
      tzm.update(focus, Math.log(zFactor)/Math.log(zStep));
    }
    updateOverlay();
    
    return response;
  }
  
  protected Entry<Integer, Object>proc_MW_MVD(long timeStamp, int event, int[] data, Entry<Integer, Object> response){
        //zooming 1.1 per rotation, so three rots = 1.1^3
        //-rotation to invert zoom direction
    
    //Son of a bitch targeted zoom
    
    Vec2D initF = focus.getCopy();
    double initZ = zFactor;
    
    double targetWX=0;
    double targetWY=0;
    
    if(data[0]>0){
      targetWX = (cursor.x-gc.getWidth()/2)/zFactor;
      targetWY = (cursor.y-gc.getHeight()/2)/zFactor;
      targetWX+=focus.x;
      targetWY+=focus.y;
    }
    //needed to update the tzm correctly
    zFactor = Math.pow(zStep, tzm.getTargetScalar());
        zFactor*=Math.pow(zStep, (data[0]/120)); // 1/120 because ... 1 change seems to be 120 ... what ... degrees? wtf
        if(data[0]>0 && zFactor < maxZF){
          int targetMX = (int)((targetWX-focus.x)*zFactor)+gc.getWidth()/2;
          int targetMY = (int)((targetWY-focus.y)*zFactor)+gc.getHeight()/2;
          
          int deltaX = (int)cursor.x-targetMX;
          int deltaY = (int)cursor.y-targetMY;
          
          focus.x-=deltaX/zFactor;
          focus.y-=deltaY/zFactor;
        }
        checkBounds();
        pm.proxyTranslate(zFactor, focus);
        tzm.putOrder(initF, focus, Math.log(initZ)/Math.log(zStep), Math.log(zFactor)/Math.log(zStep));
        //tzm.update(focus, zFactor);
    return response;
  }
  
  //##### #####
  //##### #####
  
  @Override
  public boolean isReady(){
    return ready;
  }
  
  @Override
  public LinkedList<UIElement> getChildren(){
    return children;
  }  
  @Override
  public Rectangle getBounds() {
    return new Rectangle(0,0,gc.getWidth(),gc.getHeight());
  }

  @Override
  public String getToolTip() {
    return "Generic bullshit. Change in "+AbstractView.class.getName()+"getToolTip";
  }

  @Override
  public Overlay getOverlay() {
    // TODO Auto-generated method stub
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

  @Override
  public URL getMusicURL() {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Vec2D getMinMaxZF(){
    return new Vec2D(minZF,maxZF);
  }
  
  public long getMax(){
    return MAX;
  }

  @Override
  public long getSeed() {
    return 0;
  }
}
