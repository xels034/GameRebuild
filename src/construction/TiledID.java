package construction;


public class TiledID implements Comparable<TiledID>{

  //root        count in tile
  //  v          v
  //0008|0001:FA0763CD:1234 || 0:1:4r < r for ring of a planet
  //        ^        ^
  //      relative pos  sun:planet:moon
  
  // % marks datainput
  // d stands for decimal
  // x for hexadecimal
  // e.g. %04d means 4 decimal places with leading zeroes
  
  private String id;
  private boolean isContainer;
  private int cLen=4;
  
  public TiledID(int x, int y){
    //new root tile
    id = String.format("%0"+cLen+"d|%0"+cLen+"d", x,y);
  }
  
  public void addCenterMass(int x){
    id += " || "+x;
    isContainer=false;
  }
  
  public void addTrabant(int x){
    id += ":"+x;
    isContainer=false;
  }
  
  public TiledID(TiledID inherit, int x, int y){
    id = inherit+" || "+new TiledID(x,y);
    isContainer=true;
  }
  
  public TiledID(TiledID inherit, int x, int y, boolean flag){
    id = inherit+" || s"+new TiledID(x,y);
    isContainer=true;
  }
  
  public TiledID(TiledID d, int relPos){
    //new subtile
    
    String s = d.toString();
    
    if(s.charAt(s.length()-1-4)=='|'){
      id=s+":";
    }else if(!d.isContainer){
      id=s.substring(0,s.length()-(cLen+1));//<<< war clen*2+2
    }else{
      id=s;
    }

    id +=String.format("%x", relPos);
    isContainer=true;
  }
  
  public TiledID (String id){
    //copy id
    this.id=id;
    isContainer=true;
  }
  
  public void addPosInTile(int x){
    //new object in tile
    if(isContainer){
      id+=String.format(":%04d", x);
      isContainer=false;
    }
  }
  
  @Override
  public String toString(){
    return id;
  }
  
  public static int getGeneration(String d){
    //to escape the | character, you need not only \, but \\! srsly, dafuq?
    
    String[] gens = d.split("\\|\\|");
    if(gens.length==1){
      //galaxy View
      String[] smt = gens[0].split(":");
      if(smt.length == 2){
        return 0;
      }else{
        return smt[1].length();
      }
      
    }else{
      return gens[1].split(":").length;
    }
  }

  @Override
  public int compareTo(TiledID arg0) {
    if(arg0 == null){
      throw new NullPointerException();
    }if(arg0 == this){
      return 0;
    }
    String myID = toString();
    String otherID = arg0.toString();
    
    String[]myID0 = myID.split("\\|\\|");
    String[]otherID0 = otherID.split("\\|\\|");
    
    String[] myID1 = myID0[0].split(":");
    String[]otherID1 = otherID0[0].split(":");
    
    String[]myID2 = myID1[0].split("\\|");
    String[]otherID2 = otherID1[0].split("\\|");
    
    //root Tile
    for(int i=0;i<2;i++){
      int myRoot = Integer.parseInt(myID2[i]);
      int otherRoot = Integer.parseInt(otherID2[i]);
      
      if(myRoot > otherRoot){
        return 1;
      }else if( myRoot != otherRoot){
        return -1;
      }
      
    }
    
    int next = 1;
    if(myID1.length == 3 && otherID1.length == 3){
    
      //Subtile
      int maxHexLen = Math.min(myID1[next].length(), otherID1[next].length());
      for(int i=0;i<maxHexLen;i++){
        int myHex = Integer.parseInt(myID1[next].charAt(i)+"", 16);
        int otherHex = Integer.parseInt(otherID1[next].charAt(i)+"", 16);
        
        if(myHex > otherHex){
          return 1;
        }else if(myHex != otherHex){
          return -1;
        }
      }
      
      if(myID1[next].length() > otherID1[next].length()){
        return -1;
      }else if(myID1[next].length() != otherID1[next].length()){
        return 1;
      }
    
      next++;
    }else if(myID1.length != otherID1.length){
      //return otherID1.length - myID1.length;
      return myID1.length - otherID1.length;
    }

    //relPos
    int myRP = Integer.parseInt(myID1[next].trim());
    int otherRP = Integer.parseInt(otherID1[next].trim());
    if(myRP > otherRP){
      return 1;
    }else if(myRP != otherRP){
      return -1;
    }
    
    //if SolView TiledID
    if(myID0.length == 2 && otherID0.length == 2){
      myID1 = myID0[1].trim().split(":");
      otherID1 = otherID0[1].trim().split(":");  
      int maxLen = Math.min(myID1.length, otherID1.length);  
      for(int i=0;i<maxLen;i++){
        int myGenPos = Integer.parseInt(myID1[i]);
        int otherGenPos = Integer.parseInt(otherID1[i]);
        if(myGenPos > otherGenPos){
          return 1;
        }else if(myGenPos != otherGenPos){
          return -1;
        }
      }  
      return myID1.length - otherID1.length;
    }else{
      //return otherID0.length - myID0.length;
      return myID0.length - otherID0.length;
    }
  }
}
