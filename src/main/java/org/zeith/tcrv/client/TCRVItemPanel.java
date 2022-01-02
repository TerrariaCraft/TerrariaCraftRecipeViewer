package org.zeith.tcrv.client;

import com.zeitheron.hammercore.net.HCNet;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.tcrv.net.PacketRequestRecipes;
import org.zeith.tcrv.proxy.ClientProxy;
import org.zeith.terraria.api.crafting.ItemViewPanel;

@SideOnly(Side.CLIENT)
public class TCRVItemPanel
		extends ItemViewPanel
{
	public TCRVItemPanel(ItemViewPanel prev)
	{
		super(prev);
	}

	@Override
	public KeyBinding getShowRecipesKey()
	{
		return ClientProxy.VIEW_RECIPES;
	}

	@Override
	public KeyBinding getShowUsesKey()
	{
		return ClientProxy.VIEW_USAGES;
	}

	@Override
	public void showUses(ItemStack ingredient)
	{
		HCNet.INSTANCE.sendToServer(new PacketRequestRecipes((byte) 1, ingredient));
	}

	@Override
	public void showRecipes(ItemStack ingredient)
	{
		HCNet.INSTANCE.sendToServer(new PacketRequestRecipes((byte) 0, ingredient));
	}

	@Override
	public ItemStack getStackUnderMouseInItemPanel()
	{
		if(!WidgetShowRecipes.hover.isEmpty()) return WidgetShowRecipes.hover;
		return super.getStackUnderMouseInItemPanel();
	}
}