package dev.luminous.mod.modules.impl.player;

import dev.luminous.Alien;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.ClickBlockEvent;
import dev.luminous.api.events.impl.PacketEvent;
import dev.luminous.api.events.impl.Render3DEvent;
import dev.luminous.api.utils.entity.InventoryUtil;
import dev.luminous.api.utils.render.Render3DUtil;
import dev.luminous.mod.modules.Module;
import dev.luminous.mod.modules.impl.client.AntiCheat;
import dev.luminous.mod.modules.settings.impl.BooleanSetting;
import dev.luminous.mod.modules.settings.impl.SliderSetting;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import java.awt.*;

public class AutoMine extends Module {

    BooleanSetting multitaskConfig = add(new BooleanSetting("Multitask", false));
    SliderSetting rangeConfig = add(new SliderSetting("Range", 4.0, 0.1, 5.0));
    SliderSetting speedConfig = add(new SliderSetting("Speed", 1.0, 0.1, 1.0));
    BooleanSetting rotateConfig = add(new BooleanSetting("Rotate",  true));
    BooleanSetting switchResetConfig = add(new BooleanSetting("SwitchReset", false));
    BooleanSetting grimConfig = add(new BooleanSetting("Grim", false));
    BooleanSetting instantConfig = add(new BooleanSetting("Instant",  true));

    private MiningData miningData;
    private long lastBreak;

    public AutoMine() {
        super("AutoMine", "Automatically mines blocks", Module.Category.Player);

    }

    @Override
    public String getInfo() {
        if (miningData != null)
        {
            return String.format("%.1f", Math.min(miningData.getBlockDamage(), miningData.getSpeed()));
        }
        return "0.0";
    }

    @Override
    public void onDisable()
    {
        if (miningData != null && miningData.isStarted())
        {
            abortMining(miningData);
        }
        miningData = null;
    }

    public void onPlayerTick()
    {
        if (miningData != null)
        {
            final double distance = miningData.getPos().toCenterPos().distanceTo(mc.player.getEyePos());
            if (distance > rangeConfig.getValueFloat())
            {
                abortMining(miningData);
                miningData = null;
                return;
            }
            if (miningData.getState().isAir())
            {
                // Once we broke the block that overrode that the auto city, we can allow the module
                // to auto mine "city" blocks
                if (instantConfig.getValue())
                {
                    miningData.setInstantRemine();
                    miningData.setDamage(1.0f);
                }
                else
                {
                    miningData.resetDamage();
                }
                return;
            }
            final float damageDelta = calcBlockBreakingDelta(
                    miningData.getState(), mc.world, miningData.getPos());
            if (miningData.damage(damageDelta) >= miningData.getSpeed() || miningData.isInstantRemine())
            {
                if (mc.player.isUsingItem() && !multitaskConfig.getValue())
                {
                    return;
                }
                stopMining(miningData);
            }
        }
    }

    @EventHandler
    public void onAttackBlock(final ClickBlockEvent event)
    {
        // Do not try to break unbreakable blocks
        BlockState state = mc.world.getBlockState(event.getPos());
        if (state.getBlock().getHardness() == -1.0f || state.isAir() || mc.player.isCreative())
        {
            return;
        }
        event.cancel();
        mc.player.swingHand(Hand.MAIN_HAND);
        if (miningData != null)
        {
            if (miningData.getPos().equals(event.getPos()))
            {
                return;
            }
            abortMining(miningData);
        }
        miningData = new MiningData(event.getPos(), event.getDirection(), speedConfig.getValueFloat());
        startMining(miningData);
    }

