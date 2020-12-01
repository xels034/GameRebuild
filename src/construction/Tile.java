package construction;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import drawable.CStar;
import drawable.Rasterable;

public class Tile {

  private int gen;
  private WeakReference<Tile> parent;
  private long seed;
  
  private Tile[][] children;
  private long[][] cSeeds;
  private HashMap<Vec2D, Rasterable>[] pointTable;
  private Vec2D start;
  private double size;
  
  private boolean ready;
  private Point relPos;
  private TiledID id;
  
  public final int VAL_DIV=4;
  
  Random rand;
  
  
  public Tile(long s, Vec2D start, double size, Tile p, int g, HashMap<Vec2D, Rasterable> superpoints, Point r, TiledID pid){
    seed=s;
    this.start=start;
    this.size=size;
    parent=new WeakReference<>(p);
    ready=false;
    gen=g;
    relPos=r;
    cSeeds = new long[4][4];
    
    if(gen==0){
      id = new TiledID(pid.toString());
    }
    else{
      id = new TiledID(pid,(r.x*VAL_DIV)+r.y);
    }
    
    rand=new Random();
    rand.setSeed(s);
    
    //first of all, create seeds, so seeds stay consistent over multiple new/finalize cycles
    for(int i=0;i<VAL_DIV;i++){
      for(int j=0;j<VAL_DIV;j++){
        cSeeds[i][j]=rand.nextLong();
      }
    }
    
    children=new Tile[VAL_DIV][VAL_DIV];
    mergeSuperPoints(superpoints);
  }
  
  public Tile createTile(int x, int y) throws TileNotFilledException{
    //a new, empty Tile
    //this must be ready, because the subtile need its superpoints
    if(ready){
      double sX = start.x+(size/VAL_DIV)*x;
      double sY = start.y+(size/VAL_DIV)*y;
      children[x][y]= new Tile(cSeeds[x][y],new Vec2D(sX,sY),size/VAL_DIV,this, gen+1,
                           pointTable[(x*VAL_DIV)+y], new Point(x,y),id);

      return children[x][y];
    }else{
      throw new TileNotFilledException();
    }
  }
  
  public Rectangle2D.Double getScreenBounds(Vec2D f, double zFactor, Vec2D screen){
    Rectangle2D.Double retu = new Rectangle2D.Double(start.x, start.y, size, size);
    retu.x-=f.x;
    retu.y-=f.y;
    
    retu.x*=zFactor;
    retu.y*=zFactor;
    
    retu.width*=zFactor;
    retu.height*=zFactor;
    
    retu.x+=screen.x/2;
    retu.y+=screen.y/2;
    
    return retu;
  }
  
  public Tile getParent(){
    if(gen>0){
      return parent.get();
    }else{
      return null;
    }
  }
  
  public long getSeed(){
    return seed;
  }
  
  public int getGen(){
    return gen;
  }
  
  public boolean contains(double x, double y){
    return (start.x < x && start.x+size > x &&
        start.y < y && start.y+size > y);
  }
  
  public void populate(Double[][] data){
    //traverse the given data
    //bring the coordinates down to int from 0-val_div by dividing the coordinate by maxwidth/val_div
    //example: width=400, div=4, x=310
    // 310 / (400/4) = 3.1 -> 3 = (int)3.1
    
    //60 startsize and 0.03 smaxsize semm to be some good values
    rand = new Random();
    if(data!=null){
      for(int i=0;i<data.length;i++){
        //data is in absolute coordinates (easier for drawing later)
        int x = (int)((data[i][0]-start.x)/(size/VAL_DIV));
        int y = (int)((data[i][1]-start.y)/(size/VAL_DIV));
        
        //"converting" a double to a long by moving the decimator 15 places right
        long bSeed=(long)(data[i][2]*10000000000000000L);
        double darta = (data[i][0]+data[i][1]+data[i][2])*1000000000L;
        bSeed = (long)darta;
        rand.setSeed(bSeed);
        
        Rasterable b=null;

        
                            //60 was original.
                            //trying new defaul=1px convention
        float scale = (float)  (data[i][2] * ( (60*128) / Math.pow(VAL_DIV,gen) ) );
        TiledID sID = new TiledID(id.toString());
        sID.addPosInTile(i);
        b = new CStar(bSeed, new Vec2D(data[i][0], data[i][1]), sID, scale);

        pointTable[(VAL_DIV*x)+y].put(new Vec2D(data[i][0]-start.x,data[i][1]-start.y),b);  
      }
    }
    ready=true;
  }
  
