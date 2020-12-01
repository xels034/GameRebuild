package view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map.Entry;

import gui.Check;
import gui.Def;
import gui.UIButton;
import gui.UITheme;
import gui.UIicon;
import gui.UIinputBox;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import java.awt.Rectangle;

public class OptionsView extends AbstractView{

  private UIinputBox inpX, inpY, inpR, inpG, inpB;
  private int saved=-1;
  
  public OptionsView(GameContainer gc) {
    super(gc);
    UIButton b1 = new UIButton("Exit to Menu",
          "Exits to the main menu\nof the game",
          20, 20,
          UITheme.font.getWidth("Exit to Menu")+10, 20,
          Def.A_ENTER_MAIN_MENU, null);
    children.add(b1);
    
    UIButton b2 = new UIButton("Save",
          "Save your settings to disk",
          300, 100,
          UITheme.font.getWidth("Save")+10, 20,
          Def.A_SAVE, null);
    children.add(b2);
    
    
    children.add(new UIicon(50,70,"Resolution"));
    children.add(new UIicon(20,100,"X:"));
    children.add(new UIicon(20,130,"Y:"));
    children.add(new UIicon(200,70,"Color"));
    children.add(new UIicon(170,100,"R:"));
    children.add(new UIicon(170,130,"G:"));
    children.add(new UIicon(170,160,"B:"));
    children.add(new UIicon(20,210,"Status:"));
    
    inpX = new UIinputBox(60,100, 1, 10, "Width of the Screen", Check.cf_IsP_Integer);
    inpY = new UIinputBox(60,130, 1, 10, "Height of the Screen", Check.cf_IsP_Integer);
    inpR = new UIinputBox(210,100, 1, 10, "Red Componen of the Color", Check.cf_IsR_Double);
    inpG = new UIinputBox(210,130, 1, 10, "Green Componen of the Color", Check.cf_IsR_Double);
    inpB = new UIinputBox(210,160, 1, 10, "Blue Componen of the Color", Check.cf_IsR_Double);
    
    inpX.setContent(gc.getWidth()+"");
    inpY.setContent(gc.getHeight()+"");
    inpR.setContent(UITheme.main.r+"");
    inpG.setContent(UITheme.main.g+"");
    inpB.setContent(UITheme.main.b+"");
    
    children.add(inpX);
    children.add(inpY);
    children.add(inpR);
    children.add(inpG);
    children.add(inpB);
  }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    Graphics gx = gc.getGraphics();
    gx.setColor(UITheme.main);
    gx.drawRect(10, 60, 335, 195);
    UITheme.addReverb(new Rectangle(10,60,335,195), gx, new int[]{1,1,1});
    renderUI(gx);
    switch(saved){
    case -1:
      gx.setColor(UITheme.main);
      gx.drawString("Not saved yet", 90, 217);
      break;
    case 0:
      gx.setColor(Color.red);
      gx.drawString("Could not save,  try again!", 90, 217);
      break;
    case 1:
      gx.setColor(new Color(.5f,1f,0f,1f));
      gx.drawString("Saved. Re-Open game", 90, 217);
      break;
    }

  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = super.handleInput(timeStamp, event, data);
    if(response.getKey() == Def.A_SAVE){
      saveFile();
    }
    return response;
  }
  
  private void saveFile(){
    try (PrintWriter pw = new PrintWriter("config/ui.ini", "UTF-8");){
      int x = Integer.parseInt(inpX.toString());
      int y = Integer.parseInt(inpY.toString());
      
      float r = Float.parseFloat(inpR.toString());
      float g = Float.parseFloat(inpG.toString());
      float b = Float.parseFloat(inpB.toString());
      
      pw.println(";Resolution");
      pw.println("x="+x);
      pw.println("y="+y);
      pw.println(";UI-Color");
      pw.println("r="+r);
      pw.println("g="+g);
      pw.println("b="+b);
      pw.close();
      saved=1;
    } catch (NumberFormatException x){
      //x.printStackTrace();
      saved=0;
    } catch (IOException x){
      //x.printStackTrace();
      saved=0;
    }
  }

}
