package view;

import java.awt.Rectangle;
import java.io.File;
import java.util.LinkedList;
import java.util.Map.Entry;

import gui.Check;
import gui.Def;
import gui.GL_Blender;
import gui.ImageManager;
import gui.UIButton;
import gui.UIPopup;
import gui.UITheme;
import gui.UIicon;
import gui.UIinputBox;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import construction.ParticleCreator;
import construction.ParticleEntry;
import construction.PhMaterial;
import construction.PhPoly;
import construction.PhysicsManager;
import construction.UniqueID;
import construction.ValueRamp;
import construction.Vec2D;
import drawable.Rasterable;
import drawable.V2DPolygon;

public class ParticleEditorView extends AbstractView{
  
  private PhPoly placer;
  private PhysicsManager phm;
  private double animTime;
  private long lastAnim;
  private boolean updateEffect;
  
  @SuppressWarnings("hiding")
    private UIinputBox path, name, vrStart, vrEnd, vrHndP, vrHndV, cVecX, cVecY, lVecX, lVecY, pRand, density, alpha, image, speed,
         sRand, angle, aRand, rot, rRand, size, szRand, emitTime, etRand, lifeTime, ltRand, blendMode;
  
  private Vec2D lblAnc, inpAnc;
  
  private int entryIdx;
  private UIicon idxCntr;
  private LinkedList<UIinputBox> inputSet;
  private LinkedList<String> entryData;
  private LinkedList<ParticleEntry> pclEntry;
  private LinkedList<Boolean> pclEntryFlags;
  
  public ParticleEditorView(GameContainer gc) {
    super(gc);
    
    updateEffect = false;
    inputSet = new LinkedList<>();
    entryData = new LinkedList<>();
    pclEntry = new LinkedList<>();
    pclEntryFlags = new LinkedList<>();
    fillUI();
    
    lastAnim = -10000;
    addEntry();

    LinkedList<Vec2D> ll = new LinkedList<>();
    ll.add(new Vec2D(0,1));
    ll.add(new Vec2D(-1,0));
    ll.add(new Vec2D(1,0));
    
    V2DPolygon dmy = new V2DPolygon(ll, new Vec2D(), Color.black, "Dummy placer");
    PhMaterial phMat = new PhMaterial();
    Rasterable r = new Rasterable(new Vec2D());
    
    placer = new PhPoly(new Vec2D(), dmy, r, 0, 0, 0, phMat, UniqueID.newID());
    phm = new PhysicsManager(1000);
    phm.start();
    
    zStep = 1.1;
    maxZF = 3;
    minZF = 0.3;
  }
  
  @Override
  public void finalize(){
    phm.stop();
  }
  
  @Override
  public void update(int delta){
    super.update(delta);
    
    long now = System.currentTimeMillis();
    if(now > lastAnim+animTime || updateEffect){
      updateEffect = false;
      LinkedList<ParticleEntry> valids = new LinkedList<>();
      int i=0;
      for(boolean b : pclEntryFlags){
        if(b) valids.add(pclEntry.get(i));
        i++;
      }
      phm.removePoly(placer);
      phm.removeEffects(placer.pUnqID);
      if(valids.size() > 0){
        placer.pBirthList = valids;
        
        phm.addPoly(placer);
        lastAnim = now;
      }
    }
  }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    Graphics gx = gc.getGraphics();
    
    rasterItems.clear();
    LinkedList<PhPoly> ppl = phm.getPolyList();
    for(PhPoly pp : ppl){
      rasterItems.add(pp.getEffigy());
    }
    
    Rectangle rs[] = new Rectangle[2];
    
    rs[0] = new Rectangle((int)lblAnc.x-5, (int)lblAnc.y+45,360,410);
    rs[1] = new Rectangle((int)lblAnc.x-5, (int)lblAnc.y-58,360,40);
    
    for(int i=0;i<rs.length;i++){
      gx.setColor(UITheme.faint_t);
      gx.fillRect(rs[i].x, rs[i].y, rs[i].width, rs[i].height);
      gx.setColor(UITheme.main);
      gx.drawRect(rs[i].x, rs[i].y, rs[i].width, rs[i].height);
      
      UITheme.addReverb(rs[i], gx, new int[]{1,1,1});
    }
    
