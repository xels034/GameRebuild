package gui;

import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import construction.Vec2D;


public class ImageManager {

  public static int OFFS_GLXY = 0;
  public static int OFFS_GLXY_D = 5;
  public static int OFFS_STAR = 10;
  public static int OFFS_HOLE = 15;
  public static int OFFS_SUN = 16;
  public static int OFFS_SFLR = 21;
  public static int OFFS_PLNT = 26;
  public static int OFFS_GAS = 31;
  public static int OFFS_FLD = 34;
  public static int OFFS_ATMO = 38;
  public static int OFFS_RING = 41;
  public static int OFFS_LGHT = 46;
  public static int OFFS_SHDW = 47;
  public static int OFFS_CLDS = 48;
  public static int OFFS_MGMA = 49;
  public static int OFFS_HGAS = 50;
  
  public static int OFFS_ICON = 51;
  
  public static int OFFS_FX = 60;
  
  public static int SIZE_GLXY = 5;
  public static int SIZE_GLXY_D = 5;
  public static int SIZE_STAR = 5;
  public static int SIZE_HOLE = 1;
  public static int SIZE_SUN = 5;
  public static int SIZE_SFLR = 5;
  public static int SIZE_PLNT = 5;
  public static int SIZE_GAS = 3;
  public static int SIZE_FLD = 4;
  public static int SIZE_ATMO = 3;
  public static int SIZE_RING = 5;
  public static int SIZE_LGHT = 1;
  public static int SIZE_SHDW = 1;
  public static int SIZE_CLDS = 1;
  public static int SIZE_MGMA = 1;
  public static int SIZE_HGAS = 1;
  public static int SIZE_FX = 4;
  
  public static int IDX_ICN_FOCUS = 0;
  public static int IDX_ICN_ENT = 1;
  public static int IDX_ICN_CB = 2;
  public static int IDX_ICN_UP = 3;
  public static int IDX_ICN_DWN = 4;
  public static int IDX_ICN_RGHT = 5;
  public static int IDX_ICN_LEFT = 6;
  public static int IDX_ICN_SAVE = 7;
  public static int IDX_ICN_LOAD = 8;
  
  public static final int TYPE_L_IMG = 0;
  public static final int TYPE_N_IMG = 1;
  public static final int TYPE_CT_IMG = 2;
  public static final int TYPE_CT_BIMG = 3;
  
  //################################
  
  public static int MAX_IDX;
  
  //#################################
  
  private static Thread glThread;
  
  private static ArrayList<String> addresses;
  private static ArrayList<SoftReference<Image>> cache;
  
  private static Object orderMonitor;
  private static ConcurrentHashMap<String, Object> orders;
  private static Entry<String, Object> delivery;

  
  public static void setUp(Thread c){
    glThread = c;
    
    addresses = new ArrayList<>();
    cache = new ArrayList<>();
    
    orderMonitor = new Object();
    orders = new ConcurrentHashMap<>();
    delivery = new AbstractMap.SimpleEntry<>("",null);
    
    loadDefault();
    MAX_IDX = addresses.size();
  }
  
  public static int getImageIndex(String path){
    return addresses.indexOf(path);
  }
  
  public static Image getImage(int pos){
    SoftReference<Image> sr = cache.get(pos);
    if(sr!= null){
      return sr.get();
    }else{
      Image im = putOrder(addresses.get(pos));
      cache.set(pos, new SoftReference<>(im));
      return im;
    }
  }
  
  public static Image getImage(String path){
    return putOrder(path);
  }
  
  private static Image putOrder(String o){
    if(Thread.currentThread()==glThread){
      try {
        return new Image(o);
      } catch (SlickException e) {
        System.out.println("Image loading exception in AppWindow.putOrder():");
        e.printStackTrace();
        return null;
      }
    }else{
      synchronized(orderMonitor){
        String id = TYPE_L_IMG+":"+System.currentTimeMillis();
        orders.put(id, o);
        while(!delivery.getKey().equals(id)){
          try {
            orderMonitor.wait();
          } catch (InterruptedException e) {
            System.out.println("Error in getNewImage monitor waiting");
            e.printStackTrace();
          }
        }
        orders.remove(id);
        return (Image)delivery.getValue();
      }
    }  
  }
  
