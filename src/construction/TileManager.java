package construction;

import gui.GL_Blender;
import gui.ImageManager;
import gui.UITheme;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

import org.newdawn.slick.Image;

import drawable.Affine;
import drawable.CBlackHole;
import drawable.Pathable;
import drawable.Rasterable;

public class TileManager {
  
  class ManagerThread extends Thread{

    @Override
    public void run(){
      System.out.println(Thread.currentThread().getName()+"[Worker] started");
      updateCycle();
    }
    
    @Override
    public void finalize(){
      System.out.println(Thread.currentThread().getName()+"[Worker] being finalized");
    }
  }
  
  private final int maxGen;
  private final int mSD; //screen dimension thingy
  private final int baseDensity;
  private final int rootTileSize;

  public double zFactor;
  private Vec2D focus;
  private Vec2D screenDim;
  
  private int gen; //actual generenation
  private long seed;
  private double actTileSize;
  private double rootNoiseSize;
  private boolean update;
  private boolean addSel;
  
  private Boolean updateFlag=false;
  private Boolean selectionFlag=false;
  private Rectangle2D.Double selRect;
  
  private Vec2D dimension;
  private ReentrantLock defaultLock;
  private ReentrantLock selectionLock;
  private CloudsNoise cNoise;
  
  private Random rand;
  
  private LinkedList<Tile> activeTiles;
  private LinkedList<Rasterable>displayItems;
  private LinkedList<Rasterable>selectedItems;
  private LinkedList<CGTile>cigriItems;
  
  private ManagerThread mThread;
  private CircularGrid cigri;
  private Rasterable backGround;
  private CBlackHole bh;
  
  public TileManager(long s, long max, Vec2D dim, int density){
    defaultLock = new ReentrantLock();
    selectionLock = new ReentrantLock();
      
    ValueRamp vr = new ValueRamp();
    vr.addHandle(0.25,0.2);
    vr.addHandle(0.75,0.9);
    rand= new Random();
    rand.setSeed(s);
    double r = rand.nextDouble();
    Image tmp = ImageManager.getImage(ImageManager.OFFS_GLXY_D+(int)(r*ImageManager.SIZE_GLXY_D));
    BufferedImage bi = ImageManager.convert(tmp, false);
    //cNoise = new CloudsNoise(im.getImage(ImageManager.OFFS_GLXY_D+(int)(r*ImageManager.SIZE_GLXY_D)), max, CloudsNoise.TYPE_BILINEAR, im);
    cNoise = new CloudsNoise(bi, max, CloudsNoise.TYPE_BICUBIC);
    
    cNoise.setValueRamp(vr);
    mThread = new ManagerThread();
    
    screenDim = dim;
    
    activeTiles = new LinkedList<>();
    cigriItems = new LinkedList<>();
    displayItems = new LinkedList<>();
    selectedItems= new LinkedList<>();

    zFactor=1;
    focus=new Vec2D(0,0);
    gen=0;
    
    dimension = dim.getCopy();
    //mSD = (int)Math.min(dimension.x-50, dimension.y-50);
    mSD=550;
    
    baseDensity= density;
    
    seed=s;
    update=true;
    
    rootTileSize = (int)(max / 1000);
    rootNoiseSize=(int)(rootTileSize/4.0);
    
    actTileSize=rootTileSize;
    addSel=false;
    
    maxGen=10;
              //radius, not diameter(texSize)
    cigri = new CircularGrid((1024*1024)/2, UITheme.dark_s);
    
    backGround = new Rasterable(new Vec2D(0,0));
    Affine a = new Affine();
    a.scale=1024;
    a.setMode(GL_Blender.MODE_TRUE_ADD);
    Image img = ImageManager.getImage(ImageManager.OFFS_GLXY+(int)(r*ImageManager.SIZE_GLXY));
    img.setAlpha(.35f);
    backGround.addLayer(img, a);
    backGround.maxScale=1024;

    bh = new CBlackHole(seed, new Vec2D(0,0),new TiledID("0000|0000:0000"));
    
    mThread.start();
  }
  
  @Override
  public void finalize(){
    System.out.println("finalized the tm, update is "+update);
  }
  
