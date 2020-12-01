package view;

import java.util.LinkedList;
import java.util.Map.Entry;

import gui.GL_Blender;
import gui.Def;
import gui.ImageManager;
import gui.UIButton;
import gui.UITheme;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import construction.ParticleCreator;
import construction.ParticleEntry;
import construction.PhMaterial;
import construction.PhPoly;
import construction.PhRay;
import construction.PhysicsManager;
import construction.UniqueID;
import construction.ValueRamp;
import construction.Vec2D;
import drawable.Affine;
import drawable.Rasterable;
import drawable.Rayable;
import drawable.V2DPolygon;

public class BattleView extends AbstractView{

  PhysicsManager phm;
  LinkedList<PhPoly> pbItems;
  PhRay pray;
  
  PhPoly ship;
  
  public BattleView(GameContainer gc) {
    super(gc);       // 100.000
    phm = new PhysicsManager(100000);
    MAX = 100000;
    
    maxZF = 5;
    minZF = .2;
    this.zStep = 1.1f;
    
    int cnt = 150;
    PhMaterial mat = new PhMaterial(PhMaterial.DMGT_DFLT, PhMaterial.HLLT_DFLT, 2, 15);
    ValueRamp vr = new ValueRamp();
    ValueRamp vr2 = new ValueRamp();
    vr.changeEnds(0, 0);
    vr.addHandle(.1, 1);
    
    vr2.changeEnds(0, 0);
    vr2.addHandle(.1, 1);
    vr2.addHandle(.8, .5);
    
    LinkedList<ParticleEntry> expl = ParticleCreator.loadEffect("assets/fx/particle/explosion.pcl");
    LinkedList<ParticleEntry> fnc = ParticleCreator.loadEffect("assets/fx/particle/collision.pcl");
    LinkedList<ParticleEntry> shld = ParticleCreator.loadEffect("assets/fx/particle/shield.pcl");
    
    Color c = new Color(1f,0f,0f,.2f);
    
    for(int i=0;i<cnt;i++){
      Vec2D center = new Vec2D(500+(i/45)*20,(50+20*i)%900);
      center.subtract(new Vec2D(gc.getWidth()/2,gc.getHeight()/2));
      Rasterable r = new Rasterable(center);
      Affine f = new Affine();
      f.scale = 10/512f;
      r.addLayer(ImageManager.getImage(ImageManager.OFFS_PLNT), f);
      
      LinkedList<Vec2D> ll = new LinkedList<>();
      ll.add(new Vec2D(-5, -5));
      ll.add(new Vec2D(-5, +5));
      ll.add(new Vec2D(+5, +5));
      ll.add(new Vec2D(+5, -5));
      
      V2DPolygon b = new V2DPolygon(ll,center,c,"boundie");
      PhPoly ob = new PhPoly(center, b, r, PhysicsManager.LYR_DBRS, 200, 0, mat, UniqueID.newID());
      phm.addPoly(ob);
      ob.applyForce(new Vec2D(500,Math.random()));
      ob.applyTorque(10);
      
      for(ParticleEntry pen : expl){
        pen.pUnqID = ob.pUnqID;
        ob.pDeathList.add(pen.getCopy());
      }for(ParticleEntry pen : fnc){
        pen.pUnqID = ob.pUnqID;
        ob.pCollList.add(pen.getCopy());
      }
    }
    c = new Color(0f,0f,1f,.2f);
    for(int i=0;i<cnt;i++){
      Vec2D center = new Vec2D(1500+(i/45)*20,(50+20*i)%900);
      center.subtract(new Vec2D(gc.getWidth()/2,gc.getHeight()/2));
      Rasterable r = new Rasterable(center);
      Affine f = new Affine();
      f.scale = 10/512f;
      r.addLayer(ImageManager.getImage(ImageManager.OFFS_PLNT), f);
      
      LinkedList<Vec2D> ll = new LinkedList<>();
      ll.add(new Vec2D(-5, -5));
      ll.add(new Vec2D(-5, +5));
      ll.add(new Vec2D(+5, +5));
      ll.add(new Vec2D(+5, -5));
      
      V2DPolygon b = new V2DPolygon(ll,center,c,"boundie");
      PhPoly ob = new PhPoly(center, b, r, PhysicsManager.LYR_DBRS, 200, 0, mat, UniqueID.newID());
      phm.addPoly(ob);
      ob.applyForce(new Vec2D(-500,Math.random()));
      ob.applyTorque(10);
      
      for(ParticleEntry pen : expl){
        pen.pUnqID = ob.pUnqID;
        ob.pDeathList.add(pen.getCopy());
      }for(ParticleEntry pen : fnc){
        pen.pUnqID = ob.pUnqID;
        ob.pCollList.add(pen.getCopy());
      }
    }
    c = new Color(0f,1f,0f,.2f);
    Vec2D center = new Vec2D(000,0);
    Rasterable r = new Rasterable(center);
    Affine f = new Affine();
    f.scale=512/1024f;
    f.mode = GL_Blender.MODE_TRUE_ADD;
    r.addLayer(ImageManager.getImage("assets/chassis/test.png"), f);
    
    LinkedList<Vec2D> ll = new LinkedList<>();
    for(int i=0;i<16;i++){
      double ro = (Math.PI*2)/16;
      ro*=i;
      ll.add(new Vec2D(Math.cos(ro)*265,Math.sin(ro)*265));
    }
    
    V2DPolygon b = new V2DPolygon(ll,center,c,"shieldie");
    PhPoly ob = new PhPoly(center, b, r, PhysicsManager.LYR_DBRS, 10000000, 0, mat,UniqueID.newID());
    ship = ob;
    
    phm.addPoly(ob);
    //ob.applyForce(new Vec2D(0,0));
    ob.applyTorque(1000);
    for(ParticleEntry pen : shld){
      pen.pUnqID = ob.pUnqID;
      ob.pCollList.add(pen.getCopy());
    }
    
    
    Rayable rbl = new Rayable(new Vec2D(350,250));
    f = new Affine();
    f.scale = 15/32f;
    f.mode = GL_Blender.MODE_TRUE_ADD;
    rbl.addLayer(ImageManager.getImage("assets/fx/lsr_tst.png"), f);
    
    pray = new PhRay(new Vec2D(350,350),1500,Math.PI/4,PhMaterial.DMGT_LSR, 25, PhysicsManager.LYR_DBRS, rbl, UniqueID.newID());
    //phm.addRay(pray);
    
    children.add(new UIButton("To Menu","Exit to Menu", gc.getWidth()-200,gc.getHeight()/2,UITheme.font.getWidth("To Menu")+10,20,Def.A_ENTER_MAIN_MENU, null));
    
    phm.start();
  }
  
