package org.zeith.tcrv.net;

import com.zeitheron.hammercore.net.*;
import lombok.var;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

@MainThreaded
public class PacketRequestRecipes
		implements IPacket
{
	protected byte kind;
	protected ItemStack stack;
	
	public PacketRequestRecipes()
	{
	}
	
	public PacketRequestRecipes(byte kind, ItemStack stack)
	{
		this.kind = kind;
		this.stack = stack.copy().splitStack(1);
	}
	
	@Override
	public void write(PacketBuffer buf)
	{
		buf.writeByte(kind);
		buf.writeItemStack(stack);
	}
	
	@Override
	public void read(PacketBuffer buf)
			throws IOException
	{
		kind = buf.readByte();
		stack = buf.readItemStack();
	}
	
	@Override
	public void executeOnServer2(PacketContext net)
	{
		var v = PacketShowRecipes.create(this, net.getSender());
		if(!v.getRecipes().isEmpty())
			net.withReply(v);
	}
}