  public LinkedList<Rasterable> getSubTilePopulation(int x, int y){
    LinkedList<Rasterable> ll = new LinkedList<>();
    ll.addAll(children[x][y].getDrawingSet());
    return ll;
  }
  
  public LinkedList<Rasterable> getAllSubTilePopulation(){
    LinkedList<Rasterable> ll = new LinkedList<>();
    for(int i=0;i<VAL_DIV;i++){
      for(int j=0;j<VAL_DIV;j++){
        if(children[i][j]!=null){
          ll.addAll(children[i][j].getDrawingSet());
        }
      }
    }
    return ll;
  }
  
  public Tile getSubTile(int x, int y){
    return children[x][y];
  }
  
  public Set<Rasterable> getDrawingSet(){
    Set<Rasterable> s = new HashSet<>();
    for(int i=0;i<pointTable.length;i++){
      s.addAll(pointTable[i].values());
    }
    return s;
  }
  
  public void collapseChild(int x, int y){
    children[x][y]=null;
  }
  
  public void collapseAllChildren(){
    for(int i=0;i<children.length;i++){
      for(int j=0;j<children[i].length;j++){
        children[i][j]=null;
      }
    }
  }
  
  public LinkedList<Tile> getAllocatedChildren(){
    LinkedList<Tile> ll = new LinkedList<>();
    
    for(int i=0;i<VAL_DIV;i++){
      for(int j=0;j<VAL_DIV;j++){
        if(children[i][j]!=null){
          ll.add(children[i][j]);
        }
      }
    }
    
    return ll;
  }
  
  public Point getRelPos(){
    return relPos;
  }
  
  @SuppressWarnings({ "unchecked"})
   private void mergeSuperPoints(HashMap<Vec2D, Rasterable> m){
     //pointTable = new HashMap<FPoint, BImgObj>[16];
    //IMPORTANT: the above command is not allowed in java (who dafuq decided this anyway?)
    //so here's a dirty little trick to fool the compiler
     pointTable =new HashMap[16];
    //initialize the 16 (in case of val_div==4) submaps, for faster points checking
    //and faster passing of superpoints
    for(int i=0;i<VAL_DIV*VAL_DIV;i++){
      pointTable[i]= new HashMap<>();
      
    }
    
    Vec2D tmpDPn;
    Rasterable tmpBo;
    int pTIdxX, pTIdxY; //pointTableIndex
    for(Entry<Vec2D, Rasterable> e:m.entrySet()){
      //IMPORTANT copy of the FP, because its relative to its tile and hence changes
      //      the BImgObj should stay the same. it has absolute coordinates and when selecting it,
      //      it should stay selected when zooming in
      tmpBo=e.getValue();  
      tmpDPn = e.getKey().getCopy();
      //to translate the "big" coordinates of the supertile to normal coordinates, exaple:
      //subtile at position x=2,y=2 (3rd item in 3rd line)
      //all coordinates are 200-300, but we need 0-100 (1/val_Div of the supertile)
      //so x-(xPos of the tile in its supertile)*(width/val_div of the supertile, or just width)
      //x(e.g. 250) = 250 - 2*100 = 50, where it should be;
      tmpDPn.x-=relPos.x*size;
      tmpDPn.y-=relPos.y*size;
      
      //see populate for finding out which map to use
      pTIdxX=(int)(tmpDPn.x/(size/VAL_DIV));
      pTIdxY=(int)(tmpDPn.y/(size/VAL_DIV));
      pointTable[(VAL_DIV*pTIdxX)+pTIdxY].put(tmpDPn, tmpBo);
    }
  }
  
  public Vec2D getStart(){
    return start;
  }
  
  public double getSize(){
    return size;
  }
  
  public String getIDStr(){
    return id.toString();
  }
  
  @Override
  public boolean equals(Object arg0){
    if(arg0 instanceof Tile){
      Tile t = (Tile)arg0;
      return id.toString().equals(t.getIDStr());
    }
    return false;
  }

  @Override
  public int hashCode(){
    return id.hashCode();
  }
}
