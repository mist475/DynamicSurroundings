/* This file is part of Dynamic Surroundings, licensed under the MIT License (MIT).
 *
 * Copyright (c) OreCruncher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.blockartistry.mod.DynSurround.data;

import cpw.mods.fml.common.Loader;
import foxie.calendar.api.CalendarAPI;
import foxie.calendar.api.ICalendarProvider;
import foxie.calendar.api.ISeasonProvider;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.Module;
import org.blockartistry.mod.DynSurround.data.config.DimensionConfig;
import org.blockartistry.mod.DynSurround.proxy.Proxy;
import org.blockartistry.mod.DynSurround.util.DiurnalUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class DimensionRegistry implements Comparable<DimensionRegistry> {

	private static final int SPACE_HEIGHT_OFFSET = 32;
	private static final boolean CALENDAR_API = Loader.isModLoaded("CalendarAPI");
	private static final String SEASON_NOT_AVAILABLE = "noseason";

	private static final List<DimensionConfig.Entry> cache = new ArrayList<>();
	private static final TIntObjectHashMap<DimensionRegistry> dimensionData = new TIntObjectHashMap<>();
	private static boolean isFlatWorld = false;

	private final int dimensionId;
	private boolean initialized;
	private String name = "<NOT SET>";
	private Integer seaLevel;
	private Integer skyHeight;
	private Integer cloudHeight;
	private Integer spaceHeight;
	private Boolean hasHaze;
	private Boolean hasAuroras;
	private Boolean hasWeather;

	public static void initialize() {
		try {
			process(DimensionConfig.load("dimensions"));
		} catch (final Exception e) {
			e.printStackTrace();
		}

		for (final String file : ModOptions.dimensionConfigFiles) {
			final File theFile = new File(Module.dataDirectory(), file);
			if (theFile.exists()) {
				try {
					final DimensionConfig config = DimensionConfig.load(theFile);
					if (config != null)
						process(config);
					else
						ModLog.warn("Unable to process dimension config file " + file);
				} catch (final Exception ex) {
					ModLog.error("Unable to process dimension config file " + file, ex);
				}
			} else {
				ModLog.warn("Could not locate dimension config file [%s]", file);
			}
		}

		ModLog.info("*** DIMENSION REGISTRY (delay init) ***");

        dimensionData.valueCollection()
            .stream()
            .sorted()
            .forEach(dimensionRegistry -> ModLog.info(dimensionRegistry.toString()))
        ;
	}

	public static void loading(final World world) {
		getData(world).initialize(world.provider);
		if (world.provider.dimensionId == 0) {
			isFlatWorld = world.getWorldInfo().getTerrainType() == WorldType.FLAT;
		}
	}

	private static DimensionConfig.Entry getData(final DimensionConfig.Entry entry) {
		for (final DimensionConfig.Entry e : cache)
			if ((e.dimensionId != null && e.dimensionId.equals(entry.dimensionId))
					|| (e.name != null && e.name.equals(entry.name)))
				return e;
		cache.add(entry);
		return entry;
	}

	private static void process(final DimensionConfig config) {
		for (final DimensionConfig.Entry entry : config.entries) {
			if (entry.dimensionId != null || entry.name != null) {
				final DimensionConfig.Entry data = getData(entry);
				if (data == entry)
					continue;
				if (data.dimensionId == null)
					data.dimensionId = entry.dimensionId;
				if (data.name == null)
					data.name = entry.name;
				if (entry.hasAurora != null)
					data.hasAurora = entry.hasAurora;
				if (entry.hasHaze != null)
					data.hasHaze = entry.hasHaze;
				if (entry.hasWeather != null)
					data.hasWeather = entry.hasWeather;
				if (entry.cloudHeight != null)
					data.cloudHeight = entry.cloudHeight;
				if (entry.seaLevel != null)
					data.seaLevel = entry.seaLevel;
				if (entry.skyHeight != null)
					data.skyHeight = entry.skyHeight;
			}
		}
	}

	private DimensionRegistry(final World world) {
		this.dimensionId = world.provider.dimensionId;
		initialize(world.provider);
	}

	private DimensionRegistry(final World world, final DimensionConfig.Entry entry) {
		this.dimensionId = world.provider.dimensionId;
		this.name = world.provider.getDimensionName();
		this.seaLevel = entry.seaLevel;
		this.skyHeight = entry.skyHeight;
		this.hasHaze = entry.hasHaze;
		this.hasAuroras = entry.hasAurora;
		this.hasWeather = entry.hasWeather;
		this.cloudHeight = entry.cloudHeight;
		initialize(world.provider);
	}

	private DimensionRegistry initialize(final WorldProvider provider) {
		if (!this.initialized) {
			this.name = provider.getDimensionName();
			if (this.seaLevel == null)
				this.seaLevel = provider.getAverageGroundLevel();
			if (this.skyHeight == null)
				this.skyHeight = provider.getHeight();
			if (this.hasHaze == null)
				this.hasHaze = !provider.hasNoSky;
			if (this.hasAuroras == null)
				this.hasAuroras = !provider.hasNoSky;
			if (this.hasWeather == null)
				this.hasWeather = !provider.hasNoSky;
			if (this.cloudHeight == null)
				this.cloudHeight = this.hasHaze ? this.skyHeight / 2 : this.skyHeight;
			if (this.spaceHeight == null)
				this.spaceHeight = this.skyHeight + SPACE_HEIGHT_OFFSET;
			this.initialized = true;
			ModLog.info("Dimension initialized " + this);
		}
		return this;
	}

	public int getDimensionId() {
		return this.dimensionId;
	}

	public String getName() {
		return this.name;
	}

	public int getSeaLevel() {
		return this.seaLevel;
	}

	public int getSkyHeight() {
		return this.skyHeight;
	}

	public int getCloudHeight() {
		return this.cloudHeight;
	}

	public int getSpaceHeight() {
		return this.spaceHeight;
	}

	public boolean getHasHaze() {
		return this.hasHaze;
	}

	public boolean getHasAuroras() {
		return this.hasAuroras;
	}

	public boolean getHasWeather() {
		return this.hasWeather;
	}

	public String getSeason() {
        //TODO: lotr shire reckoning -> CalenderAPI bridge in MistLotrTweaks
        if (Module.LOTR && this.name.equals("MiddleEarth") && !ModOptions.useNoLotrProxy) {
            return Module.LOTR_PROXY.getSeason();
        }
        if (!CALENDAR_API) {
            return SEASON_NOT_AVAILABLE;
        }

		final ISeasonProvider provider = CalendarAPI.getSeasonProvider(this.dimensionId);
		if (provider == null)
			return SEASON_NOT_AVAILABLE;

		final World world = DimensionManager.getWorld(this.dimensionId);
		if (world == null)
			return SEASON_NOT_AVAILABLE;

		final ICalendarProvider calendar = CalendarAPI.getCalendarInstance(world);
		if (calendar == null)
			return SEASON_NOT_AVAILABLE;

		return provider.getSeason(calendar).getName();
	}

	public static DimensionRegistry getData(final World world) {
		DimensionRegistry data = dimensionData.get(world.provider.dimensionId);
		if (data == null) {
			DimensionConfig.Entry entry = null;
			for (final DimensionConfig.Entry e : cache)
				if ((e.dimensionId != null && e.dimensionId == world.provider.dimensionId)
						|| (e.name != null && e.name.equals(world.provider.getDimensionName()))) {
					entry = e;
					break;
				}
			if (entry == null) {
				data = new DimensionRegistry(world);
			} else {
				data = new DimensionRegistry(world, entry);
			}
			dimensionData.put(world.provider.dimensionId, data);
		}
		return data;
	}

	public static boolean hasHaze(final World world) {
		return getData(world).getHasHaze();
	}

	public static int getSeaLevel(final World world) {
		if (world.provider.dimensionId == 0 && isFlatWorld)
			return 0;
		return getData(world).getSeaLevel();
	}

	public static int getSkyHeight(final World world) {
		return getData(world).getSkyHeight();
	}

	public static int getCloudHeight(final World world) {
		return getData(world).getCloudHeight();
	}

	public static int getSpaceHeight(final World world) {
		return getData(world).getSpaceHeight();
	}

	public static boolean hasAuroras(final World world) {
		return getData(world).getHasAuroras();
	}

	public static boolean hasWeather(final World world) {
		return getData(world).getHasWeather();
	}

	public static String getSeason(final World world) {
		return getData(world).getSeason();
	}

	private static final String CONDITION_TOKEN_RAINING = "raining";
	private static final String CONDITION_TOKEN_DAY = "day";
	private static final String CONDITION_TOKEN_NIGHT = "night";
	private static final char CONDITION_SEPARATOR = '#';

	public static String getConditions(final World world) {
		final StringBuilder builder = new StringBuilder();
		builder.append(CONDITION_SEPARATOR);
		if (DiurnalUtils.isDaytime(world))
			builder.append(CONDITION_TOKEN_DAY);
		else
			builder.append(CONDITION_TOKEN_NIGHT);
		builder.append(CONDITION_SEPARATOR).append(world.provider.getDimensionName());
		if (world.getRainStrength(1.0F) > 0.0F)
			builder.append(CONDITION_SEPARATOR).append(CONDITION_TOKEN_RAINING);
		builder.append(CONDITION_SEPARATOR).append(getSeason(world));
		builder.append(CONDITION_SEPARATOR);
		return builder.toString();
	}

	@Override
	public String toString() {
        return String.valueOf(this.dimensionId) + '/' + this.name + ':' +
            " seaLevel:" + this.seaLevel +
            " cloudH:" + this.cloudHeight +
            " skyH:" + this.skyHeight +
            " haze:" + this.hasHaze +
            " aurora:" + this.hasAuroras +
            " weather:" + this.hasWeather;
	}

    @Override
    public int compareTo(DimensionRegistry e) {
        return this.toString().compareToIgnoreCase(e.toString());
    }
}
