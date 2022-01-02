package org.zeith.tcrv.net;

import com.zeitheron.hammercore.net.HCNet;
import com.zeitheron.hammercore.net.IPacket;
import com.zeitheron.hammercore.net.MainThreaded;
import com.zeitheron.hammercore.net.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.zeith.tcrv.api.RecipeRestriction;
import org.zeith.tcrv.client.WidgetShowRecipes;
import org.zeith.terraria.api.crafting.CraftingRegistry;
import org.zeith.terraria.api.crafting.Recipe;
import org.zeith.terraria.client.gui.api.TerrariaGui;
import org.zeith.terraria.common.data.player.PlayerDataTC;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MainThreaded
public class PacketShowRecipes
		implements IPacket
{
	List<ResourceLocation> recipes;
	ItemStack stack;
	int kind;

	public static PacketShowRecipes create(PacketRequestRecipes packet, EntityPlayerMP mp)
	{
		ItemStack stack = packet.stack;
		boolean usages = packet.kind == 1;

		PlayerDataTC pd = PlayerDataTC.get(mp);

		Stream<Recipe> recipeStream = usages
				? CraftingRegistry.findUses(stack).stream()
				: CraftingRegistry.recipes().filter(recipe -> recipe.output.isItemEqual(stack));

		if(RecipeRestriction.SPOILER_FREE_MODE.get())
			recipeStream = recipeStream.filter(recipe -> RecipeRestriction.passesRestriction(recipe, pd));

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

	public void to(EntityPlayerMP mp)
	{
		if(!recipes.isEmpty())
			HCNet.INSTANCE.sendTo(this, mp);
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

			tg.widgets.removeIf(w -> w instanceof WidgetShowRecipes);

			if(!recipes.isEmpty())
			{
				WidgetShowRecipes wgSr = new WidgetShowRecipes(recipes);
				wgSr.targetStack = stack;
				wgSr.targetKind = kind;
				wgSr.initWidget(gui.width, gui.height);
				((TerrariaGui<?>) gui).widgets.add(0, wgSr);
				wgSr.setGui((TerrariaGui<?>) gui);
			}
		}
	}
}