  public static BufferedImage convert(Image i, boolean hasAlpha){
    if(Thread.currentThread()==glThread){
      return toBufferedImage(i, hasAlpha);
    }else{
      synchronized(orderMonitor){
        String id = TYPE_CT_BIMG+":"+System.currentTimeMillis();
        orders.put(id, i);
        while(!delivery.getKey().equals(id)){
          try {
            orderMonitor.wait();
          } catch (InterruptedException e) {
            System.out.println("Error in getNewImage monitor waiting");
            e.printStackTrace();
          }
        }
        orders.remove(id);
        return (BufferedImage)delivery.getValue();
      }
    }
  }
  
  public static Image convert(BufferedImage bi){
    if(Thread.currentThread()==glThread){
      return toImage(bi, false);
    }else{
      synchronized(orderMonitor){
        String id = TYPE_CT_IMG+":"+System.currentTimeMillis();
        orders.put(id, bi);
        while(!delivery.getKey().equals(id)){
          try {
            orderMonitor.wait();
          } catch (InterruptedException e) {
            System.out.println("Error in getNewImage monitor waiting");
            e.printStackTrace();
          }
        }
        orders.remove(id);
        return (Image)delivery.getValue();
      }
    }
  }
  
  public static Image getNewImage(int x, int y){
    if(Thread.currentThread()==glThread){
      try {
        return new Image(x, y);
      } catch (SlickException e) {
        System.out.println("Error in thread:main creating new image:");
        e.printStackTrace();
        return null;
      }
    }else{
      synchronized(orderMonitor){
        String id = TYPE_N_IMG+":"+System.currentTimeMillis();
        orders.put(id, x+"-"+y);
        while(!delivery.getKey().equals(id)){
          try {
            orderMonitor.wait();
          } catch (InterruptedException e) {
            System.out.println("Error in getNewImage monitor waiting");
            e.printStackTrace();
          }
        }
        orders.remove(id);
        return (Image)delivery.getValue();
      }
    }
  }
  
  public static void generickLoadChunk() throws SlickException{
    synchronized(orderMonitor){
      if(!orders.isEmpty()){
        String msg = orders.keySet().iterator().next();
        int type = Integer.parseInt(msg.split(":")[0]);
        Image i=null;
        BufferedImage bi=null;
        switch (type){
        case TYPE_L_IMG:
          i = new Image((String)orders.get(msg));
          delivery = new AbstractMap.SimpleEntry<>(msg,i);
          break;
        case TYPE_N_IMG:
          String coords = (String)orders.get(msg);
          int x = Integer.parseInt(coords.split("-")[0]);
          int y = Integer.parseInt(coords.split("-")[1]);
          i = new Image(x,y);
          delivery = new AbstractMap.SimpleEntry<>(msg,i);
          break;
        case TYPE_CT_IMG:
          i = toImage((BufferedImage)orders.get(msg),false);
          delivery = new AbstractMap.SimpleEntry<>(msg,i);
          break;
        case TYPE_CT_BIMG:
          bi = toBufferedImage((Image)orders.get(msg), false);
          delivery = new AbstractMap.SimpleEntry<>(msg, bi);
          break;
        }
        orderMonitor.notifyAll();
      }
    }
  }
  
  public static Color getSample(Image i, Rectangle2D.Double bounds, double rotAngle){
    
    Vec2D lookAt = new Vec2D(-bounds.x, -bounds.y);
    
    lookAt.x /= bounds.width;
    lookAt.x *= i.getWidth();
    
    lookAt.y /= bounds.height;
    lookAt.y *= i.getHeight();
    
    //rotation
    
    lookAt.x -= i.getWidth()/2;
    lookAt.y -= i.getHeight()/2;
    
    lookAt.y*=-1;
    
    float tmpx = (float)(lookAt.x*Math.cos(rotAngle)-lookAt.y*Math.sin(rotAngle));
    float tmpy = (float)(lookAt.x*Math.sin(rotAngle)+lookAt.y*Math.cos(rotAngle));
    lookAt.x = tmpx;
    lookAt.y = tmpy;
    
    //System.out.println("x: "+lookAt.x+" y: "+lookAt.y);
    lookAt.y*=-1;
    
    lookAt.x += i.getWidth()/2;
    lookAt.y += i.getHeight()/2;
    
    //interpolation
    
    int prevX= (int)lookAt.x;
    int nextX = (int)lookAt.x+1;
    
    int prevY = (int)lookAt.y;
    int nextY = (int)lookAt.y+1;
    
    //System.out.println("px: "+prevX+" nx: "+nextX);
    //System.out.println("py: "+prevY+" ny: "+nextY);
    
    float dx = (float)lookAt.x-prevX;
    float dy = (float)lookAt.y-prevY;
    
    //System.out.println("dx: "+dx+" dy: "+dy);
    
    Color c1 = i.getColor(prevX, prevY);
    Color c2 = i.getColor(nextX, prevY);
    Color c3 = i.getColor(prevX, nextY);
    Color c4 = i.getColor(nextX, nextY);
    
    float r,g,b,a;
    
    r = c1.r*(1-dx)+c2.r*dx;
    g = c1.g*(1-dx)+c2.g*dx;
    b = c1.b*(1-dx)+c2.b*dx;
    a = c1.a*(1-dx)+c2.a*dx;
    
    Color c12 = new Color(r,g,b,a);
    
    r = c3.r*(1-dx)+c4.r*dx;
    g = c3.g*(1-dx)+c4.g*dx;
    b = c3.b*(1-dx)+c4.b*dx;
    a = c3.a*(1-dx)+c4.a*dx;
    
    Color c34 = new Color(r,g,b,a);
    
    r = c12.r*(1-dy)+c34.r*dy;
    g = c12.g*(1-dy)+c34.g*dy;
    b = c12.b*(1-dy)+c34.b*dy;
    a = c12.a*(1-dy)+c34.a*dy;
    
    return new Color(r,g,b,a);
  }
  
