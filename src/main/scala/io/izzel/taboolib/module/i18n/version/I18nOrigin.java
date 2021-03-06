package io.izzel.taboolib.module.i18n.version;

import io.izzel.taboolib.TabooLib;
import io.izzel.taboolib.Version;
import io.izzel.taboolib.module.i18n.I18nBase;
import io.izzel.taboolib.module.locale.TLocaleLoader;
import io.izzel.taboolib.module.nms.NMS;
import io.izzel.taboolib.module.nms.nbt.NBTCompound;
import io.izzel.taboolib.util.Files;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * @Author sky
 * @Since 2020-04-04 19:44
 */
public class I18nOrigin extends I18nBase {

    public static final I18nOrigin INSTANCE = new I18nOrigin();

    private static FileConfiguration lang;
    private static boolean released;

    @Override
    public void init() {
        File localeFile = getLocaleFile(TabooLib.getPlugin());
        if (localeFile == null) {
            lang = new YamlConfiguration();
        } else {
            lang = Files.load(localeFile);
        }
        if (lang.getInt("version") < 3 && !released) {
            released = true;
            Files.deepDelete(new File(TabooLib.getPlugin().getDataFolder(), "simpleI18n"));
            init();
        }
    }

    @Override
    public String getName(Player player, Entity entity) {
        return entity == null ? "-" : lang.getString(NMS.handle().getName(entity).replace(".", "_"), entity.getName());
    }

    @Override
    public String getName(Player player, ItemStack itemStack) {
        if (itemStack == null) {
            return "-";
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof BookMeta && ((BookMeta) itemMeta).getTitle() != null) {
            return ((BookMeta) itemMeta).getTitle();
        }
        if (!Version.isAfter(Version.v1_11)) {
            if (itemStack.getType().name().equals("MONSTER_EGG")) {
                NBTCompound nbtCompound = NMS.handle().loadNBT(itemStack);
                if (nbtCompound.containsKey("EntityTag")) {
                    return lang.getString("item_monsterPlacer_name") + " " + lang.getString("entity_" + nbtCompound.get("EntityTag").asCompound().get("id").asString() + "_name");
                }
                return lang.getString("item_monsterPlacer_name");
            }
        } else if (!Version.isAfter(Version.v1_13)) {
            if (itemMeta instanceof SpawnEggMeta) {
                String spawnEggType = lang.getString("entity_" + ((SpawnEggMeta) itemMeta).getSpawnedType().getEntityClass().getSimpleName().replace(".", "_") + "_name");
                if (spawnEggType != null) {
                    return lang.getString(NMS.handle().getName(itemStack).replace(".", "_"), itemStack.getType().name().toLowerCase().replace("_", "")) + " " + spawnEggType;
                }
            }
        }
        return lang.getString(NMS.handle().getName(itemStack).replace(".", "_"), itemStack.getType().name().toLowerCase().replace("_", ""));
    }

    private File getLocaleFile(Plugin plugin) {
        TLocaleLoader.getLocalePriority(plugin).forEach(localeName -> Files.releaseResource(plugin, "simpleI18n/" + getVersion() + "/" + localeName + ".yml", false));
        return TLocaleLoader.getLocalePriority(plugin).stream().map(localeName -> new File("plugins/TabooLib/simpleI18n/" + getVersion() + "/" + localeName + ".yml")).filter(File::exists).findFirst().orElse(null);
    }

    private String getVersion() {
        return Version.isAfter(Version.v1_13) ? "high" : "low";
    }
}