  public void shutDown(){
    update=false;
    mThread.interrupt();
    System.out.println("shutdown the tm, update is "+update);
  }
  
  
  public void translate(double factor,double x, double y){
    if(defaultLock.tryLock()){
      if(focus.x!=x || focus.y!=y || zFactor!=factor){
        focus.x=x;
        focus.y=y;
        zFactor=factor;
        synchronized(activeTiles){
          activeTiles.notifyAll();
        }
        synchronized(updateFlag){
          updateFlag=true;
        }
      }
      defaultLock.unlock();
    }
  }
  
  public void setSelection(Rectangle r, boolean add){
    if(selectionLock.tryLock()){
      selRect=new Rectangle2D.Double(r.getX(),r.getY(),r.getWidth(),r.getHeight());      
      synchronized(selectionFlag){
        selectionFlag=true;
        addSel=add;
      }
      synchronized(activeTiles){
        activeTiles.notifyAll();
      }
      selectionLock.unlock();
    }
  }
  
  private void updateCycle(){
    while(update){
      try{
        //methods are self-explanatory
        if(updateFlag){
          synchronized(updateFlag){
            updateFlag=false;
          }
          defaultLock.lock();
          checkGen();
          addTilesAfterMovement();
          removeUnneededTiles();
          fillRasterItems();
          updateCigri();
          defaultLock.unlock();
        }
        
        if(selectionFlag){
          synchronized(selectionFlag){
            selectionFlag=false;
          }
          selectionLock.lock();
          updateSelectionList();
          selectionLock.unlock();
        }
        Thread.sleep(100);
        if(!updateFlag && !selectionFlag){
          synchronized(activeTiles){
            activeTiles.wait();
          }
        }
      } catch (InterruptedException exc){
        System.out.println("Manager interrupted in update method");
      }
    }
    System.out.println("worker out of cycle");
  }
  
  private void checkGen(){
    //see if displayedTiles should be split or merged, threshold is minScreenDimension
    //g < 10 to ensure a really zoomed in feeling leaving out the last gen
    //System.out.println(g+" at: zF: "+zFactor+"   and actTS: "+actTileSize+"    should mean: "+actTileSize*zFactor+"   MAX:"+MAX);
    while(actTileSize*zFactor > mSD && gen < maxGen){
      gen++;
      actTileSize/=4;
    }
    while((actTileSize*4)*zFactor < mSD){
      gen--;
      actTileSize*=4;
    }
    
  }
  
