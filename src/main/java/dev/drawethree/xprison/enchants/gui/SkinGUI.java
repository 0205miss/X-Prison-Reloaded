package dev.drawethree.xprison.enchants.gui;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.config.FileManager;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.utils.GuiUtils;
import dev.drawethree.xprison.utils.compat.CompMaterial;
import dev.drawethree.xprison.utils.item.ItemStackBuilder;
import dev.drawethree.xprison.utils.misc.SkullUtils;
import dev.drawethree.xprison.utils.player.PlayerUtils;
import dev.drawethree.xprison.utils.text.TextUtils;
import lombok.Getter;
import lombok.Setter;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class SkinGUI extends Gui {
    private static String USED_LORE;
    private static String UNUSED_LORE;
    private static String GUI_TITLE;
    private static Item EMPTY_SLOT_ITEM;
    private static int PICKAXE_ITEM_SLOT;
    private static int GUI_LINES;
    private static boolean PICKAXE_ITEM_ENABLED;
    private static Map<String, ConfigurationSection> skinsById;
    @Getter
    @Setter
    private ItemStack pickAxe;

    @Getter
    private final int pickaxePlayerInventorySlot;

    private final XPrisonEnchants plugin;

    public SkinGUI(XPrisonEnchants plugin, Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
        super(player, GUI_LINES, GUI_TITLE);
        this.plugin = plugin;
        this.pickAxe = pickAxe;
        this.pickaxePlayerInventorySlot = pickaxePlayerInventorySlot;

        Events.subscribe(InventoryCloseEvent.class, EventPriority.LOWEST)
                .filter(e -> e.getInventory().equals(this.getHandle()))
                .handler(e -> {
                    XPrison.getInstance().getEnchants().getEnchantsManager().handlePickaxeUnequip(this.getPlayer(), this.pickAxe);
                    XPrison.getInstance().getEnchants().getEnchantsManager().handlePickaxeEquip(this.getPlayer(), this.pickAxe);
                }).bindWith(this);

        // Checking for duping
        Schedulers.sync().runLater(() -> {
            if (!pickAxe.equals(this.getPlayer().getInventory().getItem(this.pickaxePlayerInventorySlot))) {
                this.close();
            }
        },10);

        setFallbackGui(player1 -> new PickaxeGUI(plugin, player, pickAxe, pickaxePlayerInventorySlot));
    }

    @Override
    public void redraw() {

        // perform initial setup.
        if (isFirstDraw()) {
            for (int i = 0; i < this.getHandle().getSize(); i++) {
                this.setItem(i, EMPTY_SLOT_ITEM);
            }
        }

        if (PICKAXE_ITEM_ENABLED) {
            this.setItem(PICKAXE_ITEM_SLOT, Item.builder(this.pickAxe).build());
        }

        Collection<XPrisonEnchantment> allEnchants = this.plugin.getEnchantsRepository().getAll();
        for (Map.Entry<String,ConfigurationSection> entry  : skinsById.entrySet()) {
            this.setItem(entry.getValue().getInt("slot"), getGuiItem(pickAxe,this,entry.getKey(), entry.getValue()));
        }
    }

    private Item getGuiItem(ItemStack item, SkinGUI gui, String id,ConfigurationSection config) {

        ItemMeta meta = item.getItemMeta();
        List<String> lore = config.getStringList("lore");

        if(meta.hasCustomModelData()){
            if(meta.getCustomModelData()==Integer.parseInt(id)){
                lore.add(0, USED_LORE);
            } else {
                lore.add(0, UNUSED_LORE);
            }
        }else{
            lore.add(0, UNUSED_LORE);
        }

        ItemStackBuilder builder = ItemStackBuilder.of(Material.DIAMOND_PICKAXE);

        builder.name(config.getString("name","外觀"));

        builder.lore(lore);
        builder.custommodel(Integer.parseInt(id));
        return builder.buildItem().bind(handler -> {
            if (handler.getClick() == ClickType.LEFT) {
                meta.setCustomModelData(Integer.parseInt(id));
                item.setItemMeta(meta);
                gui.getPlayer().getInventory().setItem(gui.getPickaxePlayerInventorySlot(),gui.getPickAxe());
                gui.redraw();
            }
        },  ClickType.LEFT).build();
    }

    public static void init() {
        ConfigurationSection skinconfig = XPrisonEnchants.getInstance().getEnchantsConfig().getSkinYamlConfig().getConfigurationSection("skins");
        skinsById = new HashMap<>();
        for(String keys : skinconfig.getKeys(false)) { // all kays: "1", "2" ...
            ConfigurationSection objSection = skinconfig.getConfigurationSection(keys);
            skinsById.put(keys, objSection);
        }

        USED_LORE = TextUtils.applyColor(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("skins.used_lore"));
        UNUSED_LORE = TextUtils.applyColor(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("skins.un_used_lore"));

        GUI_TITLE = TextUtils.applyColor(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("skins.title"));
        EMPTY_SLOT_ITEM = ItemStackBuilder.
                of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("skins.empty_slots")).toItem()).buildItem().build();
        GUI_LINES = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("skins.lines");

        PICKAXE_ITEM_ENABLED = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getBoolean("skins.pickaxe_enabled", true);


        if (PICKAXE_ITEM_ENABLED) {
            PICKAXE_ITEM_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("skins.pickaxe_slot");
        }
    }
}