  private static void loadDefault(){
    addresses.add("assets/galaxy/color/uniform_img.png");
    addresses.add("assets/galaxy/color/barred_img.png");
    addresses.add("assets/galaxy/color/spiral_img.png");
    addresses.add("assets/galaxy/color/halo_img.png");
    addresses.add("assets/galaxy/color/irregular_img.png");
    
    addresses.add("assets/galaxy/dens/uniform_density.png");
    addresses.add("assets/galaxy/dens/barred_density.png");
    addresses.add("assets/galaxy/dens/spiral_density.png");
    addresses.add("assets/galaxy/dens/halo_density.png");
    addresses.add("assets/galaxy/dens/irregular_density.png");
    
    addresses.add("assets/star/star_r_new.png");
    addresses.add("assets/star/star_o_new.png");
    addresses.add("assets/star/star_w_new.png");
    addresses.add("assets/star/star_b_new.png");
    addresses.add("assets/star/star_u_new.png");
    
    addresses.add("assets/star/bHole/black_hole.png");
    
    addresses.add("assets/star/sun/sun_r_new.png");
    addresses.add("assets/star/sun/sun_o_new.png");
    addresses.add("assets/star/sun/sun_w_new.png");
    addresses.add("assets/star/sun/sun_b_new.png");
    addresses.add("assets/star/sun/sun_u_new.png");
    
    /*addresses.add("assets/star/sun/sun_r_flare.png");
    addresses.add("assets/star/sun/sun_o_flare.png");
    addresses.add("assets/star/sun/sun_w_flare.png");
    addresses.add("assets/star/sun/sun_b_flare.png");
    addresses.add("assets/star/sun/sun_u_flare.png");*/
    
    addresses.add("assets/fx/flare_red.png");
    addresses.add("assets/fx/flare_orange.png");
    addresses.add("assets/fx/flare_yellow.png");
    addresses.add("assets/fx/flare_blue.png");
    addresses.add("assets/fx/flare_violet.png");
    
    addresses.add("assets/planet/solid/sur_Molt_b.png");
    addresses.add("assets/planet/solid/sur_Rock.png");
    addresses.add("assets/planet/solid/sur_Metal.png");
    addresses.add("assets/planet/solid/sur_Mineral.png");
    addresses.add("assets/planet/solid/sur_Prec.png");
    
    addresses.add("assets/planet/gas/gas_Oxy.png");
    addresses.add("assets/planet/gas/gas_Hel.png");
    addresses.add("assets/planet/gas/gas_Hydro.png");
    
    addresses.add("assets/planet/modifier/water_low.png");
    addresses.add("assets/planet/modifier/water_med.png");
    addresses.add("assets/planet/modifier/water_hig.png");
    addresses.add("assets/planet/modifier/water_sld.png");
    
    addresses.add("assets/planet/atmosphere/atmo_Oxy.png");
    addresses.add("assets/planet/atmosphere/atmo_Hel.png");
    addresses.add("assets/planet/atmosphere/atmo_Hydro.png");
    
    addresses.add("assets/planet/ring/ring_Molten.png");
    addresses.add("assets/planet/ring/ring_Rock.png");
    addresses.add("assets/planet/ring/ring_Metal.png");
    addresses.add("assets/planet/ring/ring_Mineral.png");
    addresses.add("assets/planet/ring/ring_Prec.png");
    
    addresses.add("assets/planet/modifier/nightLights.png");
    addresses.add("assets/planet/modifier/shadow_noA.png");
    addresses.add("assets/planet/modifier/clouds.png");
    addresses.add("assets/planet/solid/sur_Molt_g.png");
    addresses.add("assets/planet/gas/gas_heat.png");
    
    addresses.add("assets/gui/icons/focus.png");
    addresses.add("assets/gui/icons/enter.png");
    addresses.add("assets/gui/icons/cBody.png");
    addresses.add("assets/gui/icons/up.png");
    addresses.add("assets/gui/icons/down.png");
    addresses.add("assets/gui/icons/right.png");
    addresses.add("assets/gui/icons/left.png");
    addresses.add("assets/gui/icons/save.png");
    addresses.add("assets/gui/icons/load.png");
    
    addresses.add("assets/fx/ep_test.png");
    addresses.add("assets/fx/ep_b_test.png");
    addresses.add("assets/fx/spark.png");
    addresses.add("assets/fx/glow_test.png");
    addresses.add("assets/fx/glow_test_b.png");
    
    for(int i=0;i<addresses.size();i++){
      cache.add(null);
    }
  }
  
