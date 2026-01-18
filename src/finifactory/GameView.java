/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

import javax.microedition.lcdui.game.*;
import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Enumeration;

/**
 *
 * @author c
 */
public class GameView extends GameCanvas implements Runnable {

    private final GameState state;
    private final Midlet midlet;
    private final BuildSystem buildSystem;
    private final Simulation simulation;

    private boolean paused = false;

    // Camera (top-left tile)
    private int camX = 0;
    private int camY = 0;

    // Cursor (tile coords)
    private int cursorX = 0;
    private int cursorY = 0;

    // Viewport size (in tiles)
    private final int viewWidth;
    private final int viewHeight;

    private int prevKeyStates = 0;

    private long lastTime = System.currentTimeMillis();
    private long simAccumulator = 0;

// Track how long each key has been held (ms)
    private long leftHold = 0, rightHold = 0, upHold = 0, downHold = 0;

// Timing thresholds
    private static final long INITIAL_DELAY = 200; // ms before repeating starts
    private static final long REPEAT_DELAY = 50;   // ms between repeats

    private Image[][] tileImages;

    private final int emptyCol = 0xC7C7C7;

    public GameView(GameState state, Midlet midlet, BuildSystem buildSystem, Simulation simulation) {
        super(true);
        setFullScreenMode(true);
        this.state = state;
        this.midlet = midlet;
        this.buildSystem = buildSystem;
        this.simulation = simulation;

        viewWidth = getWidth() / Configuration.Tile.WIDTH;
        viewHeight = (getHeight() / Configuration.Tile.HEIGHT) - Configuration.Map.Y_OFFSET;
    }

    private Image[] generateDir(Image src) {
        Image[] images = new Image[4];
        images[0] = src;
        images[1] = rotate90(images[0]);
        images[2] = rotate90(images[1]);
        images[3] = rotate90(images[2]);
        return images;
    }

    private Image rotate90(Image src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] srcPixels = new int[w * h];
        int[] dstPixels = new int[w * h];

