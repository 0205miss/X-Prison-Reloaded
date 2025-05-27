package dev.drawethree.xprison.enchants.gui;

import dev.drawethree.xprison.XPrison;
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
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

public final class PickaxeGUI extends Gui {
    private static String GUI_TITLE;
    private static Item EMPTY_SLOT_ITEM;
    private static int PICKAXE_ITEM_SLOT;
    private static int SKIN_SLOT;
    private static int ENCHANT_SLOT;
    private static int GUI_LINES;
    private static ItemStack SKIN_ITEM;
    private static ItemStack ENCHANT_ITEM;
    private static boolean PICKAXE_ITEM_ENABLED;

    @Getter
    @Setter
    private ItemStack pickAxe;

    @Getter
    private final int pickaxePlayerInventorySlot;

    private final XPrisonEnchants plugin;

    public PickaxeGUI(XPrisonEnchants plugin, Player player, ItemStack pickAxe, int pickaxePlayerInventorySlot) {
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

        this.setItem(SKIN_SLOT, ItemStackBuilder.of(SKIN_ITEM).build(() -> {
            this.close();
            new SkinGUI(this.plugin, this.getPlayer(), this.pickAxe, this.pickaxePlayerInventorySlot).open();
        }));

        this.setItem(SKIN_SLOT, ItemStackBuilder.of(SKIN_ITEM).build(() -> {
            this.close();
            new SkinGUI(this.plugin, this.getPlayer(), this.pickAxe, this.pickaxePlayerInventorySlot).open();
        }));

        this.setItem(ENCHANT_SLOT, ItemStackBuilder.of(ENCHANT_ITEM).build(() -> {
            this.close();
            new EnchantGUI(this.plugin, this.getPlayer(), this.pickAxe, this.pickaxePlayerInventorySlot).open();
        }));

    }


    public static void init() {

        GUI_TITLE = TextUtils.applyColor(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.title"));
        EMPTY_SLOT_ITEM = ItemStackBuilder.
                of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.empty_slots")).toItem()).buildItem().build();
        GUI_LINES = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("pickaxe_menu.lines");
        ENCHANT_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("pickaxe_menu.enchant.slot");
        SKIN_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("pickaxe_menu.skin.slot");
        PICKAXE_ITEM_ENABLED = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getBoolean("pickaxe_menu.pickaxe_enabled", true);
        Pattern base64_format = Pattern.compile("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$");

        String base64 = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.skin.Base64", "null");
        if(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.skin.material").equals("PLAYER_HEAD")&& base64_format.matcher(base64).matches()){
            SKIN_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(base64))
                    .name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.skin.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("pickaxe_menu.skin.lore")).build();
        }else{
            SKIN_ITEM = ItemStackBuilder.of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.skin.material")).toMaterial())
                    .name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.skin.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("pickaxe_menu.skin.lore")).build();
        }

        String enchant_base64 = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.enchant.Base64", "null");
        if(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.enchant.material").equals("PLAYER_HEAD")&& base64_format.matcher(enchant_base64).matches()){
            ENCHANT_ITEM = ItemStackBuilder.of(SkullUtils.getCustomTextureHead(enchant_base64))
                    .name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.enchant.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("pickaxe_menu.enchant.lore")).build();
        }else{
            ENCHANT_ITEM = ItemStackBuilder.of(CompMaterial.fromString(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.enchant.material")).toMaterial())
                    .name(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getString("pickaxe_menu.enchant.name")).lore(XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getStringList("pickaxe_menu.enchant.lore")).build();
        }

        if (PICKAXE_ITEM_ENABLED) {
            PICKAXE_ITEM_SLOT = XPrisonEnchants.getInstance().getEnchantsConfig().getYamlConfig().getInt("pickaxe_menu.pickaxe_slot");
        }
    }
}
