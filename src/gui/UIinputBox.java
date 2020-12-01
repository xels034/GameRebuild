package gui;

import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

public class UIinputBox implements UIElement{

  private static final int NONE= 0;
  private static final int WRITE= 1;
  private static final int OK = 2;
  private static final int FALSE = 3;
  private static final long DURATION = 5500;
  
  private String content;
  private String tempContent;
  private Rectangle bounds;
  private boolean pressed;
  private boolean inFocus;
  private int[] mask;
  private boolean shift;
  private int charWidth;
  private int status;
  private long statusStamp;
  
  private Overlay ovrly;
  private CheckFunc cf;
  
  public UIinputBox(int xPos, int yPos, int rows, int cols, String tooltip, CheckFunc c){
    
    content="";
    tempContent="";
    charWidth = cols;
    StringBuilder sb = new StringBuilder();
    for(int i=0;i<cols;i++){
      sb.append(" ");
    }
    
    int height = rows*20;
    int width = UITheme.font.getWidth(sb.toString())+10;
    
    bounds = new Rectangle(xPos,yPos,width,height);
    pressed=false;
    
    cf = c;
    ovrly = new Overlay(tooltip, xPos+5, yPos+height+5, bounds);
    mask = new int[]{1,0,1};
    inFocus=false;
    shift=false;
    
    status = 0;
    statusStamp = 0;
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = new AbstractMap.SimpleEntry<>(Def.A_NONE, null);
    switch(event){
    case Def.EVENT_K_PRESSED:
      switch(data[0]){
      case Input.KEY_ESCAPE:
        status = FALSE;
        tempContent = content;
        inFocus=false;
        response = new AbstractMap.SimpleEntry<>(Def.A_RLS_FCS, this);
        break;
      case Input.KEY_NUMPADENTER:
      case Input.KEY_ENTER:
        if(cf.check(tempContent)){
          status = OK;
          content = tempContent;
        }else{
          status = FALSE;
          tempContent = content;
        }
        inFocus=false;
        response = new AbstractMap.SimpleEntry<>(Def.A_RLS_FCS, this);
        break;
      case Input.KEY_BACK:
        if(tempContent.length()>0) tempContent = tempContent.substring(0, tempContent.length()-1);
        break;
      case Input.KEY_LSHIFT:
      case Input.KEY_RSHIFT:
        shift = true;
        break;
      case Input.KEY_SPACE:
        tempContent += " ";
        break;
      case Input.KEY_UNDERLINE:
        tempContent += "_";
        break;
      case Input.KEY_MINUS:
        if(shift) tempContent+="_";
        else      tempContent+= "-";
        break;
      case Input.KEY_PERIOD:
        tempContent+=".";
        break;
      case Input.KEY_7:
        if(shift) tempContent+="/";
        else tempContent+="7";
        break;
      
      //NUMPAD CONV  
      case Input.KEY_NUMPAD0:
        tempContent += "0";
        break;
      case Input.KEY_NUMPAD1:
        tempContent += "1";    
        break;
      case Input.KEY_NUMPAD2:
        tempContent += "2";
        break;
      case Input.KEY_NUMPAD3:
        tempContent += "3";
        break;
      case Input.KEY_NUMPAD4:
        tempContent += "4";
        break;
      case Input.KEY_NUMPAD5:
        tempContent += "5";
        break;
      case Input.KEY_NUMPAD6:
        tempContent += "6";
        break;
      case Input.KEY_NUMPAD7:
        tempContent += "7";
        break;
      case Input.KEY_NUMPAD8:
        tempContent += "8";
        break;
      case Input.KEY_NUMPAD9:
        tempContent += "9";
        break;
        
      default:
        
        String s = Input.getKeyName(data[0]);
        if(!shift) s = s.toLowerCase();
        if(s.length() == 1) tempContent += s;
      }
      break;
    
    case Def.EVENT_K_RELEASED: 
      if(data[0] == Input.KEY_LSHIFT || data[0] == Input.KEY_RSHIFT)
        shift = false;
      break;
    
    case Def.EVENT_M_PRESSED:
      if(bounds.contains(data[1], data[2]))   press();
      else                                    release();
      break;
    case Def.EVENT_M_RELEASED:
      release();
      if(!inFocus) {
        status = WRITE;
        response = new AbstractMap.SimpleEntry<>(Def.A_RQST_FCS, this);
        inFocus = true;
      } else{
        if(cf.check(tempContent)){
          status = OK;
          content = tempContent;
        }else{
          status = FALSE;
          tempContent = content;
        }
        inFocus=false;
        response = new AbstractMap.SimpleEntry<>(Def.A_RLS_FCS, this);
      }
      
    }
    return response;
  }

  @Override
  public Rectangle getBounds() {
    return bounds;
  }

  @Override
  public void render(GameContainer gc) {
    Graphics gx = gc.getGraphics();
    GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);

    if(status == WRITE) statusStamp = System.currentTimeMillis();
    else if (System.currentTimeMillis() > (statusStamp+DURATION)) status = NONE;

    gx.setColor(new Color(0,0,0,.7f));
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    
    gx.setColor(UITheme.default_t);
    
    gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    gx.setColor(UITheme.main);
    gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
    
    if(inFocus){
      gx.setColor(UITheme.default_t);
      gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    
    
    gx.setColor(UITheme.main);
    gx.setFont(gc.getDefaultFont());
    String output = tempContent;
    if(tempContent.length() > charWidth-1){
      output = tempContent.substring(0, charWidth-3);
      output += "...";
    }
    
    gx.drawString(output, bounds.x+4, bounds.y+6);

    //STATUS LAMP

    Color c = null;
    float a = 1-(Math.min(DURATION, System.currentTimeMillis()-statusStamp) / 1000f);
    
    switch (status){
    case WRITE:
      c = new Color(UITheme.main.r, UITheme.main.g, UITheme.main.b, a);
      int w = UITheme.font.getWidth(tempContent)+7;
      gx.setColor(c);
      gx.drawLine(bounds.x+w, bounds.y+4, bounds.x+w, bounds.y+bounds.height-4);
      break;
    case OK:
      c = new Color(.5f,1f,0f,a);
      break;
    case FALSE:
      c = new Color(1f,0f,0f,a);
      break;
    default:
      c = new Color(0,0,0,0);
    }
    gx.setColor(c);
    gx.fillRect(bounds.x+bounds.width-7, bounds.y+bounds.height-7, 4, 4);
    
    UITheme.addReverb(bounds, gx, mask);
  }
  
  public void setContent(String s){
    content = s;
    tempContent=s;
  }

  @Override
  public String getToolTip() {
    return ovrly.toString();
  }

  @Override
  public LinkedList<UIElement> getChildren() {
    return new LinkedList<>();
  }

  @Override
  public Overlay getOverlay() {
    return ovrly;
  }

  @Override
  public void press() {
    pressed = true;  
  }

  @Override
  public void release() {
    pressed=false;
  }

  @Override
  public boolean isPressed() {
    return pressed;
  }
  
  @Override
  public String toString(){
    return content;
  }

}
