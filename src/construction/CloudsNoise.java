/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package construction;

import gui.ImageManager;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.Random;

import org.newdawn.slick.Image;


/**
 *
 * @author Xels
 */
public class CloudsNoise {

    public static final int TYPE_NONE=0;
    public static final int TYPE_BILINEAR=1;
    public static final int TYPE_BICUBIC=2;
    private final long MAX;//=1000000000; //1.000.000.000
    
    private int mode;
    private Raster density;
    //private ValueRamp ramp;
    private ValueRamp slope;
    private Vec2D offset;
    private Random rGen;
    //private int sType;   //0: for universe
              //1: with texture, galaxies
              //2: with ramp, asteroid-belts
    
    public CloudsNoise(BufferedImage densTex, long mx, int m){
      mode=m;
      MAX=mx;

      density = densTex.getRaster();

      slope=new ValueRamp();
      offset = new Vec2D(0,0);
      rGen = new Random();
    }
    
    /*public CloudsNoise(ValueRamp vr, long mx, int m){
      mode=m;
      MAX=mx;
      ramp=vr;
      slope=new ValueRamp();
      offset = new Pnt2D(0,0);
      rGen = new Random();
      
      sType=2;
      density=null;
    }*/
    
    public CloudsNoise(long mx, int m){
      mode=m;
      MAX=mx;
      slope=new ValueRamp();
      offset = new Vec2D(0,0);
      rGen = new Random();

      density=null;
    }
    
    public Image getTexRB(int width, int height, long seed, double noiseSize, int depth, double distStr){
      
      BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
      WritableRaster wr = bi.getRaster();
      
      /*Random r = new Random();
      r.setSeed(seed);
      
      int shiftX = r.nextInt();
      int shiftY = r.nextInt();*/
      
      rGen.setSeed(seed);
      
      

      for(int y=0;y<height;y++){
        for(int x=0;x<width;x++){
          
          //int ar = (int)(getDistortedNoiseBWPoint2D(x+shiftX,   y+shiftY   ,noiseSize,depth,noiseSize*(distStr/100))*255);
          //int be = (int)(getDistortedNoiseBWPoint2D(x+shiftY   ,y+shiftX   ,noiseSize,depth,noiseSize*(distStr/100))*255);
          int ar = (int)(getDistortedNoiseBWPoint2D(x+offset.x, y+offset.y, noiseSize,depth,noiseSize*(distStr/100))*255);
          int be = (int)(getDistortedNoiseBWPoint2D(y+offset.y, x+offset.x, noiseSize,depth,noiseSize*(distStr/100))*255);
          float mod = (float)(getDistortedNoiseBWPoint2D(y+offset.y ,-(x+offset.x),noiseSize,depth,noiseSize*(distStr/100)));
          
          ar = Math.max(0,ar-55);
          be = Math.max(0,be-55);
          ar/=6;
          be/=3;
          mod/=4;
          mod+=.3;
          
          //wr.setPixel(x, y, new int[]{ar+be/3,be/2,be});
          wr.setPixel(x, y, new int[]{ar+(int)(be*mod*.7),(int)(be*mod),be});
          
          
          //wr.setPixel(x, y, new int[]{ar,ar,ar});
        }
      }
      return ImageManager.convert(bi);
    }
    
    public void setValueRamp(ValueRamp v){
      slope=v;
    }
    
    public void setOffset(Vec2D d){
      offset=d;
    }

    public Double[][] getPointsDistorted2D(double width, double height, int amount, long seed, double noiseSize, int depth, double distStr) throws IllegalArgumentException{
      LinkedList<Double[]> points = new LinkedList<>();
        int actAm=0;
        double tmpX;
        double tmpY;
        
        rGen.setSeed(seed);
        while(actAm<amount){

          tmpX=(rGen.nextDouble()*width)+offset.x;
            tmpY=(rGen.nextDouble()*height)+offset.y;

            double rSample = getDistortedNoiseBWPoint2D(tmpX,tmpY,noiseSize,depth,noiseSize*(distStr/100));
            double tSample=1;

            tSample=tVal(tmpX, tmpY);

            double rand=rGen.nextDouble();
            if(tmpX>-MAX && tmpX<MAX && tmpY>-MAX && tmpY<MAX){
              if(rand<(rSample*tSample)/2){
                  Double[] tmpArr ={tmpX,tmpY,rSample*tSample};
                  points.add(tmpArr);
              }
            }else{
              points.clear();
            }
            actAm++;
        }
        return points.toArray(new Double[0][0]);
    }

    public double getDistortedNoiseBWPoint2D(double x, double y, double size, int depth, double str) throws IllegalArgumentException{
        if(mode>=0 && mode<=2){
        //distort the texture for the sample by another one(in this case itself).
          //High values mean high shift in xy direction
          double base = noiseComposite2D(x,y+1500, size, depth);
          x+=base*(str*100);
          y+=base*(str*100);
          return noiseComposite2D(x,y+1500, size, depth);
        }else{
          throw new IllegalArgumentException();
        }
    }


