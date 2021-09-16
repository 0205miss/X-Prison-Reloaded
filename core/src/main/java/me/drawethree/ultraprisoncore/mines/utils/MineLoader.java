package me.drawethree.ultraprisoncore.mines.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.drawethree.ultraprisoncore.mines.managers.MineManager;
import me.drawethree.ultraprisoncore.mines.model.mine.BlockPalette;
import me.drawethree.ultraprisoncore.mines.model.mine.Mine;
import me.drawethree.ultraprisoncore.mines.model.mine.reset.ResetType;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.serialize.Point;
import me.lucko.helper.serialize.Region;
import org.bukkit.potion.PotionEffectType;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MineLoader {

	public static Mine load(MineManager manager, FileReader reader) {
		JsonObject obj = GsonProvider.readObject(reader);

		String name = obj.get("name").getAsString();
		Point teleportLocation = obj.get("teleport-location").isJsonNull() ? null : Point.deserialize(obj.get("teleport-location"));
		Region region = obj.get("region").isJsonNull() ? null : Region.deserialize(obj.get("region"));
		BlockPalette palette = obj.get("blocks").isJsonNull() ? new BlockPalette() : BlockPalette.deserialize(obj.get("blocks"));

		ResetType resetType = obj.get("reset-type").isJsonNull() ? ResetType.INSTANT : ResetType.of(obj.get("reset-type").getAsString());
		double resetPercentage = obj.get("reset-percentage").getAsDouble();
		boolean broadcastReset = obj.get("broadcast-reset").isJsonNull() || obj.get("broadcast-reset").getAsBoolean();

		Hologram blocksLeftHologram = obj.get("hologram-blocks-left").isJsonNull() ? null : Hologram.deserialize(obj.get("hologram-blocks-left"));
		Hologram blocksMinedHologram = obj.get("hologram-blocks-mined").isJsonNull() ? null : Hologram.deserialize(obj.get("hologram-blocks-mined"));

		Map<PotionEffectType, Integer> mineEffects = new HashMap<>();

		JsonElement mineEffectsObj = obj.get("effects");

		if (mineEffectsObj != null) {
			for (Map.Entry<String, JsonElement> entry : mineEffectsObj.getAsJsonObject().entrySet()) {
				PotionEffectType type = PotionEffectType.getByName(entry.getKey());
				int amplifier = entry.getValue().getAsInt();
				mineEffects.put(type, amplifier);
			}
		}

		return new Mine(manager, name, region, teleportLocation, palette, resetType, resetPercentage, broadcastReset, blocksLeftHologram, blocksMinedHologram, mineEffects);
	}

	public static void save(Mine mine) {
		try (FileWriter writer = new FileWriter(mine.getFile())) {
			GsonProvider.writeObjectPretty(writer, mine.serialize().getAsJsonObject());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
