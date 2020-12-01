package drawable;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import construction.Vec2D;

public class CGCore extends CGTile{

  private double downScale = 1.65;
  
  public CGCore(LinkedList<Vec2D> ll, Vec2D c, Color cl, String n, CGTile p, int g) {
    super(ll, c, cl, n, p, g);
    // TODO Auto-generated constructor stub
    thickness = Math.hypot(nodes.getFirst().x, nodes.getFirst().y);
  }
  
  @Override
  public LinkedList<CGTile> populate(){
    //shrinked core
    LinkedList<Vec2D> workerList = new LinkedList<>();
    //Vec2D actual;
    for(Vec2D v: nodes){
      workerList.add(shiftCopy(v, -thickness/downScale));
    }
    subTiles.add(new CGCore(workerList, new Vec2D(), col, name+":"+gen, this, gen+1));
    workerList.clear();
    
    //6 tiles around it
    int segments = nodes.size()/6;
    for(int i=0;i<6;i++){
      for(int k=segments*i; k<=segments*(i+1); k++){
        workerList.add(shiftCopy(nodes.get(k%nodes.size()), -thickness/downScale));
      }
      for(int k=segments*(i+1); k>=segments*i; k--){
        workerList.add(nodes.get(k%nodes.size()).getCopy());
      }
      subTiles.add(new CGTile(workerList, new Vec2D(), col, name+":s"+i,this,gen+1));
      workerList.clear();
    }
    
    return getSubTiles();
  }
  
  protected Vec2D shiftCopyFrac(Vec2D source, double fraction){
    double len = Math.hypot(source.x, source.y);
    Vec2D returner = source.getCopy();
    
    returner.x /= len;
    returner.y /= len;
    
    returner.x*=fraction;
    returner.y*=fraction;
    
    return returner;
  }

}
