package org.zeith.tcrv.proxy;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import org.zeith.tcrv.TCRecipeViewer;
import org.zeith.tcrv.client.TCRVItemPanel;
import org.zeith.terraria.api.crafting.ItemViewPanel;

import static org.zeith.tcrv.TCRecipeViewer.MOD_ID;

public class ClientProxy
		extends CommonProxy
{
	public static final KeyBinding BACKTRACK_RECIPES = new KeyBinding("key." + MOD_ID + ".backtrack", KeyConflictContext.GUI, Keyboard.KEY_BACK, "key.category." + MOD_ID);
	public static final KeyBinding VIEW_RECIPES = new KeyBinding("key." + MOD_ID + ".recipes", KeyConflictContext.GUI, Keyboard.KEY_R, "key.category." + MOD_ID);
	public static final KeyBinding VIEW_USAGES = new KeyBinding("key." + MOD_ID + ".usages", KeyConflictContext.GUI, Keyboard.KEY_U, "key.category." + MOD_ID);

	@Override
	public void setup()
	{
		ClientRegistry.registerKeyBinding(BACKTRACK_RECIPES);
		ClientRegistry.registerKeyBinding(VIEW_RECIPES);
		ClientRegistry.registerKeyBinding(VIEW_USAGES);

		TCRecipeViewer.BACKTRACK_RECIPES.bind(BACKTRACK_RECIPES);
		TCRecipeViewer.VIEW_RECIPES.bind(VIEW_RECIPES);
		TCRecipeViewer.VIEW_USAGES.bind(VIEW_USAGES);

		ItemViewPanel.modify(TCRVItemPanel::new);
	}
}