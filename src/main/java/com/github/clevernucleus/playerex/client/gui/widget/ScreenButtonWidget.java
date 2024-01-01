package com.github.clevernucleus.playerex.client.gui.widget;

import com.github.clevernucleus.playerex.client.PlayerExClient;
import com.github.clevernucleus.playerex.client.gui.ExScreenData;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ScreenButtonWidget extends ButtonWidget {
	public static final Identifier EMPTY_KEY = new Identifier("playerex:empty");
	private HandledScreen<?> parent;
	private final Identifier key;
	private int u, v, dx, dy;
	public boolean alt;

	public ScreenButtonWidget(HandledScreen<?> parent, int x, int y, int u, int v, int width, int height,
			Identifier key, PressAction pressAction, NarrationSupplier narrationSupplier) {
		super(x, y, width, height, Text.empty(), pressAction, narrationSupplier);

		this.parent = parent;
		this.key = key;
		this.u = u;
		this.v = v;
		this.dx = x;
		this.dy = y;
		this.alt = false;
	}

	public ScreenButtonWidget(HandledScreen<?> parent, int x, int y, int u, int v, int width, int height,
			PressAction pressAction, NarrationSupplier narrationSupplier) {
		this(parent, x, y, u, v, width, height, EMPTY_KEY, pressAction, narrationSupplier);
	}

	public ScreenButtonWidget(HandledScreen<?> parent, int x, int y, int u, int v, int width, int height,
			PressAction pressAction) {
		this(parent, x, y, u, v, width, height, EMPTY_KEY, pressAction, DEFAULT_NARRATION_SUPPLIER);
	}

	public Identifier key() {
		return this.key;
	}

	@Override
	public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
		ExScreenData handledScreen = (ExScreenData) this.parent;
		this.setX(handledScreen.getX() + this.dx);
		this.setY(handledScreen.getY() + this.dy);

		RenderSystem.setShaderTexture(0, PlayerExClient.GUI);
		RenderSystem.disableDepthTest();

		int i = this.u;
		int j = this.v;

		if (this.alt) {
			i += this.width;
		}

		if (this.active) {
			if (this.isHovered()) {
				j += this.height;
			}
		} else {
			j += (2 * this.height);
		}

		// context.drawTexture(this.page.icon(), (int) ((getX() + 6) / this.scale),
		// (int) ((getY() + w) / this.scale), 0, 0, 256, 256);

		RenderSystem.enableDepthTest();
	}

}
