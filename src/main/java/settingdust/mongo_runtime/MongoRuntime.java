package settingdust.mongo_runtime;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Mod;

@Mod(modid = "mongo_runtime", acceptableRemoteVersions = "*")
public class MongoRuntime {
    public MongoRuntime() {
        Launch.classLoader.addTransformerExclusion("com.mongodb.Jep395RecordCodecProvider");
    }
}
