package drawable;

import gui.GL_Blender;
import gui.ImageManager;

import java.util.HashMap;
import java.util.Random;

import org.newdawn.slick.Image;

import construction.ValueRamp;
import construction.TiledID;
import construction.Vec2D;

public class CPlanet extends CBody{

  public static final int THRESH_LAVA=800;
  public static final int THRESH_SOLID=150;
  public static final int THRESH_GAS_SIZE=80000;
  public static final int THRESH_HOT_SIZE=180000;
  public static final int MIN_SIZE=2000;
  public static final int MAX_SIZE=200000;
  public static final String[] solidMatTypes = {"Rock","Metal","Mineral","Precious Metal"};
  public static final String[] gasMatTypes = {"Oxygen","Helium","Hydrogen"};
  
  protected Compound atmoComp;
  protected Compound crustComp;
  protected Compound coreComp;
  
  public CPlanet(long s, Vec2D pos, TiledID pid, CBody par) {
    super(s, pos, pid);
    tid=pid;
    //double dist = Math.hypot(pos.x, pos.y);
    
    centerMass=par;

    atmoComp = new Compound();
    crustComp = new Compound();
    coreComp = new Compound();
    
    Random r = new Random();
    r.setSeed(s);
    
    double atmoThresh = buildSize(r, par);
    buildType(r, atmoThresh);
    buildWater(r, par);
    buildDetails(r);
    buildLayers(r);
    
    properties.put("Kind", "Planet");
    properties.put("SDistance",(int)Math.hypot(position.x, position.y)+"");
    properties.put("SDUnit", "km");
  }
  
  protected double buildSize(Random r, CBody sun){
    properties.remove("Temperature");
    properties.remove("TUnit");
    properties.remove("Size");
    properties.remove("SUnit");
    
    double dist = Math.hypot(position.x, position.y);
    
    ValueRamp sizeRamp = new ValueRamp();
    ValueRamp gaussRamp = new ValueRamp();
    ValueRamp gasGRamp = new ValueRamp();
    
    sizeRamp.changeEnds(0, 0.01);
    
    sizeRamp.addHandle(0.1, 0.3);
    sizeRamp.addHandle(0.25,0.5);
    sizeRamp.addHandle(0.45,1);
    sizeRamp.addHandle(0.5, 1.2);
    sizeRamp.addHandle(0.55,1);
    sizeRamp.addHandle(0.75,0.3);
    sizeRamp.addHandle(0.85,0.15);
    
    gaussRamp.addHandle(0.25, 0.4);
    gaussRamp.addHandle(0.75, 0.6);
    
    gasGRamp.addHandle(0.85, 0);
    gasGRamp.addHandle(0.925, 0.2);
    
    int sunTemp=Integer.parseInt(sun.properties.get("Temperature"));
    double temperature = CSun.fTemp(dist,sunTemp)*1.2;
    
    temperature += (r.nextDouble()*50)-25;
    
    properties.put("Temperature", (int)temperature+"");
    properties.put("TUnit", "k");
    
    double relativeSizeMedian = sizeRamp.getValue(dist/CSun.getRawSize(sunTemp));
    double deviation = gaussRamp.getValue(r.nextDouble())*0.2;
    int maxSize = (int)Math.min(MAX_SIZE, Double.parseDouble(sun.properties.get("Size"))*20000+MIN_SIZE);
    int size = (int)((deviation+relativeSizeMedian)*maxSize)+MIN_SIZE;
    if(size<0)size*=-1;
    properties.put("Size", size+"");
    properties.put("SUnit", "km");
    
    return deviation+relativeSizeMedian;
  }
  
  protected void buildType(Random r, double atmoThresh){
    properties.remove("Type");
    properties.remove("Atmosphere");
    
    int size = Integer.parseInt(properties.get("Size"));
    double temperature = Double.parseDouble(properties.get("Temperature"));
    
    
    String type;
    boolean atmo=false;

    if(size>THRESH_HOT_SIZE){
      type="Hot Gasgiant";
      atmo=true;
    }else if(size>THRESH_GAS_SIZE){
      type="Gasgiant";
      atmo=true;
    }else{
      if(temperature>THRESH_LAVA*1.5){
        type="Molten";
      }else if(temperature>THRESH_LAVA){
        if(r.nextDouble()<0.65){
          type="Molten";
        }else{
          type="Crust";
        }
      }else if(temperature<THRESH_SOLID){
        if(r.nextDouble()<0.65){
          type="Solid";
        }else{
          type="Crust";
        }
      }else{
        type="Crust";
      }
    }
    properties.put("Type", type);
    
    if(!atmo){
      //double atmoThresh = deviation+relativeSizeMedian;
      if(r.nextDouble()<atmoThresh/*+0.15*/){
        atmo=true;
      }
    }
    properties.put("Atmosphere", atmo+"");
  }
  
