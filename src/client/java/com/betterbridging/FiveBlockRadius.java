package com.betterbridging;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.BlockItem;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class FiveBlockRadius implements ClientModInitializer {

	private static KeyBinding togglePlaceBlockKeybind;
	private static KeyBinding placeBlockOnceKeybind;
	private boolean placingBlocks = false; // Toggle flag

	@Override
	public void onInitializeClient() {
		// Register the keybindings
		togglePlaceBlockKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.toggle_place_block_below_5_Radius", // Translation key
				GLFW.GLFW_KEY_Z, // Default key is 'Z'
				"category.betterbridging" // Category
		));

		placeBlockOnceKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.place_block_once_5_Radius", // Translation key
				GLFW.GLFW_KEY_X, // Default key is 'X'
				"category.betterbridging" // Category
		));

		// Register a tick event listener to check for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (togglePlaceBlockKeybind.wasPressed()) {
				// Toggle the placingBlocks flag when the key is pressed
				placingBlocks = !placingBlocks;

				// Notify the player about the toggle state
				if (client.player != null) {
					if (placingBlocks) {
						client.player.sendMessage(Text.literal("Block placing toggled ON"), true);
					} else {
						client.player.sendMessage(Text.literal("Block placing toggled OFF"), true);
					}
				}
			}

			// If placingBlocks is true, continue placing blocks
			if (placingBlocks) {
				placeBlocksInRadius(client);
			}

			// Check if the single-place keybind is pressed
			if (placeBlockOnceKeybind.wasPressed()) {
				placeBlocksInRadius(client); // Place blocks once without toggling
			}
		});
	}

	private void placeBlocksInRadius(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		World world = client.world;
		BlockPos playerPos = client.player.getBlockPos().down(); // Get the block position below the player

		// Define a 5-block radius
		int radius = 2; // Radius of 2 blocks in each direction (5x5 area)

		// Iterate through all block positions in the radius
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				BlockPos targetPos = playerPos.add(x, 0, z); // Position relative to the player's feet

				// Check if we can place a block at that position (air or water)
				if (canPlaceOn(world, targetPos)) {
					BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(targetPos), Direction.UP, targetPos, false);

					// Attempt to place the block from main hand or offhand
					if (client.player.getMainHandStack().getItem() instanceof BlockItem) {
						client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
					} else if (client.player.getOffHandStack().getItem() instanceof BlockItem) {
						client.interactionManager.interactBlock(client.player, Hand.OFF_HAND, hitResult);
					}
				}
			}
		}
	}

	// Method to check if a block can be placed on air or water
	private boolean canPlaceOn(World world, BlockPos pos) {
		return world.isAir(pos) || world.getBlockState(pos).isOf(Blocks.WATER);
	}
}
