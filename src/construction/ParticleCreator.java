package construction;

import gui.ImageManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;

import drawable.Affine;
import drawable.ParticleDraw;
import drawable.V2DPolygon;

public class ParticleCreator {

  private LinkedList<ParticleEntry> systems;
  private PhMaterial dummyMat;
  private LinkedList<Vec2D> dummyList;
  
  public ParticleCreator(){
    systems = new LinkedList<>();
    dummyMat = new PhMaterial();
    dummyList = new LinkedList<>();
    
    dummyList.add(new Vec2D( .5, -.5));
    dummyList.add(new Vec2D(-.5, -.5));
    dummyList.add(new Vec2D(-.5,  .5));
    dummyList.add(new Vec2D( .5,  .5));
  }
  
  public void addSystem(ParticleEntry pe){
    if(pe != null){
      ParticleEntry ent = pe.getCopy();
      ent.touch();
      systems.add(ent);
    }
  }
  
  public void addAllSystems(LinkedList<ParticleEntry> pel){
    for(ParticleEntry pe: pel){
      ParticleEntry ent = pe.getCopy();
      ent.touch();
      systems.add(ent);
    }
  }
  
  public void abortSystems(long unqID){
    LinkedList<ParticleEntry> removed = new LinkedList<>();
    for(ParticleEntry pe : systems){
      if(pe.pUnqID == unqID) removed.add(pe);
    }
    systems.removeAll(removed);
  }
  
  public LinkedList<PhPoly> generate(){
    long now = System.currentTimeMillis();
    Random r = new Random();
    r.setSeed(now);
    
    LinkedList<PhPoly> ret = new LinkedList<>();
    LinkedList<ParticleEntry> empty = new LinkedList<>();
    for(ParticleEntry pe : systems){
      long gap = now - pe.lastTouch;
      pe.lastTouch = now;
      
      double amt = (gap/1000.0)*pe.density;
      if(amt < 1){
        if (r.nextDouble() < amt) amt = 1;
        else amt = 0;
      }
      
      for(int i=0;i<amt;i++){
        
        //new center
        Vec2D c = pe.startPos.getCopy();
        Vec2D l = pe.direction.getCopy();
        l.multiply(r.nextDouble());
        c.add(l);
        Vec2D rF = new Vec2D(r.nextDouble(),r.nextDouble());
        rF.multiply(pe.posRand);
        c.add(rF);
        c.x-=pe.posRand/2;
        c.y-=pe.posRand/2;
        
        //lifetime
        double lt = pe.lifeTime;
        lt += (r.nextDouble()*pe.lifeRand)*pe.lifeTime;
        lt -= (pe.lifeRand*pe.lifeTime)/2;
        
        //particle rasterable
        ParticleDraw pr = new ParticleDraw(c,lt,pe.lifeVR);
        Image img = pe.img;
        Affine f = new Affine();
        f.scale = 1.0/img.getWidth();
        f.scale *= pe.scale;
        double std = f.scale;
        double dev = r.nextDouble()*pe.sclRand*std;
        f.scale += dev;
        f.scale -= (pe.sclRand*std)/2;
        f.mode = pe.drawMode;
        pr.addLayer(img, f);
        pr.rasterAffine.alpha = pe.alpha;
        
        //dummy bounds
        V2DPolygon bnds = new V2DPolygon(dummyList,c,Color.black,"");
        
        //ph obj
        PhPoly pp = new PhPoly(c,bnds,pr,0,1,0,dummyMat, 0);
        
        //movement
        double angle = pe.angle;
        angle += r.nextDouble()*pe.anglRand;
        angle -= pe.anglRand/2;
        double speed = pe.speed;
        speed += r.nextDouble()*pe.speed*pe.spdRand;
        speed -= (pe.speed*pe.spdRand)/2;
        Vec2D dir = new Vec2D(Math.cos(angle), -Math.sin(angle));
        if(speed == 0) speed = Double.MIN_NORMAL*1000;
        dir.multiply(speed);
        pp.setVelocity(dir);

        //rotation
        double torque = pe.rot;
        double fac = r.nextDouble()*pe.rot*pe.rotRand;
        torque += fac;
        torque -= (pe.rot*pe.rotRand)/2;
        torque*= Math.signum(r.nextDouble()-.5);
        //init rotation because of movement
        pp.setRotation(Math.PI/2+Math.atan2(dir.x, dir.y));
        //torque by system settings
        pp.applyTorque(torque);
        
        ret.add(pp);
      }
      
      if(now-pe.emitTime > pe.timeStarted) empty.add(pe);
    }
    
    systems.removeAll(empty);
    return ret;
  }
  
