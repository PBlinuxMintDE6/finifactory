/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;
import java.util.Vector;
import javax.microedition.lcdui.Image;

/**
 *
 * @author c
 */

public class BuildingDef {

    public String id;
    public String name;
    public String description;
    public String logic;
    public boolean noOutput;
    public int cost;
    public int stackLimit;
    public int inputSize = 0;
    public int outputSize = 0;
    public String imagePath;
    public Image[] images;

    private final Vector recipes = new Vector();

    public void addRecipe(RecipeDef recipe) {
        recipes.addElement(recipe);
    }

    public Vector getRecipes() {
        return recipes;
    }
}
