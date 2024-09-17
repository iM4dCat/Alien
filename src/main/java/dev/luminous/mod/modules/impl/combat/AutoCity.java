package dev.luminous.mod.modules.impl.combat;

import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.world.BlockPosX;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.player.PacketMine;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.CobwebBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static dev.luminous.api.utils.world.BlockUtil.getBlock;

public class AutoCity extends Module {
	public static AutoCity INSTANCE;
	private final BooleanSetting burrow = add(new BooleanSetting("Burrow", true));
	private final BooleanSetting face = add(new BooleanSetting("Face", true));
	private final BooleanSetting down = add(new BooleanSetting("Down", false));
	private final BooleanSetting surround = add(new BooleanSetting("Surround", true));
	private final BooleanSetting lowVersion = add(new BooleanSetting("1.12", false));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
	public final SliderSetting range =
			add(new SliderSetting("Range", 6.0, 0.0, 8.0, 0.1).setSuffix("m"));
	public AutoCity() {
		super("AutoCity", Category.Combat);
		setChinese("自动挖掘");
		INSTANCE = this;
	}

	@Override
	public void onUpdate() {
		if (AntiCrawl.INSTANCE.work) return;
		PlayerEntity player = CombatUtil.getClosestEnemy(targetRange.getValue());
		if (player == null) return;
		doBreak(player);
	}

