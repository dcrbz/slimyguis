package xyz.upperlevel.spigot.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.Arrays;

import static org.bukkit.ChatColor.RED;

public final class GuiUtil {

    @Deprecated
    public static ItemStack wool(DyeColor color, String name, String... lores) {
        return setNameAndLores(new Wool(color).toItemStack(1), name, lores);
    }

    @Deprecated
    public static ItemStack wood(TreeSpecies type, String name, String... lores) {
        Material mat = null;

        switch (type) {
            case ACACIA:
                mat = Material.ACACIA_WOOD;
                break;
            case BIRCH:
                mat = Material.BIRCH_WOOD;
                break;
            case DARK_OAK:
                mat = Material.DARK_OAK_WOOD;
                break;
            case JUNGLE:
                mat = Material.JUNGLE_WOOD;
                break;
            case REDWOOD:
                mat = Material.SPRUCE_WOOD;
                break;
            case GENERIC:
            default:
                mat = Material.OAK_WOOD;
        }

        return setNameAndLores(new ItemStack(mat), name, lores);
    }

    public static ItemStack itemStack(Material display, String name, String... lores) {
        return setNameAndLores(new ItemStack(display, 1), name, lores);
    }

    public static ItemStack potion(PotionType type, boolean extended, boolean upgraded, String name, String... lores) {
        ItemStack potion = new ItemStack(Material.POTION, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        potion.setItemMeta(meta);
        return potion;
    }

    public static ItemStack splashPotion(PotionType type, boolean extended, boolean upgraded, String name, String... lores) {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        potion.setItemMeta(meta);
        return potion;
    }

    public static ItemStack setNameAndLores(ItemStack item, String name, String... lores) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lores));
        item.setItemMeta(meta);
        return item;
    }

    public static void sendErrorMessage(Player player, String message) {
        sendErrorMessage(player, message, Sound.BLOCK_ANVIL_USE);
    }

    public static void sendErrorMessage(Player player, String message, Sound sound) {
        player.sendMessage(RED + message);
        player.playSound(player.getLocation(), sound, 1, 1);
    }

    private GuiUtil() {}
}
