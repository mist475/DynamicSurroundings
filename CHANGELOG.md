### DynamicSurroundings-1.7.10-1.0.7.6
**What's New**
* Add Et Futurum Requiem blockmap by Omgise

### DynamicSurroundings-1.7.10-1.0.7.5
**Fixes**
* Fix support for all mods that use access transformers on classes with mixins (Thanks Kaguya233qwq & Makamys)
* Add option to not use the lotr proxy, even if the mod is present (some versions of lotr break due to a missing class)
* Add option to use reflection to retrieve lotr biomes (Same reason as above)

### DynamicSurroundings-1.7.10-1.0.7.4
**Fixes**
* Fix MAtmos startup crash due to mixin/AT clash
* Fix crash with non-default fog settings

### DynamicSurroundings-1.7.10-1.0.7.3
**What's New**
* Move all but one asm transformer to mixins
* Cleanup code with generics injection & newer java syntax via Jabel
* Added lotr season detection to dimension registry (previously only CalenderAPI was supported, I don't think anything uses seasons by default atm but might turn out useful at some point)
* Move lotr biome detection to much less hacky approach

### DynamicSurroundings-1.7.10-1.0.7.2
**What's New**
* Basic configuration for lotr blocks:
  * Footstep sounds fully done
  * Basic effects, might receive some tweaks in the future

**Fixes**
* Fix update checker only printing a notification in singleplayer
* Remove 1.8+ blocks from config, in debug mode these produced an error, and I'm not planning on updating my fork to newer versions

### DynamicSurroundings-1.7.10-1.0.7.1
**Fixes**
* Fix vanilla biome detection

### DynamicSurroundings-1.7.10-1.0.7.0
**Fixes**
* Fix lotr biomes returning the wrong sounds due to duplicate biome ids

**Changes**
* BiomeRegistry now uses biome names instead of biome id's as key.
This is less performant but more compatible as I expect lotr isn't the only mod with duplicate biomes ids from vanilla

### DynamicSurroundings-1.7.10-1.0.6.5
**Fixes**
* Fix update checker and move urls to this repo

### DynamicSurroundings-1.7.10-1.0.6.4
**Fixes**
* Added defensive code to protect against bad SoundType entries.
* Patch getNormalizedPitch() to catch invalid parameters to avoid NPE.

### DynamicSurroundings-1.7.10-1.0.6.3
**Fixes**
* Sounds sporadically do not play.  Added ASM to patch up Minecraft's SoundManager to flush the sound engine command queue to mitigate potential concurrency issues.

**Changes**
* Backport genGaussian changes for performance
* Stone block no longer has dust drop effect

### DynamicSurroundings-1.7.10-1.0.6.2
**Fixes**
* Fixed NPE related to fog calculations when having the blindness effect.

**Changes**
* Pulled in ambient sounds from the 1.10.x+ branches.  Biome sounds for crickets, forest, plains, jungle, river, and wind have been updated.

### DynamicSurroundings-1.7.10-1.0.6.1
**What's New**
* Backported the fog changes made in the 1.10.x+ branches.  Summary of impact:
    * Various fog factors have been removed from the config.  They don't work with the new system, and they were little used.
    * Elevation haze calculation is fixed in a band about cloud height.  Elevation haze can be disabled in the config.
    * Much better compatibility with other mods such as Biomes O'Plenty.
    * Added morning fog - sets in about 3AM, peaks at dawn, and burns off in the morning.  By default it happens every morning, but that can be changed using the config.
    * Added weather fog.  The density of the fog is based on the intensity of the rain fall.  Can be disabled in the config.
* Backported some sound manager changes from 1.10.x+ branches:
    * Add method synchronization to eliminate a common cause of comodification errors reported from  SoundManager.
    * IC2 sounds will work better, or at least work. :)
* Backport some aurora rendering performance tweaks.
* Added config option "Disable Weather Effects" when set to **true** will disable all weather related ASM and handling.  Essentially you get Vanilla weather patterns.

**Fixes**
* Several NPEs/race conditions reported via OpenEye.

**Changes**
* Use LCG random number generator for block scan coordinates.  Bottom line is that the process of doing the area block scan is faster.

### DynamicSurroundings-1.7.10-1.0.6.0
* Removed all that sound engine restart stuff and replaced with patches to the underlying Minecraft sound engine to avoid the situation all together.  Thanks to CreativeMD and his work on getting to the bottom of things!  This should eliminate a variety of reported problems up through and including:
    * Sound Engine restart lag
    * Frequency of the sound engine crashes
    * Various repeating errors in the client log related to sound muting and sounds not being found
    * Crashes due to sound engine being yanked out from under other mods (such as IC2)
* Sometimes a sound instance would not play.  When this occurs something like "Error in class 'LibraryLWJGLOpenAL'" would show in the log.  Put in some additional code to make sure the sound information is flushed down into the sound engine.

### DynamicSurroundings-1.7.10-1.0.5.12
**Fixes**
* Setting footstep sound scale factor to 0 reverts to vanilla footstep sounds (backport)
* [OpenEye](https://openeye.openmods.info/crashes/4a99da03285429c87ec8d9347210268e): Fix NPE in storm render when calculating color
* [OpenEye](https://openeye.openmods.info/crashes/35c791f2c48ae15e0a8c42b832c38aac): Defensive code for bad potion IDs
* [OpenEye](https://openeye.openmods.info/crashes/ef9cee918b144eaa128da318420d6dbf): Hard requirement of [Forge 10.13.4.1614](https://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.7.10.html) or later for Minecraft 1.7.10

**Changes**
* Footstep sound processing performance changes (backport)
* Use ASM to hook Minecraft sound stream loading to improve responsiveness and reduce stream errors; can be turned off in config if needed (backport)
* Pumpkin/Melon footstep sounds no longer squishy (backport)
* Display diagnostic info only when debug screen is active and debug logging is enabled in config

### DynamicSurroundings-1.7.10-1.0.5.11
**Fixes**
* No more sound clicking when moving fast; was noticeable when flying over a beach (backport)

**Changes**
* No sound attenuation for player centered sounds (backport)
* Increase total number of sound channels (backport)
* Attempt automatic restart of crashed sound system (backport)
* Improve RNG used by scanning routines (backport)

### DynamicSurroundings-1.7.10-1.0.5.10
**Fixes**
* Backports from Dynamic Surroundings 1.10.2/1.11.x:
    * Sounds at biome transition boundaries were "edgy"; new logic fades in/out to specified volumes
    * Simplify sound emitter logic
* Harden exception handling for footstep sound play to guard against erroneous sound event handling.  Based on reports from OpenEye.

**Changes**
* Updated footstep sound profile for Tinker's Construct tool stations, etc.
* Updated biome sounds:
    * Forest - new background as well as bird chirps and woodpeckers
    * Water biomes - River, Ocean, Deep Ocean
    * Underwater - when a player's head is in a source block but not in a watery biome
    * Coyote spot sounds in various biomes
* Take into account Wasteland Forest ([Wasteland Mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2274942-wasteland-mod-1-4-4-abandoned-world-cities-and)) when applying biome rules
