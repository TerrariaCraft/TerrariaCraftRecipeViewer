package org.zeith.tcrv.client;

import com.zeitheron.hammercore.client.utils.texture.gui.theme.GuiTheme;

public class TerrariaCraftTheme
		extends GuiTheme
{
	public static final TerrariaCraftTheme INSTANCE = new TerrariaCraftTheme();

	public TerrariaCraftTheme()
	{
		super("terraria");
		applyColor(0, 0xCC2A3763);
		applyColor(1, 0xAA6484EA);
		applyColor(2, 0xAA495EAB);
		applyColor(3, 0xAA495EAB);
	}

	private void applyColor(int id, int rgb)
	{
		switch(id)
		{
			case 0:
				bodyCover = rgb;
				break;
			case 1:
				bodyLayerLU = rgb;
				break;
			case 2:
				bodyLayerRD = rgb;
				break;
			case 3:
				bodyColor = rgb;
				break;
		}
	}

	public int getColor(int id)
	{
		switch(id)
		{
			case 0:
				return (bodyCover);
			case 1:
				return (bodyLayerLU);
			case 2:
				return (bodyLayerRD);
			case 3:
				return (bodyColor);
			case 4:
				return (slotCoverLU);
			case 5:
				return (slotCoverRD);
			case 6:
				return (slotColor);
			case 7:
				return (textColor);
			case 8:
				return (textShadeColor);
		}

		return 0xFFFFFFFF;
	}
}