package drawable;

import java.awt.Polygon;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import org.newdawn.slick.Color;

import construction.Line;
import construction.ValueRamp;
import construction.Vec2D;

public class V2DPolygon implements Fillable, Pathable{
  
  protected LinkedList<Vec2D> nodes;
  protected Vec2D center;
  protected double rotation;
  protected Color col;
  protected String name;
  protected ValueRamp vr;
  
  public LinkedList<V2DPolygon> children;
  private int ccw;
  private LinkedList<Line> shape;
  //a vector from top left to bottom right describes the rectangular bounds
  //used to cast a ray thats always outside of the polygon
  protected Line boundry;
  
  private void transformVec(Vec2D v){
    v.y*=-1;
    
    float tmpx = (float)(v.x*Math.cos(rotation)-v.y*Math.sin(rotation));
    float tmpy = (float)(v.x*Math.sin(rotation)+v.y*Math.cos(rotation));
    v.x = tmpx;
    v.y = tmpy;
    
    v.y*=-1;
    
    v.add(center);
  }
  
  private void constructBounds(){
    shape = new LinkedList<>();
    
    Vec2D initer = nodes.getFirst().getCopy();
    //initer.add(center);
    transformVec(initer);
    
    double minX=initer.x;
    double minY=initer.y;
    double maxX=initer.x;
    double maxY=initer.y;
    
    Vec2D f,l;
    for(int i=0;i<nodes.size()-1;i++){
      f=nodes.get(i).getCopy();
      l=nodes.get(i+1).getCopy();
      
      //f.add(center);
      //l.add(center);
      transformVec(f);
      transformVec(l);
      
      minX=Math.min(minX, f.x);
      minX=Math.min(minX, l.x);
      
      minY=Math.min(minY, f.y);
      minY=Math.min(minY, l.y);
      
      maxX = Math.max(maxX, f.x);
      maxX = Math.max(maxX, l.x);
      
      maxY = Math.max(maxY, f.y);
      maxY = Math.max(maxY, l.y);
      
      shape.addLast(new Line(f,l,col));
    }
    f = nodes.getLast().getCopy();
    //f.add(center);
    transformVec(f);
    l = nodes.getFirst().getCopy();
    //l.add(center);
    transformVec(l);
    shape.addLast(new Line(f,l,col));
    
    //System.out.println(new Vec2D(minX,minY) + " - " + new Vec2D(maxX,maxY));
    
    boundry = new Line(new Vec2D(minX,minY),new Vec2D(maxX,maxY),col);
  }
  
  @SuppressWarnings("unchecked")
  private void init(LinkedList<Vec2D> ll, Vec2D c, Color cl, String n){
    name=n;
    nodes=(LinkedList<Vec2D>)ll.clone();
    center=c.getCopy();
    col=cl;
    constructBounds();
  }
  
  private void init(Polygon p, Vec2D c, Color cl, String n){
    name=n;
    nodes = new LinkedList<>();
    center=c.getCopy();
    for(int i=0;i<p.npoints;i++){
      nodes.addLast(new Vec2D(p.xpoints[i],p.ypoints[i]));
    }
    col=cl;
    constructBounds();
  }
  
  public V2DPolygon(LinkedList<Vec2D> pll, LinkedList<V2DPolygon> all, Vec2D c, String n){
    init(pll,c,all.getFirst().getBaseColor(),n);
    ccw=0;
    children=all;
  }
  
  public V2DPolygon(LinkedList<Vec2D> ll, Vec2D c, Color cl, String n){
    init(ll, c, cl, n);
    ccw=0;
    children = new LinkedList<>();
    //non-merged polys consist only of themselfes
    children.add(this);
  }
  
  public V2DPolygon(Polygon p, Vec2D c, Color cl, String n){
    init(p,c,cl,n);
    ccw=0;
    children = new LinkedList<>();
    //non-merged polys consist only of themselfes
    children.add(this);
  }
  