  private void addTilesAfterMovement(){
    //translate screen coordinates into game coordinates
    final double xMin = focus.x-((dimension.x/2)/zFactor);
    final double yMin = focus.y-((dimension.y/2)/zFactor);
    
    double x = xMin;
    double y = yMin;
    double tmpY=y;
    
    //plus tileSize or otherwise the most right and bottom rows won't be created
    final double xMax = x+(dimension.x/zFactor)+actTileSize;
    final double yMax = y+(dimension.y/zFactor)+actTileSize;
    
    //get the first pixel, see if a tile coveres it. if not, create
    //look for the next posible uncovered pixel, repead until screen is covered
    while(y < yMax){
      while(x < xMax){
        boolean covered=false;
        Tile container=null;
        int relativeG=gen;
        //go up while tile does not cover the point up to g=0, start with g-1
        //if(g==0)
          //calculate which root tile will contain the point, create
        //while actG<g
          //calculate in which tile the point falls, create if necessary, down to actG=G
        
        //go up while no covering tile is found
        while(!covered && relativeG>0){
          relativeG--;
          //is there a covering tile in this gen?
          for(Tile t:activeTiles){
            if(t.getGen()==relativeG && t.contains(x, y)){
              covered=true;
              container=t;
              break;
            }
          }
        }
        //if there's no root containing the point, create the corresponding root
        if(!covered){
          //get the starting coords for the containing Tile by clamping x
          //to only those vlaues, root tiles could be created
          double tileX=x/rootTileSize;
          tileX=Math.floor(tileX);
          tileX*=rootTileSize;
          
          double tileY=y/rootTileSize;
          tileY=Math.floor(tileY);
          tileY*=rootTileSize;
        
          int pX = (int)((tileX)/rootTileSize);
          int pY = (int)((tileY)/rootTileSize);
          //unique seed by concatenating the two coordinates
          String s1 = Math.abs(Math.round(tileX))+""+Math.abs(Math.round(tileY));
          long newS=0;
          try{
            newS = Long.parseLong(s1)+seed;
          }catch(NumberFormatException nfx){
            newS = Long.MAX_VALUE-rand.nextLong();
            System.out.println("Long was too long (lol ^^)");
          }
          
          rand.setSeed(newS);    
          TiledID tid=null;
          //int shift = (int)Math.ceil(MAX/initTileSize);
          int shift = 1000; //because initT is MAX/1000, shift is always 1000
          
          tid = new TiledID(pX+shift,pY+shift);
          Tile t = new Tile(rand.nextLong(), new Vec2D(tileX,tileY), rootTileSize, null, 0,
              new HashMap<Vec2D, Rasterable>(), new Point(pX,pY),tid);

          if(!activeTiles.contains(t)){
            activeTiles.add(t);
            container=t;
            
            cNoise.setOffset(container.getStart());                      //the very root tiles need some more density
            Double[][] tileData = cNoise.getPointsDistorted2D(container.getSize(), container.getSize(), baseDensity*40, container.getSeed(),
                                      rootNoiseSize, 1, 1.2);
            container.populate(tileData);
          }else{
            container=activeTiles.get(activeTiles.indexOf(t));
          }
        }
        //go down the tree, calculate the needed tile
        while(relativeG<gen){
          //calculate the tile that should contain the point
          double relX=x-container.getStart().x;
          double relY=y-container.getStart().y;
          
          int pInTX = (int)(relX/(container.getSize()/4));
          int pInTY = (int)(relY/(container.getSize()/4));
          
          //look if the desired tile is already present
          if(container.getSubTile(pInTX, pInTY)==null){
            //if not, create and populate
            try{
              container=container.createTile(pInTX, pInTY);

              cNoise.setOffset(container.getStart());
              Double[][] tileData = cNoise.getPointsDistorted2D(container.getSize(), container.getSize(), baseDensity, container.getSeed(),
                    rootNoiseSize/Math.pow(4, relativeG), 1, 1.2);

              container.populate(tileData);
              activeTiles.add(container);
            } catch (TileNotFilledException tnfx){
              System.out.println(tnfx.getMessage());
            }
          }else{
            //else just set the reference
            container=container.getSubTile(pInTX, pInTY);
          }
          relativeG++;    
        }
        x+=container.getSize();
        tmpY=container.getSize();
      }
      x=xMin;
      y+=tmpY;
    }
    //AND FINALLY YOU'RE DONE :D
  }
  
  private void removeUnneededTiles(){
    //remoceDuplicates();
    
    //1)get rid of all too deep subtiles (TileG>g, or just children of TileG>=g)
    //2)get rid of all tileG>g tileG=g tiles not in screen proximity
    //3)get rid of all tileG<g tiles not parents of displayed tiles (tileG=g)
    
    //4)3 can be ensured by 2: If the children is in proximity, so is the parent
    //  so modifying 2) to: get rid of all tiles not in screenProximity where tileG <= g+1
    //               or all children not in screenProximity where tileG < g+1
    
    
    //remember: you can't delete items of a list you're iteratung on, so remember which one
    //you want to delete afterwards
    LinkedList<Tile> forCollapse = new LinkedList<>();
    
    for(Tile t:activeTiles){
      if(t.getGen()>=gen){
        forCollapse.addAll(t.getAllocatedChildren());
        t.collapseAllChildren();  
      }
      if(t.getGen() < gen+1){
        LinkedList<Point> pointsForCollapse = new LinkedList<>();
        //determine if any of the children are eligible for deletion
        for(Tile candidate:t.getAllocatedChildren()){
          //eligible if they are out of screenBounds +- 1 tileSize
          // -(focus+(Res/2)/zFator represents the game coordinates of pixel 0,0 in screenspace
          
          double xMin = focus.x-((dimension.x/2)/zFactor)-actTileSize;
          double xMax = focus.x+((dimension.x/2)/zFactor)+actTileSize;
          
          double yMin = focus.y-((dimension.y/2)/zFactor)-actTileSize;
          double yMax = focus.y+((dimension.y/2)/zFactor)+actTileSize;
          
          if(candidate.getStart().x+candidate.getSize() < xMin || 
             candidate.getStart().x > xMax ||
             candidate.getStart().y+candidate.getSize() < yMin ||
             candidate.getStart().y > yMax){
            
            
            pointsForCollapse.add(candidate.getRelPos());
            forCollapse.add(candidate);
          }
        }
        for(Point p:pointsForCollapse){        
          t.collapseChild(p.x, p.y);
        }
      }
    }
    activeTiles.removeAll(forCollapse);
    forCollapse.clear();
  }
  
