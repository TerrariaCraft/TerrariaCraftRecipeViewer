package org.zeith.tcrv;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.*;
import org.zeith.tcrv.proxy.CommonProxy;
import org.zeith.terraria.api.mod.ITerrariaMod;
import org.zeith.terraria.common.data.player.KeyMapTC;

@Mod(
		modid = TCRecipeViewer.MOD_ID,
		name = "TerrariaCraft RecipeViewer",
		dependencies = "required-after:terraria",
		version = "@VERSION@",
		certificateFingerprint = "9f5e2a811a8332a842b34f6967b7db0ac4f24856",
		updateJSON = "https://api.modrinth.com/updates/LmQmk6VW/forge_updates.json"
)
public class TCRecipeViewer
		implements ITerrariaMod
{
	public static final Logger LOG = LogManager.getLogger();
	public static final String MOD_ID = "tcrecipeview";

	@SidedProxy(serverSide = "org.zeith.tcrv.proxy.CommonProxy", clientSide = "org.zeith.tcrv.proxy.ClientProxy")
	public static CommonProxy proxy;

	@Mod.Instance
	public static TCRecipeViewer instance;

	@Mod.EventHandler
	public void setup(FMLPreInitializationEvent e)
	{
		proxy.setup();
	}

	public static final KeyMapTC.KeyButtonTC BACKTRACK_RECIPES = new KeyMapTC.KeyButtonTC(new ResourceLocation(MOD_ID, "backtrack"), (data, state) ->
	{
	});

	public static final KeyMapTC.KeyButtonTC VIEW_RECIPES = new KeyMapTC.KeyButtonTC(new ResourceLocation(MOD_ID, "recipes"), (data, state) ->
	{
	});

	public static final KeyMapTC.KeyButtonTC VIEW_USAGES = new KeyMapTC.KeyButtonTC(new ResourceLocation(MOD_ID, "uses"), (data, state) ->
	{
	});
}