  public static V2DPolygon[] merge (V2DPolygon p1, V2DPolygon p2){
    if(p1.contains(p2)){
      p1.children.add(p2);
      V2DPolygon[] arr = {p1};
      return arr;
    }else if(p2.contains(p1)){
      p2.children.add(p1);
      V2DPolygon[] arr = {p2};
      return arr;
    }else if(p1.intersects(p2)){
      LinkedList<Line[]> crossings = new LinkedList<>();
      LinkedList<Line> p1Shape = p1.getPlot();
      LinkedList<Line> p2Shape = p2.getPlot();
      for(Line v1: p1Shape){
        for(Line v2: p2Shape){
          if(Line.isIntersecting(v1, v2)){
            Line[] arr = {v1,v2};
            crossings.add(arr);
          }
        }
      }
      
      System.out.println(crossings.size());
      //for(Line[] xr: crossings){
        //TODO
        //TODO do it do it :D
        //find the "first" one with Vec2D.intersectsFromRight
        //center point -> c->l2.e is ALWAYS the line you go
        //work with copys, lines may be used elsewhere
        //if line.len=0:
        //previous.next=this.next;
        //this.next.previous=this.previous;
      //}

    }else{
      V2DPolygon[] arr = {p1,p2};
      return arr;
    }
    return null;
  }
  
  @Override
  public double getArea(){
    //from the interwebsz. i don't know if that works
    double area=0;
    Vec2D v1,v2;
    for(int i=0;i<nodes.size()-1;i++){
      v1=nodes.get(i);
      v2=nodes.get(i+1);
      
      area+=(v1.x*v2.y)-(v2.x*v1.y);
    }
    
    v1=nodes.getLast();
    v2=nodes.getFirst();
    area+=(v1.x*v2.y)-(v2.x*v1.y);
    
    return Math.abs(area/2);
  }
  
  @Override
  public Line intersectedLine(Fillable f, Vec2D pos){
    //pos as a by-ref parameter
    Line l1 = null;
    Line l2 = null;
    for(Line sL: shape){
      for(Line v: f.getFastPlot()){
        if(Line.isIntersecting(sL, v)){
          Vec2D asd = Line.getIntersection(sL, v);
          pos.x = asd.x;
          pos.y = asd.y;
          
          if(l1 == sL || l1 == v){
            return l1;
          }else if(l2 == sL || l2 == v){
            return l2;
          }
          l1=sL;
          l2=v;
        }
      }
    }
    
    if(l1 == null && l2 == null) return null;
    Vec2D asd = Line.getIntersection(l1, l2);
    pos.x = asd.x;
    pos.y = asd.y;
    //if there's no definitive answer, return an imaginary
    //line thats the normal of the difference vector of the
    //two centers

    Vec2D lineVec = f.getCenter().getCopy();
    lineVec.subtract(center);
    
    Vec2D start = lineVec.getCopy();
    start.multiply(.5);
    
    double tmp = lineVec.x;
    lineVec.x = lineVec.y;
    lineVec.y = -tmp;
    
    lineVec.add(start);
    
    return new Line(start,lineVec,Color.black);
  }
  
