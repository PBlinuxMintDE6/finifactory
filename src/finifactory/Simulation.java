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
public class Simulation {

    private final GameState state;

    public Simulation(GameState state) {
        this.state = state;
    }

    public void tick() {
        for (int x = 0; x < state.width; x++) {
            for (int y = 0; y < state.height; y++) {
                Tile tile = state.getTile(x, y);
                if (tile == null || tile.isEmpty()) {
                    continue;
                }

                if (tile.building.logic.equals("machine")) {
                    updateMachine(tile);
                } else if (tile.building.logic.equals("belt")) {
                    updateBelt(tile);
                } else if (tile.building.logic.equals("seller")) {
                    updateSell(tile);
                }
            }
        }
        state.advanceTick();
    }

    private Tile getTileInDirection(Tile tile, int dir) {
        int tx = tile.x;
        int ty = tile.y;
        switch (dir) {
            case Enum.Direction.UP:
                ty--;
                break;
            case Enum.Direction.DOWN:
                ty++;
                break;
            case Enum.Direction.LEFT:
                tx--;
                break;
            case Enum.Direction.RIGHT:
                tx++;
                break;
        }
        return state.getTile(tx, ty);
    }

    private Tile[] getInputCandidates(Tile tile) {
        Tile[] candidates = new Tile[3]; // preferred + 2 backups
        int tickMod = (int) (state.tick % 4); // global tick mod 4

        // Calculate preferred direction
        int preferredDir = (tile.dir + tickMod) % 4;

        // If preferredDir matches tile's facing direction, offset -90 degrees
        if (preferredDir == tile.dir) {
            preferredDir = (preferredDir + 3) % 4; // -90 degrees
        }

        int count = 0;

        // Add preferred direction first
        candidates[count++] = getTileInDirection(tile, preferredDir);

        // Add remaining directions except the facing direction and preferredDir's opposite
        for (int dir = 0; dir < 4; dir++) {
            if (dir != tile.dir && dir != preferredDir && dir != ((preferredDir + 2) % 4)) {
                candidates[count++] = getTileInDirection(tile, dir);
                if (count >= 3) {
                    break;
                }
            }
        }

        return candidates;
    }
    
    private boolean tryConsumePower(int amount) {
        if (state.power - amount >= 0) {
            state.power -= amount;
            return true;
        }
        return false;
    }
    
    private boolean trySummonPower(int amount) {
        if (state.power + amount <= state.maxPower) {
            state.power += amount;
            return true;
        }
        return false;
    }

    private boolean tryInsertInput(Tile tile, String itemId, int amount) {
        if (tile.inputItemIds == null || tile.inputItemIds.length == 0) {
            return false; // cannot accept inputs
        }

        // First: try to find an existing slot for this item
        for (int i = 0; i < tile.inputItemIds.length; i++) {
            if (tile.inputItemIds[i].equals(itemId)) {
                if (tile.inputAmounts[i] + amount <= tile.building.stackLimit) {
                    tile.inputAmounts[i] += amount;
                    return true;
                } else {
                    return false; // would exceed stack limit
                }
            }
        }

        // Second: find the first empty slot
        for (int i = 0; i < tile.inputItemIds.length; i++) {
            if (tile.inputItemIds[i].equals("air")) {
                if (amount <= tile.building.stackLimit) {
                    tile.inputItemIds[i] = itemId;
                    tile.inputAmounts[i] = amount;
                    return true;
                } else {
                    return false; // amount exceeds stack limit
                }
            }
        }

        return false; // no space available
    }

    private boolean tryPushOutput(Tile tile, int slot) {
        if (tile.outputItemIds == null || tile.outputItemIds.length == 0 || slot < 0 || slot >= tile.outputItemIds.length) {
            return false;
        }

        String itemId = tile.outputItemIds[slot];
        if (itemId.equals("air") || tile.outputAmounts[slot] <= 0) {
            return false;
        }

        // Directly check the tile in front
        Tile forward = getTileInDirection(tile, tile.dir);
        if (forward != null && forward.building != null && tryInsertInput(forward, itemId, 1)) {
            tile.outputAmounts[slot]--;
            if (tile.outputAmounts[slot] <= 0) {
                tile.outputItemIds[slot] = "air";
                tile.outputAmounts[slot] = 0;
            }
            return true;
        }

        // Optionally: try side/back tiles for overflow
        // Tile[] overflowTargets = getAdjacentTiles(forward); // implement if needed
        return false;
    }

    private boolean canRunRecipe(Tile tile, RecipeDef r) {
        // If there are input requirements, check them
        if (r.inputs != null) {
            for (int i = 0; i < r.inputs.length; i++) {
                ItemStack req = r.inputs[i];
                if (!hasInput(tile, req.itemId, req.amount)) {
                    return false;
                }
            }
        }

        // Always check that there’s enough output space
        return hasFreeOutputSpace(tile, r.outputs);
    }

