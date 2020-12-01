package construction;

public class PhMaterial {
  
  //+++ DEFINES +++
  
  public static final int DMGT_DFLT = 0;
  public static final int DMGT_LSR = 1;
  public static final int DMGT_MSSL = 2;
  public static final int DMGT_BLSC = 3;
  
  public static final int HLLT_DFLT = 0;
  public static final int HLLT_HLL = 1;
  public static final int HLLT_SHLD = 2;
  public static final int HLLT_MSSL = 3;
  public static final int HLLT_SHLL = 4;

                      //dflt, lsr, mssl, blsc
  public static final double[][] DMG_JT = {{1  ,1  ,1  ,1 },  //dflt
                       {1  ,.8 ,1  ,.6},  //hll
                       {1  ,1  ,.4,.8 },  //shld
                       {1  ,1  ,1  ,1 },  //mssl
                       {1  ,1  ,1  ,1 }}; //shll
  
  //+++ ++++++ +++

  public final int DMG_T;
  public final int HLL_T;
  public final double absorbtion;
  public final double density;
  
  public PhMaterial(){
    DMG_T = DMGT_DFLT;
    HLL_T = HLLT_DFLT;
    absorbtion=1;
    density=1;
  }
  
  public PhMaterial(int dmgt, int hllt, double dens, double abs){
    DMG_T = dmgt;
    HLL_T = hllt;
    absorbtion = abs;
    density=dens;
  }
  
  public double getMass(double area){
    return area*density;
  }
  
  public static double getCoeff(int hllt, int dmgt){
    return DMG_JT[hllt][dmgt];
  }
}
