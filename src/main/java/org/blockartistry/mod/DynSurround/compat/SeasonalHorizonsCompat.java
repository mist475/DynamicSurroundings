package org.blockartistry.mod.DynSurround.compat;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.darkshadow44.seasonalhorizons.api.SeasonsAPI;

public class SeasonalHorizonsCompat implements ISeasonalTemperatureCompat {

    @Override
    public float getTemperature(World world, BiomeGenBase biome, int x, int y, int z) {
        return SeasonsAPI.getTemperature(world, biome, x, y, z);
    }
}