  @Override
  public boolean intersects(Fillable f){
    for(Line v: f.getPlot()){
      if(intersects(v)){
        return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean intersects(Line v){
    for(Line v2d: shape){
      if(Line.isIntersecting(v2d, v)){
        return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean contains(Vec2D p){
    int c=0;
    Vec2D origin = boundry.getPoints()[0];
    //ensures that the origin is always outside of the polygon
    origin.x-=1;
    origin.y-=1;
    
    //the idea is, whenever a ray cast from outside of the polygon
    //to the point in question, the number is always odd if it is inside
    //and even if it is not
    
    Line ray = new Line(origin,p,col);
    
    for(Line v: shape){
      if(Line.isPointOnLine(p, v)){
        //it is also inside if its exactly on a line
        return true;
      }else if(Line.isIntersecting(ray, v)){
        c++;
      }
    }
    return c%2==1;
  }
  
  @Override
  public boolean contains(Fillable f){
    LinkedList<Vec2D> ll = f.getVertices();
    for(Vec2D p: ll){
      if(!contains(p)){
        return false;
      }
    }
    return true;
  }
  
  @Override
  public LinkedList<Line> getTransformedLines(Vec2D focus, Vec2D screen, double zFactor){
    //(realWorld coords-focus)*zFactor  +  screen/2
    Vec2D start, end;
    start = end = null;
    LinkedList<Line> returns = new LinkedList<>();
    
    float i=0;
    for(Vec2D v: nodes){
      start = end;
      end = v.getCopy();
      //end.add(center);
      transformVec(end);
      
      Vec2D.viewTransform(end,focus,screen,zFactor);
      
      if(end != null && start != null){
        Color c = col.scaleCopy(1);
        if(vr != null){
          c.scale((float)vr.getValue(i/nodes.size()));
        }
        returns.add(new Line(start, end, c));
      }
      i++;
    }
    
    start = end;
    end = nodes.getFirst().getCopy();
    //end.add(center);
    transformVec(end);
    Vec2D.viewTransform(end,focus,screen,zFactor);
    returns.add(new Line(start, end, col));
    
    return returns;
  }
  

  
  @Deprecated
  @Override
  public Polygon getPolygon(){
    //Vec2D[] arr = transNodes.toArray(new Vec2D[0]);
    Polygon p = new Polygon();
    //for(int i=0;i<arr.length;i++){
      //p.addPoint((int)arr[i].x, (int)arr[i].y);
    //}
    return p;
  }
  
  @Override
  public Color getFillColor(){
    return new Color(col.getRed(),
             col.getGreen(),
             col.getBlue(),
             col.getAlpha()/3);
  }
  
  @Override
  public int isCCW(){
    return ccw;
  }
  
  public Color getBaseColor(){
    return col;
  }
  
  @Override
  public LinkedList<Line> getPlot(){
    //used for merging two polygons
    //its similar to a DrawablePath, only in game-space
    //and the Lines are linked to each other in a ring
    //also checking if the structure is CW oder CCW
    LinkedList<Line> ll = new LinkedList<>();
    Vec2D[] arr = nodes.toArray(new Vec2D[0]);
    
    Line last=null;
    Line act;
    
    int fails=0;
    
    for(int i=0;i<arr.length-1;i++){
      Vec2D v1 = arr[i].getCopy();
      Vec2D v2 = arr[i+1].getCopy();
      //v1.add(center);
      //v2.add(center);
      transformVec(v1);
      transformVec(v2);
      act = new Line(v1,v2,col);
      if(last!=null){
        if(last.getAngleDegree()>act.getAngleDegree()){
          fails++;
        }
        last.next=new WeakReference<>(act);
        act.previous = new WeakReference<>(last);
      }
      ll.add(act);
      last=act;
    }
    Vec2D v1 = arr[arr.length-1].getCopy();
    Vec2D v2 = arr[0].getCopy();
    //v1.add(center);
    //v2.add(center);
    transformVec(v1);
    transformVec(v2);
    act = new Line(arr[arr.length-1],arr[0],col);
    if(last.getAngleDegree()>act.getAngleDegree()){
      fails++;
    }
    last.next=new WeakReference<>(act);
    act.previous = new WeakReference<>(last);
    ll.add(act);
    act.next=new WeakReference<>(ll.getFirst());
    act.previous = new WeakReference<>(last);
    
    if(fails==1){
      //smallest polygon is a triangle.
      //in a CCW triangle, all prev. Vects angles are smaller that their followers
      //except when it jumps from 0 to 360.
      //so, the check in a CCW fails once
      ccw=1;
    }else{
      ccw=-1;
    }
    return ll;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public LinkedList<Line> getFastPlot(){
    return (LinkedList<Line>)shape.clone();
  }
  
  public void setTranslation(Vec2D v){
    center = v.getCopy();
    constructBounds();
  }
  
  public void translate(Vec2D v){
    center.add(v);
    constructBounds();
  }
  
  public void setRotation(double r){
    rotation=r;
    constructBounds();
  }
  
  public void rotate(double r){
    rotation+=r;
    constructBounds();
  }
  
  @SuppressWarnings("unchecked")
  public LinkedList<V2DPolygon> getChildren(){
    return (LinkedList<V2DPolygon>)children.clone();
  }

  @Override
  public LinkedList<Vec2D> getVertices(){
    LinkedList<Vec2D> ret = new LinkedList<>();
    Vec2D i;
    for(Vec2D v : nodes){
      i = v.getCopy();
      //i.add(center);
      transformVec(i);
      ret.add(i);
    }
    return ret;
  }
  
  public String getName(){
    return name;
  }
  
  @Override
  public Line getBounds(){
    return boundry.clone();
  }

  @Override
  public void setValueRamp(ValueRamp vr) {
    this.vr=vr;
  }

  @Override
  public Vec2D getCenter() {
    return center;
  }
}
