package view;

import gui.Def;
import gui.UIButton;
import gui.UITheme;

import org.newdawn.slick.GameContainer;

public class AssetsEditorView extends AbstractView{

  public AssetsEditorView(GameContainer gc) {
    super(gc);

    UIButton b = new UIButton("Exit to Menu",
          "Exits to the main menu\nof the game",
          20, 20,
          UITheme.font.getWidth("Exit to Menu")+10, 20,
          Def.A_ENTER_MAIN_MENU, null);
    children.add(b);
  }

}