	private void doBreak(PlayerEntity player) {
		BlockPos pos = EntityUtil.getEntityPos(player, true);
		{
			double[] yOffset = new double[]{-0.8, 0.5, 1.1};
			double[] xzOffset = new double[]{0.3, -0.3};
			for (PlayerEntity entity : CombatUtil.getEnemies(targetRange.getValue())) {
				for (double y : yOffset) {
					for (double x : xzOffset) {
						for (double z : xzOffset) {
							BlockPos offsetPos = new BlockPosX(entity.getX() + x, entity.getY() + y, entity.getZ() + z);
							if (canBreak(offsetPos) && offsetPos.equals(PacketMine.getBreakPos())) {
								return;
							}
						}
					}
				}
			}
			List<Float> yList = new ArrayList<>();
			if (down.getValue()) {
				yList.add(-0.8f);
			}
			if (burrow.getValue()) {
				yList.add(0.5f);
			}
			if (face.getValue()) {
				yList.add(1.1f);
			}
			for (double y : yList) {
				for (double offset : xzOffset) {
					BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
					if (canBreak(offsetPos)) {
						PacketMine.INSTANCE.mine(offsetPos);
						return;
					}
				}
			}
			for (double y : yList) {
				for (double offset : xzOffset) {
					for (double offset2 : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
						if (canBreak(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
			}
		}
		if (surround.getValue()) {
			if (!lowVersion.getValue()) {
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if ((mc.world.isAir(pos.offset(i)) || pos.offset(i).equals(PacketMine.getBreakPos())) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}
				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), true)) {
						list.add(pos.offset(i));
					}
				}
				if (!list.isEmpty()) {
					//System.out.println("found");
					PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
				} else {
					for (Direction i : Direction.values()) {
						if (i == Direction.UP || i == Direction.DOWN) continue;
						if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
							continue;
						}
						if (canBreak(pos.offset(i)) && canPlaceCrystal(pos.offset(i), false)) {
							list.add(pos.offset(i));
						}
					}
					if (!list.isEmpty()) {
						//System.out.println("found");
						PacketMine.INSTANCE.mine(list.stream().min(Comparator.comparingDouble((E) -> E.getSquaredDistance(mc.player.getEyePos()))).get());
					}
				}

			} else {

				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (mc.player.getEyePos().distanceTo(pos.offset(i).toCenterPos()) > range.getValue()) {
						continue;
					}
					if ((mc.world.isAir(pos.offset(i)) && mc.world.isAir(pos.offset(i).up())) && canPlaceCrystal(pos.offset(i), false)) {
						return;
					}
				}

				ArrayList<BlockPos> list = new ArrayList<>();
				for (Direction i : Direction.values()) {
					if (i == Direction.UP || i == Direction.DOWN) continue;
					if (Math.sqrt(mc.player.getEyePos().squaredDistanceTo(pos.offset(i).toCenterPos())) > range.getValue()) {
						continue;
					}
					if (canCrystal(pos.offset(i))) {
						list.add(pos.offset(i));
					}
				}

				int max = 0;
				BlockPos minePos = null;
				for (BlockPos cPos : list) {
					if (getAir(cPos) >= max) {
						max = getAir(cPos);
						minePos = cPos;
					}
				}
				if (minePos != null) {
					doMine(minePos);
				}
			}
		}
		if (PacketMine.getBreakPos() == null) {
			if (burrow.getValue()) {
				double[] yOffset;
				double[] xzOffset = new double[]{0, 0.3, -0.3};

				yOffset = new double[]{0.5, 1.1};
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						BlockPos offsetPos = new BlockPosX(player.getX() + offset, player.getY() + y, player.getZ() + offset);
						if (isObsidian(offsetPos)) {
							PacketMine.INSTANCE.mine(offsetPos);
							return;
						}
					}
				}
				for (double y : yOffset) {
					for (double offset : xzOffset) {
						for (double offset2 : xzOffset) {
							BlockPos offsetPos = new BlockPosX(player.getX() + offset2, player.getY() + y, player.getZ() + offset);
							if (isObsidian(offsetPos)) {
								PacketMine.INSTANCE.mine(offsetPos);
								return;
							}
						}
					}
				}
			}
		}
	}

	private void doMine(BlockPos pos) {
		if (canBreak(pos)) {
			PacketMine.INSTANCE.mine(pos);
		} else if (canBreak(pos.up())) {
			PacketMine.INSTANCE.mine(pos.up());
		}
	}
	private boolean canCrystal(BlockPos pos) {
		if (PacketMine.godBlocks.contains(getBlock(pos)) || getBlock(pos) instanceof BedBlock || getBlock(pos) instanceof CobwebBlock || !canPlaceCrystal(pos, true) || BlockUtil.getClickSideStrict(pos) == null) {
			return false;
		}
		if (PacketMine.godBlocks.contains(getBlock(pos.up())) || getBlock(pos.up()) instanceof BedBlock || getBlock(pos.up()) instanceof CobwebBlock || BlockUtil.getClickSideStrict(pos.up()) == null) {
			return false;
		}
		return true;
	}
	private int getAir(BlockPos pos) {
		int value = 0;
		if (!canBreak(pos)) {
			value++;
		}
		if (!canBreak(pos.up())) {
			value++;
		}

		return value;
	}
	public boolean canPlaceCrystal(BlockPos pos, boolean block) {
		BlockPos obsPos = pos.down();
		BlockPos boost = obsPos.up();
		return (getBlock(obsPos) == Blocks.BEDROCK || getBlock(obsPos) == Blocks.OBSIDIAN || !block)
				&& !BlockUtil.hasEntityBlockCrystal(boost, true, true)
				&& !BlockUtil.hasEntityBlockCrystal(boost.up(), true, true)
				&& (!lowVersion.getValue() || mc.world.isAir(boost.up()));
	}

	private boolean isObsidian(BlockPos pos) {
		return mc.player.getEyePos().distanceTo(pos.toCenterPos()) <= range.getValue() && (getBlock(pos) == Blocks.OBSIDIAN || getBlock(pos) == Blocks.ENDER_CHEST || getBlock(pos) == Blocks.NETHERITE_BLOCK || getBlock(pos) == Blocks.RESPAWN_ANCHOR) && BlockUtil.getClickSideStrict(pos) != null;
	}
	private boolean canBreak(BlockPos pos) {
		return isObsidian(pos) && (BlockUtil.getClickSideStrict(pos) != null || PacketMine.getBreakPos().equals(pos)) && (!pos.equals(PacketMine.secondPos) || !(mc.player.getMainHandStack().getItem() instanceof PickaxeItem));
	}
}