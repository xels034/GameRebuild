package construction;

import java.awt.geom.Rectangle2D;
import java.util.LinkedList;


public class QuadTree {

  private int maxDepth;
  private int maxItems;
  private int level;
  private QuadTree[] nodes;
  private LinkedList<PhPoly> items;
  private Rectangle2D.Double bounds;
  
  public QuadTree(Rectangle2D.Double rect, int mD, int mI, int lvl){
    maxDepth = mD;
    maxItems = mI;
    level=lvl;
    
    items = new LinkedList<>();
    nodes = new QuadTree[4];
    bounds = rect;
  }
  
  public void clear(){
    items.clear();
    for(int i=0; i < nodes.length; i++){
      if(nodes[i] != null){
        nodes[i].clear();
        nodes[i] = null;
      }
    }
  }
  
  private void split(){
    double subWidth = bounds.getWidth()/2;
    double subHeight = bounds.getHeight()/2;
    double x = bounds.getX();
    double y = bounds.getY();
    
    nodes[0] = new QuadTree(new Rectangle2D.Double(x+subWidth, y, subWidth, subHeight), maxDepth, maxItems, level+1);
    nodes[1] = new QuadTree(new Rectangle2D.Double(x, y, subWidth, subHeight), maxDepth, maxItems, level+1);
    nodes[2] = new QuadTree(new Rectangle2D.Double(x, y+subHeight, subWidth, subHeight), maxDepth, maxItems, level+1);
    nodes[3] = new QuadTree(new Rectangle2D.Double(x+subWidth, y+subHeight, subWidth, subHeight), maxDepth, maxItems, level+1);
  }
  
  private int getIndex(Rectangle2D.Double pbB){
    double vLine = bounds.x+(bounds.width/2);
    double hLine = bounds.y+(bounds.height/2);
    
    if(pbB.x < bounds.x || pbB.x+pbB.width > bounds.x+bounds.height) return -1;
    if(pbB.y < bounds.y || pbB.y+pbB.height > bounds.y+bounds.height) return -1;
    
    boolean fitsTop = (pbB.y+pbB.height < hLine);
    boolean fitsBottom =(pbB.y > hLine);
    boolean fitsLeft = (pbB.x+pbB.width < vLine);
    boolean fitsRight = (pbB.x > vLine);
    
    if(fitsTop){
      if(fitsLeft)     return 1;
      else if(fitsRight)  return 0;
    }else if(fitsBottom){
      if(fitsLeft)     return 2;
      else if(fitsRight)  return 3;
    }
    return -1;
  }
  
  public void insert(PhPoly pb){
    if(nodes[0] != null){
      int idx = getIndex(pb.getSquareBounds());
      if(idx != -1){
        nodes[idx].insert(pb);
        return;
      }
    }
    
    items.add(pb);
    
    if(items.size() > maxItems && level < maxDepth){
      if(nodes[0] == null){
        split();
      }
      int i=0;
      while( i < items.size()){
        PhPoly pbIter = items.get(i);
        int idx = getIndex(pbIter.getSquareBounds());
        //insert and get rid of item
        //else ++ to skip this item
        if(idx != -1){
          nodes[idx].insert(pbIter);
          items.remove(i);
        }else{
          i++;
        }
      }
    }
  }
  
  public LinkedList<PhPoly> getCandidates(Rectangle2D.Double pbB){
    LinkedList<PhPoly> ret = new LinkedList<>();
    int idx = getIndex(pbB);
    
    if(nodes[0] != null){
      if(idx != -1)            ret.addAll(nodes[idx].getCandidates(pbB));
      else{
        for(int i=0;i<4;i++) ret.addAll(nodes[i].getCandidates(pbB));
      }
    }
    
    ret.addAll(items);
    return ret;
  }
  
  public LinkedList<PhPoly> getEscapers(){
    LinkedList<PhPoly> ret = new LinkedList<>();
    for(PhPoly p: items){
      if(!bounds.contains(p.getSquareBounds())){
        ret.add(p);
      }
    }
    return ret;
  }
  
}
