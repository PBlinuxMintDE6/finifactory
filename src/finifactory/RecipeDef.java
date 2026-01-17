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
public class RecipeDef {

    String id;
    ItemStack[] inputs;
    ItemStack[] outputs;
    int time;

    public String getDisplayName(Hashtable items) {
        if (outputs == null || outputs.length == 0) {
            return "No Output";
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < outputs.length; i++) {
            ItemStack stack = outputs[i];
            ItemDef item = (ItemDef) items.get(stack.itemId); // cast since Hashtable returns Object

            if (item != null) {
                sb.append(item.name);
            } else {
                sb.append(stack.itemId); // fallback if not found
            }

            sb.append(" x");
            sb.append(stack.amount);

            if (i < outputs.length - 1) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }
}
