package dev.luminous.mod.modules.impl.combat;

import com.mojang.authlib.GameProfile;
import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.LookAtEvent;
import dev.luminous.api.events.impl.Render3DEvent;
import dev.luminous.api.utils.combat.CombatUtil;
import dev.luminous.api.utils.entity.EntityUtil;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.math.AnimateUtil;
import dev.luminous.api.utils.math.ExplosionUtil;
import dev.luminous.api.utils.math.Timer;
import dev.luminous.api.utils.render.ColorUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.api.utils.world.BlockUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.exploit.Blink;
import dev.luminous.mod.modules.settings.SwingSide;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.ColorSetting;
import dev.luminous.mod.modules.settings.impl.EnumSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.kryptondevelopment.annotations.DontInvokeDynamic;

import java.awt.*;
import java.util.ArrayList;
import java.util.UUID;

import static dev.luminous.api.utils.world.BlockUtil.*;

@DontInvokeDynamic
public class AutoAnchor extends Module {
	public static AutoAnchor INSTANCE;
	public final EnumSetting<Page> page = add(new EnumSetting<>("Page", Page.General));
	//Assist
	private final BooleanSetting assist =
			add(new BooleanSetting("Assist", true, () -> page.getValue() == Page.Assist));
	private final BooleanSetting checkMine =
			add(new BooleanSetting("DetectMining", false, () -> page.getValue() == Page.Assist));
	private final SliderSetting assistRange =
			add(new SliderSetting("AssistRange", 5.0, 0.0, 6.0, 0.1, () -> page.getValue() == Page.Assist).setSuffix("m"));
	private final SliderSetting assistDamage =
			add(new SliderSetting("AssistDamage", 6.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Assist).setSuffix("h"));
	private final SliderSetting delay =
			add(new SliderSetting("AssistDelay", 0.1, 0.0, 1, 0.01, () -> page.getValue() == Page.Assist).setSuffix("s"));

	//
	private final BooleanSetting thread =
			add(new BooleanSetting("Thread", false, () -> page.getValue() == Page.General));
	private final BooleanSetting light =
			add(new BooleanSetting("LessCPU", true, () -> page.getValue() == Page.General));
	public final SliderSetting range =
			add(new SliderSetting("Range", 5.0, 0.0, 6.0, 0.1, () -> page.getValue() == Page.General).setSuffix("m"));
	public final SliderSetting targetRange =
			add(new SliderSetting("TargetRange", 8.0, 0.10, 12, 0.1, () -> page.getValue() == Page.General).setSuffix("m"));
	private final BooleanSetting inventorySwap =
			add(new BooleanSetting("InventorySwap", true, () -> page.getValue() == Page.General));
	private final BooleanSetting breakCrystal =
			add(new BooleanSetting("BreakCrystal", true, () -> page.getValue() == Page.General));
	private final BooleanSetting spam =
			add(new BooleanSetting("Spam", true, () -> page.getValue() == Page.General).setParent());
	private final BooleanSetting mineSpam =
			add(new BooleanSetting("OnlyMining", true, () -> page.getValue() == Page.General && spam.isOpen()));
	private final BooleanSetting spamPlace =
			add(new BooleanSetting("Fast", true, () -> page.getValue() == Page.General).setParent());
	private final BooleanSetting inSpam =
			add(new BooleanSetting("WhenSpamming", true, () -> page.getValue() == Page.General && spamPlace.isOpen()));
	private final BooleanSetting usingPause =
			add(new BooleanSetting("UsingPause", true, () -> page.getValue() == Page.General));
	private final EnumSetting<SwingSide> swingMode = add(new EnumSetting<>("Swing", SwingSide.All, () -> page.getValue() == Page.General));
	private final SliderSetting placeDelay =
			add(new SliderSetting("Delay", 100, 0, 500, 1, () -> page.getValue() == Page.General).setSuffix("ms"));
	private final SliderSetting spamDelay =
			add(new SliderSetting("SpamDelay", 200, 0, 1000, 1, () -> page.getValue() == Page.General).setSuffix("ms"));
	private final SliderSetting updateDelay =
			add(new SliderSetting("UpdateDelay", 200, 0, 1000, 1, () -> page.getValue() == Page.General).setSuffix("ms"));