    @EventHandler
    public void onPacketOutbound(PacketEvent.Send event) {
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket && switchResetConfig.getValue()) {
            if (miningData != null) {
                miningData.resetDamage();
            }
        }
    }

    @EventHandler
    public void onRenderWorld(final Render3DEvent event)
    {
        renderMiningData(event.getMatrixStack(), miningData);
    }

    private void renderMiningData(MatrixStack matrixStack, MiningData data) {
        if (data != null && !mc.player.isCreative()) {
            BlockPos mining = data.getPos();
            VoxelShape outlineShape = VoxelShapes.fullCube();
            if (!data.isInstantRemine()) {
                outlineShape = data.getState().getOutlineShape(mc.world, mining);
                outlineShape = outlineShape.isEmpty() ? VoxelShapes.fullCube() : outlineShape;
            }
            Box render1 = outlineShape.getBoundingBox();
            Box render = new Box(mining.getX() + render1.minX, mining.getY() + render1.minY,
                    mining.getZ() + render1.minZ, mining.getX() + render1.maxX,
                    mining.getY() + render1.maxY, mining.getZ() + render1.maxZ);
            Vec3d center = render.getCenter();
            float scale = MathHelper.clamp(data.getBlockDamage() / data.getSpeed(), 0, 1.0f);
            double dx = (render1.maxX - render1.minX) / 2.0;
            double dy = (render1.maxY - render1.minY) / 2.0;
            double dz = (render1.maxZ - render1.minZ) / 2.0;
            final Box scaled = new Box(center, center).expand(dx * scale, dy * scale, dz * scale);
            Render3DUtil.drawFill(matrixStack, scaled,
                    new Color(data.getBlockDamage() > (0.95f * data.getSpeed()) ? 0x6000ff00 : 0x60ff0000));
            Render3DUtil.drawBox(matrixStack, scaled,
                    new Color(data.getBlockDamage() > (0.95f * data.getSpeed()) ? 0x6000ff00 : 0x60ff0000));
        }
    }

    private void startMining(MiningData data) {
        if (data.getState().isAir()) {
            return;
        }
        boolean canSwap = data.getSlot() != -1;
        int slot = mc.player.getInventory().selectedSlot;
        if (canSwap) {
            InventoryUtil.switchToSlot(data.getSlot());
        }
        if (rotateConfig.getValue()) {
            Alien.ROTATION.lookAt(data.getPos().toCenterPos());
        }
        sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        if (canSwap) {
            InventoryUtil.switchToSlot(slot);
        }
        if (rotateConfig.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
            Alien.ROTATION.snapBack();
        }
        data.setStarted();
    }

    private void abortMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir() || data.isInstantRemine() || data.getBlockDamage() >= 1.0f) {
            return;
        }
        sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        //Managers.INVENTORY.syncToClient();
    }

    private void stopMining(MiningData data) {
        if (!data.isStarted() || data.getState().isAir()) {
            return;
        }
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L76
        // https://github.com/GrimAnticheat/Grim/blob/2.0/src/main/java/ac/grim/grimac/checks/impl/misc/FastBreak.java#L98
        boolean canSwap = data.getSlot() != -1;
        int slot = mc.player.getInventory().selectedSlot;
        if (canSwap) {
            InventoryUtil.switchToSlot(data.getSlot());
        }
        if (rotateConfig.getValue()) {
            Alien.ROTATION.lookAt(data.getPos().toCenterPos());
        }
        sendSequencedPacket(id -> new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, data.getPos(), data.getDirection(), id));
        if (grimConfig.getValue()) {
            sendSequencedPacket(id -> new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, data.getPos().up(500), data.getDirection(), id));
        }
        data.setBroken();
        lastBreak = System.currentTimeMillis();
        if (canSwap) {
            InventoryUtil.switchToSlot(slot);
        }
        if (rotateConfig.getValue() && AntiCheat.INSTANCE.snapBack.getValue()) {
                Alien.ROTATION.snapBack();
        }
    }

    public boolean isBlockDelayGrim() {
        return System.currentTimeMillis() - lastBreak <= 280 && grimConfig.getValue();
    }

    float calcBlockBreakingDelta(BlockState state, BlockView world,
                                 BlockPos pos) {
        float f = state.getHardness(world, pos);
        if (f == -1.0f) {
            return 0.0f;
        } else {
            int i = canHarvest(state) ? 30 : 100;
            return getBlockBreakingSpeed(state) / f / (float) i;
        }
    }
    private boolean canHarvest(BlockState state) {
        if (state.isToolRequired()) {
            int tool = getBestTool(state);
            return mc.player.getInventory().getStack(tool).isSuitableFor(state);
        }
        return true;
    }
    private float getBlockBreakingSpeed(BlockState block) {
        int tool = getBestTool(block);
        float f = mc.player.getInventory().getStack(tool).getMiningSpeedMultiplier(block);
        if (f > 1.0F) {
            ItemStack stack = mc.player.getInventory().getStack(tool);
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }
        if (StatusEffectUtil.hasHaste(mc.player)) {
            f *= 1.0f + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2f;
        }
        if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            float g = switch (mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
                case 0 -> 0.3f;
                case 1 -> 0.09f;
                case 2 -> 0.0027f;
                default -> 8.1e-4f;
            };
            f *= g;
        }
        if (mc.player.isSubmergedIn(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0f;
        }
        if (!mc.player.isOnGround()) {
            f /= 5.0f;
        }
        return f;
    }
    public int getBestTool(final BlockState state) {
        int slot = getBestToolNoFallback(state);
        if (slot != -1) {
            return slot;
        }
        return mc.player.getInventory().selectedSlot;
    }

    public static int getBestToolNoFallback(final BlockState state)
    {
        int slot = -1;
        float bestTool = 0.0f;
        for (int i = 0; i < 9; i++)
        {
            final ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof ToolItem))
            {
                continue;
            }
            float speed = stack.getMiningSpeedMultiplier(state);
            final int efficiency = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (efficiency > 0)
            {
                speed += efficiency * efficiency + 1.0f;
            }
            if (speed > bestTool)
            {
                bestTool = speed;
                slot = i;
            }
        }
        return slot;
    }

    public static class MiningData {

        private final BlockPos pos;
        private final Direction direction;
        private final float speed;
        private float blockDamage;
        private boolean instantRemine;
        private boolean started;
        private int breakPackets;

        public MiningData(BlockPos pos, Direction direction, float speed) {
            this.pos = pos;
            this.direction = direction;
            this.speed = speed;
        }

        public boolean isInstantRemine() {
            return instantRemine;
        }

        public void setInstantRemine() {
            this.instantRemine = true;
        }

        public float damage(final float dmg)
        {
            blockDamage += dmg;
            return blockDamage;
        }

        public void setDamage(float blockDamage) {
            this.blockDamage = blockDamage;
        }

        public void resetDamage() {
            instantRemine = false;
            blockDamage = 0.0f;
        }

        public int getBreakPackets() {
            return breakPackets;
        }

        public void setBroken() {
            breakPackets++;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Direction getDirection() {
            return direction;
        }

        public float getSpeed() {
            return speed;
        }

        public int getSlot() {
            return getBestToolNoFallback(getState());
        }

        public BlockState getState() {
            return mc.world.getBlockState(pos);
        }

        public boolean isStarted() {
            return started;
        }

        public void setStarted() {
            this.started = true;
        }

        public float getBlockDamage() {
            return blockDamage;
        }
    }
}