/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package finifactory;

import java.util.Hashtable;

/**
 *
 * @author c
 */
public class GameState {
    
    public final int width = Configuration.Map.WIDTH;
    public final int height = Configuration.Map.HEIGHT;
    
    public static Hashtable buildings = new Hashtable();
    public static Hashtable items = new Hashtable();
    
    public final Tile[][] map;
    
    public int money;
    public long tick;
    public int power = 0;
    public int maxPower = 100;
    
    public GameState() {
        map = new Tile[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = new Tile();
            }
        }
        
        money = Configuration.Init.MONEY;
        tick = 0;
    }
    
    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return null;
        }
        return map[x][y];
    }
    
    public ItemDef getItemFromId(String itemId) {
        ItemDef item = (ItemDef) items.get(itemId);
        return item != null ? item : null;
    }
    
    public  BuildingDef getBuildingFromId(String buildingId) {
        BuildingDef building = (BuildingDef) buildings.get(buildingId);
        return building != null ? building : null;
    }
    
    public void clearTile(int x, int y) {
        Tile t = getTile(x, y);
        if (t != null) {
            t.set(null, (byte)0, x, y);
        }
    }
    
    public void advanceTick() {
        tick++;
    }
}
