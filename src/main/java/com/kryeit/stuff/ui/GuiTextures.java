/* Code copied from Polyfactory by Patbox
 * https://github.com/Patbox/PolyFactory
 *
 * Learn more about his mods: https://pb4.eu
 */

package com.kryeit.stuff.ui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.text.Text;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static com.kryeit.stuff.ui.UiResourceCreator.*;

public class GuiTextures {

    public static final Function<Text, Text> SHOP = background("shop");
    public static final Function<Text, Text> PAGINATED_SHOP = background("paginated_shop");

    public static void register() {

    }

}