  protected void buildWater(Random r, CBody sun){
    properties.remove("Fluid");
    boolean atmo = Boolean.parseBoolean(properties.get("Atmosphere"));
    
    String type = properties.get("Type");
    double dist = Math.hypot(position.x, position.y);
    int sunTemp=Integer.parseInt(sun.properties.get("Temperature"));
    
    String fluid = "none";
    double fluidChance = CSun.fFluid(dist,sunTemp);
    
    double pTemp = Double.parseDouble(properties.get("Temperature"));
    
    //fluidChance += (1-fluidChance)*2;
    if(atmo && r.nextDouble()<fluidChance && (type.equals("Crust")|| type.equals("Solid")) && pTemp < 370){
      //fluid=true;
      double fA = r.nextDouble();
      if(fA<1/8f){
        fluid = "high";
      }else if(fA<1/3f){
        fluid = "medium";
      }else{
        fluid="low";
      }
    }
    properties.put("Fluid", fluid+"");
  }
  
  protected void buildDetails(Random r){
    properties.remove("Moons");
    properties.remove("Habitable");
    properties.remove("Ring");
    
    //String type = properties.get("Type");
    double temperature = Double.parseDouble(properties.get("Temperature"));
    String fluid = properties.get("Fluid");
    int size = Integer.parseInt(properties.get("Size"));
    boolean atmo = Boolean.parseBoolean(properties.get("Atmosphere"));
    
    String t;
    if(temperature>240){
      t="Fluid";
    }else{
      t="Solid";
    }
    properties.put("FState", t);
    
    boolean rings=false;
    if(r.nextDouble()<0.15){
      rings=true;
    }
    properties.put("Ring", rings+"");
    
    boolean livable=false;
    if(!fluid.equals("none") && atmo){
      if(temperature > 200 && temperature < 385)
      livable=true;
    }
    properties.put("Habitable", livable+"");
    
    //max size = 8 moons
    //min size = .75 chance on one moon
    
    double moonCount = size-MIN_SIZE;
    
    ValueRamp mr = new ValueRamp();
    
    mr.changeEnds(.85, 5);
    mr.addHandle(.1, 3);
    //mr.addHandle(.5, 4);
    
    moonCount = moonCount / MAX_SIZE;
    moonCount = mr.getValue(moonCount);
    //moonCount+=.85;
    
    properties.put("Moons", (int)moonCount+"");
  }
  