  public static LinkedList<ParticleEntry> loadEffect(String fn){
    try (BufferedReader br = new BufferedReader(new FileReader(fn));){
        if(fn.equals("assets/fx/particle/"))throw new IOException();
      LinkedList<ParticleEntry> effect = new LinkedList<>();
      
      String line = br.readLine();
      
      ValueRamp vr;
      Vec2D center, lineVector;
      double pRand, density, alpha, speed, sRand, angle, aRand, rotation, rRand, size, szRand, emitTime, etRand, lifeTime, ltRand;
      int imageID, blendMode;
      String name;
      
      while(line != null){
        //line with content entry
        
        name = line.split("=")[1];
        line = br.readLine();
        //line with vr entry
        vr = new ValueRamp();
        double s,e;
        s = Double.parseDouble(br.readLine().split("=")[1]);
        e = Double.parseDouble(br.readLine().split("=")[1]);
        vr.changeEnds(s, e);
        line = br.readLine();
        //line with custom handles
        while(!line.contains("center")){
          double p,v;
          p = Double.parseDouble(line.split("=")[0]);
          v = Double.parseDouble(line.split("=")[1]);
          vr.addHandle(p, v);
          line = br.readLine();
        }
        s = Double.parseDouble(br.readLine().split("=")[1]);
        e = Double.parseDouble(br.readLine().split("=")[1]);
        center = new Vec2D(s,e);
        line = br.readLine();
        line = br.readLine();
        //line with lineVec x value
        s = Double.parseDouble(line.split("=")[1]);
        line = br.readLine();
        e = Double.parseDouble(line.split("=")[1]);
        lineVector = new Vec2D(s,e);
        
        pRand     = Double.parseDouble(br.readLine().split("=")[1]);
        density   = Double.parseDouble(br.readLine().split("=")[1]);
        alpha     = Double.parseDouble(br.readLine().split("=")[1]);
        imageID   = Integer.parseInt(br.readLine().split("=")[1]);
        speed     = Double.parseDouble(br.readLine().split("=")[1]);
        sRand     = Double.parseDouble(br.readLine().split("=")[1]);
        angle     = Double.parseDouble(br.readLine().split("=")[1]);
        aRand     = Double.parseDouble(br.readLine().split("=")[1]);
        rotation  = Double.parseDouble(br.readLine().split("=")[1]);
        rRand     = Double.parseDouble(br.readLine().split("=")[1]);
        size      = Double.parseDouble(br.readLine().split("=")[1]);
        szRand    = Double.parseDouble(br.readLine().split("=")[1]);
        emitTime  = Double.parseDouble(br.readLine().split("=")[1]);
        etRand    = Double.parseDouble(br.readLine().split("=")[1]);
        lifeTime  = Double.parseDouble(br.readLine().split("=")[1]);
        ltRand    = Double.parseDouble(br.readLine().split("=")[1]);
        blendMode = Integer.parseInt(br.readLine().split("=")[1]);
        
        Image img = ImageManager.getImage(imageID);
        
        effect.add(new ParticleEntry(name, vr, center, lineVector, pRand, density, alpha, img, speed, sRand, angle, aRand,
                       rotation, rRand, size, szRand, emitTime, etRand, lifeTime, ltRand, blendMode, 0));
        line = br.readLine();
      }
      br.close();
      return effect;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  
  public static boolean saveEffect(LinkedList<ParticleEntry> pe, String fn){
    System.out.println("lolololo saving @"+fn+"  with img="+pe.get(0).img.getResourceReference());
    
    try (BufferedWriter bw = new BufferedWriter(new FileWriter("assets/fx/particle/"+fn));){
      
      for(ParticleEntry p : pe){
        System.out.println("entrey "+p);
        bw.write("entry="+p.name); bw.newLine();
        bw.write("vr=;"); bw.newLine();
        
        //because of change ends, they have to be written first
        bw.write("0.0="+p.lifeVR.getValue(0)); bw.newLine();
        bw.write("1.0="+p.lifeVR.getValue(1)); bw.newLine();
        
        Iterator<Vec2D> it = p.lifeVR.getHandles().iterator();
        Vec2D v = null;
        it.next();
        
        for(int i = 1; i< p.lifeVR.getHandles().size() -1; i++){
          v = it.next();
          bw.write(v.x + "=" + v.y); bw.newLine();
        }
        
        bw.write("center=;"); bw.newLine();
        bw.write("x=" + p.startPos.x); bw.newLine();
        bw.write("y=" + p.startPos.y); bw.newLine();
        
        bw.write("lineVector=;"); bw.newLine();
        bw.write("x=" + p.direction.x); bw.newLine();
        bw.write("y=" + p.direction.y); bw.newLine();
        
        bw.write("pRand="+p.posRand); bw.newLine();
        bw.write("density="+p.density); bw.newLine();
        bw.write("alpha="+p.alpha); bw.newLine();
        int imgID = ImageManager.getImageIndex(p.img.getResourceReference());
        bw.write("image="+imgID); bw.newLine();
        bw.write("speed="+p.speed); bw.newLine();
        bw.write("sRand="+p.spdRand); bw.newLine();
        bw.write("angle="+p.angle); bw.newLine();
        bw.write("aRand="+p.anglRand); bw.newLine();
        bw.write("rotation="+p.rot); bw.newLine();
        bw.write("rRand="+p.rotRand); bw.newLine();
        bw.write("size="+p.scale); bw.newLine();
        bw.write("szRand="+p.sclRand); bw.newLine();
        bw.write("emitTime="+p.emitTime); bw.newLine();
        bw.write("etRand="+p.emtRand); bw.newLine();
        bw.write("lifeTime="+p.lifeTime); bw.newLine();
        bw.write("ltRand="+p.lifeRand); bw.newLine();
        bw.write("blendMode="+p.drawMode); bw.newLine();
      }
      
      bw.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    
    return true;
  }
}
