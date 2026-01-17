/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Image;
import java.io.IOException;
import java.util.Enumeration;

/**
 *
 * @author c
 */
public class XMLParser extends DefaultHandler {

    private final Hashtable items = new Hashtable();
    private final Hashtable buildings = new Hashtable();

    private ItemDef currentItem;
    private BuildingDef currentBuilding;
    private RecipeDef currentRecipe;

    private boolean parsingInputs;

    private Vector recipeInputs;
    private Vector recipeOutputs;

    private String currentText;

    public Hashtable getItems() {
        return items;
    }

    public Hashtable getBuildings() {
        return buildings;
    }

    public void startElement(String uri, String local, String qName, Attributes atts) {

        if ("item".equals(qName) && currentRecipe == null && currentBuilding == null) {
            // Item definition
            currentItem = new ItemDef();
            currentItem.id = atts.getValue("id");

        } else if ("building".equals(qName)) {
            currentBuilding = new BuildingDef();
            currentBuilding.id = atts.getValue("id");

        } else if ("recipe".equals(qName)) {
            currentRecipe = new RecipeDef();
            currentRecipe.id = atts.getValue("id");
            recipeInputs = new Vector();
            recipeOutputs = new Vector();

        } else if ("inputs".equals(qName)) {
            parsingInputs = true;

        } else if ("outputs".equals(qName)) {
            parsingInputs = false;

        } else if ("item".equals(qName) && currentRecipe != null) {
            ItemStack stack = new ItemStack();
            stack.itemId = atts.getValue("type");
            stack.amount = Integer.parseInt(atts.getValue("amount"));

            if (parsingInputs) {
                recipeInputs.addElement(stack);
            } else {
                recipeOutputs.addElement(stack);
            }
        } else if ("image".equals(qName) && currentBuilding != null) {
            currentBuilding.imagePath = atts.getValue("path");
        }

        currentText = "";
    }

    public void characters(char[] ch, int start, int length) {
        currentText += new String(ch, start, length);
    }

    public void endElement(String uri, String local, String qName) {

        if ("name".equals(qName)) {
            if (currentItem != null) {
                currentItem.name = currentText.trim();
            }
            if (currentBuilding != null) {
                currentBuilding.name = currentText.trim();
            }
        } else if ("description".equals(qName)) {
            if (currentItem != null) {
                currentItem.description = currentText.trim();
            }
            if (currentBuilding != null) {
                currentBuilding.description = currentText.trim();
            }
        } else if ("price".equals(qName)) {
            currentItem.price = Integer.parseInt(currentText.trim());
        } else if ("logic".equals(qName)) {
            currentBuilding.logic = currentText.trim();
        } else if ("cost".equals(qName)) {
            currentBuilding.cost = Integer.parseInt(currentText.trim());
        } else if ("stackLimit".equals(qName)) {
            currentBuilding.stackLimit = Integer.parseInt(currentText.trim());
        } else if ("noOutput".equals(qName)) {
            currentBuilding.noOutput = "true".equals(currentText.trim());
        } else if ("time".equals(qName)) {
            currentRecipe.time = Integer.parseInt(currentText.trim());
        } else if ("inputs".equals(qName)) {
            currentRecipe.inputs = toArray(recipeInputs);
            recipeInputs = null;
        } else if ("outputs".equals(qName)) {
            currentRecipe.outputs = toArray(recipeOutputs);
            recipeOutputs = null;
        } else if ("recipe".equals(qName)) {
            currentBuilding.addRecipe(currentRecipe);
            currentRecipe = null;
        } else if ("building".equals(qName)) {
            buildings.put(currentBuilding.id, currentBuilding);
            currentBuilding = null;
        } else if ("item".equals(qName) && currentItem != null) {
            items.put(currentItem.id, currentItem);
            currentItem = null;
        } else if ("inputSize".equals(qName)) {
            currentBuilding.inputSize = Integer.parseInt(currentText.trim());
        } else if ("outputSize".equals(qName)) {
            currentBuilding.outputSize = Integer.parseInt(currentText.trim());
        }
    }

    private ItemStack[] toArray(Vector v) {
        ItemStack[] arr = new ItemStack[v.size()];
        v.copyInto(arr);
        return arr;
    }

    public static void loadBuildingImages(Hashtable buildings) {
        for (Enumeration e = buildings.elements(); e.hasMoreElements();) {
            BuildingDef def = (BuildingDef) e.nextElement();

            if (def.imagePath == null) {
                continue;
            }

            try {
                Image base = Image.createImage(def.imagePath);
                def.images = generateDir(base);
            } catch (IOException ex) {
                try {
                    System.out.println("Failed to load image for " + def.id);
                    Image base = Image.createImage("finifactory/invalid.png");
                    def.images = generateDir(base);
                } catch (IOException exe) {
                    throw new RuntimeException("Failed to load backup image");
                }
            }
        }
    }

    private static Image[] generateDir(Image src) {
        Image[] images = new Image[4];
        images[0] = src;
        images[1] = rotate90(images[0]);
        images[2] = rotate90(images[1]);
        images[3] = rotate90(images[2]);
        return images;
    }

    private static Image rotate90(Image src) {
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
}