  protected void buildLayers(Random r){
    String type = properties.get("Type");
    String fluid = properties.get("Fluid");
    int size = Integer.parseInt(properties.get("Size"));
    boolean atmo = Boolean.parseBoolean(properties.get("Atmosphere"));
    
    String[] matPointers = new String[3];
    if(atmo){
      matPointers[0]=Compound.fillCompound(atmoComp, gasMatTypes, r);
    }
    if(!type.equals("Gasgiant") && !type.equals("Hot Gasgiant")){
      matPointers[1]=Compound.fillCompound(crustComp, solidMatTypes, r);
      properties.put("Main Component", matPointers[1]);
      Compound.fillCompound(coreComp, solidMatTypes, r);
      matPointers[2]=fluid;
    }else{
      properties.put("Main Component", matPointers[0]);
      matPointers[2]="none";
    }

    int aggPointer = 0;
    int smPointer = 0;
    int atmPointer = -1;
    int fldPointer = -1;
    
    String prop = properties.get("Type");
    if(prop.equals("Molten")){
      aggPointer=0;
    }else if(prop.equals("Crust") || prop.equals("Solid")){
      aggPointer=1;
    }else{
      aggPointer=2;
    }
    
    if(aggPointer==0){
      smPointer=0;
    }else if(aggPointer==1){
      if(matPointers[1].equals("Rock")){
        smPointer=1;
      }else if(matPointers[1].equals("Metal")){
        smPointer=2;
      }else if(matPointers[1].equals("Mineral")){
        smPointer=3;
      }else if(matPointers[1].equals("Precious Metal")){
        smPointer=4;
      }
    }else{
      if(matPointers[0].equals("Oxygen")){
        smPointer=0;
        atmPointer=0;
      }else if(matPointers[0].equals("Helium")){
        smPointer=1;
        atmPointer=1;
      }else if(matPointers[0].equals("Hydrogen")){
        smPointer=2;
        atmPointer=2;
      }
    }

    if(properties.get("Atmosphere").equals("true")){
      properties.put("AType", matPointers[0]);
      if(matPointers[0].equals("Oxygen")){
        atmPointer=0;
      }else if(matPointers[0].equals("Helium")){
        atmPointer=1;
      }else if(matPointers[0].equals("Hydrogen")){
        atmPointer=2;
      }
    }else{
      properties.put("AType", "None");
    }
    
    if(!matPointers[2].equals("none") && properties.get("FState").equals("Solid")){
      fldPointer=3;
    }else if(matPointers[2].equals("high")){
      fldPointer=2;
    }else if(matPointers[2].equals("medium")){
      fldPointer=1;
    }else if(matPointers[2].equals("low")){
      fldPointer=0;
    }
    
    Image i;
    Affine a;
    float correction;
    float translate;
    
    if(aggPointer != 2){
      i = ImageManager.getImage(ImageManager.OFFS_PLNT+smPointer);
    }else{
      i = ImageManager.getImage(ImageManager.OFFS_GAS+smPointer);
    }
    
        a = new Affine();
        correction = 1f/i.getWidth();  
        a.scale = correction;
        a.rotation = r.nextDouble()*Math.PI*2;
        scale = size;
        //how much it must be shifted due to scaling, so that its in the center again
        translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_NORMAL);
        addLayer(i, a);
     
        
        if(fldPointer>=0){
          i = ImageManager.getImage(ImageManager.OFFS_FLD+fldPointer);
          a = new Affine();
            correction = 1f/i.getWidth();  
            a.scale = correction;
            a.rotation = r.nextDouble()*Math.PI*2;
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_NORMAL);
            addLayer(i, a);
            
            
          i = ImageManager.getImage(ImageManager.OFFS_CLDS);
          a = new Affine();
            correction = 1f/i.getWidth();  
            a.scale = correction;
            a.rotation = r.nextDouble()*Math.PI*2;
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_NORMAL);
            addLayer(i, a);
        }
        
        //without an atmosphere, there's a black fringe
        if(atmPointer>=0){
          i = ImageManager.getImage(ImageManager.OFFS_ATMO+atmPointer);
          a = new Affine();
            correction = 1f/i.getWidth();  
            a.scale = correction;
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_TRUE_ADD);
            addLayer(i, a);
        }
        
      i = ImageManager.getImage(ImageManager.OFFS_SHDW);
      a = new Affine();
        correction = 1f/i.getWidth();  
        a.scale = correction;
        a.rotation = Math.atan2(position.y, position.x);
        translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
        a.translation.add(new Vec2D(translate,translate));
        a.setMode(GL_Blender.MODE_TRUE_MULTIPLY);
        addLayer(i, a);
        
        int rIdx = 1;
        
        if(properties.get("Type").equals("Molten")){
          i = ImageManager.getImage(ImageManager.OFFS_MGMA);
          a = new Affine();
            correction = 1f/i.getWidth();  
            a.scale = correction;
            a.rotation = r.nextDouble()*Math.PI*2;
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_TRUE_ADD);
            addLayer(i, a);
            rIdx=0;
        }else if (properties.get("Type").equals("Hot Gasgiant")){
          i = ImageManager.getImage(ImageManager.OFFS_HGAS);
          a = new Affine();
            correction = 1f/i.getWidth();  
            a.scale = correction;
            a.rotation = r.nextDouble()*Math.PI*2;
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_TRUE_ADD);
            addLayer(i, a);
            rIdx=0;
        }
        
        if(properties.get("Ring").equals("true")){
          double diameter = r.nextDouble()+2;
        //diameter*=Double.parseDouble(centerMass.properties.get("Size"));
        
          rIdx = Math.max(rIdx, smPointer);
          
        i = ImageManager.getImage(ImageManager.OFFS_RING+rIdx);
            a = new Affine();
            correction = 1f/i.getWidth(); 
            correction*=diameter;
            a.scale = correction;//*diameter;
            a.rotation = Math.atan2(position.y, position.x);
            //scale = (float)diameter;
            //how much it must be shifted due to scaling, so that its in the center again
            translate = (i.getWidth()/2)-(i.getWidth()/2 * correction);
            a.translation.add(new Vec2D(translate,translate));
            a.setMode(GL_Blender.MODE_NORMAL);
            addLayer(i, a);
        }
        
        minScale = 1;//applies to rasterAffine, so already 1px convenient
        maxScale = 1000000;//To ensure its not to big for openGL
  }
  
  protected void clearAllInfo(){
    minScale = 0;
    maxScale = Float.POSITIVE_INFINITY;
    
    layer.clear();
    layerAffine.clear();
    rasterable = false;
    
    properties.clear();
  }
  
  public HashMap<String, Double> getAtmoComp(){
    return atmoComp.getComponents();
  }
  
  public HashMap<String, Double> getCrustComp(){
    return crustComp.getComponents();
  }
  
  public HashMap<String, Double> getCoreComp(){
    return coreComp.getComponents();
  }

}
