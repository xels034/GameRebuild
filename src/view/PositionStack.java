package view;

import java.util.LinkedList;

import construction.Vec2D;


public class PositionStack {

  private int d;
  private LinkedList<Vec2D> posStack;
  private LinkedList<Long> seedStack;
  
  public PositionStack(){
    d=0;
    posStack = new LinkedList<>();
    seedStack = new LinkedList<>();
  }
  
  public void put(Vec2D pos, long s){
    posStack.addLast(pos.getCopy());
    seedStack.addLast(s);
    d++;
  }
  
  public long get(Vec2D output){
    d--;
    Vec2D v = posStack.removeLast();
    output.x=v.x;
    output.y=v.y;
    return seedStack.removeLast();
  }
  
  public int getDepth(){
    return d;
  }
}
