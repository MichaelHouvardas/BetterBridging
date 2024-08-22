package com.betterbridging;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public class BetterBridgingClient implements ClientModInitializer {

	private static KeyBinding placeBlockBelowKeybind;

	@Override
	public void onInitializeClient() {
		// Register the keybinding
		placeBlockBelowKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.betterbridging.place_block_below", // Translation key
				GLFW.GLFW_KEY_B, // Default key is 'B'
				"category.betterbridging" // Category
		));

		// Register a tick event listener to check for key presses
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (placeBlockBelowKeybind.wasPressed()) {
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
			// Attempt to place the block
			BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(playerPos), Direction.UP, playerPos, false);
			client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hitResult);
		}
	}
}
