package dev.luminous.mod.modules.impl.render;

import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HoleESP extends Module {

	public HoleESP() {
		super("HoleESP", Category.Render);
		setChinese("坑透视");
	}

	public final SliderSetting startFade = add(new SliderSetting("AlphaFade", 5f, 1f, 20f));
	public final SliderSetting distance = add(new SliderSetting("Distance", 6f, 1f, 20f));
	public final SliderSetting airHeight = add(new SliderSetting("AirHeight", 1, -3, 3, .1));
	public final BooleanSetting airYCheck = add(new BooleanSetting("AirYCheck", true));
	private final ColorSetting airFill = add(new ColorSetting("AirFill", new Color(148, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting airBox = add(new ColorSetting("AirBox", new Color(148, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting airFade = add(new ColorSetting("AirFade", new Color(148, 0, 0, 0)).injectBoolean(true));
	public final SliderSetting height = add(new SliderSetting("Height", 1, -3, 3, .1));
	private final ColorSetting normalFill = add(new ColorSetting("NormalFill", new Color(255, 0,0, 50)).injectBoolean(true));
	private final ColorSetting normalBox = add(new ColorSetting("NormalBox", new Color(255, 0, 0, 100)).injectBoolean(true));
	private final ColorSetting normalFade = add(new ColorSetting("NormalFade", new Color(255, 0, 0, 0)).injectBoolean(true));
	private final ColorSetting bedrockFill = add(new ColorSetting("BedrockFill", new Color(8, 255, 79, 50)).injectBoolean(true));
	private final ColorSetting bedrockBox = add(new ColorSetting("BedrockBox", new Color(8, 255, 79, 100)).injectBoolean(true));
	private final ColorSetting bedrockFade = add(new ColorSetting("BedrockFade", new Color(8, 255, 79, 100)).injectBoolean(true));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 50, 0, 1000));
	List<BlockPos> normalList = new CopyOnWriteArrayList<>();
	List<BlockPos> bedrockList = new CopyOnWriteArrayList<>();
	List<BlockPos> airList = new CopyOnWriteArrayList<>();
	final List<BlockPos> tempNormalList = new CopyOnWriteArrayList<>();
	final List<BlockPos> tempBedrockList = new CopyOnWriteArrayList<>();
	final List<BlockPos> tempAirList = new CopyOnWriteArrayList<>();
	final Timer timer = new Timer();
	@Override
	public void onThread() {
		if (nullCheck()) return;
		if (!drawing && timer.passed(updateDelay.getValue())) {
			normalList = new CopyOnWriteArrayList<>(tempNormalList);
			bedrockList = new CopyOnWriteArrayList<>(tempBedrockList);
			airList = new CopyOnWriteArrayList<>(tempAirList);
			timer.reset();
			tempBedrockList.clear();
			tempNormalList.clear();
			tempAirList.clear();
			for (BlockPos pos : BlockUtil.getSphere(distance.getValueFloat(), mc.player.getPos())) {
				Type type = isHole(pos);
				if (type == Type.Bedrock) {
					tempBedrockList.add(pos);
				} else if (type == Type.Normal) {
					tempNormalList.add(pos);
				} else if (type == Type.Air) {
					tempAirList.add(pos);
				}
			}
		}
	}

	public enum Type {
		None,
		Air,
		Normal,
		Bedrock
	}
	Type isHole(BlockPos pos) {
		if (mc.world.isAir(pos) && (!airYCheck.getValue() || pos.getY() == mc.player.getBlockY() - 1) && Alien.HOLE.isHard(pos.up())) return Type.Air;
		int blockProgress = 0;
		boolean bedRock = true;
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if (Alien.HOLE.isHard(pos.offset(i))) {
				if (mc.world.getBlockState(pos.offset(i)).getBlock() != Blocks.BEDROCK) {
					bedRock = false;
				}
				blockProgress++;
			}
		}
		if ((mc.world.isAir(pos) && mc.world.isAir(pos.up()) && mc.world.isAir(pos.up(2))) && blockProgress > 3 && mc.world.canCollide(mc.player, new Box(pos.down()))) {
			if (bedRock) return Type.Bedrock;
			return Type.Normal;
		}
		if (Alien.HOLE.isDoubleHole(pos)) return Type.Normal;
		return Type.None;
	}

	boolean drawing = false;

	@Override
	public void onRender3D(MatrixStack matrixStack) {
		drawing = true;
		draw(matrixStack, bedrockList, bedrockFill, bedrockFade, bedrockBox, height.getValue());
		draw(matrixStack, airList, airFill, airFade, airBox, airHeight.getValue());
		draw(matrixStack, normalList, normalFill, normalFade, normalBox, height.getValue());
		drawing = false;
	}

	private void draw(MatrixStack matrixStack, List<BlockPos> list, ColorSetting fill, ColorSetting fade, ColorSetting box, double height) {
		for (BlockPos pos : list) {
			double distance = mc.player.getPos().distanceTo(pos.toCenterPos());
			double alpha = distance > startFade.getValue() ? Math.max(Math.min(1, (1 - ((distance - startFade.getValue()) / (this.distance.getValue() - startFade.getValue())))), 0) : 1;
			Box espBox = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + height, pos.getZ() + 1);
			if (fill.booleanValue) {
				if (fade.booleanValue) {
					Render3DUtil.drawFadeFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * alpha)), ColorUtil.injectAlpha(fade.getValue(), (int) (fade.getValue().getAlpha() * alpha)));
				} else {
					Render3DUtil.drawFill(matrixStack, espBox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * alpha)));
				}
			}
			if (box.booleanValue) {
				Render3DUtil.drawBox(matrixStack, espBox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * alpha)));
			}
		}
	}
}
