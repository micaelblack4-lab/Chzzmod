package com.chz.shopchz.shop;

import com.chz.shopchz.ShopCHZ;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ShopManager {

	private static final List<ShopCategory> CATEGORIES = new ArrayList<>();
	private static boolean initialized = false;
	private static MinecraftServer server;

	public static void init() {
		if (initialized) return;
		initialized = true;

		ShopCHZ.LOGGER.info("Carregando todos os itens do jogo para o ShopCHZ...");

		ShopCategory blocks = new ShopCategory("blocks", Text.literal("§6Blocos"), new ItemStack(Items.STONE));
		ShopCategory decoration = new ShopCategory("decoration", Text.literal("§dDecoração"), new ItemStack(Items.FLOWER_POT));
		ShopCategory tools = new ShopCategory("tools", Text.literal("§bFerramentas"), new ItemStack(Items.DIAMOND_PICKAXE));
		ShopCategory armor = new ShopCategory("armor", Text.literal("§9Armaduras"), new ItemStack(Items.DIAMOND_CHESTPLATE));
		ShopCategory food = new ShopCategory("food", Text.literal("§aComida"), new ItemStack(Items.COOKED_BEEF));
		ShopCategory redstone = new ShopCategory("redstone", Text.literal("§cRedstone"), new ItemStack(Items.REDSTONE));
		ShopCategory mobDrops = new ShopCategory("mob_drops", Text.literal("§4Drops de Mobs"), new ItemStack(Items.BONE));
		ShopCategory containers = new ShopCategory("containers", Text.literal("§5Containers & Shulkers"), new ItemStack(Items.SHULKER_BOX));
		ShopCategory enchanted = new ShopCategory("enchanted", Text.literal("§5Encantamentos"), new ItemStack(Items.ENCHANTED_BOOK));
		ShopCategory misc = new ShopCategory("misc", Text.literal("§7Diversos"), new ItemStack(Items.STICK));
		ShopCategory spawnEggs = new ShopCategory("spawn_eggs", Text.literal("§eSpawn Eggs"), new ItemStack(Items.ZOMBIE_SPAWN_EGG));

		for (Item item : Registries.ITEM) {
			if (item == Items.AIR) continue;

			ItemStack stack = new ItemStack(item);
			Identifier id = Registries.ITEM.getId(item);
			String path = id.getPath();

			int buyPrice = calculateBuyPrice(item, path);
			int sellPrice = Math.max(1, buyPrice / 4);

			if (isSpawnEgg(path)) {
				spawnEggs.addItem(stack, buyPrice, sellPrice);
			} else if (isShulkerOrContainer(path)) {
				containers.addItem(stack, buyPrice, sellPrice);
			} else if (isTool(path)) {
				tools.addItem(stack, buyPrice, sellPrice);
			} else if (isArmor(path)) {
				armor.addItem(stack, buyPrice, sellPrice);
			} else if (isFood(item)) {
				food.addItem(stack, buyPrice, sellPrice);
			} else if (isRedstone(path)) {
				redstone.addItem(stack, buyPrice, sellPrice);
			} else if (isMobDrop(path)) {
				mobDrops.addItem(stack, buyPrice, sellPrice);
			} else if (isDecoration(path)) {
				decoration.addItem(stack, buyPrice, sellPrice);
			} else if (isBlock(item)) {
				blocks.addItem(stack, buyPrice, sellPrice);
			} else {
				misc.addItem(stack, buyPrice, sellPrice);
			}
		}

		enchanted.addItem(new ItemStack(Items.BOOK), 5, 1);
		enchanted.addItem(new ItemStack(Items.ENCHANTED_BOOK), 50, 10);

		addIfNotEmpty(CATEGORIES, blocks);
		addIfNotEmpty(CATEGORIES, decoration);
		addIfNotEmpty(CATEGORIES, tools);
		addIfNotEmpty(CATEGORIES, armor);
		addIfNotEmpty(CATEGORIES, food);
		addIfNotEmpty(CATEGORIES, redstone);
		addIfNotEmpty(CATEGORIES, mobDrops);
		addIfNotEmpty(CATEGORIES, containers);
		addIfNotEmpty(CATEGORIES, enchanted);
		addIfNotEmpty(CATEGORIES, spawnEggs);
		addIfNotEmpty(CATEGORIES, misc);

		int totalItems = CATEGORIES.stream().mapToInt(c -> c.getEntries().size()).sum();
		ShopCHZ.LOGGER.info("ShopCHZ carregou " + totalItems + " itens em " + CATEGORIES.size() + " categorias!");
	}

	public static void loadEnchantments(MinecraftServer serverInstance) {
		server = serverInstance;
		ShopCategory enchanted = getCategory("enchanted");
		if (enchanted == null) return;

		try {
			var enchantmentRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

			int count = 0;
			for (RegistryEntry<Enchantment> entry : enchantmentRegistry.getIndexedEntries()) {
				Enchantment enchantment = entry.value();
				int maxLevel = enchantment.getMaxLevel();

				for (int level = 1; level <= maxLevel; level++) {
					ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);

					ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);
					builder.add(entry, level);
					book.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());

					int price = 30 + (level * 25) + (maxLevel * 10);
					enchanted.addItem(book, price, Math.max(5, price / 5));
					count++;
				}
			}

			ShopCHZ.LOGGER.info("Carregados " + count + " livros encantados no ShopCHZ!");
		} catch (Exception e) {
			ShopCHZ.LOGGER.error("Erro ao carregar encantamentos: " + e.getMessage());
		}
	}

	private static void addIfNotEmpty(List<ShopCategory> list, ShopCategory cat) {
		if (!cat.getEntries().isEmpty()) {
			list.add(cat);
		}
	}

	public static List<ShopCategory> getCategories() {
		return CATEGORIES;
	}

	public static ShopCategory getCategory(String id) {
		return CATEGORIES.stream()
				.filter(c -> c.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	private static boolean isSpawnEgg(String path) {
		return path.endsWith("_spawn_egg");
	}

	private static boolean isShulkerOrContainer(String path) {
		return path.contains("shulker") || path.contains("chest") || path.contains("barrel")
				|| path.contains("hopper") || path.contains("dispenser") || path.contains("dropper")
				|| path.contains("furnace") || path.contains("smoker") || path.contains("blast_furnace")
				|| path.contains("brewing_stand") || path.contains("anvil") || path.contains("grindstone")
				|| path.contains("cartography") || path.contains("loom") || path.contains("stonecutter")
				|| path.contains("smithing") || path.contains("crafter");
	}

	private static boolean isTool(String path) {
		return path.contains("_sword") || path.contains("_pickaxe") || path.contains("_axe")
				|| path.contains("_shovel") || path.contains("_hoe") || path.contains("bow")
				|| path.contains("crossbow") || path.contains("trident") || path.contains("mace")
				|| path.contains("fishing_rod") || path.contains("flint_and_steel") || path.contains("shears")
				|| path.contains("brush") || path.contains("spyglass") || path.contains("compass")
				|| path.contains("clock") || path.contains("recovery_compass");
	}

	private static boolean isArmor(String path) {
		return path.contains("_helmet") || path.contains("_chestplate") || path.contains("_leggings")
				|| path.contains("_boots") || path.contains("elytra") || path.contains("turtle_helmet")
				|| path.contains("shield") || path.contains("horse_armor") || path.contains("wolf_armor");
	}

	private static boolean isFood(Item item) {
		return item.getComponents().contains(DataComponentTypes.FOOD);
	}

	private static boolean isRedstone(String path) {
		return path.contains("redstone") || path.contains("piston") || path.contains("observer")
				|| path.contains("repeater") || path.contains("comparator") || path.contains("lever")
				|| path.contains("button") || path.contains("pressure_plate") || path.contains("tripwire")
				|| path.contains("daylight") || path.contains("target") || path.contains("calibrated_sculk")
				|| path.contains("sculk_sensor") || path.contains("note_block") || path.contains("jukebox");
	}

	private static boolean isMobDrop(String path) {
		return path.equals("bone") || path.equals("rotten_flesh") || path.equals("string")
				|| path.equals("spider_eye") || path.equals("gunpowder") || path.equals("ender_pearl")
				|| path.equals("blaze_rod") || path.equals("blaze_powder") || path.equals("ghast_tear")
				|| path.equals("magma_cream") || path.equals("slime_ball") || path.equals("phantom_membrane")
				|| path.equals("shulker_shell") || path.equals("nautilus_shell") || path.equals("heart_of_the_sea")
				|| path.equals("prismarine_shard") || path.equals("prismarine_crystals") || path.equals("ink_sac")
				|| path.equals("glow_ink_sac") || path.equals("feather") || path.equals("leather")
				|| path.equals("rabbit_hide") || path.equals("rabbit_foot") || path.equals("scute")
				|| path.equals("armadillo_scute") || path.equals("breeze_rod") || path.equals("wind_charge")
				|| path.equals("echo_shard") || path.equals("dragon_breath") || path.equals("nether_star")
				|| path.contains("head") || path.contains("skull") || path.equals("totem_of_undying");
	}

	private static boolean isDecoration(String path) {
		return path.contains("carpet") || path.contains("banner") || path.contains("bed")
				|| path.contains("candle") || path.contains("lantern") || path.contains("torch")
				|| path.contains("flower") || path.contains("pot") || path.contains("painting")
				|| path.contains("item_frame") || path.contains("armor_stand") || path.contains("sign")
				|| path.contains("hanging_sign") || path.contains("head") || path.contains("skull")
				|| path.contains("glass_pane") || path.contains("stained_glass") || path.contains("wool")
				|| path.contains("terracotta") || path.contains("concrete") || path.contains("glazed")
				|| path.contains("curtain") || path.contains("decorated_pot") || path.contains("pottery")
				|| path.contains("vase") || path.contains("shelf");
	}

	private static boolean isBlock(Item item) {
		return item.getComponents().contains(DataComponentTypes.BLOCK_STATE)
				|| Registries.BLOCK.containsId(Registries.ITEM.getId(item));
	}

	private static int calculateBuyPrice(Item item, String path) {
		if (path.contains("netherite")) return 32;
		if (path.contains("diamond")) return 2;
		if (path.contains("emerald")) return 1;
		if (path.contains("gold") || path.contains("golden")) return 4;
		if (path.contains("iron")) return 2;
		if (path.contains("copper")) return 1;
		if (path.contains("shulker")) return 1;
		if (path.endsWith("_spawn_egg")) return 16;
		if (path.contains("spawner")) return 64;
		if (path.contains("beacon")) return 48;
		if (path.contains("elytra")) return 40;
		if (path.contains("totem")) return 200;
		if (path.contains("nether_star")) return 36;
		if (path.contains("dragon_egg")) return 128;
		if (path.contains("enchanted_golden_apple")) return 20;
		if (path.contains("golden_apple")) return 1;
		return 1;
	}
}