    private boolean hasInput(Tile tile, String itemId, int amount) {
        int total = 0;
        for (int i = 0; i < tile.inputItemIds.length; i++) {
            if (tile.inputItemIds[i].equals(itemId)) {
                total += tile.inputAmounts[i];
                if (total >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    private void consumeInputs(Tile tile, ItemStack[] inputs) {
        if (inputs == null) {
            return; // nothing to consume
        }

        for (int i = 0; i < inputs.length; i++) {
            ItemStack req = inputs[i];
            int remaining = req.amount;

            for (int j = 0; j < tile.inputItemIds.length && remaining > 0; j++) {
                if (tile.inputItemIds[j].equals(req.itemId)) {
                    int take = Math.min(tile.inputAmounts[j], remaining);
                    tile.inputAmounts[j] -= take;
                    remaining -= take;

                    if (tile.inputAmounts[j] <= 0) {
                        tile.inputItemIds[j] = "air";
                        tile.inputAmounts[j] = 0;
                    }
                }
            }
        }
    }

    private boolean hasFreeOutputSpace(Tile tile, ItemStack[] outputs) {
        for (int i = 0; i < outputs.length; i++) {
            ItemStack out = outputs[i];
            boolean placed = false;

            for (int j = 0; j < tile.outputItemIds.length; j++) {
                if (tile.outputItemIds[j].equals("air")
                        || tile.outputItemIds[j].equals(out.itemId)) {

                    if (tile.outputAmounts[j] + out.amount <= tile.building.stackLimit) {
                        placed = true;
                        break;
                    }
                }
            }

            if (!placed) {
                return false;
            }
        }
        return true;
    }

    private void storeOutputs(Tile tile, ItemStack[] outputs) {
        for (int i = 0; i < outputs.length; i++) {
            ItemStack out = outputs[i];

            boolean added = false;

            // First: try to find an existing slot with the same item
            for (int j = 0; j < tile.outputItemIds.length; j++) {
                if (tile.outputItemIds[j].equals(out.itemId)) {
                    tile.outputAmounts[j] += out.amount;
                    added = true;
                    break;
                }
            }

            // Second: if not already in outputs, find the first empty slot
            if (!added) {
                for (int j = 0; j < tile.outputItemIds.length; j++) {
                    if (tile.outputItemIds[j].equals("air")) {
                        tile.outputItemIds[j] = out.itemId;
                        tile.outputAmounts[j] = out.amount;
                        //added = true;
                        break;
                    }
                }
            }
        }
    }

    private void updateSell(Tile tile) {
        for (int i = 0; i < tile.inputItemIds.length; i++) {
            String item = tile.inputItemIds[i];
            int amount = tile.inputAmounts[i];

            if (!item.equals("air") && amount > 0) {
                ItemDef def = state.getItemFromId(item);
                state.money += def.price * amount;

                tile.inputItemIds[i] = "air";
                tile.inputAmounts[i] = 0;
            }
        }
    }

    private void updateMachine(Tile tile) {
        RecipeDef recipe = tile.recipe;
        if (recipe == null) {
            tile.timer = 0;
            return;
        }

        // Working phase
        if (tile.timer > 0) {
            tile.timer--;
            return;
        }

        // First: try pushing outputs
        for (int i = 0; i < tile.outputItemIds.length; i++) {
            tryPushOutput(tile, i);
        }

        // Check if recipe can run
        if (!canRunRecipe(tile, recipe)) {
            return;
        }

        // Consume inputs
        consumeInputs(tile, recipe.inputs);

        // Produce outputs
        storeOutputs(tile, recipe.outputs);

        tile.timer = recipe.time;
    }

    private void updateBelt(Tile tile) {
        if (tile.timer > 0) {
            tile.timer--;
            return;
        }

        if (tile.inputItemIds.length == 0 || tile.inputItemIds[0].equals("air") || tile.inputAmounts[0] <= 0) {
            tile.timer = 1;
            return;
        }

        if (tile.outputItemIds.length > 0) {
            // Only a single-slot belt assumed for now
            if (tile.outputItemIds[0].equals("air") || tile.outputItemIds[0].equals(tile.inputItemIds[0])) {
                int canTransfer = tile.building.stackLimit - tile.outputAmounts[0];
                if (canTransfer > 0) {
                    int transferAmount = Math.min(canTransfer, tile.inputAmounts[0]);
                    tile.outputItemIds[0] = tile.inputItemIds[0];
                    tile.outputAmounts[0] += transferAmount;
                    tile.inputAmounts[0] -= transferAmount;

                    if (tile.inputAmounts[0] <= 0) {
                        tile.inputItemIds[0] = "air";
                        tile.inputAmounts[0] = 0;
                    }
                }
            }
        }

        tryPushOutput(tile, 0);
        tile.timer = 1;
    }
}
