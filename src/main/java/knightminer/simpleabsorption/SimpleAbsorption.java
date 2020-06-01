package knightminer.simpleabsorption;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(SimpleAbsorption.MOD_ID)
public class SimpleAbsorption {
	// IDs
	protected static final String MOD_ID = "simple_absorption";

	public SimpleAbsorption() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
		MinecraftForge.EVENT_BUS.addListener(AbsorptionHandler::playerTick);
	}
}
