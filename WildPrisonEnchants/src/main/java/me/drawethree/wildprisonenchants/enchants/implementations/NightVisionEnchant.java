package me.drawethree.wildprisonenchants.enchants.implementations;

import me.drawethree.wildprisonenchants.WildPrisonEnchants;
import me.drawethree.wildprisonenchants.enchants.WildPrisonEnchantment;
import org.bukkit.event.block.BlockBreakEvent;

public class NightVisionEnchant extends WildPrisonEnchantment {
    public NightVisionEnchant(WildPrisonEnchants instance) {
        super(instance, 7);
    }

    @Override
    public void onBlockBreak(BlockBreakEvent e, int enchantLevel) {

    }
}