  public static BufferedImage toBufferedImage(org.newdawn.slick.Image image, boolean hasAlpha) {
    // convert the image into a byte buffer by reading each pixel in turn
    int len = 4 * image.getWidth() * image.getHeight();
        if (!hasAlpha) {
           len = 3 * image.getWidth() * image.getHeight();
        }

        ByteBuffer out = ByteBuffer.allocate(len);
        org.newdawn.slick.Color c;

        for (int y = image.getHeight()-1; y >= 0; y--) {
          for (int x = 0; x < image.getWidth(); x++) {
            c = image.getColor(x, y);

            out.put((byte) (c.r * 255.0f));
            out.put((byte) (c.g * 255.0f));
            out.put((byte) (c.b * 255.0f));
            if (hasAlpha) {
              out.put((byte) (c.a * 255.0f));
            }
          }
        }
        // create a raster of the correct format and fill it with our buffer
        DataBufferByte dataBuffer = new DataBufferByte(out.array(), len);
        PixelInterleavedSampleModel sampleModel;
        ColorModel cm;

        if (hasAlpha) {
          int[] offsets = { 0, 1, 2, 3 };
          sampleModel = new PixelInterleavedSampleModel(
              DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 4,
              4 * image.getWidth(), offsets);

          cm = new ComponentColorModel(ColorSpace
              .getInstance(ColorSpace.CS_sRGB), new int[] { 8, 8, 8, 8 },
              true, false, Transparency.TRANSLUCENT,
              DataBuffer.TYPE_BYTE);
        }else{
          int[] offsets = { 0, 1, 2};
          sampleModel = new PixelInterleavedSampleModel(
              DataBuffer.TYPE_BYTE, image.getWidth(), image.getHeight(), 3,
              3 * image.getWidth(), offsets);

          cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                    new int[] {8,8,8,0},
                    false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        }
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        // finally create the buffered image based on the data from the texture
        // and spit it through to ImageIO
        return new BufferedImage(cm, raster, false, null);       
      }
  
  public static Image toImage(BufferedImage bi, boolean premul){
    //Quite unimaginative by graphics.draw. but as long as there's only one flush maybe its ok
    Image i = null;
    int[] pixel = new int[3];
    Raster r = null;
    Graphics g=null;
    
    try{
      i = new Image(bi.getWidth(), bi.getHeight());
      r = bi.getRaster();
      g = i.getGraphics();
    }catch(SlickException x){
      System.out.println("Error in toImage");
      x.printStackTrace();
    }
    float ar=0;
    float ge=0;
    float be=0;
    float ai=1;
    
    for(int x=0;x<bi.getWidth();x++){
      for(int y=0;y<bi.getHeight();y++){
        r.getPixel(x, y, pixel);
        ar = pixel[0]/255f;
        ge = pixel[1]/255f;
        be = pixel[2]/255f;
        if(premul){
          ai = (ar+ge+be)/3f;
        }
        g.setColor(new Color(ar,ge,be,ai));
        g.drawLine(x, y, x, y);
      }
    }
    g.flush();
    i.setFilter(Image.FILTER_LINEAR);
    return i;
  }
}
