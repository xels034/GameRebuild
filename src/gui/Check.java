package gui;

public class Check {

  public static CheckFunc cf_IsInteger = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        Integer.parseInt((String)o);
        return true;
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_IsP_Integer = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        int i = Integer.parseInt((String)o);
        return (i>=0);
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_IsDouble = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        Double.parseDouble((String)o);
        return true;
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_IsP_Double = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        double d = Double.parseDouble((String)o);
        return (d>=0);
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_IsR_Double  = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        double d = Double.parseDouble((String)o);
        return (d>=0 && d<=1);
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_NameFilter = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.contains("/")) return false;
      if(s.contains(".")) return false;
      if(s.contains("=")) return false;
      if(s.contains(" ")) return false;
      return true;
    }
  };
  
  public static CheckFunc cf_IsPath = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.contains("+")) return false;
      if(s.contains(" ")) return false;
      else return true;
    }
  };
  
  public static CheckFunc cf_ImageIndex = new CheckFunc(){

    @Override
        public boolean check(Object o){
      String s = (String)o;
      if(s.equals("")) return true;
      try{
        int i = Integer.parseInt((String)o);
        return (i >= 0 && i < ImageManager.MAX_IDX);
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
  public static CheckFunc cf_BlendMode = new CheckFunc(){

    @Override
        public boolean check(Object o){
      try{
        String s = (String)o;
        if(s.equals("")) return true;
        if(s.equals("Normal")) return true;
        if(s.equals("Add")) return true;
        if(s.equals("Screen")) return true;
        if(s.equals("Multiply")) return true;
        if(s.equals("Subtract")) return true;
        return false;
      }catch (NumberFormatException x){
        return false;
      }
    }
  };
  
}
