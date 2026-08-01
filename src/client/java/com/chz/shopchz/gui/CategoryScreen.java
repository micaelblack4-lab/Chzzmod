package com.chz.shopchz.gui;

import com.chz.shopchz.shop.ShopCategory;
import com.chz.shopchz.shop.ShopEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class CategoryScreen extends Screen {

	private final Screen parent;
	private final ShopCategory category;
	private int page = 0;
	private static final int COLUMNS = 8;
	private static final int ROWS = 5;
	private static final int ITEMS_PER_PAGE = COLUMNS * ROWS;

	public CategoryScreen(Screen parent, ShopCategory category) {
		super(category.getName());
		this.parent = parent;
		this.category = category;
	}

	@Override
	protected void init() {
		super.init();

		List<ShopEntry> entries = category.getEntries();
		int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) ITEMS_PER_PAGE));

		int centerX = this.width / 2;

		if (page > 0) {
			this.addDrawableChild(ButtonWidget.builder(Text.literal("§e◀"), b -> {
				page--;
				clearAndInit();
			}).dimensions(centerX - 120, this.height - 28, 30, 20).build());
		}

		if (page < totalPages - 1) {
			this.addDrawableChild(ButtonWidget.builder(Text.literal("§e▶"), b -> {
				page++;
				clearAndInit();
			}).dimensions(centerX + 90, this.height - 28, 30, 20).build());
		}

		this.addDrawableChild(ButtonWidget.builder(Text.literal("§cVoltar"), b -> this.close())
				.dimensions(centerX - 40, this.height - 28, 80, 20).build());
	}

	private void clearAndInit() {
		this.clearChildren();
		this.init();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, this.width, this.height, 0xCC000000);

		int boxWidth = 420;
		int boxHeight = this.height - 20;
		int boxX = this.width / 2 - boxWidth / 2;
		int boxY = 5;

		context.fill(boxX - 2, boxY - 2, boxX + boxWidth + 2, boxY + boxHeight + 2, 0xFFD4AF37);
		context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0xFF1A1A2E);

		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFD700);

		List<ShopEntry> entries = category.getEntries();
		int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) ITEMS_PER_PAGE));

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("§7Página §e" + (page + 1) + "§7/§e" + totalPages + " §8• §7" + entries.size() + " itens"),
				this.width / 2, 26, 0xAAAAAA);

		context.drawCenteredTextWithShadow(this.textRenderer,
				Text.literal("§aEsquerdo = Comprar  §8|  §cDireito = Vender  §8|  §eDinheiro virtual"),
				this.width / 2, 38, 0x88AA88);

		int startIndex = page * ITEMS_PER_PAGE;
		int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, entries.size());

		int slotSize = 18;
		int gap = 6;
		int gridWidth = COLUMNS * (slotSize + gap) - gap;
		int startX = this.width / 2 - gridWidth / 2;
		int startY = 52;

		ShopEntry hovered = null;

		for (int i = startIndex; i < endIndex; i++) {
			int local = i - startIndex;
			int col = local % COLUMNS;
			int row = local / COLUMNS;

			int x = startX + col * (slotSize + gap);
			int y = startY + row * (slotSize + 20);

			ShopEntry entry = entries.get(i);
			ItemStack stack = entry.getStack();

			boolean isHovered = mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16;

			int bgColor = isHovered ? 0xFF4A4A6A : 0xFF2A2A3E;
			int borderColor = isHovered ? 0xFFD4AF37 : 0xFF555570;

			context.fill(x - 2, y - 2, x + 18, y + 18, borderColor);
			context.fill(x - 1, y - 1, x + 17, y + 17, bgColor);

			context.drawItem(stack, x, y);
			context.drawItemInSlot(this.textRenderer, stack, x, y);

			String price = "§a" + entry.getBuyPrice();
			context.drawText(this.textRenderer, price, x + 1, y + 18, 0x55FF55, true);

			if (isHovered) {
				hovered = entry;
			}
		}

		if (hovered != null) {
			ItemStack stack = hovered.getStack();
			List<Text> tooltip = List.of(
					Text.literal("§6§l" + stack.getName().getString()),
					Text.literal(""),
					Text.literal("§a▶ Comprar: §e$" + hovered.getBuyPrice()),
					Text.literal("§c▶ Vender: §e$" + hovered.getSellPrice()),
					Text.literal(""),
					Text.literal("§7Clique §aEsquerdo §7para comprar"),
					Text.literal("§7Clique §cDireito §7para vender")
			);
			context.drawTooltip(this.textRenderer, tooltip, mouseX, mouseY);
		}

		super.render(context, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		List<ShopEntry> entries = category.getEntries();
		int startIndex = page * ITEMS_PER_PAGE;
		int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, entries.size());

		int slotSize = 18;
		int gap = 6;
		int gridWidth = COLUMNS * (slotSize + gap) - gap;
		int startX = this.width / 2 - gridWidth / 2;
		int startY = 52;

		for (int i = startIndex; i < endIndex; i++) {
			int local = i - startIndex;
			int col = local % COLUMNS;
			int row = local / COLUMNS;

			int x = startX + col * (slotSize + gap);
			int y = startY + row * (slotSize + 20);

			if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16) {
				if (this.client != null && this.client.player != null) {
					String catId = category.getId();
					if (button == 0) {
						this.client.player.networkHandler.sendChatCommand("loja " + catId + " comprar " + i);
					} else if (button == 1) {
						this.client.player.networkHandler.sendChatCommand("loja " + catId + " vender " + i);
					}
				}
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
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
