package org.zeith.tcrv.api;

import com.zeitheron.hammercore.utils.base.Cast;
import com.zeitheron.hammercore.utils.base.SideLocal;
import net.minecraft.util.ResourceLocation;
import org.zeith.terraria.api.crafting.Recipe;
import org.zeith.terraria.api.data.DataAttachment;
import org.zeith.terraria.common.data.player.PlayerDataTC;

import java.util.Optional;
import java.util.function.Predicate;

import static org.zeith.tcrv.TCRecipeViewer.MOD_ID;

public class RecipeRestriction
{
	public static final ResourceLocation ATTACHMENT = new ResourceLocation(MOD_ID, "restriction");

	public static SideLocal<Boolean> SPOILER_FREE_MODE = SideLocal.withInitial(true, true);

	public static boolean passesRestriction(Recipe recipe, PlayerDataTC pd)
	{
		return getRestriction(recipe).map(f -> f.test(pd)).orElse(true);
	}

	public static Optional<Predicate<PlayerDataTC>> getRestriction(Recipe recipe)
	{
		DataAttachment att = recipe.getAttachment(ATTACHMENT);
		if(att != null)
			return Cast.cast(att.optionally(Predicate.class));
		return Optional.empty();
	}

	public static void attachRestriction(Recipe recipe, Predicate<PlayerDataTC> restriction)
	{
		DataAttachment a = recipe.getAttachment(ATTACHMENT);
		if(a == null)
		{
			a = new DataAttachment(ATTACHMENT);
			a.data = restriction;
		} else
			a.data = getRestriction(recipe)
					.map(r -> r.and(restriction))
					.orElse(restriction);
		recipe.attachData(a);
	}
}