    private double noiseComposite2D(double x, double y, double interval, int depth){
        //add depth layers of simple noise ontop, to get nice fractal features
        if(depth>0){
            double vAbsC=0;
            for(int i=1;i<=depth;i++){
                vAbsC+=noisePoint2D(x,y,interval/(2*i))/(1+((i-1)/10.0));
            }
            if(vAbsC<0){
                vAbsC*=-1;
            }
            
            vAbsC/=depth;

            return Math.min(1, vAbsC);
        }else{
            return 0.5f;
        }
    }

    private double noisePoint2D(double x, double y, double interval){
        //works only in 2D. Had a solution for n-D, but overlaying dimensions looked
        //crappy. By doing all (2) dimensions at a time, overlaying is replaced by dividing,
        //giving very random, desireable, results. Dividing din't work on n-D, because
        //the interpolation would also get dividied, giving ugly bands-noise
        double res=0;

        //absolute distance from the last "whole" point
        double dx=x%interval;
        double dy=y%interval;

        //weights for interpolating
        double wx=dx/interval;
        double wy=dy/interval;

        switch(mode){
            case 0:
                //non-interpolated
                res=(rVal(x-dx)/rVal(y-dy))%1;
                break;
            case 1:
                //bi-linear
                //interpolation of 2 interpolations of 2 nearest (4 total) values
                //x-dx to get to first "whole" value

                //base values. overlayed they would look ugly
                double sX1=1/(rVal(x-dx)+0.1f);
                double sY1=1/(rVal(y-dy)+0.1f);
                double sX2=1/(rVal(x-dx+interval)+0.1f);
                double sY2=1/(rVal(y-dy+interval)+0.1f);
                //by dividing them, nice randomnes is gained
                double x1y1=(sX1/sY1)%1;
                double x1y2=(sX1/sY2)%1;
                double x2y1=(sX2/sY1)%1;
                double x2y2=(sX2/sY2)%1;
                //first 2 interpolations
                double ix1 = x1y1*(1-wx)+x2y1*wx;
                double ix2 = x1y2*(1-wx)+x2y2*wx;
                //interpolation oder interpolations: bi-linear interpolation
                res = ix1*(1-wy)+ix2*wy;
                break;
            case 2:
                //bi-cubic interpolation. works like linear, but with 4 nearest
                //values in onre dimension instead of 2, 16 total
                double[] baseX = new double[4];
                double[] baseY = new double[4];
                double[][]coefficients = new double[4][4];
                double[] partialResults = new double[4];

                //filling base values. baseX/baseY is the actual cell value
                for(int i=0;i<4;i++){
                    baseX[i]=1/(rVal(x-dx+(interval*(i-1)))+0.1f);
                    baseY[i]=1/(rVal(y-dy+(interval*(i-1)))+0.1f);
                }

                //get the real coefficients
                for(int xi=0;xi<4;xi++){
                    for(int yi=0;yi<4;yi++){
                        coefficients[xi][yi]=(baseX[xi]/baseY[yi])%1;
                    }
                }

                //interpolating first dimension
                for(int i=0;i<4;i++){
                    partialResults[i]=cubic1D(coefficients[i],wy);
                }
                //interpolating 2nd dimension: bi-cubic
                res=cubic1D(partialResults,wx);
                break;
        }
        //applying Valueramp
        return slope.getValue(res);
    }

    private double cubic1D(double[] x, double w){
        //cubic interpolation on 4 nearest points in 1 dimension
        //some magic mumbo-jumbo, copied from the internet
        //4 cubic1D can be interpolated to get bi-cubic (16 samples total)
        //4 times 4 cubic1D to get tri-cubic and so on  (64 samples total)
        double a0,a1,a2,a3;
        a0=x[3]-x[2]-x[0]+x[1];
        a1=x[0]-x[1]-a0;
        a2=x[2]-x[0];
        a3=x[1];
        return (a0*(w*w*w)+a1*(w*w)+(a2*w)+(a3));
    }

    private double rVal(double seed){
        Random r=new Random();
        long l=(long)(seed+1)*190287087698765897L;
        r.setSeed(l);
        return r.nextDouble();
    }
    
    private double tVal(double x, double y){
      int ix=(int)Math.floor(x/1000)+density.getWidth()/2;
      int iy=(int)Math.floor(y/1000)+density.getHeight()/2;
      
      //because toBufferedImage inverts my texture
      ix = density.getWidth()-ix;
      
      double val;
      
      if(ix<density.getWidth() && ix>0 && iy<density.getHeight() && iy>0){
        int[] cols = new int[3];
        cols=density.getPixel(ix, iy, cols);
        
        val=cols[0]+cols[1]+cols[2];
        
        val/=(3*256);
      }else{
        val=0;
      }
      return val;
    }
    
