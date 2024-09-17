package dev.luminous.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.RotateEvent;
import dev.luminous.api.events.impl.UpdateWalkingPlayerEvent;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.ExplosionUtil;
import dev.luminous.api.utils.math.FadeUtils;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.Alien;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.settings.SwingSide;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BedItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;


public class BedAura extends Module {
	public static BedAura INSTANCE;
	public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
	//General
	private final BooleanSetting legacy = add(new BooleanSetting("Legacy", false, () -> page.getValue() == Page.General));
	private final BooleanSetting spam = add(new BooleanSetting("Spam", true, () -> page.getValue() == Page.General));
	private final BooleanSetting onlyGround = add(new BooleanSetting("OnlyGround", true, () -> page.getValue() == Page.General));
	private final BooleanSetting yawDeceive = add(new BooleanSetting("YawDeceive", true, () -> page.getValue() == Page.General));
	private final BooleanSetting checkMine = add(new BooleanSetting("DetectMining", true, () -> page.getValue() == Page.General));
	private final BooleanSetting noUsing =
			add(new BooleanSetting("EatingPause", true, () -> page.getValue() == Page.General));
	private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.All, () -> page.getValue() == Page.General));
	private final SliderSetting antiSuicide =
			add(new SliderSetting("AntiSuicide", 3.0, 0.0, 10.0, () -> page.getValue() == Page.General));
	private final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 12.0, 0.0, 20.0, () -> page.getValue() == Page.General));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 50, 0, 1000, () -> page.getValue() == Page.General));
	private final SliderSetting calcDelay =
			add(new SliderSetting("CalcDelay", 200, 0, 1000, () -> page.getValue() == Page.General));
	private final BooleanSetting inventorySwap =
			add(new BooleanSetting("InventorySwap", true, () -> page.getValue() == Page.General));
	//Rotate
	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting yawStep =
			add(new BooleanSetting("YawStep", false, () -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting steps =
			add(new SliderSetting("Steps", 0.3f, 0.1f, 1.0f, 0.01f, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkFov =
			add(new BooleanSetting("OnlyLooking", true, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 5f, 0f, 30f, () -> rotate.isOpen() && yawStep.getValue() && checkFov.getValue() && page.getValue() == Page.Rotate));

	//Calc
	private final BooleanSetting place =
			add(new BooleanSetting("Place", true, () -> page.getValue() == Page.Calc));
	private final SliderSetting placeDelay =
			add(new SliderSetting("PlaceDelay", 300, 0, 1000, () -> page.getValue() == Page.Calc && place.getValue()));
	private final BooleanSetting Break =
			add(new BooleanSetting("Break", true, () -> page.getValue() == Page.Calc));
	private final SliderSetting breakDelay =
			add(new SliderSetting("BreakDelay", 300, 0, 1000, () -> page.getValue() == Page.Calc && Break.getValue()));
	private final SliderSetting range =
			add(new SliderSetting("Range", 5.0, 0.0, 6, () -> page.getValue() == Page.Calc));
	private final SliderSetting placeMinDamage =
			add(new SliderSetting("MinDamage", 5.0, 0.0, 36.0, () -> page.getValue() == Page.Calc));
	private final SliderSetting placeMaxSelf =
			add(new SliderSetting("MaxSelfDamage", 12.0, 0.0, 36.0, () -> page.getValue() == Page.Calc));
	private final BooleanSetting smart =
			add(new BooleanSetting("Smart", true, () -> page.getValue() == Page.Calc));
	private final BooleanSetting breakOnlyHasCrystal =
			add(new BooleanSetting("OnlyHasBed", false, () -> page.getValue() == Page.Calc && Break.getValue()));
	//Render
	private final BooleanSetting render =
			add(new BooleanSetting("Render", true, () -> page.getValue() == Page.Render));
	private final BooleanSetting shrink =
			add(new BooleanSetting("Shrink", true, () -> page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting outline =
			add(new BooleanSetting("Outline", true, () -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting outlineAlpha =
			add(new SliderSetting("OutlineAlpha", 150, 0, 255, () -> outline.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting box =
			add(new BooleanSetting("Box", true, () -> page.getValue() == Page.Render && render.getValue()).setParent());
	private final SliderSetting boxAlpha =
			add(new SliderSetting("BoxAlpha", 70, 0, 255, () -> box.isOpen() && page.getValue() == Page.Render && render.getValue()));
	private final BooleanSetting reset =
			add(new BooleanSetting("Reset", true, () -> page.getValue() == Page.Render && render.getValue()));
	private final ColorSetting color =
			add(new ColorSetting("Color", new Color(255, 255, 255), () -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting animationTime =
			add(new SliderSetting("AnimationTime", 2f, 0f, 8f, () -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting startFadeTime =
			add(new SliderSetting("StartFadeTime", 0.3d, 0d, 2d, 0.01, () -> page.getValue() == Page.Render && render.getValue()));
	private final SliderSetting fadeTime =
			add(new SliderSetting("FadeTime", 0.3d, 0d, 2d, 0.01, () -> page.getValue() == Page.Render && render.getValue()));
	//Predict
	private final SliderSetting predictTicks =
			add(new SliderSetting("PredictTicks", 4, 0, 10, () -> page.getValue() == Page.Predict));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", true, () -> page.getValue() == Page.Predict));
	public BedAura() {
		super("BedAura", Category.Combat);
		setChinese("床光环");
		INSTANCE = this;
	}
	public static BlockPos placePos;
	private final Timer delayTimer = new Timer();
	private final Timer calcTimer = new Timer();
	private final Timer breakTimer = new Timer();
	private final Timer placeTimer = new Timer();
	private final Timer noPosTimer = new Timer();
	private final FadeUtils fadeUtils = new FadeUtils(500);
	private final FadeUtils animation = new FadeUtils(500);
	double lastSize = 0;
	private PlayerEntity displayTarget;
	private float lastYaw = 0f;
	private float lastPitch = 0f;
	public float lastDamage;
	public Vec3d directionVec = null;
	private BlockPos renderPos = null;
	private Box lastBB = null;
	private Box nowBB = null;

	@Override
	public String getInfo() {
		if (displayTarget != null && placePos != null) {
			return displayTarget.getName().getString();
		}
		return super.getInfo();
	}

	@Override
	public void onEnable() {
		lastYaw = Alien.ROTATION.lastYaw;
		lastPitch = Alien.ROTATION.lastPitch;
	}

	@EventHandler()
	public void onRotate(RotateEvent event) {
		if (!rotate.getValue() && yawDeceive.getValue()) {
			event.setYaw(yaw);
		} else if (placePos != null && yawStep.getValue() && directionVec != null) {
			float[] newAngle = injectStep(Alien.ROTATION.getRotation(directionVec), steps.getValueFloat());
			lastYaw = newAngle[0];
			lastPitch = newAngle[1];
			event.setYaw(lastYaw);
			event.setPitch(lastPitch);
		} else {
			lastYaw = Alien.ROTATION.lastYaw;
			lastPitch = Alien.ROTATION.lastPitch;
		}
	}

	@EventHandler
	public void onUpdateWalking(UpdateWalkingPlayerEvent event) {
		update();
	}

	@Override
	public void onUpdate() {
		update();
	}

	private void update() {
		if (nullCheck()) return;
		animUpdate();
		if (!delayTimer.passedMs((long) updateDelay.getValue())) return;
		if (noUsing.getValue() && mc.player.isUsingItem()) {
			placePos = null;
			return;
		}
		if (onlyGround.getValue() && !mc.player.isOnGround()) {
			placePos = null;
			return;
		}
		if (mc.player.isSneaking()) {
			placePos = null;
			return;
		}
		if (mc.world.getRegistryKey().equals(World.OVERWORLD)) {
			placePos = null;
			return;
		}
		if (breakOnlyHasCrystal.getValue() && getBed() == -1) {
			placePos = null;
			return;
		}
		delayTimer.reset();
		if (calcTimer.passedMs(calcDelay.getValueInt())) {
			calcTimer.reset();
			placePos = null;
			lastDamage = 0f;
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity target : CombatUtil.getEnemies(targetRange.getValueFloat())) {
				list.add(new PlayerAndPredict(target));
			}
			PlayerAndPredict self = new PlayerAndPredict(mc.player);
			for (BlockPos pos : BlockUtil.getSphere((float) range.getValue())) {
				if (!canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) continue;
				for (PlayerAndPredict pap : list) {
					float damage = calculateDamage(pos, pap.player, pap.predict);
					float selfDamage = calculateDamage(pos, self.player, self.predict);
					if (selfDamage > placeMaxSelf.getValue())
						continue;
					if (antiSuicide.getValue() > 0 && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount() - antiSuicide.getValue())
						continue;
					if (damage < EntityUtil.getHealth(pap.player)) {
						if (damage < placeMinDamage.getValueFloat()) continue;
						if (smart.getValue()) {
							if (damage < selfDamage) {
								continue;
							}
						}
					}
					if (placePos == null || damage > lastDamage) {
						displayTarget = pap.player;
						placePos = pos;
						lastDamage = damage;
					}
				}
			}
		}
		if (placePos != null) {
			doBed(placePos);
		}
	}

	public void doBed(BlockPos pos) {
		if (canPlaceBed(pos) && !(BlockUtil.getBlock(pos) instanceof BedBlock)) {
			if (getBed() != -1) {
				doPlace(pos);
				if (spam.getValue()) {
					Direction side = BlockUtil.getClickSide(pos);
					Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
					if (rotate.getValue()) {
						if (!faceVector(directionVec)) return;
					}
					if (!breakTimer.passedMs((long) breakDelay.getValue())) return;
					breakTimer.reset();
					EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
					BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
					Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
				}
			}
		} else {
			doBreak(pos);
		}
	}

	private void doBreak(BlockPos pos) {
		if (!Break.getValue()) return;
		if (mc.world.getBlockState(pos).getBlock() instanceof BedBlock) {
			Direction side = BlockUtil.getClickSide(pos);
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!breakTimer.passedMs((long) breakDelay.getValue())) return;
			breakTimer.reset();
			EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
			BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
			Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
		}
	}

	private float yaw = 0;
	private void doPlace(BlockPos pos) {
		if (!place.getValue()) return;
		int bedSlot;
		if ((bedSlot = getBed()) == -1) {
			placePos = null;
			return;
		}

		int oldSlot = mc.player.getInventory().selectedSlot;
		Direction facing = null;
		for (Direction i : Direction.values()) {
			if (i == Direction.UP || i == Direction.DOWN) continue;
			if ((legacy.getValue() && BlockUtil.canReplace(pos.offset(i)) || BlockUtil.clientCanPlace(pos.offset(i), false)) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !Alien.BREAK.isMining(pos.offset(i)))) {
				facing = i;
				break;
			}
		}
		if (facing != null) {
			Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + Direction.UP.getVector().getX() * 0.5, pos.getY() + 0.5 + Direction.UP.getVector().getY() * 0.5, pos.getZ() + 0.5 + Direction.UP.getVector().getZ() * 0.5);
			if (rotate.getValue()) {
				if (!faceVector(directionVec)) return;
			}
			if (!placeTimer.passedMs((long) placeDelay.getValue())) return;
			placeTimer.reset();
			doSwap(bedSlot);
			if (yawDeceive.getValue() && !isTrueFacingNow(facing.getOpposite())) {
				AutoPush.pistonFacing(facing.getOpposite());
			}
			yaw = getYaw(facing.getOpposite());
			BlockUtil.clickBlock(pos.offset(facing).down(), Direction.UP, false);
			if (inventorySwap.getValue()) {
				doSwap(bedSlot);
				EntityUtil.syncInventory();
			} else {
				doSwap(oldSlot);
			}
		}
	}
	public static float getYaw(Direction i) {
		if (i == Direction.EAST) {
			return (-90.0f);
		} else if (i == Direction.WEST) {
			return (90.0f);
		} else if (i == Direction.NORTH) {
			return (180.0f);
		} else if (i == Direction.SOUTH) {
			return (0.0f);
		}
		return 0f;
	}

	
	@Override
	public void onRender3D(MatrixStack matrixStack) {
		update();
		double quad = noPosTimer.passedMs(startFadeTime.getValue() * 1000L) ? fadeUtils.easeOutQuad() : 0;
		if (nowBB != null && render.getValue() && quad < 1) {
			Box bb = nowBB;
			if (shrink.getValue()) {
				bb = nowBB.shrink(quad * 0.5, quad * 0.5, quad * 0.5);
				bb = bb.shrink(-quad * 0.5, -quad * 0.5, -quad * 0.5);
			}
			if (this.box.getValue())
				Render3DUtil.drawFill(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (boxAlpha.getValue() * Math.abs(quad - 1))));
			if (outline.getValue())
				Render3DUtil.drawBox(matrixStack, bb, ColorUtil.injectAlpha(color.getValue(), (int) (outlineAlpha.getValue() * Math.abs(quad - 1))));
		} else if (reset.getValue()) nowBB = null;
	}
	private void animUpdate() {
		fadeUtils.setLength((long) (fadeTime.getValue() * 1000));
		if (placePos != null) {
			lastBB = new Box(placePos);
			noPosTimer.reset();
			if (nowBB == null) {
				nowBB = lastBB;
			}
			if (renderPos == null || !renderPos.equals(placePos)) {
				animation.setLength((animationTime.getValue() * 1000) <= 0 ? 0 :
						(long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) <= 5 ?
								(long) ((Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ)) * (animationTime.getValue() * 1000))
								: (animationTime.getValue() * 5000L))
				);
				animation.reset();
				renderPos = placePos;
			}
		}
		if (!noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000))) {
			fadeUtils.reset();
		}
		double size = animation.easeOutQuad();
		if (nowBB != null && lastBB != null) {
			if (Math.abs(nowBB.minX - lastBB.minX) + Math.abs(nowBB.minY - lastBB.minY) + Math.abs(nowBB.minZ - lastBB.minZ) > 16) {
				nowBB = lastBB;
			}
			if (lastSize != size) {
				nowBB = new Box(nowBB.minX + (lastBB.minX - nowBB.minX) * size,
						nowBB.minY + (lastBB.minY - nowBB.minY) * size,
						nowBB.minZ + (lastBB.minZ - nowBB.minZ) * size,
						nowBB.maxX + (lastBB.maxX - nowBB.maxX) * size,
						nowBB.maxY + (lastBB.maxY - nowBB.maxY) * size,
						nowBB.maxZ + (lastBB.maxZ - nowBB.maxZ) * size
				);
				lastSize = size;
			}
		}
	}
	public int getBed() {
		return inventorySwap.getValue() ? InventoryUtil.findClassInventorySlot(BedItem.class) : InventoryUtil.findClass(BedItem.class);
	}

	private void doSwap(int slot) {
		if (inventorySwap.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
			//mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}

	public float calculateDamage(BlockPos pos, PlayerEntity player, PlayerEntity predict) {
		CombatUtil.modifyPos = pos;
		CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
		float damage = calculateDamage(pos.toCenterPos(), player, predict);
		CombatUtil.modifyPos = null;
		return damage;
	}

	public float calculateDamage(Vec3d pos, PlayerEntity player, PlayerEntity predict) {
		if (terrainIgnore.getValue()) {
			CombatUtil.terrainIgnore = true;
		}
		float damage = ExplosionUtil.calculateDamage(pos.getX(), pos.getY(), pos.getZ(), player, predict, 6);
		CombatUtil.terrainIgnore = false;
		return damage;
	}
	
	private boolean canPlaceBed(BlockPos pos) {
		if (BlockUtil.canReplace(pos) && (!checkMine.getValue() || !Alien.BREAK.isMining(pos)) && (!legacy.getValue() || BlockUtil.canClick(pos.down()))) {
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (!BlockUtil.isStrictDirection(pos.offset(i).down(), Direction.UP)) continue;
				if (!isTrueFacing(pos.offset(i), i.getOpposite())) continue;
				if ((legacy.getValue() && BlockUtil.canReplace(pos.offset(i)) || BlockUtil.clientCanPlace(pos.offset(i), false)) && BlockUtil.canClick(pos.offset(i).down()) && (!checkMine.getValue() || !Alien.BREAK.isMining(pos.offset(i)))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isTrueFacing(BlockPos pos, Direction facing) {
		if (yawDeceive.getValue()) return true;
		Vec3d hitVec = pos.toCenterPos().add(new Vec3d(0, -0.5, 0));
		return Direction.fromRotation(Alien.ROTATION.getRotation(hitVec)[0]) == facing;
	}

	private boolean isTrueFacingNow(Direction facing) {
		return Direction.fromRotation(Alien.ROTATION.lastYaw) == facing;
	}
	public enum Page {
		General,
		Rotate,
		Calc,
		Predict,
		Render
	}

	public boolean faceVector(Vec3d directionVec) {
		if (!yawStep.getValue()) {
			Alien.ROTATION.lookAt(directionVec);
			return true;
		} else {
			this.directionVec = directionVec;
			if (Alien.ROTATION.inFov(directionVec, fov.getValueFloat())) return true;
		}
		return !checkFov.getValue();
	}

	private float[] injectStep(float[] angle, float steps) {
		if (steps < 0.01f) steps = 0.01f;

		if (steps > 1) steps = 1;

		if (steps < 1 && angle != null) {
			float packetYaw = lastYaw;
			float diff = MathHelper.wrapDegrees(angle[0] - lastYaw);

			if (Math.abs(diff) > 90 * steps) {
				angle[0] = (packetYaw + (diff * ((90 * steps) / Math.abs(diff))));
			}

			float packetPitch = lastPitch;
			diff = angle[1] - packetPitch;
			if (Math.abs(diff) > 90 * steps) {
				angle[1] = (packetPitch + (diff * ((90 * steps) / Math.abs(diff))));
			}
		}

		return new float[]{
				angle[0],
				angle[1]
		};
	}
	public class PlayerAndPredict {
		final PlayerEntity player;
		final PlayerEntity predict;
		public PlayerAndPredict(PlayerEntity player) {
			this.player = player;
			if (predictTicks.getValueFloat() > 0) {
				predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
				predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
				predict.setHealth(player.getHealth());
				predict.prevX = player.prevX;
				predict.prevZ = player.prevZ;
				predict.prevY = player.prevY;
				predict.setOnGround(player.isOnGround());
				predict.getInventory().clone(player.getInventory());
				predict.setPose(player.getPose());
				for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
					predict.addStatusEffect(se);
				}
			} else {
				predict = player;
			}
		}
	}
}