    renderRasterItems(gx);
    renderUI(gx);
    boolean b = pclEntryFlags.get(entryIdx);
    String t = "Invalid";
    if(b){
      gx.setColor(new Color(.5f,1f,0f,1f));
      t = "Valid";
    }
    else{
      gx.setColor(Color.red);
    }
    gx.drawString(t, (int)inpAnc.x, (int)lblAnc.y+420+7);
  }
  
  @Override
  public Entry<Integer, Object> handleInput(long timeStamp, int event, int[] data) {
    Entry<Integer, Object> response = super.handleInput(timeStamp, event, data);
    
    int oldIdx = entryIdx;
    
    switch(response.getKey()){
    case Def.A_RLS_FCS:
       parseInput();
      break;
    case Def.A_SCRL_UP:
      entryIdx+=2;
    case Def.A_SCRL_DWN:
      entryIdx--;
      entryIdx = Math.min(entryIdx, pclEntry.size()-1);
      entryIdx = Math.max(0, entryIdx);
      idxCntr.setText("Active Entry: ["+(entryIdx+1)+" of "+pclEntry.size()+"]");
      updateInput(oldIdx);
      break;
    case Def.A_NEW:
      addEntry();
      break;
    case Def.A_DEL:
      deleteEntry();
      break;
    case Def.A_SINGLE:
      updateEffect = true;
      break;
    case Def.A_CLR:
      for(UIinputBox uiib : inputSet){
        uiib.setContent("");
      }
      updateInput(entryIdx);
      pclEntryFlags.set(entryIdx, false);
      updateEffect = true;
      break;
    case Def.A_LOAD:
      loadFile();
      break;
    case Def.A_SAVE:
      saveFile();
      break;
    }
    return response;
  }
  
  private void loadFile(){
    String pth = "assets/fx/particle/"+path.toString();
    File f = new File(pth);
    if (f.exists() && !f.isDirectory()){
      pclEntry = ParticleCreator.loadEffect(pth);
      pclEntryFlags.clear();
      entryData.clear();
      animTime = 0;
      for(int i=0;i<pclEntry.size();i++){
        pclEntryFlags.add(true);
        ParticleEntry pe = pclEntry.get(i);
        String eds = "";
        
        eds+=pe.name + ":";

        LinkedList<Vec2D> handles = pe.lifeVR.getHandles();
        eds+=handles.get(0).y + ":" + handles.get(handles.size()-1).y +":";
        eds+=handles.get(1).x + ":" + handles.get(1).y +":";
        eds+=pe.startPos.x + ":" + pe.startPos.y + ":";
        eds+=pe.direction.x+ ":" + pe.direction.y+ ":";
        eds+=pe.posRand + ":";
        eds+=pe.density + ":";
        eds+=pe.alpha + ":";
        eds+=ImageManager.getImageIndex(pe.img.getResourceReference()) + ":";
        eds+=pe.speed + ":";
        eds+=pe.spdRand + ":";
        eds+=pe.angle + ":";
        eds+=pe.anglRand + ":";
        eds+=pe.rot + ":";
        eds+=pe.rotRand + ":";
        eds+=pe.scale + ":";
        eds+=pe.sclRand + ":";
        eds+=pe.emitTime + ":";
        eds+=pe.emtRand + ":";
        eds+=pe.lifeTime + ":";
        eds+=pe.lifeRand + ":";
        String[] modes = {"Normal", "Multiply", "Add", "Screen", "Subtract"};
        
        eds+=modes[pe.drawMode-GL_Blender.MODE_NORMAL];
        entryData.add(eds);
        
        animTime = Math.max(animTime, pe.emitTime+pe.lifeTime);
      }
      entryIdx=0;
      
      String[] data = specialSplit(entryData.get(entryIdx), ':');

      int i=0;
      for(UIinputBox uiib : inputSet){
        uiib.setContent(data[i]);
        i++;
      }
      
      updateEffect=true;
      idxCntr.setText("Active Entry: ["+(entryIdx+1)+" of "+pclEntry.size()+"]");
    }
    
  }
  
  private void saveFile(){
    LinkedList<ParticleEntry> valids = new LinkedList<>();
    int i = 0;
    for(boolean b : pclEntryFlags){
      if(b) valids.add(pclEntry.get(i));
      i++;
    }
    String pth = path.toString();
    if(!pth.endsWith(".pcl")) pth+=".pcl";
    ParticleCreator.saveEffect(valids, path.toString());
  }
  
  private void addEntry(){
    pclEntry.add(new ParticleEntry());
    pclEntryFlags.add(false);
    String entryDat = "";
    for(int i=0;i<inputSet.size();i++){
      entryDat+=":";
    }
    entryDat = entryDat.substring(0, entryDat.length()-1);
    entryData.add(entryDat);
    idxCntr.setText("Active Entry: ["+(entryIdx+1)+" of "+pclEntry.size()+"]");
  }
  
  private void deleteEntry(){
    if(pclEntry.size() > 1){
      pclEntry.remove(entryIdx);
      pclEntryFlags.remove(entryIdx);
      entryData.remove(entryIdx);
      if(entryIdx == pclEntry.size()) entryIdx--;
      
      idxCntr.setText("Active Entry: ["+(entryIdx+1)+" of "+pclEntry.size()+"]");
      updateEffect = true;
    }
  }
  
  private void updateInput(int oldIdx){
    String put = "";
    for(UIinputBox uiib : inputSet){
      put += uiib.toString() +":";
    }
    put = put.substring(0, put.length()-1);
    entryData.set(entryIdx-(entryIdx-oldIdx), put);
    String[] data = specialSplit(entryData.get(entryIdx), ':');

    int i=0;
    for(UIinputBox uiib : inputSet){
      uiib.setContent(data[i]);
      i++;
    }
  }
  
  private String[] specialSplit(String data, char marker){
    LinkedList<String> returner = new LinkedList<>();
    String tmp = "";
    int i=0;
    for(i=0;i<data.length();i++){
      if(data.charAt(i) == marker){
        returner.add(tmp);
        tmp= "";
      }
      else tmp += data.charAt(i);
    }
    returner.add(tmp);
    return returner.toArray(new String[0]);
  }
  
  private void parseInput(){
    boolean correctParse = false;
    try{
      String strNm = name.toString();
      double lvrx = Double.parseDouble(vrStart.toString());
      double lvry = Double.parseDouble(vrEnd.toString());
      double lvrhx = Double.parseDouble(vrHndP.toString());
      double lvrhy = Double.parseDouble(vrHndV.toString());
      ValueRamp lifeVR = new ValueRamp();
      lifeVR.changeEnds(lvrx, lvry);
      lifeVR.addHandle(lvrhx, lvrhy);
      
      double x = Double.parseDouble(cVecX.toString());
      double y = Double.parseDouble(cVecY.toString());
      Vec2D startPos = new Vec2D(x,y);
      
      x = Double.parseDouble(lVecX.toString());
      y = Double.parseDouble(lVecY.toString());
      Vec2D direction = new Vec2D(x,y);
      double posRand = Double.parseDouble(pRand.toString());
      double dens = Double.parseDouble(density.toString());
      Image img = ImageManager.getImage(Integer.parseInt(image.toString()));
      double a = Double.parseDouble(alpha.toString());
      double spd = Double.parseDouble(speed.toString());
      double spdRand = Double.parseDouble(sRand.toString());
      double angl = Double.parseDouble(angle.toString());
      double anglRand = Double.parseDouble(aRand.toString());
      double r = Double.parseDouble(rot.toString());
      double rotRand = Double.parseDouble(rRand.toString());
      double scl = Double.parseDouble(size.toString());
      double sclRand = Double.parseDouble(szRand.toString());
      double emt = Double.parseDouble(emitTime.toString());
      double emtR = Double.parseDouble(etRand.toString());
      double lft = Double.parseDouble(lifeTime.toString());
      double lftr = Double.parseDouble(ltRand.toString());
      
      int drawMode;
      String t = blendMode.toString();
      if(t.equals("Add")) drawMode = GL_Blender.MODE_TRUE_ADD;
      else if(t.equals("Normal")) drawMode = GL_Blender.MODE_NORMAL;
      else if(t.equals("Multiply")) drawMode = GL_Blender.MODE_TRUE_MULTIPLY;
      else if(t.equals("Screen")) drawMode = GL_Blender.MODE_TRUE_SCREEN;
      else if(t.equals("Subtract")) drawMode = GL_Blender.MODE_TRUE_SUBTRACT;
      else throw new NumberFormatException();
      
      ParticleEntry pe = new ParticleEntry(strNm, lifeVR,startPos,direction,posRand, dens, a, img, spd, spdRand, angl, anglRand, r,
                        rotRand, scl, sclRand, emt, emtR, lft,lftr, drawMode, placer.pUnqID);
      pclEntry.set(entryIdx, pe);
      double newTime = emt+lft;
      if(animTime == Double.POSITIVE_INFINITY) animTime = newTime;
      animTime = Math.max(animTime, newTime);
      correctParse = true;
      
    } catch (NumberFormatException x){
      correctParse = false;
    }
    updateEffect = true;
    pclEntryFlags.set(entryIdx, correctParse);
  }
  
  private void fillUI(){
    UIButton b1 = new UIButton("Exit to Menu",
          "Exits to the main menu\nof the game",
          20, 20,
          UITheme.font.getWidth("Exit to Menu")+10, 20,
          Def.A_ENTER_MAIN_MENU, null);
    children.add(b1);
    
    UIButton b2 = new UIButton(ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_LEFT), "Previous Entry",187,175,15,15,Def.A_SCRL_DWN, null);
    UIButton b3 = new UIButton(ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_RGHT), "Next Entry",207,175,15,15,Def.A_SCRL_UP, null);
    UIButton b4 = new UIButton("New Entry", "Add a new entry\nto the System list", 227, 175, UITheme.font.getWidth("New Entry")+10,15, Def.A_NEW, null);
    UIButton b5 = new UIButton("Delete Entry", "Delete the current\nentry from the System", 295, 175, UITheme.font.getWidth("Delete Entry")+10, 15, Def.A_DEL, null);
    
    
    UIButton b6 = new UIButton(ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_SAVE), "Save this System", 330,100,15,15, Def.A_OPOP, null);
    LinkedList<UIButton> llb = new LinkedList<>();
    llb.add(new UIButton("Yes", "Overwrite for sure", 330,150, UITheme.font.getWidth("Yes")+10,15, Def.A_SAVE, null));
    llb.add(new UIButton("No", "Cancel saving", 270,150, UITheme.font.getWidth("No")+10,15, Def.A_NONE, null));
    UIPopup uipp = new UIPopup(240,100, "This will override\nany previous data.", llb);
    b6.setPopup(uipp);
    
    UIButton b7 = new UIButton(ImageManager.getImage(ImageManager.OFFS_ICON+ImageManager.IDX_ICN_LOAD), "Load this System", 350,100,15,15, Def.A_LOAD, null);
    children.add(b2);
    children.add(b3);
    children.add(b4);
    children.add(b5);
    children.add(b6);
    children.add(b7);
    //children.add(uipp);
    
    UIButton b8 = new UIButton("Reset\nAnimation", "Starts the Animation\nfrom scratch", 390,93, UITheme.font.getWidth("Animation")+10, 27, Def.A_SINGLE, null);
    UIButton b9 = new UIButton("Clear\nFields", "Clear all input\nFields", 390, 175, UITheme.font.getWidth("Fields")+10, 27, Def.A_CLR, null);
    children.add(b8);
    children.add(b9);
    
    
    
    lblAnc = new Vec2D(20,150);
    inpAnc = lblAnc.getCopy();
    inpAnc.x+=100;
    
    path    = new UIinputBox((int)inpAnc.x, (int)inpAnc.y-50,   1,30,"Path do load/save from", Check.cf_IsPath);
    name    = new UIinputBox((int)inpAnc.x, (int)inpAnc.y-5,   1,30,"Name of this Entry", Check.cf_NameFilter);
    
    vrStart = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+60,1,10,"Initial Alpha\nBetween 0-1", Check.cf_IsR_Double);
    vrEnd   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+90,1,10,"Alpha at death", Check.cf_IsR_Double);
    vrHndP  = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+120,1,10,"Position in\nAlphaRamp", Check.cf_IsR_Double);
    vrHndV  = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+150,1,10,"Alpha at Position", Check.cf_IsR_Double);
    cVecX   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+180,1,10,"X-Position\nof the Effect", Check.cf_IsDouble);
    cVecY   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+210,1,10,"Y-Position\nof the Effect", Check.cf_IsDouble);
    lVecX   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+240,1,10,"X-Position\nof the Line,\nif needed", Check.cf_IsDouble);
    lVecY   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+270,1,10,"Y-Position\nof the Line,\nif needed", Check.cf_IsDouble);
    pRand   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+300,1,10,"Position randomness", Check.cf_IsP_Double);
    density = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+330,1,10,"Particles per\nsecond", Check.cf_IsP_Double);
    alpha   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+360,1,10,"Base Alpha.\nWill be multiplied\nby AlphaRamp", Check.cf_IsR_Double);
    image   = new UIinputBox((int)inpAnc.x, (int)inpAnc.y+390,1,5,"Image Index", Check.cf_ImageIndex);
    speed   = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+60,1,10,"Maximal Speed\nof particles", Check.cf_IsDouble);
    sRand   = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+90,1,10,"Randomness of\nspeed", Check.cf_IsR_Double);
    angle   = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+120,1,10,"Direction of\nemission", Check.cf_IsP_Double);
    aRand   = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+150,1,10,"Angle Randomness\nbetween 0-2*PI", Check.cf_IsP_Double);
    rot     = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+180,1,10,"Rotating speed", Check.cf_IsDouble);
    rRand   = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+210,1,10,"Rotation randomness", Check.cf_IsR_Double);
    size    = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+240,1,10,"Base size", Check.cf_IsP_Double);
    szRand  = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+270,1,10,"Size randomness", Check.cf_IsR_Double);
    emitTime= new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+300,1,10,"Emit Time in ms\n(1000=1sek)\n'Infinity' also possible", Check.cf_IsP_Double);
    etRand  = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+330,1,10,"Emit Time randomness\nCurrently unnused!", Check.cf_IsR_Double);
    lifeTime= new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+360,1,10,"Base Life Time in ms\n(1000=1sek)\n'Infinity' also possible", Check.cf_IsP_Double);
    ltRand  = new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+390,1,10,"Life Time randomness", Check.cf_IsR_Double);
    blendMode=new UIinputBox((int)inpAnc.x+180, (int)inpAnc.y+420,1,10,"Blend Mode:\n\n'Normal'\n'Add'\n'Screen'\n'Multiply'\n'Subtract'\n\nare allowed", Check.cf_BlendMode);
    
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y-50,"Filepath:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y-5, "Entry Name:"));
    
    idxCntr = new UIicon((int)lblAnc.x, (int)lblAnc.y+22, "Active Entry: [1 of 1]");
    children.add(idxCntr);
    
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+60,"Alpha Start:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+90,"Alpha End:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+120,"Handle Pos:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+150,"Handle Val:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+180,"Center X:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+210,"Center Y:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+240,"Line X:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+270,"Line Y:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+300,"Pos Rand:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+330,"Density:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+360,"Base Alpha:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+390,"Image ID:"));
    children.add(new UIicon((int)lblAnc.x, (int)lblAnc.y+420,"Entry Status:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+60,"Speed:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+90,"Speed Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+120,"Angle:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+160,"Angle Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+180,"Rot. Speed:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+210,"R. Speed Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+240,"Size:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+270,"Size Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+300,"Emit Time:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+330,"E. Time Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+360,"Life Time:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+390,"L. Time Rand:"));
    children.add(new UIicon((int)lblAnc.x+180, (int)lblAnc.y+420,"Blend Mode:"));
    
    children.add(path);
    children.add(name);
    children.add(vrStart);
    children.add(vrEnd);
    children.add(vrHndP);
    children.add(vrHndV);
    children.add(cVecX);
    children.add(cVecY);
    children.add(lVecX);
    children.add(lVecY);
    children.add(pRand);
    children.add(density);
    children.add(alpha);
    children.add(image);
    children.add(speed);
    children.add(sRand);
    children.add(angle);
    children.add(aRand);
    children.add(rot);
    children.add(rRand);
    children.add(size);
    children.add(szRand);
    children.add(emitTime);
    children.add(etRand);
    children.add(lifeTime);
    children.add(ltRand);
    children.add(blendMode);
    
    //TODO
    //inputSet.add(path);
    inputSet.add(name);
    inputSet.add(vrStart);
    inputSet.add(vrEnd);
    inputSet.add(vrHndP);
    inputSet.add(vrHndV);
    inputSet.add(cVecX);
    inputSet.add(cVecY);
    inputSet.add(lVecX);
    inputSet.add(lVecY);
    inputSet.add(pRand);
    inputSet.add(density);
    inputSet.add(alpha);
    inputSet.add(image);
    inputSet.add(speed);
    inputSet.add(sRand);
    inputSet.add(angle);
    inputSet.add(aRand);
    inputSet.add(rot);
    inputSet.add(rRand);
    inputSet.add(size);
    inputSet.add(szRand);
    inputSet.add(emitTime);
    inputSet.add(etRand);
    inputSet.add(lifeTime);
    inputSet.add(ltRand);
    inputSet.add(blendMode);
  }

}