	private final BooleanSetting rotate =
			add(new BooleanSetting("Rotate", true, () -> page.getValue() == Page.Rotate).setParent());
	private final BooleanSetting yawStep =
			add(new BooleanSetting("YawStep", true, () -> rotate.isOpen() && page.getValue() == Page.Rotate));
	private final SliderSetting steps =
			add(new SliderSetting("Steps", 0.05, 0, 1, 0.01, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final BooleanSetting checkFov =
			add(new BooleanSetting("OnlyLooking", true, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting fov =
			add(new SliderSetting("Fov", 30, 0, 50, () -> rotate.isOpen() && yawStep.getValue() && checkFov.getValue() && page.getValue() == Page.Rotate));
	private final SliderSetting priority =
			add(new SliderSetting("Priority", 10, 0, 100, () -> rotate.isOpen() && yawStep.getValue() && page.getValue() == Page.Rotate));


	private final BooleanSetting noSuicide =
			add(new BooleanSetting("NoSuicide", true, () -> page.getValue() == Page.Calc));
	private final BooleanSetting terrainIgnore =
			add(new BooleanSetting("TerrainIgnore", true, () -> page.getValue() == Page.Calc));
	public final SliderSetting minDamage =
			add(new SliderSetting("Min", 4.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting breakMin =
			add(new SliderSetting("ExplosionMin", 4.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting headDamage =
			add(new SliderSetting("ForceHead", 7.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Calc).setSuffix("dmg"));
	private final SliderSetting minPrefer =
			add(new SliderSetting("Prefer", 7.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Calc).setSuffix("dmg"));
	private final SliderSetting maxSelfDamage =
			add(new SliderSetting("MaxSelf", 8.0, 0.0, 36.0, 0.1, () -> page.getValue() == Page.Calc).setSuffix("dmg"));
	public final SliderSetting predictTicks =
			add(new SliderSetting("Predict", 2, 0.0, 50, 1, () -> page.getValue() == Page.Calc).setSuffix("ticks"));

	private final EnumSetting<KillAura.TargetESP> mode = add(new EnumSetting<>("TargetESP", KillAura.TargetESP.Jello, () -> page.getValue() == Page.Render));
	private final ColorSetting color = add(new ColorSetting("TargetColor", new Color(255, 255, 255, 250), () -> page.getValue() == Page.Render));
	final BooleanSetting render =
			add(new BooleanSetting("Render", true, () -> page.getValue() == Page.Render));
	final BooleanSetting shrink =
			add(new BooleanSetting("Shrink", true, () -> page.getValue() == Page.Render && render.getValue()));
	final ColorSetting box =
			add(new ColorSetting("Box", new Color(255, 255, 255, 255), () -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
	final SliderSetting lineWidth =
			add(new SliderSetting("LineWidth", 1.5d, 0.01d, 3d, 0.01, () -> page.getValue() == Page.Render && render.getValue()));
	final ColorSetting fill =
			add(new ColorSetting("Fill", new Color(255, 255, 255, 100), () -> page.getValue() == Page.Render && render.getValue()).injectBoolean(true));
	final SliderSetting sliderSpeed = add(new SliderSetting("SliderSpeed", 0.2, 0d, 1, 0.01, () -> page.getValue() == Page.Render && render.getValue()));
	final SliderSetting startFadeTime =
			add(new SliderSetting("StartFade", 0.3d, 0d, 2d, 0.01, () -> page.getValue() == Page.Render && render.getValue()).setSuffix("s"));
	final SliderSetting fadeSpeed =
			add(new SliderSetting("FadeSpeed", 0.2d, 0.01d, 1d, 0.01, () -> page.getValue() == Page.Render && render.getValue()));

	private final Timer delayTimer = new Timer();
	private final Timer calcTimer = new Timer();
	public Vec3d directionVec = null;

	public AutoAnchor() {
		super("AutoAnchor", Category.Combat);
		setChinese("自动重生锚");
		INSTANCE = this;
		Alien.EVENT_BUS.subscribe(new AnchorRender());
	}
	public PlayerEntity displayTarget;
	@Override
	public String getInfo() {
		if (displayTarget != null && currentPos != null) return displayTarget.getName().getString();
		return null;
	}
	private final ArrayList<BlockPos> chargeList = new ArrayList<>();
	public BlockPos currentPos;
	public BlockPos tempPos;
	public double lastDamage;
	final Timer noPosTimer = new Timer();
	static Vec3d placeVec3d;
	static Vec3d curVec3d;
	double fade = 0;
	@Override
	public void onRender3D(MatrixStack matrixStack) {
		if (displayTarget != null && currentPos != null) {
			KillAura.doRender(matrixStack, mc.getTickDelta(), displayTarget, color.getValue(), mode.getValue());
		}
	}

	@EventHandler
	public void onRotate(LookAtEvent event) {
		if (currentPos != null&& rotate.getValue() && yawStep.getValue() && directionVec != null) {
			event.setTarget(directionVec, steps.getValueFloat(), priority.getValueFloat());
		}
	}

	@Override
	public void onDisable() {
		currentPos = null;
	}

	@Override
	public void onThread() {
		if (thread.getValue()) {
			calc();
		}
	}
	@Override
	public void onUpdate() {
		if (assist.getValue()) onAssist();

		int anchor = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
		int unBlock = inventorySwap.getValue() ? anchor : InventoryUtil.findUnBlock();
		int old = mc.player.getInventory().selectedSlot;
		if (!thread.getValue()) calc();
		if (anchor == -1) {
			return;
		}
		if (glowstone == -1) {
			return;
		}
		if (unBlock == -1) {
			return;
		}
		if (mc.player.isSneaking()) {
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			return;
		}
		if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
		if (currentPos != null) {
			if (breakCrystal.getValue()) CombatUtil.attackCrystal(new BlockPos(currentPos), rotate.getValue(), false);
			boolean shouldSpam = this.spam.getValue() && (!mineSpam.getValue() || Alien.BREAK.isMining(currentPos, false));
			if (shouldSpam) {
				if (!delayTimer.passed((long) (spamDelay.getValueFloat()))) {
					return;
				}
				delayTimer.reset();
				if (canPlace(currentPos, range.getValue(), breakCrystal.getValue())) {
					placeBlock(currentPos, rotate.getValue(), anchor);
				}
				if (!chargeList.contains(currentPos)) {
					delayTimer.reset();
					clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), glowstone);
					chargeList.add(currentPos);
				}
				chargeList.remove(currentPos);
				clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), unBlock);
				if (spamPlace.getValue() && inSpam.getValue()) {
					if (yawStep.getValue() && checkFov.getValue()) {
						Direction side = getClickSide(currentPos);
						Vec3d directionVec = new Vec3d(currentPos.getX() + 0.5 + side.getVector().getX() * 0.5, currentPos.getY() + 0.5 + side.getVector().getY() * 0.5, currentPos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
						if (Alien.ROTATION.inFov(directionVec, fov.getValueFloat())) {
							CombatUtil.modifyPos = currentPos;
							CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
							placeBlock(currentPos, rotate.getValue(), anchor);
							CombatUtil.modifyPos = null;
						}
					} else {
						CombatUtil.modifyPos = currentPos;
						CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
						placeBlock(currentPos, rotate.getValue(), anchor);
						CombatUtil.modifyPos = null;
					}
				}
			} else {
				if (canPlace(currentPos, range.getValue(), breakCrystal.getValue())) {
					if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
						return;
					}
					delayTimer.reset();
					placeBlock(currentPos, rotate.getValue(), anchor);
				} else if (getBlock(currentPos) == Blocks.RESPAWN_ANCHOR) {
					if (!chargeList.contains(currentPos)) {
						if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
							return;
						}
						delayTimer.reset();
						clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), glowstone);
						chargeList.add(currentPos);
					} else {
						if (!delayTimer.passed((long) (placeDelay.getValueFloat()))) {
							return;
						}
						delayTimer.reset();
						chargeList.remove(currentPos);
						clickBlock(currentPos, getClickSide(currentPos), rotate.getValue(), unBlock);
						if (spamPlace.getValue()) {
							if (yawStep.getValue() && checkFov.getValue()) {
								Direction side = getClickSide(currentPos);
								Vec3d directionVec = new Vec3d(currentPos.getX() + 0.5 + side.getVector().getX() * 0.5, currentPos.getY() + 0.5 + side.getVector().getY() * 0.5, currentPos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
								if (Alien.ROTATION.inFov(directionVec, fov.getValueFloat())) {
								CombatUtil.modifyPos = currentPos;
								CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
								placeBlock(currentPos, rotate.getValue(), anchor);
								CombatUtil.modifyPos = null;
								}
							} else {
								CombatUtil.modifyPos = currentPos;
								CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
								placeBlock(currentPos, rotate.getValue(), anchor);
								CombatUtil.modifyPos = null;
							}
						}
					}
				}
			}
			if (!inventorySwap.getValue()) doSwap(old);
		}
	}
	private void calc() {
		if (nullCheck()) return;
		if (calcTimer.passed((long) (updateDelay.getValueFloat()))) {
			PlayerAndPredict selfPredict = new PlayerAndPredict(mc.player);
			calcTimer.reset();
			tempPos = null;
			double placeDamage = minDamage.getValue();
			double breakDamage = breakMin.getValue();
			boolean anchorFound = false;
			java.util.List<PlayerEntity> enemies = CombatUtil.getEnemies(targetRange.getValue());
			ArrayList<PlayerAndPredict> list = new ArrayList<>();
			for (PlayerEntity player : enemies) {
				list.add(new PlayerAndPredict(player));
			}
			for (PlayerAndPredict pap : list) {
				BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
				if (canPlace(pos, range.getValue(), breakCrystal.getValue()) || getBlock(pos) == Blocks.RESPAWN_ANCHOR && BlockUtil.getClickSideStrict(pos) != null) {
					double selfDamage;
					if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
						continue;
					}
					double damage;
					if ((damage = getAnchorDamage(pos, pap.player, pap.predict)) > headDamage.getValueFloat()) {
						lastDamage = damage;
						displayTarget = pap.player;
						tempPos = pos;
						break;
					}
				}
			}
			if (tempPos == null) {
				for (BlockPos pos : getSphere(range.getValueFloat())) {
					for (PlayerAndPredict pap : list) {
						if (light.getValue()) {
							CombatUtil.modifyPos = pos;
							CombatUtil.modifyBlockState = Blocks.AIR.getDefaultState();
							boolean skip = !canSee(pos.toCenterPos(), pap.predict.getPos());
							CombatUtil.modifyPos = null;
							if (skip) continue;
						}

						if (getBlock(pos) != Blocks.RESPAWN_ANCHOR) {
							if (anchorFound) continue;
							if (!canPlace(pos, range.getValue(), breakCrystal.getValue())) continue;

							CombatUtil.modifyPos = pos;
							CombatUtil.modifyBlockState = Blocks.OBSIDIAN.getDefaultState();
							boolean skip = BlockUtil.getClickSideStrict(pos) == null;
							CombatUtil.modifyPos = null;
							if (skip) continue;

							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (damage >= placeDamage) {
								if (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									double selfDamage;
									if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
										continue;
									}
									lastDamage = damage;
									displayTarget = pap.player;
									placeDamage = damage;
									tempPos = pos;
								}
							}
						} else {
							double damage = getAnchorDamage(pos, pap.player, pap.predict);
							if (getClickSideStrict(pos) == null) continue;
							if (damage >= breakDamage) {
								if (damage >= minPrefer.getValue()) anchorFound = true;
								if (!anchorFound && damage < placeDamage) {
									continue;
								}
								if (AutoCrystal.crystalPos == null || AutoCrystal.INSTANCE.isOff() || AutoCrystal.INSTANCE.lastDamage < damage) {
									double selfDamage;
									if ((selfDamage = getAnchorDamage(pos, selfPredict.player, selfPredict.predict)) > maxSelfDamage.getValue() || noSuicide.getValue() && selfDamage > mc.player.getHealth() + mc.player.getAbsorptionAmount()) {
										continue;
									}
									lastDamage = damage;
									displayTarget = pap.player;
									breakDamage = damage;
									tempPos = pos;
								}
							}
						}
					}
				}
			}
		}
		currentPos = tempPos;
	}
	public double getAnchorDamage(BlockPos anchorPos, PlayerEntity target, PlayerEntity predict) {
		if (terrainIgnore.getValue()) {
			CombatUtil.terrainIgnore = true;
		}
		double damage = ExplosionUtil.anchorDamage(anchorPos, target, predict);
		CombatUtil.terrainIgnore = false;
		return damage;
	}
	public void placeBlock(BlockPos pos, boolean rotate, int slot) {
		if (airPlace()) {
			//BlockUtil.placedPos.add(pos);
			clickBlock(pos, Direction.DOWN, rotate, slot);
			return;
		}
		Direction side = getPlaceSide(pos);
		if (side == null) return;
		//BlockUtil.placedPos.add(pos);
		clickBlock(pos.offset(side), side.getOpposite(), rotate, slot);
	}
	public void clickBlock(BlockPos pos, Direction side, boolean rotate, int slot) {
		if (pos == null) return;
		Vec3d directionVec = new Vec3d(pos.getX() + 0.5 + side.getVector().getX() * 0.5, pos.getY() + 0.5 + side.getVector().getY() * 0.5, pos.getZ() + 0.5 + side.getVector().getZ() * 0.5);
		if (rotate) {
			if (!faceVector(directionVec)) return;
		}
		doSwap(slot);
		EntityUtil.swingHand(Hand.MAIN_HAND, swingMode.getValue());
		BlockHitResult result = new BlockHitResult(directionVec, side, pos, false);
		Module.sendSequencedPacket(id -> new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, result, id));
		if (inventorySwap.getValue()) {
			doSwap(slot);
		}
	}
	private void doSwap(int slot) {
		if (inventorySwap.getValue()) {
			InventoryUtil.inventorySwap(slot, mc.player.getInventory().selectedSlot);
		} else {
			InventoryUtil.switchToSlot(slot);
		}
	}
	public boolean faceVector(Vec3d directionVec) {
		if (!yawStep.getValue()) {
			Alien.ROTATION.lookAt(directionVec);
			return true;
		} else {
			this.directionVec = directionVec;
			if (Alien.ROTATION.inFov(directionVec, fov.getValueFloat())) {
				return true;
			}
		}
		return !checkFov.getValue();
	}

	public static boolean canSee(Vec3d from, Vec3d to) {
		HitResult result = mc.world.raycast(new RaycastContext(from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
		return result == null || result.getType() == HitResult.Type.MISS;
	}
	public static class PlayerAndPredict {
		public final PlayerEntity player;
		public final PlayerEntity predict;
		public PlayerAndPredict(PlayerEntity player) {
			this.player = player;
			if (INSTANCE.predictTicks.getValueFloat() > 0) {
				predict = new PlayerEntity(mc.world, player.getBlockPos(), player.getYaw(), new GameProfile(UUID.fromString("66123666-1234-5432-6666-667563866600"), "PredictEntity339")) {@Override public boolean isSpectator() {return false;} @Override public boolean isCreative() {return false;}};
				predict.setPosition(player.getPos().add(CombatUtil.getMotionVec(player, INSTANCE.predictTicks.getValueInt(), true)));
				predict.setHealth(player.getHealth());
				predict.prevX = player.prevX;
				predict.prevZ = player.prevZ;
				predict.prevY = player.prevY;
				predict.setPose(player.getPose());
				predict.setOnGround(player.isOnGround());
				predict.getInventory().clone(player.getInventory());
				for (StatusEffectInstance se : new ArrayList<>(player.getStatusEffects())) {
					predict.addStatusEffect(se);
				}
			} else {
				predict = player;
			}
		}
	}

	public enum Page {
		General,
		Calc,
		Rotate,
		Assist,
		Render,
	}

	public class AnchorRender {
		@EventHandler
		public void onRender3D(Render3DEvent event) {
			if (currentPos != null) {
				noPosTimer.reset();
				placeVec3d = currentPos.toCenterPos();
			}
			if (placeVec3d == null) {
				return;
			}
			if (fadeSpeed.getValue() >= 1) {
				fade = noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5;
			} else {
				fade = AnimateUtil.animate(fade, noPosTimer.passedMs((long) (startFadeTime.getValue() * 1000)) ? 0 : 0.5, fadeSpeed.getValue() / 10);
			}
			if (fade == 0) {
				curVec3d = null;
				return;
			}
			if (curVec3d == null || sliderSpeed.getValue() >= 1) {
				curVec3d = placeVec3d;
			} else {
				curVec3d = new Vec3d(AnimateUtil.animate(curVec3d.x, placeVec3d.x, sliderSpeed.getValue() / 10),
						AnimateUtil.animate(curVec3d.y, placeVec3d.y, sliderSpeed.getValue() / 10),
						AnimateUtil.animate(curVec3d.z, placeVec3d.z, sliderSpeed.getValue() / 10));
			}

			if (render.getValue()) {
				Box cbox = new Box(curVec3d, curVec3d);
				if (shrink.getValue()) {
					cbox = cbox.expand(fade);
				} else {
					cbox = cbox.expand(0.5);
				}
				MatrixStack matrixStack = event.getMatrixStack();
				if (fill.booleanValue) {
					Render3DUtil.drawFill(matrixStack, cbox, ColorUtil.injectAlpha(fill.getValue(), (int) (fill.getValue().getAlpha() * fade * 2D)));
				}
				if (box.booleanValue) {
					Render3DUtil.drawBox(matrixStack, cbox, ColorUtil.injectAlpha(box.getValue(), (int) (box.getValue().getAlpha() * fade * 2D)), lineWidth.getValueFloat());
				}
			}
		}
	}

	private final Timer assistTimer = new Timer();
	BlockPos assistPos;

	public void onAssist() {
		assistPos = null;
		int anchor = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.RESPAWN_ANCHOR) : InventoryUtil.findBlock(Blocks.RESPAWN_ANCHOR);
		int glowstone = inventorySwap.getValue() ? InventoryUtil.findBlockInventorySlot(Blocks.GLOWSTONE) : InventoryUtil.findBlock(Blocks.GLOWSTONE);
		int old = mc.player.getInventory().selectedSlot;
		if (anchor == -1) {
			return;
		}
		if (glowstone == -1) {
			return;
		}
		if (mc.player.isSneaking()) {
			return;
		}
		if (usingPause.getValue() && mc.player.isUsingItem()) {
			return;
		}
		if (Blink.INSTANCE.isOn() && Blink.INSTANCE.pauseModule.getValue()) return;
		if (!assistTimer.passed((long) (delay.getValueFloat() * 1000))) {
			return;
		}
		assistTimer.reset();
		double bestDamage;
		ArrayList<AutoAnchor.PlayerAndPredict> list = new ArrayList<>();
		for (PlayerEntity player : CombatUtil.getEnemies(assistRange.getValue())) {
			list.add(new AutoAnchor.PlayerAndPredict(player));
		}

		bestDamage = assistDamage.getValue();
		for (AutoAnchor.PlayerAndPredict pap : list) {
			BlockPos pos = EntityUtil.getEntityPos(pap.player, true).up(2);
			if (mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR) {
				return;
			}
			if (BlockUtil.clientCanPlace(pos, false)) {
				double damage = getAnchorDamage(pos, pap.player, pap.predict);
				if (damage >= bestDamage) {
					bestDamage = damage;
					assistPos = pos;
				}
			}
			for (Direction i : Direction.values()) {
				if (i == Direction.UP || i == Direction.DOWN) continue;
				if (BlockUtil.clientCanPlace(pos.offset(i), false)) {
					double damage = getAnchorDamage(pos.offset(i), pap.player, pap.predict);
					if (damage >= bestDamage) {
						bestDamage = damage;
						assistPos = pos.offset(i);
					}
				}
			}
		}
		if (assistPos != null && BlockUtil.getPlaceSide(assistPos, range.getValue()) == null) {
			BlockPos placePos;
			if ((placePos = getHelper(assistPos)) != null) {
				doSwap(anchor);
				BlockUtil.placeBlock(placePos, rotate.getValue());
				if (inventorySwap.getValue()) {
					doSwap(anchor);
				} else {
					doSwap(old);
				}
			}
		}
	}

	public BlockPos getHelper(BlockPos pos) {
		for (Direction i : Direction.values()) {
			if (checkMine.getValue() && Alien.BREAK.isMining(pos.offset(i))) continue;
			if (!BlockUtil.isStrictDirection(pos.offset(i), i.getOpposite())) continue;
			if (BlockUtil.canPlace(pos.offset(i))) return pos.offset(i);
		}
		return null;
	}
}