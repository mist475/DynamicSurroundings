package org.blockartistry.mod.DynSurround.mixinplugin;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;
import cpw.mods.fml.relauncher.FMLLaunchHandler;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.common.config.Configuration;
import org.blockartistry.mod.DynSurround.ModLog;
import org.blockartistry.mod.DynSurround.ModOptions;
import org.blockartistry.mod.DynSurround.asm.Transformer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Adapted from BugTorch & the old asm transformer
 */
@IFMLLoadingPlugin.Name("DynamicSurroundingsEarlyMixins")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions({"org.blockartistry.mod.DynSurround.asm."})
@IFMLLoadingPlugin.SortingIndex(10001)
public class DynamicSurroundingsEarlyMixins implements IFMLLoadingPlugin, IEarlyMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.dsurround.early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        ModLog.info("Kicking off Dynamic Surroundings early mixins.");
        boolean client = FMLLaunchHandler.side().isClient();
        List<String> mixins = new ArrayList<>();

        if (client) {
            if (!ModOptions.disableWeatherEffects) {
                mixins.add("MixinEntityRenderer");
            }
            mixins.add("MixinSoundManager");
            //Sound engine crash patches
            mixins.add("MixinPaulsCodeSource");
            mixins.add("MixinPaulsCodeSoundLibrary");
            mixins.add("MixinPaulsCodeStreamThread");
        }

        if (!ModOptions.disableWeatherEffects) {
            mixins.add("MixinWorld");
            mixins.add("MixinWorldServer");
        }


        return mixins;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{Transformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {
        // Tickle the configuration so we can get some options initialized
        final File configFile = new File((File) map.get("mcLocation"), "/config/dsurround/dsurround.cfg");
        final Configuration config = new Configuration(configFile);
        ModOptions.load(config);
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
