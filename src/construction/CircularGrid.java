package construction;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import drawable.V2DPolygon;

public class CircularGrid {

  private LinkedList<CGTile> activeTiles;
  private LinkedList<CGTile> coreTiles;
  
  private int radius;
  private Color color;
  //private int rootSub; //sub WITHOUT Core
  //private final float subScale = 1.5f;
  //private final int segments = 6;
  private final int resolution = 828;//828
  private final int thresh = 1800;
  
  public CircularGrid(int r, Color c){
    radius=r;
    color=c;
    activeTiles = new LinkedList<>();
    coreTiles = new LinkedList<>();
    //rootSub = sub;
    constructGrid();
  }
  
  private void constructGrid(){
    //Determinating the radius of the core:
    //r=radius;s=scale;d=subdiv
    
    //r = x + x*s + x*s*s + x*s*s*s + ...
    //r = x*s^0 + x*s^1 + x*s^2 + ...
    //r = x(s^0 + s^1 + s^2 ...)
    //r = x*Sum(n = 0 -> d: [s^n])
    
    
    double actRadius = radius;
    //Build Core
    LinkedList<Vec2D> cList = new LinkedList<>();
    double rad = 0;
    for(int i=0;i<resolution;i++){
      cList.add(new Vec2D(actRadius*Math.cos(rad), actRadius*Math.sin(rad)));
      rad+= (Math.PI*2)/resolution;
    }
    //core = new V2DPolygon(cList,new Vec2D(0,0),color,"Core");
    coreTiles.add(new CGCore(cList,new Vec2D(), color, "Core", null, 0));
    cList.clear();

  }
  
  @SuppressWarnings("unchecked")
  public void updateTiles(Vec2D focus, double zFactor, Vec2D screen){
    
    V2DPolygon screenRect = makeScreen(focus, zFactor, screen);
    LinkedList<CGTile> delta = new LinkedList<>();
    boolean deltaAction;
    
    //clear out everything and rebiuld from scratch. not the most efficient
    //but eeh. its in another thread
    activeTiles = (LinkedList<CGTile>)coreTiles.clone();
    for(CGTile cgt: activeTiles){
      cgt.coalesce();
    }
    
    do{
      deltaAction = false;
      for(CGTile cgt: activeTiles){
        if(cgt.intersects(screenRect) || cgt.contains(screenRect) || screenRect.contains(cgt)){
          if(cgt.getBounds().getLength()*zFactor > thresh){
            if(!cgt.hasChildren()){
              delta.addAll(cgt.populate());
              deltaAction = true;
            }
          }
        }
      }
      activeTiles.addAll(delta);
      delta.clear();
    }while(deltaAction);

  }
  
  public static V2DPolygon makeScreen(Vec2D focus, double zFactor, Vec2D screen){
    LinkedList<Vec2D> screenVecList= new LinkedList<>();
    
    Vec2D vec = new Vec2D(-screen.x/2, -screen.y/2);
    vec.x/=zFactor;
    vec.y/=zFactor;
    vec.add(focus);
    screenVecList.add(vec);
    
    vec=new Vec2D(-screen.x/2,screen.y/2);
    vec.x/=zFactor;
    vec.y/=zFactor;
    vec.add(focus);
    screenVecList.add(vec);
    
    vec=new Vec2D(screen.x/2,screen.y/2);
    vec.x/=zFactor;
    vec.y/=zFactor;
    vec.add(focus);
    screenVecList.add(vec);
    
    vec=new Vec2D(screen.x/2,-screen.y/2);
    vec.x/=zFactor;
    vec.y/=zFactor;
    vec.add(focus);
    screenVecList.add(vec);
    
    return new V2DPolygon(screenVecList,new Vec2D(),Color.black,"ScreenRect");
  }
  
  public LinkedList<Line> getAllAdjustedLines(Vec2D focus, Vec2D screen, double zFactor){
    LinkedList<Line> returns = new LinkedList<>();
    for(CGTile cgt: activeTiles){
      returns.addAll(cgt.getTransformedLines(focus, screen, zFactor));
    }
    return returns;
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<CGTile> getAllTiles(){
    return (LinkedList<CGTile>)activeTiles.clone();
  }
}
