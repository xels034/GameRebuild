package construction;

public class UniqueID {

  private static int generated=0;
  
  public static long newID(){
    generated++;
    //-Long.MIN_VALUE to get some great headroom
    return System.currentTimeMillis()+generated-Long.MIN_VALUE;
  }
}
