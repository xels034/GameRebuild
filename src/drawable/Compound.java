package drawable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class Compound {
  
  HashMap<String, Double> components;
  
  public Compound(){
    components = new HashMap<>();
  }
  
  public Compound(HashMap<String, Double> hm){
    components=hm;
  }
  
  public void addComponent(String s, double d){
    components.put(s, d);
  }
  
  public HashMap<String, Double> getComponents(){
    return components;
  }
  
  public static String fillCompound(Compound c, String[] choices, Random r){
    int amt = (int)Math.min((r.nextDouble()*choices.length)+2,choices.length);
    HashSet<Integer> used = new HashSet<>();
    String s="";
    double max=0;
    int idx=0;
    double left=1;
    for(int i=0;i<amt;i++){
      do{
        idx = (int)(r.nextDouble()*choices.length);
      } while(used.contains(idx));
      
      used.add(idx);
      
      double percentage;
      if(used.size()<amt){
        percentage=r.nextDouble()*left;
      }else{
        percentage=left;
      }
      left-=percentage;
      c.addComponent(choices[idx], percentage);
      if(percentage > max){
        max = percentage;
        s = choices[idx];
      }
    }
    return s;
  }
  
}
