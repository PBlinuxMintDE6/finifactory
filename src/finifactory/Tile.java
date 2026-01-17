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
public final class Tile {

    public BuildingDef building;
    public int dir;

    public String[] inputItemIds;
    public int[] inputAmounts;

    public String[] outputItemIds;
    public int[] outputAmounts;

    public RecipeDef recipe;
    public int timer;

    public int x;
    public int y;

    public Tile() {
        this.building = null;
        this.dir = Enum.Direction.UP;
        resetState();
    }

    public void set(BuildingDef type, byte dir, int x, int y) {
        this.building = type;
        this.dir = dir;
        this.x = x;
        this.y = y;
        this.timer = 0;

        // Allocate input/output arrays safely
        if (type == null) {
            this.inputItemIds = null;
            this.inputAmounts = null;
            this.outputItemIds = null;
            this.outputAmounts = null;
            return;
        }
        if (building.inputSize > 0) {
            this.inputItemIds = new String[building.inputSize];
            this.inputAmounts = new int[building.inputSize];
            for (int i = 0; i < building.inputSize; i++) {
                this.inputItemIds[i] = "air";
                this.inputAmounts[i] = 0;
            }
        } else {
            this.inputItemIds = new String[0];
            this.inputAmounts = new int[0];
        }

        if (building.outputSize > 0) {
            this.outputItemIds = new String[building.outputSize];
            this.outputAmounts = new int[building.outputSize];
            for (int i = 0; i < building.outputSize; i++) {
                this.outputItemIds[i] = "air";
                this.outputAmounts[i] = 0;
            }
        } else {
            this.outputItemIds = new String[0];
            this.outputAmounts = new int[0];
        }
    }

    public void resetState() {
        this.timer = 0;

        if (building != null) {
            inputItemIds = new String[building.inputSize];
            inputAmounts = new int[building.inputSize];

            outputItemIds = new String[building.outputSize];
            outputAmounts = new int[building.outputSize];

            if (inputItemIds != null) {
                for (int i = 0; i < inputItemIds.length; i++) {
                    inputItemIds[i] = "air";
                    inputAmounts[i] = 0;
                }
            }

            if (outputItemIds != null) {
                for (int i = 0; i < outputItemIds.length; i++) {
                    outputItemIds[i] = "air";
                    outputAmounts[i] = 0;
                }
            }
        }
    }

    public boolean isEmpty() {
        return building == null;
    }
}
