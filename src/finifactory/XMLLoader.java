/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finifactory;

import java.io.*;
import javax.xml.parsers.*;

/**
 *
 * @author c
 */
public final class XMLLoader {

    private XMLLoader() {
    }

    public static void load(String path) {
        InputStream is = null;

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            is = XMLLoader.class.getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("XML not found: " + path);
            }

            XMLParser handler = new XMLParser();
            parser.parse(is, handler);

            XMLParser.loadBuildingImages(handler.getBuildings());
            GameState.buildings = handler.getBuildings();
            GameState.items = handler.getItems();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load XML: " + e.toString());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception ignored) {
            }
        }
    }
}
