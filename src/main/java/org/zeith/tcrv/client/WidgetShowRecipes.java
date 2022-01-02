package org.zeith.tcrv.client;

import com.zeitheron.hammercore.client.utils.RenderUtil;
import com.zeitheron.hammercore.client.utils.Scissors;
import com.zeitheron.hammercore.client.utils.texture.gui.DynGuiTex;
import com.zeitheron.hammercore.client.utils.texture.gui.GuiTexBakery;
import com.zeitheron.hammercore.utils.ItemStackUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.zeith.terraria.api.crafting.CountableIngredient;
import org.zeith.terraria.api.crafting.ItemViewPanel;
import org.zeith.terraria.api.crafting.Recipe;
import org.zeith.terraria.api.items.ICustomDescriptor;
import org.zeith.terraria.api.tooltip.StringTooltipElement;
import org.zeith.terraria.api.tooltip.TooltipBody;
import org.zeith.terraria.client.gui.api.widget.WidgetBase;
import org.zeith.terraria.client.render.RenderHelperTC;

import java.awt.*;
import java.util.List;

public class WidgetShowRecipes
		extends WidgetBase
{
	public static ItemStack hover = ItemStack.EMPTY;
	public static Float forceTabScrollAmount;
	public static int prevTabScrollAmount, tabScrollAmount;
	protected static boolean prevShowRecipes, prevShowUses;

	DynGuiTex tx;

	public ItemStack targetStack = ItemStack.EMPTY;
	public int targetKind;
	private final Rectangle box = new Rectangle(), hoverBox = new Rectangle();
	protected boolean mouseInArea;

	public List<Recipe> recipes;

	public WidgetShowRecipes(List<Recipe> recipes)
	{
		this.recipes = recipes;
		this.width = 100;
		this.height = 30;
		prevTabScrollAmount = tabScrollAmount = 0;
	}

	@Override
	public void initWidget(int width, int height)
	{
		super.initWidget(width, height);

		x = 220F / width;
		y = 91F / height;

		this.width = width - 220 - 90;
		this.height = height - 25 - 91;

		int skip = (height - 14) / 16;

		GuiTexBakery b = GuiTexBakery.start();

		String targetText = I18n.format("gui.tcrecipeview.recipes_widget.target." + targetKind, targetStack.getDisplayName());
		int subWid = Math.min(Minecraft.getMinecraft().fontRenderer.getStringWidth(targetText) + 16, this.width - 4);

		b.body((this.width - subWid) / 2, -12, subWid, 16);
		tx = b.bake();

		b.body(0, 0, this.width, this.height);

		tx.theme = TerrariaCraftTheme.INSTANCE;
	}

	@Override
	public void updateWidget()
	{
		prevTabScrollAmount = tabScrollAmount;
		if(mouseInArea)
		{
			int dw = Mouse.getDWheel();

			if(forceTabScrollAmount != null)
			{
				tabScrollAmount = Math.max(0, (int) Math.round((double) forceTabScrollAmount * Math.ceil(this.recipes.size()) - 5.0D));
			} else if(dw != 0)
			{
				tabScrollAmount -= dw / 120;
			}
		}

		tabScrollAmount = Math.max(0, Math.min(tabScrollAmount, (int) Math.ceil(this.recipes.size()) - 5));
		prevTabScrollAmount = Math.max(0, Math.min(prevTabScrollAmount, (int) Math.ceil(this.recipes.size()) - 5));
	}

	@Override
	public void drawWidget(float partialTicks, Point mouse)
	{
		hover = ItemStack.EMPTY;

		box.setBounds(0, 0, width, height);
		mouseInArea = box.contains(mouse);

		GlStateManager.enableBlend();
		tx.render(0, 0);


		Minecraft mc = Minecraft.getMinecraft();
		RenderItem renderItem = mc.getRenderItem();
		FontRenderer fontRenderer = mc.fontRenderer;

		{
			String targetText = I18n.format("gui.tcrecipeview.recipes_widget.target." + targetKind, targetStack.getDisplayName());
			int txtWidth = fontRenderer.getStringWidth(targetText);
			int subWid = Math.min(txtWidth + 16, this.width - 4);

			GlStateManager.pushMatrix();
			float x = (width - subWid) / 2F + 16;
			float toRemove = Math.min(1, (subWid - 20F) / (txtWidth));
			GlStateManager.translate(x, -8 + fontRenderer.FONT_HEIGHT / 2, 0);
			GlStateManager.scale(toRemove, toRemove, toRemove);
			fontRenderer.drawString(targetText, 0, -fontRenderer.FONT_HEIGHT / 2, 0xFFFFFF);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(x - 10, -8.5F, 0);
			GlStateManager.scale(0.5, 0.5, 0.5);
			renderItem.renderItemIntoGUI(targetStack, 0, 0);
			GlStateManager.popMatrix();
		}

		TooltipBody body = null;

		Scissors.begin();
		Scissors.scissor((int) getActualX(), (int) getActualY() + 3, width, height - 6);

		for(int i = 0; i < recipes.size(); i++)
		{
			Recipe r = recipes.get(i);

			int y = 7 + i * 18;
			y -= (prevTabScrollAmount + (tabScrollAmount - prevTabScrollAmount) * partialTicks) * 18;

			if(y < -18) continue;

			int ingWidth = 6 + r.items.length * 17;
			int termX = width - 21 - 10 - 1;
			float scale = Math.min(1F, termX / (float) ingWidth);

			float x = 6;
			for(int i1 = 0; i1 < r.items.length; i1++)
			{
				CountableIngredient ci = r.items[i1];

				ItemStack stack = ItemStackUtil.cycleItemStack(ci.ingr).copy();
				stack.setCount(ci.count);

				renderItem.renderItemAndEffectIntoGUI(stack, (int) x, y);
				renderItem.renderItemOverlays(fontRenderer, stack, (int) x, y);
				box.setBounds((int) x, y, 16, 16);

				if(box.contains(mouse) && mouse.y > 3 && mouse.y < height - 4)
				{
					hoverBox.setBounds(box);
					hover = stack;

					body = new TooltipBody();
					ICustomDescriptor.generateTooltip(hover, body, mc.player);
				}

				x += 17 * scale;
			}

			if(r.predicate != null)
			{
				ItemStack stack = ItemStackUtil.cycleItemStack(r.predicate.getIcons());

				GlStateManager.pushMatrix();
				box.setBounds(width - 21 - 10, y + 4, 8, 8);
				GlStateManager.translate(box.x, box.y, 0);
				GlStateManager.scale(box.width / 16F, box.height / 16F, box.width / 16F);
				renderItem.renderItemAndEffectIntoGUI(stack, 0, 0);
				GlStateManager.popMatrix();

				if(!stack.isEmpty() && box.contains(mouse) && mouse.y > 3 && mouse.y < height - 4)
				{
					hoverBox.setBounds(box);
					hover = stack;
					body = new TooltipBody();
					body.append(new StringTooltipElement(r.predicate.getDependency()));
				}
			}

			RenderHelper.enableGUIStandardItemLighting();
			renderItem.renderItemAndEffectIntoGUI(r.output, width - 21, y);
			renderItem.renderItemOverlays(fontRenderer, r.output, width - 21, y);

			RenderHelper.disableStandardItemLighting();
			GlStateManager.color(1F, 1F, 1F, 1F);

			box.setBounds(width - 21, y, 16, 16);
			if(box.contains(mouse) && mouse.y > 3 && mouse.y < height - 4)
			{
				hoverBox.setBounds(box);
				hover = r.output;
				body = new TooltipBody();
				ICustomDescriptor.generateTooltip(hover, body, mc.player);
			}

			if(y >= height - 7 - 16)
				break;
		}

		if(!hover.isEmpty())
		{
			KeyBinding showRecipe = ItemViewPanel.getInstance().getShowRecipesKey();
			KeyBinding showUses = ItemViewPanel.getInstance().getShowUsesKey();
			if(showRecipe != null && Keyboard.isKeyDown(showRecipe.getKeyCode()) != prevShowRecipes)
			{
				prevShowRecipes = !prevShowRecipes;
				if(prevShowRecipes)
					ItemViewPanel.getInstance().showRecipes(hover);
			}

			if(showUses != null && Keyboard.isKeyDown(showUses.getKeyCode()) != prevShowUses)
			{
				prevShowUses = !prevShowUses;
				if(prevShowUses)
					ItemViewPanel.getInstance().showUses(hover);
			}

			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0F, 0.0F, 400.0F);
			GlStateManager.disableTexture2D();
			RenderUtil.drawColoredModalRect(hoverBox.x, hoverBox.y, hoverBox.width, hoverBox.height, -1426063361);
			GlStateManager.enableTexture2D();

			GlStateManager.popMatrix();
		}

		Scissors.end();

		if(body != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(-getActualX(), -getActualY(), 405);
			RenderHelperTC.drawTooltip(body, (int) (getActualX() + mouse.x), (int) (getActualY() + mouse.y), gui.width, gui.height);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void mouseClick(Point mouse, int button)
	{
		if(!hover.isEmpty())
		{
			switch(button)
			{
				case 0:
					ItemViewPanel.getInstance().showRecipes(hover);
					break;
				case 1:
					ItemViewPanel.getInstance().showUses(hover);
					break;
			}
		}
	}
}