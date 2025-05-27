package dev.drawethree.xprison.enchants.repo;

import dev.drawethree.xprison.XPrison;
import dev.drawethree.xprison.database.SQLDatabase;
import dev.drawethree.xprison.enchants.XPrisonEnchants;
import dev.drawethree.xprison.enchants.model.XPrisonEnchantment;
import dev.drawethree.xprison.enchants.model.impl.*;
import dev.drawethree.xprison.utils.text.TextUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EnchantsRepository {

	private static final String TABLE_NAME = "Rebirth";
	private final XPrisonEnchants plugin;
	private final SQLDatabase database;
	private final Map<Integer, XPrisonEnchantment> enchantsById;
	private final Map<String, XPrisonEnchantment> enchantsByName;

	public EnchantsRepository(XPrisonEnchants plugin) {
		this.plugin = plugin;
		this.enchantsById = new HashMap<>();
		this.enchantsByName = new HashMap<>();
        this.database = this.plugin.getCore().getPluginDatabase();
    }

	public Collection<XPrisonEnchantment> getAll() {
		return enchantsById.values();
	}

	public XPrisonEnchantment getEnchantBy(Object object) {
		if (object instanceof Integer) {
			return getEnchantById((int) object);
		} else {
			final String s = String.valueOf(object);
			try {
				return getEnchantById(Integer.parseInt(s));
			} catch (NumberFormatException e) {
				return getEnchantByName(s);
			}
		}
	}

	public XPrisonEnchantment getEnchantById(int id) {
		return enchantsById.get(id);
	}

	public XPrisonEnchantment getEnchantByName(String name) {
		return enchantsByName.get(name.toLowerCase());
	}

	public void reload() {

		enchantsById.values().forEach(XPrisonEnchantment::reload);

		XPrison.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully reloaded all enchants."));
	}

	public void loadDefaultEnchantments() {
		register(new EfficiencyEnchant(this.plugin));
		register(new UnbreakingEnchant(this.plugin));
		register(new FortuneEnchant(this.plugin));
		register(new HasteEnchant(this.plugin));
		register(new SpeedEnchant(this.plugin));
		register(new JumpBoostEnchant(this.plugin));
		register(new NightVisionEnchant(this.plugin));
		register(new FlyEnchant(this.plugin));
		register(new ExplosiveEnchant(this.plugin));
		register(new LayerEnchant(this.plugin));
		register(new CharityEnchant(this.plugin));
		register(new SalaryEnchant(this.plugin));
		register(new BlessingEnchant(this.plugin));
		register(new TokenatorEnchant(this.plugin));
		register(new KeyFinderEnchant(this.plugin));
		register(new PrestigeFinderEnchant(this.plugin));
		register(new BlockBoosterEnchant(this.plugin));
		register(new KeyallsEnchant(this.plugin));
		if (XPrison.getInstance().isUltraBackpacksEnabled()) {
			register(new BackpackAutoSellEnchant(this.plugin));
		} else {
			register(new AutoSellEnchant(this.plugin));
		}
		register(new VoucherFinderEnchant(this.plugin));
		register(new NukeEnchant(this.plugin));
		register(new GemFinderEnchant(this.plugin));
		register(new GangValueFinderEnchant(this.plugin));
		createTables();
	}

	public boolean register(XPrisonEnchantment enchantment) {

		if (enchantsById.containsKey(enchantment.getId()) || enchantsByName.containsKey(enchantment.getRawName())) {
			XPrison.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to register enchant " + enchantment.getName() + "&c created by " + enchantment.getAuthor() + ". That enchant is already registered."));
			return false;
		}

		Validate.notNull(enchantment.getRawName());

		enchantsById.put(enchantment.getId(), enchantment);
		enchantsByName.put(enchantment.getRawName().toLowerCase(), enchantment);

		XPrison.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully registered enchant " + enchantment.getName() + "&a created by " + enchantment.getAuthor()));
		return true;
	}

	public boolean unregister(XPrisonEnchantment enchantment) {

		if (!enchantsById.containsKey(enchantment.getId()) && !enchantsByName.containsKey(enchantment.getRawName())) {
			XPrison.getInstance().getLogger().warning(TextUtils.applyColor("&cUnable to unregister enchant " + enchantment.getName() + "&c created by " + enchantment.getAuthor() + ". That enchant is not registered."));
			return false;
		}

		enchantsById.remove(enchantment.getId());
		enchantsByName.remove(enchantment.getRawName());

		XPrison.getInstance().getLogger().info(TextUtils.applyColor("&aSuccessfully unregistered enchant " + enchantment.getName() + "&a created by " + enchantment.getAuthor()));
		return true;
	}

	public void createTables() {
		StringBuilder s = new StringBuilder();
		for (Map.Entry<Integer, XPrisonEnchantment> entry: enchantsById.entrySet()){
			String key = entry.getKey().toString();
			s.append("enchant_").append(key).append(" bigint DEFAULT 0,");
		}
		this.database.executeSql("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(UUID varchar(36) NOT NULL UNIQUE,rebirth bigint DEFAULT 0,"+ s+" primary key (UUID))");
		this.database.executeSql("CREATE TABLE IF NOT EXISTS CurrentPickaxe (UUID varchar(36) NOT NULL UNIQUE,"+ s+" primary key (UUID))");
	}

	public void addnewEnchants(Player p) {
		this.database.executeSql("INSERT OR REPLACE INTO " + TABLE_NAME + " (UUID) values(?)", p.getUniqueId().toString());
		this.database.executeSql("INSERT OR REPLACE INTO CurrentPickaxe (UUID,enchant_1) values(?,?)", p.getUniqueId().toString(),1);
	}

	public void updateEnchants(Player p,int id,int amount) {
		String enchant = "enchant_"+ id;
		this.database.executeSql("UPDATE CurrentPickaxe SET "+enchant+" = "+enchant+" + "+amount+" WHERE UUID=?", p.getUniqueId().toString());
	}

	public void rebirth(Player p, ItemStack item) {
		this.database.executeSql("UPDATE "+TABLE_NAME+" SET rebirth = rebirth + 1 WHERE UUID=?", p.getUniqueId().toString());
		this.plugin.getEnchantsManager().forEachEffectiveEnchant(p,item,(enchant, level) ->{
			String enchant_FORMAT = "enchant_"+ enchant.getId();
			this.database.executeSql("UPDATE "+TABLE_NAME+" SET "+enchant_FORMAT+" = "+level+ " WHERE UUID=?", p.getUniqueId().toString());
		});

	}

	public Map<XPrisonEnchantment, Integer> getenchantfromdatabase(Player p) {
		try (Connection con = this.database.getConnection(); PreparedStatement statement = database.prepareStatement(con,"SELECT * FROM CurrentPickaxe WHERE UUID=?")) {
			statement.setString(1, p.getUniqueId().toString());
			try (ResultSet set = statement.executeQuery()) {
				if (set.next()) {
					Map<XPrisonEnchantment, Integer> returnMap = new HashMap<>();
					for (int i = 1; i < set.getMetaData().getColumnCount()+1; i++) {
						if(set.getMetaData().getColumnName(i).startsWith("enchant_")){
							String[] a = set.getMetaData().getColumnName(i).split("_");
							int num = Integer.parseInt(a[1]);
							int lvl = (int) set.getObject(i);
							if(lvl==0)
								continue;
							returnMap.put(getEnchantById(num),lvl);
						}
					}
					return returnMap;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return Map.of();
    }
}
