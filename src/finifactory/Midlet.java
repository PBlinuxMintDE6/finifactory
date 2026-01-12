/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 * @author c
 */
public class Midlet extends MIDlet {

    public static final int DPAD_UP = -1;
    public static final int DPAD_DOWN = -2;
    public static final int DPAD_LEFT = -3;
    public static final int DPAD_RIGHT = -4;
    public static final int DPAD_OK = -5;
    public static final int DPAD_BUMPER_LEFT = -6;
    public static final int DPAD_BUMPER_RIGHT = -7;

    Display display;
    GameCanvas canvas;

    public void startApp() {
        display = Display.getDisplay(this);
        canvas = new GameCanvas();
        display.setCurrent(canvas);
        canvas.startLoop();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
        canvas.stopLoop();
    }

    public class GameCanvas extends Canvas {

        private Thread loopThread;
        private boolean running = false;

        protected void paint(Graphics g) {

        }

        protected void keyPressed(int keyCode) {
            switch (keyCode) {
                case DPAD_UP:
                    break;
                case DPAD_DOWN:
                    break;
                case DPAD_LEFT:
                    break;
                case DPAD_RIGHT:
                    break;
                case KEY_NUM2:
                    break;
                case KEY_NUM8:
                    break;
                case KEY_NUM4:
                    break;
                case KEY_NUM6:
                    break;
            }
        }

        private void startLoop() {
            running = true;
            loopThread = new Thread() {
                public void run() {
                    while (running) {
                        repaint();
                        serviceRepaints();

                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            loopThread.start();
        }

        private void stopLoop() {
            running = false;
            if (loopThread != null) {
                loopThread.interrupt();
                loopThread = null;
            }
        }
    }
}
