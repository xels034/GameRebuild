package drawable;

import java.awt.geom.Rectangle2D;

import construction.Vec2D;

public class BGRasterable extends Rasterable{

  private double paralax;
  
  public BGRasterable(Vec2D p, double par) {
    super(p);
    paralax=par;
    // TODO Auto-generated constructor stub
  }
  
  @Override
  public Rectangle2D.Double getScreenBounds(Vec2D f, double zFactor, Vec2D screen, int i){
    double bSD = Math.max(screen.x,  screen.y);
    double sRD = Math.min(bounds.width, bounds.height);
    Rectangle2D.Double rec = new Rectangle2D.Double();

    double scaleFac = bSD/sRD;
    scaleFac*=1.25;
    
    rec.x = (screen.x-(bounds.width*scaleFac))/2;
    rec.y = (screen.y-(bounds.height*scaleFac))/2;
    
    rec.width = bounds.width*scaleFac;
    rec.height = bounds.height*scaleFac;
    
    rec.x-=f.x*paralax;
    rec.y-=f.y*paralax;
    
    return rec;
  }

}
