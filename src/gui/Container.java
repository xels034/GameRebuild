package gui;

public class Container{
  Object o;
  public Container(Object o){
    this.o=o;
  }
  public Container(){}
  public synchronized void setObject(Object o){
    this.o=o;
  }
  public synchronized Object getObject(){
    return o;
  }
  public synchronized void clear(){
    o=null;
  }
}
