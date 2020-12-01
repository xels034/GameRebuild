package gui;

import java.awt.Rectangle;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map.Entry;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import construction.Vec2D;

import drawable.Rasterable;

public class UISelectionList implements UIElement{

  private final int rowOffset;
  private final int maxHeight;
  private final int maxItems;
  private final int minItems;
  private int displayOffset;
  private int width;
  private long lastInput;
  
  private LinkedList<Rasterable> source;
  private LinkedList<UIElement> actions;
  private Rectangle bounds;
  private Rectangle scrollArea;
  private Vec2D scrollBar; //x=startx y=endx
  private Image fcsIcn, cBody, upIcn, dwnIcn;
  private Vec2D anchor;
  private String headLine;
  private boolean pressed;
  private Vec2D pressAnchor;
  
  public UISelectionList(String n, int x, int y, int mxH, int minI){
    rowOffset = UITheme.font.getLineHeight()+10;
    width = 280;
    maxHeight = mxH;
    maxItems = (int)(maxHeight/(rowOffset+4f));
    minItems = minI;
    displayOffset = 0;
    
    headLine = n;
    
    anchor = new Vec2D(x,y);
    source = new LinkedList<>();
    actions = new LinkedList<>();
    //20 180
    bounds = new Rectangle(x,y,width,0);
    scrollBar = new Vec2D(0,0);
    
    fcsIcn = ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_FOCUS);
    cBody  = ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_CB);
    upIcn =  ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_UP);
    dwnIcn = ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_DWN);
    
    pressed=false;
  }
  
  
  public void updateSource(LinkedList<Rasterable> src){
    updateSource(src, false);
  }
  
  private void updateSource(LinkedList<Rasterable> src, boolean updateNeeded){
    //flag can be set privatley to true, so a update is forced (scrolling)
    boolean flag = updateNeeded;
    
    if(!updateNeeded){
      updateNeeded=source.size() != src.size();
    }
    
    if(!updateNeeded){
      for(int i=0;i<source.size();i++){
        if(source.get(i) != src.get(i)){
          updateNeeded=true;
        }
      }
    }
    
    if(updateNeeded){
      source=src;
      actions.clear();
      
      int maxL = 0;
      for(Rasterable r : source){
        maxL = Math.max(maxL, UITheme.font.getWidth(r.toString()));
      }
      
      width = maxL+85;
      //one because the headline counts as the first row
      
      //Up/Down buttons if needed
      if(source.size()>maxItems){
        actions.add(new UIButton(upIcn,
             "Scroll up",
             (int)anchor.x+width, (int)anchor.y+10,
             upIcn.getWidth()+5,
             upIcn.getHeight()+5,
             Def.A_SCRL_UP,
             null));

        actions.add(new UIButton(dwnIcn,
                     "Scroll down",
                     (int)anchor.x+width, (int)anchor.y+rowOffset*maxItems+15,
                     upIcn.getWidth()+5,
                     upIcn.getHeight()+5,
                     Def.A_SCRL_DWN,
                     null));
        displayOffset = Math.min(displayOffset, source.size()-maxItems);
      }else{
        displayOffset = 0;
      }

      //start at displayOffset and fill list with items
      ListIterator<Rasterable> iter = source.listIterator(displayOffset);
      
      for(int i=1; i<=maxItems && iter.hasNext() && source.size()>=minItems;i++){
        Rasterable r = iter.next();
        UIButton b = new UIButton(cBody,
                     "Make single Selection",
                     (int)anchor.x+10,(int)anchor.y+10+rowOffset*i,
                     cBody.getWidth()+5,
                     cBody.getHeight()+5,
                     Def.A_SINGLE,
                     r);
        b.setMask(new int[]{0,0,1});
        actions.add(b);
        
        
        b = new UIButton(fcsIcn,
             "Center view on\n"+r.toString(),
             (int)anchor.x+width-25,(int)anchor.y+10+rowOffset*i,
             fcsIcn.getWidth()+5,
             fcsIcn.getHeight()+5,
             Def.A_CNTR_FCS,
             r);
        b.setMask(new int[]{0,0,1});
        actions.add(b);

        //TODO think about how to render the elements different
        //if(r instanceof CStar){
        /*  b = new UIButton(entIcn,
               "Enter the Solar System of\n"+r.toString(),
               (int)anchor.x+width-25,(int)anchor.y+10+rowOffset*i,
               entIcn.getWidth()+5,
               entIcn.getHeight()+5,
               Statics.A_ENTER_SOL_FROM_GALAXY,
               r);
          b.setMask(new int[]{0,0,1});
          actions.add(b);
        //}
        */
      }
      
      if(source.size() >= minItems){
        bounds.height= rowOffset*(Math.min(maxItems, source.size())+1);
      }else{
        bounds.height=0;
      }
      
      bounds.width = width;
      scrollArea = new Rectangle(0,0,0,0);
      
      //when scrolling needs to be added
      if(source.size()>maxItems){
        
        
        bounds.width+=25;
        
        int sbHeight = bounds.height-40;
        scrollArea = new Rectangle(bounds.x+width+1, bounds.y+30, 15, sbHeight);
        if(!flag){
          updateScrollBar();
        }
        
      }if(source.size()>0){
        bounds.height+=15;
      }
    }
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
    if(timeStamp > lastInput){
      lastInput = timeStamp;
      boolean mw_mvd=false;
      
      switch(event){
      case Def.EVENT_M_PRESSED:
        if(scrollArea.contains(data[1],  data[2])){
          response = new AbstractMap.SimpleEntry<>(Def.A_RQST_FCS, this);
        }
        press();
        pressAnchor = new Vec2D(data[1], data[2]);
        break;
      case Def.EVENT_M_RELEASED:
        release();
        response = new AbstractMap.SimpleEntry<>(Def.A_RLS_FCS, this);
        break;
      case Def.EVENT_M_DRAGGED:
        //misusing a Vec2D as ystart and height
        //remember drag: oldx,oldy,newx,newy
        //ose old values for checking, otherwise a big jump could be missed
        if(data[1] > scrollBar.x && data[1] < scrollBar.x+scrollBar.y){
          scrollBar.x += data[3]-pressAnchor.y;
          scrollBar.x = Math.max(scrollBar.x, scrollArea.y);
          scrollBar.x = Math.min(scrollBar.x, scrollArea.y+scrollArea.height-scrollBar.y);
          pressAnchor.x = data[2];
          pressAnchor.y = data[3];
          
          int space = (int)(scrollArea.height-scrollBar.y);
          
                  //where the scrollbar Starts
          double ratio = (scrollBar.x-scrollArea.y)/space;
          int idxSpace = source.size()-maxItems;
          
          displayOffset = (int)(idxSpace*ratio);
          updateSource(source,true);
        }
        break;
      case Def.EVENT_MW_MOVED:
        mw_mvd=true;
        break;
      }
      
      if(!mw_mvd){
        for(UIElement e: actions){
          if(e.getBounds().contains(data[1],data[2])){
            response = e.handleInput(timeStamp, event, data);
          }
        }
      }
  
      switch(response.getKey()){
      case Def.A_SCRL_UP:
        if(displayOffset>0){
          displayOffset--;
          updateSource(source, true);
          updateScrollBar();
        }
        response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
        break;
      case Def.A_SCRL_DWN:
        if(displayOffset < source.size()-maxItems){
          displayOffset++;
          updateSource(source, true);
          updateScrollBar();
        }
        response = new AbstractMap.SimpleEntry<>(Def.A_NONE,null);
        break;
      }
      if(mw_mvd && source.size() > maxItems){
        displayOffset-=Math.signum(data[0]);
        
        displayOffset = Math.max(displayOffset, 0);
        displayOffset = Math.min(displayOffset,  source.size()-maxItems);
        updateSource(source,true);
        updateScrollBar();
        
        response = new AbstractMap.SimpleEntry<>(Def.A_IGNR_INPT,null);
      }
    }
    return response;
  }
  
  private void updateScrollBar(){
    int sbHeight = bounds.height-40;
    scrollArea = new Rectangle(bounds.x+width+1, bounds.y+30, 15, sbHeight);
    //shouldn't be done while scrolling. barPos vs itemsCount discrepancy
    //make up for a chaos. use only when a real update from outside is needed
    sbHeight *= maxItems/(float)source.size();
    //offset in px
    int actOffsetC = bounds.height-60-sbHeight;
    //offset in int
    int maxOffsetN = source.size()-maxItems;
    actOffsetC *=(displayOffset/(float)maxOffsetN);
    scrollBar.x = bounds.y+30+actOffsetC;
    scrollBar.y = sbHeight;
  }

  @Override
  public Rectangle getBounds() {
    return (Rectangle)bounds.clone();
  }

  @Override
  public void render(GameContainer gc) {
    //only render if anythign is actually selected
    if(source.size()>=minItems){
      //preps
      Graphics gx = gc.getGraphics();
      GL_Blender.setDrawMode(GL_Blender.MODE_NORMAL);
      UITheme.readGLFont();
      
      //background
      gx.setColor(new Color(0,0,0,.7f));
      gx.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
      gx.setColor(UITheme.main);
      gx.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
      UITheme.addReverb(bounds, gx, new int[]{1,1,1});
      
      //headline
      String printText = headLine+" ("+source.size()+")";
      gx.setColor(UITheme.main);
      gx.drawString(printText, (int)anchor.x+10, bounds.y+10);
      gx.drawLine((int)anchor.x+10, bounds.y+10+10+4, (int)anchor.x+width-10, bounds.y+10+10+4);
      
      //items
      ListIterator<UIElement> lia;
      
      //render the up/down buttons, added first, manually
      if(source.size() > maxItems){
        actions.get(0).render(gc);
        actions.get(1).render(gc);
        lia = actions.listIterator(2);
      }else{
        //no need to offset the buttons, because the ofsetting
        //already happened at creation
        lia = actions.listIterator(0);
      }
      ListIterator<Rasterable> lir = source.listIterator(displayOffset);
      
      //1 because the headline was the first row
      for(int i=1;i<=maxItems && lir.hasNext();i++){
        Rasterable r = lir.next();
        gx.setColor(UITheme.main);
        gx.drawString(r.toString(), bounds.x+45, bounds.y+rowOffset*i+15);
        
        //all three rows
        UIElement b = lia.next();
        b.render(gc);
        b = lia.next();
        b.render(gc);
        //b = lia.next();
        //b.render(gc);
      }
      
      //scrollBar, not really suitable for an extra ui element
      //when it needs so much data from the list
      if(source.size() > maxItems){
        gx.setColor(UITheme.dimmed_s);
        gx.drawRect(scrollArea.x, scrollArea.y, scrollArea.width, scrollArea.height);
        gx.setColor(UITheme.main);
        //misusing a vec2d ast start/end y
        Rectangle r = new Rectangle(scrollArea.x, (int)scrollBar.x, scrollArea.width, (int)scrollBar.y);
        gx.drawRect(r.x, r.y, r.width, r.height);
        UITheme.addReverb(r, gx, new int[]{1,1,1});
      }
    }
  }

  @Override
  public String getToolTip() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public LinkedList<UIElement> getChildren() {
    return (LinkedList<UIElement>)actions.clone();
  }

  @Override
  public Overlay getOverlay() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void press() {
    pressed=true;
  }

  @Override
  public void release() {
    pressed=false;
    for(UIElement b: actions){
      b.release();
    }
    
  }

  @Override
  public boolean isPressed() {
    return pressed;
  }

}
