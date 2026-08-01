package com.chz.shopchz.gui;

import com.chz.shopchz.shop.ShopCategory;
import com.chz.shopchz.shop.ShopManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ShopScreen extends Screen {

	private final Screen parent;

	public ShopScreen(Screen parent) {
		super(Text.literal("Loja ShopCHZ"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();

		List<ShopCategory> categories = ShopManager.getCategories();

		int buttonWidth = 160;
		int buttonHeight = 24;
		int columns = 2;
		int spacingX = 20;
		int spacingY = 8;
		int startY = 55;

		int totalWidth = (columns * buttonWidth) + ((columns - 1) * spacingX);
		int startX = this.width / 2 - totalWidth / 2;

		for (int i = 0; i < categories.size(); i++) {
			ShopCategory category = categories.get(i);
			int col = i % columns;
			int row = i / columns;

			int x = startX + col * (buttonWidth + spacingX);
			int y = startY + row * (buttonHeight + spacingY);

			String label = category.getName().getString() + " §8(" + category.getEntries().size() + ")";

			this.addDrawableChild(ButtonWidget.builder(
					Text.literal(label),
					button -> {
						if (this.client != null) {
							this.client.setScreen(new CategoryScreen(this, category));
						}
					}
			).dimensions(x, y, buttonWidth, buttonHeight).build());
		}

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("§a§lSaldo"),
				button -> {
					if (this.client != null && this.client.player != null) {
						this.client.player.networkHandler.sendChatCommand("saldo");
					}
				}
		).dimensions(this.width / 2 - 110, this.height - 30, 70, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
				Text.literal("§c§lFechar"),
				button -> this.close()
		).dimensions(this.width / 2 + 40, this.height - 30, 70, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0xCC000000);

		int boxWidth = 380;
		int boxHeight = this.height - 40;
		int boxX = this.width / 2 - boxWidth / 2;
		int boxY = 10;

		context.fill(boxX - 2, boxY - 2, boxX + boxWidth + 2, boxY + boxHeight + 2, 0xFFD4AF37);
		context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF1A1A2E);

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("§6§l✦ LOJA SHOPCHZ ✦"),
				this.width / 2, 20, 0xFFD700);

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("§7Dinheiro virtual do Shop"),
				this.width / 2, 35, 0xAAAAAA);

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public void close() {
		if (this.client != null) {
			this.client.setScreen(this.parent);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
