package gui;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.newdawn.slick.opengl.Texture;

public class GL_Blender{
  public static final int MODE_NORMAL = 665;
  
  public static final int MODE_TRUE_MULTIPLY = 666;
  public static final int MODE_TRUE_ADD = 667;
  public static final int MODE_TRUE_SCREEN = 668;
  public static final int MODE_TRUE_SUBTRACT = 669;
  public static final int MODE_ERASE = 670;
  
  public static void setDrawMode(int m){
    switch(m){
    case MODE_TRUE_MULTIPLY:
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL14.glBlendEquation(GL14.GL_FUNC_ADD);
      break;
    case MODE_TRUE_ADD:
          GL11.glEnable(GL11.GL_BLEND);
          GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
          GL14.glBlendEquation(GL14.GL_FUNC_ADD);
      break;
    case MODE_TRUE_SCREEN:
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR);
      GL14.glBlendEquation(GL14.GL_FUNC_ADD);
      break;
    case MODE_TRUE_SUBTRACT:
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_COLOR);
      GL14.glBlendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
      break;
    case MODE_ERASE:
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_ZERO);
      GL14.glBlendEquation(GL14.GL_FUNC_ADD);
      break;
    case MODE_NORMAL:
      //Reset to default. Graphics doesn't do anything thats needed
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL14.glBlendEquation(GL14.GL_FUNC_ADD);
      break;
    }
  }
  
  @Deprecated
  public static void blur_tex(Texture t, int passes, int radius){
    //NIFTY BLUR CODE. Copied from the interwebz. Dunno if it works
    //<<UPDATE>> ... nope, doesn't provide anything useful -_-
      int i, x, y;
      i = x = y = 0;
      int WIDTH = t.getTextureWidth()-radius;
      int HEIGHT = t.getTextureHeight()-radius;
      
      GL11.glEnable(GL11.GL_BLEND);
      GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, t.getTextureID());
      while (passes > 0) {
          i = 0;
          for (x = 0; x < 2; x++) {
              for (y = 0; y < 2; y++, i++) {
                GL11.glColor4f (1.0f,1.0f,1.0f,1.0f / (i+1));
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glTexCoord2f(0 + (x-0.5f)/WIDTH, 1 + (y-0.5f)/HEIGHT); GL11.glVertex2f(0, 0);
                GL11.glTexCoord2f(0 + (x-0.5f)/WIDTH, 0 + (y-0.5f)/HEIGHT); GL11.glVertex2f(0, HEIGHT);
                GL11.glTexCoord2f(1 + (x-0.5f)/WIDTH, 1 + (y-0.5f)/HEIGHT); GL11.glVertex2f(WIDTH, 0);
                GL11.glTexCoord2f(1 + (x-0.5f)/WIDTH, 0 + (y-0.5f)/HEIGHT); GL11.glVertex2f(WIDTH, HEIGHT);
                GL11.glEnd ();
              }
          }
          GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 0, 0, WIDTH, HEIGHT, 0);
          passes--;
      }
      GL11.glDisable(GL11.GL_BLEND);
  }
  
}
