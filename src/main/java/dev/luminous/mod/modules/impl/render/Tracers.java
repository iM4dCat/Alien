package dev.luminous.mod.modules.impl.render;

import dev.luminous.api.utils.math.MathUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.asm.accessors.IEntity;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;

public class Tracers extends Module {
	private final ColorSetting item = add(new ColorSetting("Item", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting player = add(new ColorSetting("Player", new Color(255, 255, 255, 100)).injectBoolean(true));
	private final ColorSetting chest = add(new ColorSetting("Chest", new Color(255, 255, 255, 100)).injectBoolean(false));
	private final ColorSetting enderChest = add(new ColorSetting("EnderChest", new Color(255, 100, 255, 100)).injectBoolean(false));
	private final ColorSetting shulkerBox = add(new ColorSetting("ShulkerBox", new Color(15, 255, 255, 100)).injectBoolean(false));
	public Tracers() {
		super("Tracers", Category.Render);
		setChinese("追踪者");
	}

    @Override
	public void onRender3D(MatrixStack matrixStack) {
		boolean prev_bob = mc.options.getBobView().getValue();
		mc.options.getBobView().setValue(false);
		if (item.booleanValue || player.booleanValue) {
			for (Entity entity : mc.world.getEntities()) {
				if (entity instanceof ItemEntity && item.booleanValue) {
					drawLine(entity.getPos(), item.getValue());
				} else if (entity instanceof PlayerEntity && player.booleanValue && entity != mc.player) {
					drawLine(entity.getPos(), player.getValue());
				}
			}
		}
		ArrayList<BlockEntity> blockEntities = BlockUtil.getTileEntities();
		for (BlockEntity blockEntity : blockEntities) {
			if (blockEntity instanceof ChestBlockEntity && chest.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), chest.getValue());
			} else if (blockEntity instanceof EnderChestBlockEntity && enderChest.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), enderChest.getValue());
			} else if (blockEntity instanceof ShulkerBoxBlockEntity && shulkerBox.booleanValue) {
				drawLine(blockEntity.getPos().toCenterPos(), shulkerBox.getValue());
			}
		}
		mc.options.getBobView().setValue(prev_bob);
	}


	private void drawLine(Vec3d pos, Color color) {
		Render3DUtil.drawLine(pos, mc.player.getCameraPosVec(mc.getTickDelta()).add(Vec3d.fromPolar(mc.player.getPitch(mc.getTickDelta()), mc.player.getYaw(mc.getTickDelta())).multiply(0.2)), color);
	}
}
