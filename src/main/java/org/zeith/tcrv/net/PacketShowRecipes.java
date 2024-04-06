package org.zeith.tcrv.net;

import com.zeitheron.hammercore.net.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.*;
import org.zeith.tcrv.TCRecipeViewer;
import org.zeith.tcrv.api.RecipeRestriction;
import org.zeith.tcrv.client.WidgetShowRecipes;
import org.zeith.terraria.api.crafting.*;
import org.zeith.terraria.client.gui.api.TerrariaGui;
import org.zeith.terraria.common.data.player.PlayerDataTC;
import org.zeith.terraria.net.util.Net;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.*;

@Getter
@MainThreaded
public class PacketShowRecipes
		implements IPacket
{
	protected List<ResourceLocation> recipes;
	protected ItemStack stack;
	protected int kind;
	
	public static PacketShowRecipes create(PacketRequestRecipes packet, EntityPlayerMP mp)
	{
		ItemStack stack = packet.stack;
		boolean usages = packet.kind == 1;
		
		Item desiredItem = stack.getItem();
		
		Stream<Recipe> recipeStream =
				usages
				? CraftingRegistry.findUses(stack).stream()
				: CraftingRegistry.recipes().filter(recipe -> recipe.output.getItem() == desiredItem);
		
		if(RecipeRestriction.SPOILER_FREE_MODE.get())
		{
			PlayerDataTC pd = PlayerDataTC.get(mp);
			recipeStream = recipeStream.filter(recipe -> RecipeRestriction.passesRestriction(recipe, pd));
		}
		
		List<ResourceLocation> recipes = recipeStream
				.map(Recipe::getId)
				.collect(Collectors.toList());
		
		return create(stack, packet.kind, recipes);
	}
	
	public static PacketShowRecipes create(ItemStack stack, int kind, List<ResourceLocation> recipes)
	{
		PacketShowRecipes pkt = new PacketShowRecipes();
		pkt.recipes = recipes;
		pkt.stack = stack;
		pkt.kind = kind;
		return pkt;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList lst = new NBTTagList();
		for(ResourceLocation recipe : recipes) lst.appendTag(new NBTTagString(recipe.toString()));
		nbt.setTag("R", lst);
		nbt.setInteger("K", kind);
		nbt.setTag("T", stack.writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		NBTTagList lst = nbt.getTagList("R", Constants.NBT.TAG_STRING);
		recipes = NonNullList.withSize(lst.tagCount(), new ResourceLocation(""));
		for(int i = 0; i < lst.tagCount(); ++i)
			recipes.set(i, new ResourceLocation(lst.getStringTagAt(i)));
		kind = nbt.getInteger("K");
		stack = new ItemStack(nbt.getCompoundTag("T"));
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void executeOnClient2(PacketContext net)
	{
		List<ResourceLocation> missing = new ArrayList<>();
		List<Recipe> recipes = this.recipes.stream().map(id ->
		{
			Recipe r = CraftingRegistry.byId(id);
			if(r == null) missing.add(id);
			return r;
		}).filter(Objects::nonNull).collect(Collectors.toList());
		
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		
		if(gui instanceof TerrariaGui)
		{
			TerrariaGui<?> tg = (TerrariaGui<?>) gui;
			
			AtomicReference<WidgetShowRecipes> parent = new AtomicReference<>();
			
			tg.widgets.removeIf(w ->
			{
				if(w instanceof WidgetShowRecipes)
				{
					parent.set((WidgetShowRecipes) w);
					return true;
				}
				return false;
			});
			
			if(!recipes.isEmpty())
			{
				WidgetShowRecipes wgSr = new WidgetShowRecipes(parent.get(), recipes);
				wgSr.targetStack = stack;
				wgSr.targetKind = kind;
				wgSr.initWidget(gui.width, gui.height);
				((TerrariaGui<?>) gui).widgets.add(0, wgSr);
				wgSr.setGui((TerrariaGui<?>) gui);
			}
		}
		
		TCRecipeViewer.LOG.warn("Ignoring {} missing recipes: {}", missing.size(), missing);
	}
}