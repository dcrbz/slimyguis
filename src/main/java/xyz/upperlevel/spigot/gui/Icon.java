package xyz.upperlevel.spigot.gui;

import lombok.Data;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.upperlevel.spigot.gui.config.InvalidGuiConfigurationException;
import xyz.upperlevel.spigot.gui.config.action.Action;
import xyz.upperlevel.spigot.gui.config.action.ActionType;
import xyz.upperlevel.spigot.gui.config.economy.EconomyManager;
import xyz.upperlevel.spigot.gui.config.itemstack.CustomItem;
import xyz.upperlevel.spigot.gui.config.placeholders.PlaceholderValue;
import xyz.upperlevel.spigot.gui.config.util.Config;
import xyz.upperlevel.spigot.gui.link.Link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
public class Icon {

    public static class IconClick implements Link {

        private String permission;
        private PlaceholderValue<String> noPermissionError;
        private Sound noPermissionSound;

        private PlaceholderValue<Double> cost;
        private PlaceholderValue<String> noMoneyError;
        private Sound noMoneySound;

        private List<Action> actions = new ArrayList<>();

        private IconClick() {
        }

        public boolean checkPermission(Player player) {
            if (permission != null && !player.hasPermission(permission)) {
                player.sendMessage(noPermissionError.get(player));
                if (noPermissionSound != null)
                    player.playSound(player.getLocation(), noPermissionSound, 1.0f, 1.0f);
                return false;
            } else
                return true;
        }

        public boolean pay(Player player) {
            double c = cost.get(player);
            if (c > 0) {
                Economy eco = EconomyManager.getEconomy();
                if (eco == null) {
                    SlimyGuis.logger().severe("Cannot use economy: vault not found!");
                    return true;
                }
                EconomyResponse res = eco.withdrawPlayer(player, c);
                if (!res.transactionSuccess()) {
                    player.sendMessage(noMoneyError.get(player));
                    if (noMoneySound != null)
                        player.playSound(player.getLocation(), noMoneySound, 1.0f, 1.0f);
                    SlimyGuis.logger().log(Level.INFO, res.errorMessage);
                    return false;
                } else return true;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        public static IconClick deserialize(Config config) {
            IconClick res = new IconClick();
            res.permission = (String) config.get("permission");
            res.noPermissionError = config.getMessage("no-permission-message", "You don't have permission!");
            res.noPermissionSound = config.getSound("no-permission-sound");

            res.cost = PlaceholderValue.doubleValue(config.getString("cost", "0"));
            res.noMoneyError = config.getMessage("no-money-error", "You don't have enough money");
            res.noMoneySound = config.getSound("no-money-sound");

            List<Object> actions = (List<Object>) config.get("actions");
            if (actions == null)
                res.actions = Collections.emptyList();
            else
                res.actions = actions.stream()
                        .map(ActionType::deserialize)
                        .collect(Collectors.toList());
            return res;
        }

        @Override
        public void run(Player player) {
            if (checkPermission(player) && pay(player)) {
                for (Action action : actions)
                    action.run(player);
            }
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            joiner.add("permission: " + permission);
            joiner.add("noPermissionError: " + noPermissionError);
            joiner.add("noPermissionSound: " + noPermissionSound);
            joiner.add("cost: " + cost);
            joiner.add("noMoneyError: " + noMoneyError);
            joiner.add("noMoneySound: " + noMoneySound);
            return '{' + joiner.toString() + '}';
        }
    }

    private CustomItem display;
    private Link link;
    private int updateInterval; // 0 or < 0 are considered null

    public Icon() {
    }

    public Icon(ItemStack display) {
        this.display = new CustomItem(display);
    }

    public Icon(ItemStack display, Link link) {
        this.display = new CustomItem(display);
        this.link = link;
    }

    public Icon(CustomItem display, Link link) {
        this.display = display;
        this.link = link;
    }

    public void setDisplay(ItemStack display) {
        this.display = new CustomItem(display);
    }

    public boolean needUpdate() {
        return updateInterval > 0;
    }

    public void onClick(InventoryClickEvent e) {
        if (link != null)
            link.run((Player) e.getWhoClicked());
    }

    public static Icon deserialize(Config config) {
        Icon result = new Icon();
        try {
            result.updateInterval = config.getInt("update-interval", -1);
            result.display = CustomItem.deserialize(config.getConfigRequired("item"));
            if (config.has("click"))
                result.link = IconClick.deserialize(config.getConfig("click"));
            return result;
        } catch (InvalidGuiConfigurationException e) {
            e.addLocalizer("in gui display");
            throw e;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Icon item;

        public Builder() {
            item = new Icon();
        }

        public Builder(Icon item) {
            this.item = item;
        }

        public Builder item(ItemStack item) {
            this.item.setDisplay(item);
            return this;
        }

        public Builder item(CustomItem item) {
            this.item.display = item;
            return this;
        }

        public Builder link(Link link) {
            item.link = link;
            return this;
        }

        public Icon build() {
            return item;
        }
    }
}