  private void updateSelectionList(){
    
    if(!addSel){
      selectedItems.clear();
    }
    addSel=false;
    //selectionCursors.clear();
    
    //translate screen-rect into gamespace-rect
    //focus-Res/2+coords
    Rectangle2D.Double tileRect;
    Rectangle2D.Double bodyBounds;
    
    //not needed with new multi-select
    //if the size is 100, it's been adjusted and meant for only 1 selection
    //boolean wasSingle =(selRect.height*selRect.width==18*18);
    
    for(Tile t:activeTiles){
      if(t.getGen()==gen){
        tileRect = t.getScreenBounds(focus, zFactor, screenDim);
        if (selRect.intersects(tileRect)){
          for(Rasterable b:t.getDrawingSet()){
            bodyBounds = b.getScreenBounds(focus, zFactor, screenDim);
            if(selRect.intersects(bodyBounds) && !selectedItems.contains(b)){
              selectedItems.add(b);
              //selectionCursors.add(new DPSelectionCursor(b, new Color(0,1,1,0.55f)));
            }
          }
        }
      }
    }
    if(selRect.intersects(bh.getScreenBounds(focus, zFactor, screenDim))){
      selectedItems.add(bh);
    }
  }
  
  private void updateCigri(){
    cigri.updateTiles(focus, zFactor, screenDim);
    LinkedList<CGTile> cigris = cigri.getAllTiles();
    cigriItems.clear();

    for(CGTile cgt: cigris){
      if(!cgt.hasChildren()){
        cigriItems.add(cgt);
      }
    }
  }
  
  private void fillRasterItems(){
    displayItems.clear();
    for(Tile t:activeTiles){
      if(t.getGen()==gen){
        displayItems.addAll(t.getDrawingSet());
      }
    }  
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<Rasterable> getSelectedItems(){
    if(selectionLock.tryLock()){
      try{
        return (LinkedList<Rasterable>)selectedItems.clone();
      }finally{
        selectionLock.unlock();
      }
    }else{
      return null;
    }
  }
  
  public int getActiveTileCount(){
    if(defaultLock.tryLock()){
      try{  
        return activeTiles.size();
      }finally{
        defaultLock.unlock();
      }
    }else{
      return -1;
    }
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<Rasterable> getRasterItems(){
    if(defaultLock.tryLock()){
      try{
        LinkedList<Rasterable> ret = (LinkedList<Rasterable>)displayItems.clone();
        ret.addFirst(bh);
        ret.addFirst(backGround);
        return ret;
      } finally{
        defaultLock.unlock();
      }
    }else{
      return null;
    }
  }
  
  @Deprecated
  public LinkedList<Line> getCiGriLines(){
    if(defaultLock.tryLock()){
      try{
        return cigri.getAllAdjustedLines(focus, screenDim, zFactor);
      }finally{
        defaultLock.unlock();
      }
    }else{
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<Pathable> getCiGriTiles(){
    if(defaultLock.tryLock()){
      try{
        return (LinkedList<Pathable>)cigriItems.clone();
      }finally{
        defaultLock.unlock();
      }
    }else{
      return null;
    }
  }
  
  public int getG(){
    if(defaultLock.tryLock()){
      try{
        return gen;
      } finally{
        defaultLock.unlock();
      }
    }else{
      return -1;
    }
  }
  
  public void forceSelection(Rasterable r){
    selectionLock.lock();
    selectedItems.clear();
    selectedItems.add(r);
    selectionLock.unlock();
  }
}
