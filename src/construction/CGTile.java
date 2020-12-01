package construction;

import java.util.LinkedList;

import org.newdawn.slick.Color;

import drawable.V2DPolygon;

public class CGTile extends V2DPolygon{

  protected CGTile parent;
  protected LinkedList<CGTile> subTiles;
  protected int gen;
  protected final int div=2;
  protected double thickness;
  
  public CGTile(LinkedList<Vec2D> ll, Vec2D c, Color cl, String n, CGTile p, int g) {
    super(ll, c, cl, n);
    parent=p;
    gen=g;
    thickness = new Line(ll.getFirst(),ll.getLast(),new Color(0)).getLength();
    subTiles = new LinkedList<>();
  }
  
  public LinkedList<CGTile> coalesce(){
    LinkedList<CGTile> returns = new LinkedList<>();
    for(CGTile cgt : subTiles){
      returns.addAll(cgt.coalesce());
    }
    returns.addAll(subTiles);
    subTiles = new LinkedList<>();
    return returns;
  }
  
  public boolean hasChildren(){
    return !subTiles.isEmpty();
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<CGTile> getSubTiles(){
    return (LinkedList<CGTile>)subTiles.clone();
  }
  
  public LinkedList<CGTile> populate(){
    double newThickness = thickness/div;
    LinkedList<Vec2D> workList = new LinkedList<>();
    
    //if there is no middle vertex to make the division, simply add one
    //TODO only works with div=2 i guess
    if((nodes.size()/2)%div != 1){
      //where the interpolated vertex should go
      int placeIdx = (int)(nodes.size()/(2f*div));
      Vec2D first = nodes.get(placeIdx-1);
      Vec2D next = nodes.get(placeIdx);
      //difference between the two
      Vec2D diff = new Vec2D((next.x-first.x)/2f, (next.y-first.y)/2f);
      //shiftet to be in the middle of the two
      diff.add(first);
      nodes.add(placeIdx, diff);
      //just so the total amount stays %2=0
      nodes.addLast(nodes.getLast().getCopy());
    }
    int arrayIdx=0;
    Vec2D actual;
    //how many vertices one subtile has at one side (so actually *2)
    int idxShift = nodes.size()/(2*div);
    //loop for inner/outer
    for(int outerIterator=0;outerIterator<div;outerIterator++){
      //loop for first/last
      for(int i=0;i<div;i++){
        //Forward
        for(int k=0;k<=idxShift;k++){
          actual = nodes.get(k+(idxShift*i));
          workList.add(shiftCopy(actual,newThickness*outerIterator));
        }
        actual = nodes.get(idxShift*(i+1));
        //Backward
        for(int k=idxShift;k>=0;k--){
          actual = nodes.get(k+(idxShift*i));
          workList.add(shiftCopy(actual,newThickness*(outerIterator+1)));
        }
        subTiles.add(new CGTile(workList, new Vec2D(0,0),col, name+":"+(arrayIdx), this, gen+1));
        workList.clear();
        arrayIdx++;
      }
    }
    return getSubTiles();
  }
  
  protected Vec2D shiftCopy(Vec2D source, double radius){
    Vec2D returner = source.getCopy();
    double len = Math.hypot(returner.x, returner.y);
    returner.x/=len;
    returner.y/=len;
    
    returner.x*=radius;
    returner.y*=radius;
    
    returner.add(source);

    return returner;
  }
  
  public int getG(){
    return gen;
  }
  
  public CGTile getParent(){
    return parent;
  }
  
  @Override
  public String toString(){
    return name;
  }
  
}
