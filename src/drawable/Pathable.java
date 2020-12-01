package drawable;

import java.util.LinkedList;

import construction.Line;
import construction.ValueRamp;
import construction.Vec2D;

public interface Pathable {

  public LinkedList<Line> getTransformedLines(Vec2D f, Vec2D screen, double zFactor);
  public void setValueRamp(ValueRamp vr);
  public Line getBounds();
}
