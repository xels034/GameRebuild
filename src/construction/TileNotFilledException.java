package construction;

public class TileNotFilledException extends Exception{
  
  private static final long serialVersionUID = 5982708944750023652L;

  @Override
  public String getMessage(){
    return "The Tile is not filled. Call populate() and wait for isReady()==true";
  }
}
