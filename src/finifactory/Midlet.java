/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.xml.*;

/**
 * @author c
 */
public class Midlet extends MIDlet implements CommandListener {

    private Display display;
    private List menu;
    private Command exitCommand;
    
    private GameState gameState;
    private GameView gameView;
    private BuildSystem buildSystem;
    private Simulation simulation;

    protected void startApp() {
        if (gameState == null) {
            XMLLoader.load("/finifactory/definition.xml");
            gameState = new GameState();
            buildSystem = new BuildSystem(gameState);
            simulation = new Simulation(gameState);
            gameView = new GameView(gameState, this, buildSystem, simulation);
            gameView.start();
        }
        
        Display.getDisplay(this).setCurrent(gameView);
        /*
        display = Display.getDisplay(this);

        menu = new List("Choose an option", List.IMPLICIT);
        menu.append("Start Game", null);
        menu.append("Load Game", null);
        menu.append("Options", null);
        menu.append("About", null);
        
        exitCommand = new Command("Back", Command.BACK, 1);
        menu.addCommand(exitCommand);
        menu.setCommandListener(this);

        display.setCurrent(menu);
        */
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            int index = menu.getSelectedIndex();
            String choice = menu.getString(index);
            System.out.println(choice);
        } else if (c == exitCommand) {
            notifyDestroyed();
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
}
