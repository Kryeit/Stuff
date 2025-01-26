/* Code copied from Polyfactory by Patbox
 * https://github.com/Patbox/PolyFactory
 *
 * Learn more about his mods: https://pb4.eu
 */

package com.kryeit.stuff.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kryeit.stuff.Stuff;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import it.unimi.dsi.fastutil.chars.Char2IntMap;
import it.unimi.dsi.fastutil.chars.Char2IntOpenHashMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class UiResourceCreator {

    public static final String BASE_MODEL = "minecraft:item/generated";
    public static final String X32_MODEL = "stuff:sgui/button_32";

    private static final Style STYLE = Style.EMPTY.withColor(0xFFFFFF).withFont(Identifier.of(Stuff.MODID, "gui"));

    private static final String ITEM_TEMPLATE = """
            {
              "parent": "|BASE|",
              "textures": {
                "layer0": "|ID|"
              }
            }
            """.replace(" ", "").replace("\n", "");
    private static final List<Pair<PolymerModelData, String>> SIMPLE_MODEL = new ArrayList<>();

    private static final Char2IntMap SPACES = new Char2IntOpenHashMap();
    private static final Char2ObjectMap<Identifier> TEXTURES = new Char2ObjectOpenHashMap<>();
    private static char character = 'a';
    private static final char CHEST_SPACE0 = character++;
    private static final char CHEST_SPACE1 = character++;

    public static Supplier<GuiElementBuilder> icon16(String path) {
        var model = genericIconRaw(Items.ALLIUM, path, BASE_MODEL);
        return () -> new GuiElementBuilder(model.item()).setName(Text.empty()).hideFlags().setCustomModelData(model.value());
    }

    public static Supplier<GuiElementBuilder> icon32(String path) {
        var model = genericIconRaw(Items.ALLIUM, path, X32_MODEL);
        return () -> new GuiElementBuilder(model.item()).setName(Text.empty()).hideFlags().setCustomModelData(model.value());
    }

    public static IntFunction<GuiElementBuilder> icon16Color(String path) {
        var model = genericIconRaw(Items.LEATHER_LEGGINGS, path, BASE_MODEL);
        return (i) -> {
            var b = new GuiElementBuilder(model.item()).setName(Text.empty()).hideFlags().setCustomModelData(model.value());
            var display = new NbtCompound();
            display.putInt("color", i);
            b.getOrCreateNbt().put("display", display);
            return b;
        };
    }

    public static PolymerModelData genericIconRaw(Item item, String path, String base) {
        var model = PolymerResourcePackUtils.requestModel(item, elementPath(path));
        SIMPLE_MODEL.add(new Pair<>(model, base));
        return model;
    }

    private static Identifier elementPath(String path) {
        return Identifier.of(Stuff.MODID, "sgui/elements/" + path);
    }

    public static Function<Text, Text> background(String path) {
        var builder = new StringBuilder().append(CHEST_SPACE0);
        var c = (character++);
        builder.append(c);
        builder.append(CHEST_SPACE1);
        TEXTURES.put(c, Identifier.of(Stuff.MODID, "sgui/" + path));

        return new TextBuilders(Text.literal(builder.toString()).setStyle(STYLE));
    }

    public static void setup() {
        SPACES.put(CHEST_SPACE0, -8);
        SPACES.put(CHEST_SPACE1, -168);

        if (Stuff.DYNAMIC_ASSETS) {
            PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((b) -> UiResourceCreator.generateAssets(b::addData));
        }
    }

    public static void generateAssets(BiConsumer<String, byte[]> assetWriter) {
        for (var texture : SIMPLE_MODEL) {
            assetWriter.accept("assets/" + texture.getLeft().modelPath().getNamespace() + "/models/" + texture.getLeft().modelPath().getPath() + ".json",
                    ITEM_TEMPLATE.replace("|ID|", texture.getLeft().modelPath().toString()).replace("|BASE|", texture.getRight()).getBytes(StandardCharsets.UTF_8));
        }

        var fontBase = new JsonObject();
        var providers = new JsonArray();

        {
            var spaces = new JsonObject();
            spaces.addProperty("type", "space");
            var advances = new JsonObject();
            SPACES.forEach((c, i) -> advances.addProperty(Character.toString(c), i));
            spaces.add("advances", advances);
            providers.add(spaces);
        }

        TEXTURES.forEach((character, id) -> {
            var bitmap = new JsonObject();
            bitmap.addProperty("type", "bitmap");
            bitmap.addProperty("file", id.toString() + ".png");
            bitmap.addProperty("ascent", 13);
            bitmap.addProperty("height", 256);
            var chars = new JsonArray();
            chars.add(Character.toString(character));
            bitmap.add("chars", chars);
            providers.add(bitmap);
        });

        fontBase.add("providers", providers);

        assetWriter.accept("assets/stuff/font/gui.json", fontBase.toString().getBytes(StandardCharsets.UTF_8));
    }

    private record TextBuilders(Text base) implements Function<Text, Text> {
        @Override
        public Text apply(Text text) {
            return Text.empty().append(base).append(text);
        }
    }
}