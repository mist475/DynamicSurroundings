package org.blockartistry.mod.DynSurround.compat;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

public interface ISeasonalTemperatureCompat {

    float getTemperature(World world, BiomeGenBase biome, int x, int y, int z);
}
