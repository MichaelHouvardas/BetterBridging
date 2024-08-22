package com.betterbridging;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
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

public class BetterBridgingClient implements ClientModInitializer {

	private static KeyBinding togglePlaceBlockKeybind;
	private boolean placingBlocks = false; // Toggle flag

	@Override
	public void onInitializeClient() {
		// Register the keybinding
		togglePlaceBlockKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.toggle_place_block_below", // Translation key
				GLFW.GLFW_KEY_B, // Default key is 'B'
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
				placeBlockBelow(client);
			}
		});
	}

	private void placeBlockBelow(MinecraftClient client) {
		if (client.player == null || client.world == null) {
			return;
		}

		World world = client.world;
		BlockPos playerPos = client.player.getBlockPos().down(); // Get block position below player

		// Check if we can place a block at that position
		if (world.isAir(playerPos)) {
			// Attempt to place the block from main hand or offhand
			BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(playerPos), Direction.UP, playerPos, false);

			// Check if the main hand contains a block item, otherwise use the offhand
			if (client.player.getMainHandStack().getItem() instanceof BlockItem) {
				client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
			} else if (client.player.getOffHandStack().getItem() instanceof BlockItem) {
				client.interactionManager.interactBlock(client.player, Hand.OFF_HAND, hitResult);
			}
		}
	}
}
