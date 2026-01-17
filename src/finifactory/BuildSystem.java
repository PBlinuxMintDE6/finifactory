/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

/**
 *
 * @author c
 */
public class BuildSystem {

    private final GameState state;

    public BuildSystem(GameState state) {
        this.state = state;
    }

    public boolean canBuild(int x, int y, BuildingDef tileType) {
        Tile tile = state.getTile(x, y);
        if (tile == null || !tile.isEmpty()) {
            return false; // Fail build
        }

        int cost = tileType.cost;

        return state.money >= cost;
    }
    
    public boolean build(int x, int y, BuildingDef tileType, byte dir) {
        if (!canBuild(x, y, tileType)) return false;
        
        state.getTile(x, y).set(tileType, dir, x, y);
        state.money -= tileType.cost;
        return true;
    }
    
    public boolean destroy(int x, int y) {
        Tile tile = state.getTile(x, y);
        if (tile == null || tile.isEmpty()) return false;
        
        int refund = tile.building.cost;
        state.money += refund;
        
        tile.set(null, Enum.Direction.UP, x, y);
        return true;
    }
}
