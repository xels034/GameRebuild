package view;

import java.net.URL;

import gui.ImageManager;
import gui.Def;
import gui.UIButton;
import gui.UITheme;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.util.ResourceLoader;

public class MainMenuView extends AbstractView{

  protected Image logo;
  protected URL musicLoopURL;
  
  public MainMenuView(GameContainer gc) {
    super(gc);
    musicLoopURL = ResourceLoader.getResource("assets/music/Next Move.ogg");

    UITheme.reload();
    
    UIButton b1 = new UIButton("New Game", "Starts new GalaxyView", 200, 250, UITheme.font.getWidth("New Game")+10, 20, Def.A_ENTER_GALAXY_FROM_MENU, null);
    UIButton b2 = new UIButton("New Sandbox", "Starts new Sandbox", 200, 280, UITheme.font.getWidth("New Sandbox")+10, 20, Def.A_ENTER_SANDBOX, null);
    UIButton b3 = new UIButton("New BattleView", "Starts a new\nDebug BattleView", 200,310, UITheme.font.getWidth("New BattleView")+10, 20, Def.A_ENTER_BATTLE_VIEW,null);
    
    UIButton b4 = new UIButton("Options", "Configure your game", 200,370, UITheme.font.getWidth("Options")+10, 20, Def.A_ENTER_OPTIONS, null);
    UIButton b5 = new UIButton("Exit", "Exits the game to Desktop", 200, 400, UITheme.font.getWidth("Exit")+10, 20, Def.A_EXIT, null);
    
    UIButton b6 = new UIButton("Particle Editor", "Go to the particle\neditor. Only available\nto developers", 200, 490, UITheme.font.getWidth("Paricle Editor")+10, 20, Def.A_ENTER_PCL_EDTR, null);
    UIButton b7 = new UIButton("Assets Editor", "Go to the assets\neditor. Only available\nto developers", 200, 520, UITheme.font.getWidth("Assets Editor")+10, 20, Def.A_ENTER_ASTS_EDTR, null);
    
    children.add(b1);
    children.add(b2);
    children.add(b3);
    children.add(b4);
    children.add(b5);
    children.add(b6);
    children.add(b7);
    
    logo = ImageManager.getImage("assets/generic/logo_holo.png").getScaledCopy(.5f);
  }
  
   @Override
   public void update(int delta){
    super.update(delta);
    ready=true;
   }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    //super.render(gc);
    gc.getGraphics().drawImage(logo, (gc.getWidth()/2)-logo.getWidth()/2, 30);
    renderUI(gc.getGraphics());
  }
  
  @Override
  public URL getMusicURL(){
    return musicLoopURL;
  }

}
