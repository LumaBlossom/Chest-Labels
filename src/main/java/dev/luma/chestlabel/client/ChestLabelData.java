package dev.luma.chestlabel.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

public class ChestLabelData {
    public static class Entry {
        public String label;
        public String itemNbtBase64;

        public Entry(String label, String itemNbtBase64) {
            this.label = label;
            this.itemNbtBase64 = itemNbtBase64;
        }
    }

    private static final Map<String, Entry> ENTRIES = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File saveFile;

    private static String makeKey(ResourceLocation dimension, BlockPos pos) {
        return dimension.toString() + "|" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static void setLabel(ResourceLocation dimension, BlockPos pos, String label) {
        String key = makeKey(dimension, pos);
        Entry existing = ENTRIES.get(key);
        String itemData = existing != null ? existing.itemNbtBase64 : "";
        if (label.isEmpty() && itemData.isEmpty()) {
            ENTRIES.remove(key);
        } else {
            ENTRIES.put(key, new Entry(label, itemData));
        }
        save();
    }

    public static void setLogoItem(ResourceLocation dimension, BlockPos pos, ItemStack stack) {
        String key = makeKey(dimension, pos);
        Entry existing = ENTRIES.get(key);
        String existingLabel = existing != null ? existing.label : "";
        String encoded = "";
        if (!stack.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            stack.save(tag);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                NbtIo.writeCompressed(tag, out);
            } catch (IOException e) {
                return;
            }
            encoded = Base64.getEncoder().encodeToString(out.toByteArray());
        }
        if (existingLabel.isEmpty() && encoded.isEmpty()) {
            ENTRIES.remove(key);
        } else {
            ENTRIES.put(key, new Entry(existingLabel, encoded));
        }
        save();
    }

    public static String getLabel(ResourceLocation dimension, BlockPos pos) {
        Entry entry = ENTRIES.get(makeKey(dimension, pos));
        return entry != null ? entry.label : "";
    }

    public static ItemStack getLogoItem(ResourceLocation dimension, BlockPos pos) {
        Entry entry = ENTRIES.get(makeKey(dimension, pos));
        if (entry == null || entry.itemNbtBase64.isEmpty()) {
            return ItemStack.EMPTY;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(entry.itemNbtBase64);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            CompoundTag tag = NbtIo.readCompressed(in, NbtAccounter.unlimitedHeap());
            return ItemStack.of(tag);
        } catch (IOException e) {
            return ItemStack.EMPTY;
        }
    }

    public static boolean hasData(ResourceLocation dimension, BlockPos pos) {
        return ENTRIES.containsKey(makeKey(dimension, pos));
    }

    private static File getSaveFile() {
        if (saveFile == null) {
            File dir = FMLPaths.CONFIGDIR.get().toFile();
            saveFile = new File(dir, "chestlabel_data.json");
        }
        return saveFile;
    }

    public static void save() {
        try (Writer writer = new FileWriter(getSaveFile())) {
            GSON.toJson(ENTRIES, writer);
        } catch (IOException e) {
        }
    }

    public static void load() {
        File file = getSaveFile();
        if (!file.exists()) {
            return;
        }
        try (Reader reader = new FileReader(file)) {
            Map<String, Entry> loaded = GSON.fromJson(reader, new TypeToken<Map<String, Entry>>(){}.getType());
            if (loaded != null) {
                ENTRIES.clear();
                ENTRIES.putAll(loaded);
            }
        } catch (IOException e) {
        }
    }
}