    /*private double vVal(double x, double y){  
      double d = Math.hypot(x, y);
      return ramp.getValue(d/MAX);
    }*/
    
    
    
    @Deprecated
    public BufferedImage getDistortedNoiseBW2D(int width, int height, int size, int depth, double str) throws IllegalArgumentException{
        if(mode>=0 && mode<=2){
        //see getDistortedNoiseWBPoint2D. basically the same, but for a whole image
          //BufferedImage distBase = getNoiseBW2D(width,height,size);
          BufferedImage result = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
          //WritableRaster distBaseWR = distBase.getRaster();
          WritableRaster resultWR = result.getRaster();
          System.out.println("getDistortedNoiseBW2D");
          for(int y=0;y<height;y++){
              for(int x=0;x<width;x++){
                double c = getDistortedNoiseBWPoint2D(x,y+2*height,size,depth,size*(str/100));
  
                  
                  //System.out.println(c);
                  int[] tmpArr = new int[3];
                  /*Color col = new Color(Color.HSBtoRGB((float)(c+0.5)%1, 0.15f, (float)c));
                  tmpArr[0]=col.getRed();
                  tmpArr[1]=col.getGreen();
                  tmpArr[2]=col.getBlue();*/
                  tmpArr[0]=(int)(c*255);
                  tmpArr[1]=(int)(c*255);
                  tmpArr[2]=(int)(c*255);

                  resultWR.setPixel(x, y, tmpArr);
              }
              if((y+1)%100==0 || y==height){
                  System.out.println("Line "+(y+1)+" of "+height+" done");
              }
          }
          result.setData(resultWR);
          return result;
        }else{
          throw new IllegalArgumentException();
        }
    }

    @Deprecated
    public BufferedImage getNoiseBW2D(int width, int height, int size, int depth)throws IllegalArgumentException{
        if(mode>=0 && mode<=2){
        //calculate the function for every pixel of an image, gives a nice texture
          BufferedImage bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
          WritableRaster wr=bi.getRaster();
          System.out.println("getNoiseBW2D");
          for(int y=0;y<height;y++){
              for(int x=0;x<width;x++){
                  //y+height is used to offset the noise pattern in one dimenstion to give
                  //a bit more varied results
  
                  //gets rid of the cross-ness by clever dividing, but limited to 2 dimensions
                  double c=noiseComposite2D(x,y+height,size,depth);
  
                  //values are between 0-1, so multiply for standard rgb colors
                  int[] tmpArr = new int[3];
                  //Color col = new Color(Color.HSBtoRGB((float)c, 1f, (float)c));
                  
                  tmpArr[0]=(int)(c*255);
                  tmpArr[1]=(int)(c*255);
                  tmpArr[2]=(int)(c*255);
                  /*tmpArr[0]=col.getRed();
                  tmpArr[1]=col.getGreen();
                  tmpArr[2]=col.getBlue();*/
                  wr.setPixel(x, y, tmpArr);
              }
              if((y+1)%100==0 || y==height){
                  System.out.println("Line "+(y+1)+" of "+height+" done");
              }
          }
          bi.setData(wr);
          return bi;
        } else{
          throw new IllegalArgumentException();
        }
    }
    
    /*@Deprecated
    public static BufferedImage getStarMapDistorted(int width, int height, int amount, long seed, int noiseSize, int depth, double distStr, boolean crisp, int mode) throws IllegalArgumentException{
        if(mode>=0 && mode<=2){
        //basically a timestamp, used for benchmarking at the end
          long ts = System.currentTimeMillis();
          //map gets saved into an image right now
          BufferedImage map = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
          WritableRaster wr = map.getRaster();
          int actAm=0;
          int tmpX;
          int tmpY;
          Random r = new Random();
          r.setSeed(seed);
          System.out.println("getStarMapDistorted");
          while(actAm<amount){
              //get a random point in the image, nextdouble() creates always a new value between 0 and 1
              tmpX=(int)(r.nextDouble()*width);
              tmpY=(int)(r.nextDouble()*height);
              //see how likely a point is there
              double sample = getDistortedNoiseBWPoint2D(tmpX,tmpY,noiseSize,depth, distStr, crisp, mode);
              if(r.nextDouble()<sample/2){
                  if(wr.getPixel(tmpX, tmpY, new int[3])[0]==0){
                      //make the star darker according to the sample, gives prettier image
                      int mod = (int)(255*(sample*2));
                      int[] cArr = {mod,mod,mod};
                      wr.setPixel(tmpX, tmpY, cArr);
                      actAm++;
                      if(actAm%100==0||actAm==amount){
                          System.out.println(actAm+" of "+amount+" Stars done");
                      }
                  }
              }
          }
          map.setData(wr);
  
          ts=System.currentTimeMillis()-ts;
          System.out.println("Took "+Math.round(ts/1000)+" seconds.");
          return map;
        }else{
          throw new IllegalArgumentException();
        }
    }*/
}
