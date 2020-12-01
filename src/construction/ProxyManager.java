package construction;

import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import drawable.CBody;
import drawable.Pathable;
import drawable.Rasterable;

public class ProxyManager {

  private TileManager tm;
  private StaticsManager sm;
  
  private LinkedList<Rasterable> tmRI;
  private LinkedList<Rasterable> tmMI;
  private LinkedList<Pathable> tmPI;
  
  private LinkedList<Rasterable> smRI;
  private LinkedList<Rasterable> smMI;
  private LinkedList<Pathable> smPI;
  
  public ProxyManager(){
    this(null,null);
  }
  
  public ProxyManager(StaticsManager sm){
    this(null, sm);
  }
  
  public ProxyManager(TileManager tm){
    this(tm,null);
  }
  
  public ProxyManager(TileManager tm, StaticsManager sm){
    this.tm=tm;
    this.sm=sm;
    
    tmRI = new LinkedList<>();
    smRI = new LinkedList<>();
    tmMI = new LinkedList<>();
    smMI = new LinkedList<>();
    tmPI = new LinkedList<>();
    smPI = new LinkedList<>();
  }
  
  public void proxyShutDown(){
    if(tm != null){
      tm.shutDown();
    }if(sm != null){
      sm.shutDown();
    }
  }

  public void proxyTranslate(double zFactor, Vec2D focus){
    if(tm != null){
      tm.translate(zFactor, focus.x, focus.y);
    }if(sm != null){
      sm.translate(zFactor, focus.x, focus.y);
    }
  }
  
  public void proxySelection(Rectangle r, boolean add){
    if(tm != null){
      tm.setSelection(r, add);
    }if(sm != null){
      sm.setSelection(r, add);
    }
  }
  
  public LinkedList<Rasterable> getRasterItems(){
    LinkedList<Rasterable> returner = new LinkedList<>();
    if(tm!= null){
      returner = tm.getRasterItems();
      if(returner != null){
        tmRI = returner;
      }
    }if(sm != null){
      returner = sm.getRasterItems();
      if(returner != null){
        smRI = returner;
      }
    }
    returner = new LinkedList<>();
    returner.addAll(tmRI);
    returner.addAll(smRI);
    return returner;
  }

  public LinkedList<Rasterable> getMarkedItems(){
    LinkedList<Rasterable> returner = new LinkedList<>();
    if(tm!= null){
      returner = tm.getSelectedItems();
      if(returner != null){
        tmMI = returner;
      }
    }if(sm != null){
      returner = sm.getSelectedItems();
      if(returner != null){
        smMI = returner;
      }
    }
    returner = new LinkedList<>();
    returner.addAll(tmMI);
    returner.addAll(smMI);
    //Sorting
    TreeMap<TiledID, Rasterable> sorted = new TreeMap<>();
    for(Rasterable r: returner){
      CBody b = (CBody)r;
      sorted.put(b.getID(), r);
    }
    returner.clear();
    for(Entry<TiledID, Rasterable> e: sorted.entrySet()){
      returner.add(e.getValue());
    }
    return returner;
  }
  
  public LinkedList<Pathable> getPathItems(){
    LinkedList<Pathable> returner = new LinkedList<>();
    if(tm!= null){
      returner = tm.getCiGriTiles();
      if(returner != null){
        tmPI = returner;
      }
    }if(sm != null){
      returner = sm.pathItems();
      if(returner != null){
        smPI = returner;
      }
    }
    returner = new LinkedList<>();
    returner.addAll(tmPI);
    returner.addAll(smPI);
    return returner;
  }
  
  public void proxyForceSelection(Rasterable r){
    if(tm!=null){
      tm.forceSelection(r);
    }if(sm!=null){
      sm.forceSelection(r);
    }
  }
}
