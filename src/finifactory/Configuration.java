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
public class Configuration {

    private Configuration() {
    }

    public class Map {

        private Map() {
        }
        public static final int WIDTH = 20;
        public static final int HEIGHT = 20;
        public static final int Y_OFFSET = 2;
    }

    public class Tile {

        private Tile() {
        }
        public static final int WIDTH = 16;
        public static final int HEIGHT = 16;
        public static final boolean BASIC_RENDERING = false;
        public static final int STACK_LIMIT = 50;
    }

    public static class Costs {

        private Costs() {
        }
        public static final int BELT = 10;
        public static final int FURNACE = 50;
        public static final int MINER = 100;
        public static final int SINK = 5;
        public static final int COAL_GEN = 100;
    }

    public class Init {

        private Init() {
        }

        public static final int MONEY = 10000;
        public static final int SIM_STEP_MS = 25;
    }
}
