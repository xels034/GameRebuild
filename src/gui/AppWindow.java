package gui;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.openal.SoundStore;

import construction.Vec2D;

import drawable.CBody;
import drawable.CSun;

import Shader.Shader;

import view.AssetsEditorView;
import view.BattleView;
import view.HeavyViewLoader;
import view.LoadingView;
import view.MainMenuView;
import view.OptionsView;
import view.ParticleEditorView;
import view.PositionStack;
import view.SandboxView;
import view.View;

public class AppWindow extends BasicGame{

  public static void main(String[] args) {
    try
    {
      AppGameContainer appgc;
      appgc = new AppGameContainer(new AppWindow("Scale Rebuild"));
      String[] ref = {"assets/generic/logo_t32.tga","assets/generic/logo_t24.tga","assets/generic/logo_t16.tga"};
      appgc.setIcons(ref);
      
      //appgc.setDisplayMode(1920-100, 1080-100, false);
      //appgc.setDisplayMode(800, 600, false);
      UITheme.readNonGL();
      appgc.setDisplayMode((int)UITheme.dimension.x, (int)UITheme.dimension.y, false);
      appgc.setAlwaysRender(true);
      appgc.setVerbose(false);
      
      appgc.start();
    }
    catch (SlickException ex)
    {
      Logger.getLogger(AppWindow.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public AppWindow(String title) {
    super(title);
  }
  
  //----############----
  //----############----
  //----############----
  //----############----
  //----############----
  //----############----

  private GameContainer gc;
  private Entry<Integer, Object> response;
  private boolean debug;
  private boolean shader;
  private boolean lalt;
  private boolean isLoading;
  private Audio music;
  private PositionStack pstk;

  private View mainView;
  private HeavyViewLoader hvl;
  private Container viewCnt;
  
  private Shader blurH,blurV;
  private Image img;
  
  @SuppressWarnings("hiding")
    @Override
  public void init(GameContainer gc) throws SlickException {
    
    ImageManager.setUp(Thread.currentThread());
    gc.setMultiSample(8);
    
    debug = false;
    lalt=false;
    this.gc=gc;
    gc.setTargetFrameRate(60);
    gc.setShowFPS(false);
    gc.setSmoothDeltas(true);
    gc.setMouseCursor(new Image("assets/generic/cursor.png"), 8, 7);
    
    Input.disableControllers();  

    mainView = new MainMenuView(gc);
    try {
      music = AudioLoader.getStreamingAudio("OGG", mainView.getMusicURL());
      music.playAsMusic(1f, 1f, true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
    
    if(UITheme.readGLFont()){
      gc.setDefaultFont(UITheme.font);
      System.out.println("Prefs successfully loaded");
    }
    
    blurH = Shader.makeShader("assets/shader/blur/basicVert.vrt", "assets/shader/blur/blurHorizontal.frg" );
    blurV = Shader.makeShader("assets/shader/blur/basicVert.vrt", "assets/shader/blur/blurVertical.frg" );
    img=  new Image(1920-100,1080-100);
    
    viewCnt = new Container();
    hvl = new HeavyViewLoader(viewCnt);
    pstk = new PositionStack();
    
    
    img = new Image(gc.getWidth(), gc.getHeight());
  }

  @Override
  public void render(GameContainer arg0, Graphics gx) throws SlickException {
    gx.setAntiAlias(false);
    mainView.render(arg0);
    if(isLoading){
      View v = (View)viewCnt.getObject();
      if(v != null){
        mainView=v;
        isLoading=false;
      }
    }

    //debug info here
    if(shader){
      
      gx.copyArea(img, 0, 0);
      gx.flush();
      Graphics geix = img.getGraphics();

      GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_SUBTRACT);
      
      //gx.setColor(new Color(.25f,.25f,.25f,1f));
      //gx.fillRect(0, 0, arg0.getWidth(), arg0.getHeight());
      
      geix.setColor(new Color(.25f,.25f,.25f,1f));
      //geix.fillRect(0, 0, 1920-100, 1080-100);
      geix.flush();

      GL_Blender.setDrawMode(GL_Blender.MODE_TRUE_ADD);
      blurH.startShader();
      gx.drawImage(img, 0, 0);
      blurV.startShader();
      gx.drawImage(img, 0, 0);
      Shader.forceFixedShader();
      
      GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
    }
  }
  
  @SuppressWarnings("hiding")
    @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    mainView.update(delta);
    SoundStore.get().poll(0);
    SoundStore.get().setCurrentMusicVolume(.1f);
    ImageManager.generickLoadChunk();
  }

  @Override
  public void keyPressed(int key, char c){
    int[] a = {key};
    if(key == Input.KEY_F12){
      if(debug)debug=false;else debug=true;
    }else if(key == Input.KEY_F11){
      if(shader)shader=false; else shader=true;
    }
    else if(key == Input.KEY_LALT){
      lalt=true;
    }else if(key == Input.KEY_F4){
      if(lalt){
        gc.exit();
      }
    }
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_K_PRESSED, a);
    if(response.getKey() == Def.A_ENTER_MAIN_MENU){
      mainView = new MainMenuView(gc);
      try {
        music = AudioLoader.getStreamingAudio("OGG", mainView.getMusicURL());
      } catch (IOException e) {
        //Go fuck yourself
      }
      music.playAsMusic(1f, 1f, true);
    }
  }
  
  @Override
  public void keyReleased(int key, char c){
    int[] a = {key};
    
    if(key == Input.KEY_LALT){
      lalt=false;
    }
    
    response = mainView.handleInput(System.currentTimeMillis(), Def.EVENT_K_RELEASED, a);
  }
  
  @Override
  public void mousePressed(int button, int x, int y){
    int[] a = {button,x,y};
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_M_PRESSED, a);
  }
  
  @Override
  public void mouseReleased(int button, int x, int y){
    int[] a = {button,x,y};
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_M_RELEASED, a);
    
    
    switch(response.getKey()){
    case Def.PRINT_ON_CONSOLE:
      System.out.println("Response from UIElement named "+response.getValue());
      break;
    case Def.A_EXIT:
      gc.exit();
      break;
    case Def.A_ENTER_SANDBOX:
      music.stop();
      mainView = new SandboxView(gc);
      break;
    case Def.A_ENTER_GALAXY_FROM_MENU:
      music.stop();
      mainView=new LoadingView(gc);
      isLoading=true;
      hvl = new HeavyViewLoader(viewCnt);
      hvl.setUpGV(gc, 0, 1000000000, 0.001, 1000, new Vec2D(0,0));
      hvl.start();
      break;
    case Def.A_ENTER_MAIN_MENU:
      mainView = new MainMenuView(gc);
      try {
        music = AudioLoader.getStreamingAudio("OGG", mainView.getMusicURL());
      } catch (IOException e) {
        //Go fuck yourself
      }
      music.playAsMusic(1f, 1f, true);
      break;
    case Def.A_ENTER_SOL_FROM_GALAXY:
      CBody c = (CBody)(response.getValue());
      pstk.put(c.position,mainView.getSeed());
      
      double temperature = Double.parseDouble(c.properties.get("Size"))*5000;
        double maxDistance = CSun.fSize(temperature);
        int rDistance = (int)CSun.fSizeRound(temperature);
        double zoom = 500d/rDistance;//half a screen width
        mainView=new LoadingView(gc);
        isLoading=true;
        hvl = new HeavyViewLoader(viewCnt);
        hvl.setUpSV(gc, c.getSeed(), (int)maxDistance, zoom, .1, c.getID(), new Vec2D(0,0));
        hvl.start();
      break;
    case Def.A_ENTER_GALAXY_FROM_SOL:
      Vec2D cali = new Vec2D(0,0);
      long seed = pstk.get(cali);
      
      mainView=new LoadingView(gc);
      isLoading=true;
      hvl = new HeavyViewLoader(viewCnt);
      hvl.setUpGV(gc, seed, 1000000000, 0.001, 1000, cali);
      hvl.start();
      break;
    case Def.A_ENTER_BATTLE_VIEW:
      mainView = new BattleView(gc);
      break;
    case Def.A_ENTER_OPTIONS:
      mainView = new OptionsView(gc);
      break;
    case Def.A_ENTER_PCL_EDTR:
      mainView = new ParticleEditorView(gc);
      break;
    case Def.A_ENTER_ASTS_EDTR:
      mainView = new AssetsEditorView(gc);
      break;
    }
  }
  
  @Override
  public void mouseMoved(int oldx, int oldy, int newx, int newy){
    int[] a = {oldx,oldy,newx,newy};
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_M_MOVED, a);
  }
  
  @Override
  public void mouseDragged(int oldx, int oldy, int newx, int newy) {
    int[] a = {oldx, oldy, newx, newy};
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_M_DRAGGED, a);
    }

  @Override
  public void mouseWheelMoved(int change){
    int[]a= {change};
    response = mainView.handleInput(System.currentTimeMillis(),Def.EVENT_MW_MOVED, a);
  }
}