  @Override
  public void update(int delta) {
    //super.update(delta);
    pray.rotate(.0003*delta);
    pray.translate(new Vec2D(-.03*delta,0));
    
    
    Vec2D v = new Vec2D(10000, 0);
    double rotation = ship.getRotation()-Math.PI/2;
    rotation *= -1;

    float tmpx = (float)(v.x*Math.cos(rotation)+v.y*Math.sin(rotation));
    float tmpy = (float)(v.x*Math.sin(rotation)-v.y*Math.cos(rotation));
    v = new Vec2D(tmpx, tmpy);
    v.multiply(-1);
    if(activeKeys.contains(Input.KEY_W)) ship.applyForce(v);
    if(activeKeys.contains(Input.KEY_S)) {
      v.multiply(-1);
      ship.applyForce(v);
    }
    if(activeKeys.contains(Input.KEY_A)) ship.applyTorque(50);
    if(activeKeys.contains(Input.KEY_D)) ship.applyTorque(-50);
    
    v = ship.getVelocity();
    v.multiply(0.999);
    
    ship.setVelocity(v);
    ship.setTorque(ship.getTorque()*0.999);
  }
  
  @Override
  public void render(@SuppressWarnings("hiding") GameContainer gc){
    Graphics gx = gc.getGraphics();
    
    pbItems = phm.getPolyList();
    rasterItems.clear();
    for(PhPoly pb : pbItems){
      rasterItems.add(pb.getEffigy());
    }for(PhRay pr : phm.getRayList()){
      rasterItems.add(pr.getEffigy());
    }
    
    renderRasterItems(gx);
    
    //DEBUG OUTLINES//
    
  /*  gx.setColor(Color.white);
    LinkedList<Line> lines = null;
    //pathItems = pm.getPathItems();
    eb.setDrawMode(ExtendedBlender.MODE_NORMAL);
    for(Pathable cgt: pathItems){
      lines = cgt.getTransformedLines(focus, new Vec2D(gc.getWidth(), gc.getHeight()),zFactor);
      for(Line l: lines){
        gx.setColor(l.getColor());
        gx.drawLine((float)l.getPoints()[0].x, (float)l.getPoints()[0].y, (float)l.getPoints()[1].x, (float)l.getPoints()[1].y);
      }
    }
    
    //DEBUG RAYS//
    
    gx.setColor(Color.pink);
    for(PhRay pray: phm.getRayList()){
      Vec2D[] pts = pray.getLine().getPoints();
      Vec2D.viewTransform(pts[0], focus, new Vec2D(gc.getWidth(), gc.getHeight()), zFactor);
      Vec2D.viewTransform(pts[1], focus, new Vec2D(gc.getWidth(), gc.getHeight()), zFactor);
      
      Vec2D len = pts[1].getCopy();
      len.subtract(pts[0]);
      len.multiply(pray.getCoef());
      len.add(pts[0]);
      
      gx.drawLine((int)pts[0].x, (int)pts[0].y, (int)len.x, (int)len.y);
    }*/

    renderUI(gx);
  }
  
  @Override
  protected void renderRasterItems(Graphics gx){
    pathItems.clear();
    
    for(PhPoly pb : pbItems){
      pb.applyExtrapolation();
      pathItems.add(pb.getShape());
    }
    super.renderRasterItems(gx);
  }
  

  @Override
  public void finalize(){
    phm.stop();
  }
  
  @Override
  protected Entry<Integer, Object>proc_K_PRS(long timeStamp, int event, int[] data, Entry<Integer, Object> response){

    switch(data[0]){
    case Input.KEY_W:
    case Input.KEY_S:
    case Input.KEY_A:
    case Input.KEY_D:
      activeKeys.add(data[0]);
      break;
    }
    return response;
  }
  

}