        src.getRGB(srcPixels, 0, w, 0, 0, w, h);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                dstPixels[x * h + (h - y - 1)]
                        = srcPixels[y * w + x];
            }
        }

        return Image.createRGBImage(dstPixels, h, w, true);
    }

    public void start() {
        new Thread(this).start();
    }

    public void run() {
        Graphics g = getGraphics();

        while (true) {
            long now = System.currentTimeMillis();
            long delta = now - lastTime;
            lastTime = now;
            if (paused) {
                continue;
            }
            simAccumulator += delta;
            updateInput();
            updateCamera();
            while (simAccumulator >= Configuration.Init.SIM_STEP_MS) {
                simulation.tick();
                simAccumulator -= Configuration.Init.SIM_STEP_MS;
            }
            render(g);
            flushGraphics();

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
        }
    }

    private void updateInput() {
        int key = getKeyStates();
        long now = System.currentTimeMillis();

        // LEFT
        if ((key & LEFT_PRESSED) != 0) {
            if ((prevKeyStates & LEFT_PRESSED) == 0) {
                // First press
                cursorX--;
                leftHold = now;
            } else if (now - leftHold >= INITIAL_DELAY) {
                cursorX--;
                leftHold = now - (INITIAL_DELAY - REPEAT_DELAY); // next repeat in REPEAT_DELAY
            }
        } else {
            leftHold = 0;
        }

        // RIGHT
        if ((key & RIGHT_PRESSED) != 0) {
            if ((prevKeyStates & RIGHT_PRESSED) == 0) {
                cursorX++;
                rightHold = now;
            } else if (now - rightHold >= INITIAL_DELAY) {
                cursorX++;
                rightHold = now - (INITIAL_DELAY - REPEAT_DELAY);
            }
        } else {
            rightHold = 0;
        }

        // UP
        if ((key & UP_PRESSED) != 0) {
            if ((prevKeyStates & UP_PRESSED) == 0) {
                cursorY--;
                upHold = now;
            } else if (now - upHold >= INITIAL_DELAY) {
                cursorY--;
                upHold = now - (INITIAL_DELAY - REPEAT_DELAY);
            }
        } else {
            upHold = 0;
        }

        // DOWN
        if ((key & DOWN_PRESSED) != 0) {
            if ((prevKeyStates & DOWN_PRESSED) == 0) {
                cursorY++;
                downHold = now;
            } else if (now - downHold >= INITIAL_DELAY) {
                cursorY++;
                downHold = now - (INITIAL_DELAY - REPEAT_DELAY);
            }
        } else {
            downHold = 0;
        }

        // FIRE / SELECT — usually just one action per press
        if ((key & FIRE_PRESSED) != 0 && (prevKeyStates & FIRE_PRESSED) == 0) {
            if (cursorY > -1) {
                showTileMenu(state.getTile(cursorX, cursorY));
            } else {
                showPauseMenu();
            }
        }

        prevKeyStates = key;

        // Clamp cursor to map
        if (cursorX < 0) {
            cursorX = 0;
        }
        if (cursorY < -1) {
            cursorY = -1;
        }
        if (cursorX >= Configuration.Map.WIDTH) {
            cursorX = Configuration.Map.WIDTH - 1;
        }
        if (cursorY >= Configuration.Map.HEIGHT) {
            cursorY = Configuration.Map.HEIGHT - 1;
        }
    }

    private static int[] rotatePoint(int x, int y, int rotation) {
        rotation = rotation % 4;

        switch (rotation) {
            case Enum.Direction.DOWN:
                return new int[]{x, y};
            case Enum.Direction.LEFT:
                return new int[]{-y, x};
            case Enum.Direction.UP:
                return new int[]{-x, -y};
            case Enum.Direction.RIGHT:
                return new int[]{y, -x};
            default:
                return new int[]{x, y};
        }
    }

    private void updateCamera() {
        int margin = 2;

        if (cursorX < camX + margin) {
            camX = cursorX - margin;
        }
        if (cursorY < camY + margin) {
            camY = cursorY - margin;
        }
        if (cursorX > camX + viewWidth - margin - 1) {
            camX = cursorX - viewWidth + margin + 1;
        }
        if (cursorY > camY + viewHeight - margin - 1) {
            camY = cursorY - viewHeight + margin + 1;
        }

        // Clamp camera
        if (camX < 0) {
            camX = 0;
        }
        if (camY < 0) {
            camY = 0;
        }
        if (camX > Configuration.Map.WIDTH * Configuration.Tile.WIDTH - viewWidth) {
            camX = Configuration.Map.WIDTH * Configuration.Tile.WIDTH - viewWidth;
        }
        if (camY > Configuration.Map.HEIGHT * Configuration.Tile.HEIGHT - viewHeight) {
            camY = Configuration.Map.HEIGHT * Configuration.Tile.HEIGHT - viewHeight;
        }
    }

    private void render(Graphics g) {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw tiles
        for (int x = 0; x < viewWidth; x++) {
            for (int y = 0; y < viewHeight; y++) {
                int wx = camX + x;
                int wy = camY + y;

                Tile tile = state.getTile(wx, wy);
                if (tile == null) {
                    continue;
                }

                int px = x * Configuration.Tile.WIDTH;
                int py = (y + Configuration.Map.Y_OFFSET) * Configuration.Tile.HEIGHT;

                Image img = null;
                int tileCol = emptyCol;
                if (tile.building != null) {
                    if (tile.building.images != null) {
                        img = tile.building.images[tile.dir];
                    }
                }

                if (tile.isEmpty()) {
                    if ((wx % 2 != 0) ^ (wy % 2 != 0)) {
                        tileCol = tileCol + 0xE4E4E4;
                    }
                }

                if (img != null) {
                    g.drawImage(img,
                            px, py,
                            Graphics.TOP | Graphics.LEFT);
                } else { // Fallback to basic render
                    g.setColor(tileCol);
                    g.fillRect(px, py,
                            Configuration.Tile.WIDTH,
                            Configuration.Tile.HEIGHT);
                    if (!tile.isEmpty()) {
                        g.setColor(0xFFFFFF);
                        int cx = px + Configuration.Tile.WIDTH / 2;
                        int cy = py + Configuration.Tile.HEIGHT / 2;
                        int[] rotated = rotatePoint(0, Configuration.Tile.WIDTH / 2, tile.dir);
                        g.drawLine(cx, cy, cx + rotated[0], cy + rotated[1]);
                    }
                }
            }
        }

        g.setColor(0xFFFFFF);
        g.fillRect(getWidth() - 24, 8, 16, 16);

        int cx = (cursorX - camX) * Configuration.Tile.WIDTH;
        int cy = (cursorY - camY + Configuration.Map.Y_OFFSET) * Configuration.Tile.HEIGHT;

        if (cursorY < 0) {
            cx = getWidth() - 24;
            cy = 8;
        }

        Tile cursorTile = state.getTile(cursorX, cursorY);

        if (cursorTile != null && !cursorTile.isEmpty()) {
            g.setColor(0xFFFFFF);

            String outputs = formatStacks(
                    cursorTile.outputItemIds,
                    cursorTile.outputAmounts
            );

            String inputs = formatStacks(
                    cursorTile.inputItemIds,
                    cursorTile.inputAmounts
            );

            g.drawString("Out: " + outputs,
                    2, 18, Graphics.TOP | Graphics.LEFT);

            g.drawString("In: " + inputs,
                    2, 34, Graphics.TOP | Graphics.LEFT);

            g.drawString("Timer: " + cursorTile.timer,
                    2, 50, Graphics.TOP | Graphics.LEFT);
        }

        g.setColor(0x08ABD4);
        g.drawRect(cx, cy, Configuration.Tile.WIDTH - 1, Configuration.Tile.HEIGHT - 1);

        g.setColor(0xFFFFFF);
        g.drawString("$" + Integer.toString(state.money) + " T:" + Long.toString(state.tick), 2, 2, Graphics.TOP | Graphics.LEFT);
    }

    private void processTileAction(Tile tile, int index) {
        switch (index) {
            case 1: // Destroy
                buildSystem.destroy(cursorX, cursorY);
                break;

            case 2: // Rotate
                if (!tile.isEmpty()) {
                    tile.dir = (byte) ((tile.dir + 1) % 4);
                }
                break;
            default:
                break;
        }
    }

    private void showConfigurationMenu(final Tile tile) {
        if (tile.building == null || tile.building.getRecipes() == null) {
            resumeGame();
            return;
        }

        Vector recipes = tile.building.getRecipes();
        if (recipes.isEmpty()) {
            resumeGame();
            return;
        }

        final List configMenu = new List("Select Recipe", List.IMPLICIT);
        final Vector recipeObjects = new Vector();

        for (int i = 0; i < recipes.size(); i++) {
            RecipeDef r = (RecipeDef) recipes.elementAt(i);
            String displayName = r.getDisplayName(GameState.items);
            if (displayName == null) {
                displayName = r.id; // fallback
            }
            configMenu.append(displayName, null);
            recipeObjects.addElement(r);
        }

        configMenu.addCommand(new Command("Back", Command.BACK, 0));

        configMenu.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    resumeGame();
                    return;
                }

                if (c == List.SELECT_COMMAND) {
                    int index = configMenu.getSelectedIndex();
                    tile.recipe = (RecipeDef) recipeObjects.elementAt(index); // assign the recipe directly
                    resumeGame();
                }
            }
        });

        paused = true;
        Display.getDisplay(midlet).setCurrent(configMenu);
    }

    private void showConstructMenu(final Tile tile, Displayable pMenu) {
        final Displayable parentMenu = pMenu;
        final List constructMenu = new List("Construct", List.IMPLICIT);

        // Keep track of building ids in menu order
        final Vector buildingIds = new Vector();

        Vector temp = new Vector();
        for (Enumeration e = GameState.buildings.elements(); e.hasMoreElements();) {
            BuildingDef def = (BuildingDef) e.nextElement();
            temp.addElement(def); // collect all first
        }

        // Add in reverse
        for (int i = temp.size() - 1; i >= 0; i--) {
            BuildingDef def = (BuildingDef) temp.elementAt(i);
            constructMenu.append(def.name, null);
            buildingIds.addElement(def.id);
        }

        constructMenu.addCommand(new Command("Back", Command.BACK, 0));

        constructMenu.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    Display.getDisplay(midlet).setCurrent(parentMenu);
                    return;
                }

                if (c == List.SELECT_COMMAND) {
                    int index = constructMenu.getSelectedIndex();
                    String buildingId = (String) buildingIds.elementAt(index);

                    BuildingDef def = (BuildingDef) GameState.buildings.get(buildingId);
                    if (def != null) {
                        buildSystem.build(cursorX, cursorY, def, Enum.Direction.UP);
                    }

                    resumeGame();
                }
            }
        });

        paused = true;
        Display.getDisplay(midlet).setCurrent(constructMenu);
    }
    
    private void showAlert(Displayable d, String title, String message, AlertType a) {
        Alert alert = new Alert(title, message, null, a);
        alert.setTimeout(Alert.FOREVER);
        Display.getDisplay(midlet).setCurrent(alert, d);
    } 

    private void showPauseMenu() {
        final List pauseMenu = new List("Pause", List.IMPLICIT);
        pauseMenu.append("Save", null);
        pauseMenu.append("Load", null);
        pauseMenu.append("Options", null);
        pauseMenu.append("Exit to title screen", null);
        pauseMenu.addCommand(new Command("Back", Command.BACK, 0));

        pauseMenu.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    resumeGame();
                } else if (c == List.SELECT_COMMAND) {
                    int index = pauseMenu.getSelectedIndex();
                    if (index == 0) {
                        showAlert(d, "Saving", "Saving is unavailable", AlertType.ERROR);
                    } else if (index == 1) {
                        showAlert(d, "Loading", "Loading is unavailable", AlertType.ERROR);
                    } else if (index == 2) {
                        showAlert(d, "Options", "Options are unavailable", AlertType.ERROR);
                    } else if (index == 3) {
                        showAlert(d, "Title screen", "The title screen is unavailable", AlertType.ERROR);
                    }
                }
            }
        });

        paused = true;
        Display.getDisplay(midlet).setCurrent(pauseMenu);
    }

    private void showTileMenu(Tile t) {
        final Tile tile = t;
        final List tileMenu = new List("Tile Actions", List.IMPLICIT);
        tileMenu.append("Construct", null);
        tileMenu.append("Destroy", null);
        tileMenu.append("Rotate", null);
        tileMenu.append("Configure", null);
        tileMenu.addCommand(new Command("Back", Command.BACK, 0));

        tileMenu.setCommandListener(new CommandListener() {
            public void commandAction(Command c, Displayable d) {
                if (c.getCommandType() == Command.BACK) {
                    resumeGame();
                } else if (c == List.SELECT_COMMAND) {
                    int index = tileMenu.getSelectedIndex();
                    if (index == 0) { // Construct → open nested menu
                        showConstructMenu(tile, tileMenu);
                    } else if (index == 3) {
                        showConfigurationMenu(tile);
                    } else {
                        processTileAction(tile, index);
                        resumeGame();
                    }
                }
            }
        });

        paused = true;
        Display.getDisplay(midlet).setCurrent(tileMenu);
    }

    private void resumeGame() {
        paused = false;
        Display.getDisplay(midlet).setCurrent(this);
    }

    private String formatStacks(String[] itemIds, int[] amounts) {
        if (itemIds == null || itemIds.length == 0) {
            return "none";
        }

        StringBuffer sb = new StringBuffer();
        boolean first = true;

        for (int i = 0; i < itemIds.length; i++) {
            if (!"air".equals(itemIds[i]) && amounts[i] > 0) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(itemIds[i]);
                sb.append(" x");
                sb.append(amounts[i]);
                first = false;
            }
        }

        if (first) {
            return "empty";
        }

        return sb.toString();
    }
}
