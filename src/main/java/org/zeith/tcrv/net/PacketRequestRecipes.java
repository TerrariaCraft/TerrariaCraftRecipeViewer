package org.zeith.tcrv.net;

import com.zeitheron.hammercore.net.IPacket;
import com.zeitheron.hammercore.net.MainThreaded;
import com.zeitheron.hammercore.net.PacketContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@MainThreaded
public class PacketRequestRecipes
		implements IPacket
{
	byte kind;
	ItemStack stack;

	public PacketRequestRecipes()
	{
	}

	public PacketRequestRecipes(byte kind, ItemStack stack)
	{
		this.kind = kind;
		this.stack = stack;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		nbt.setByte("K", kind);
		nbt.setTag("T", stack.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		kind = nbt.getByte("K");
		stack = new ItemStack(nbt.getCompoundTag("T"));
	}

	@Override
	public void executeOnServer2(PacketContext net)
	{
		PacketShowRecipes
				.create(this, net.getSender())
				.to(net.getSender());
	}
}