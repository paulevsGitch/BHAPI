package net.bhapi.mixin.common.block;

import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.properties.LegacyProperties;
import net.bhapi.blockstate.properties.StateProperty;
import net.bhapi.client.render.block.BHBlockRender;
import net.bhapi.client.render.block.BlockItemView;
import net.bhapi.client.render.texture.TextureSample;
import net.bhapi.client.render.texture.Textures;
import net.bhapi.interfaces.ClientPostInit;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockDirection;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.level.BlockView;
import net.minecraft.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LeverBlock.class)
public abstract class LeverBlockMixin extends BaseBlock implements BlockStateContainer, ClientPostInit, BHBlockRender {
	private static final Vec3I[] OFFSETS = new Vec3I[] {
		new Vec3I(0, -1, 0),
		new Vec3I(-1, 0, 0),
		new Vec3I(1, 0, 0),
		new Vec3I(0, 0, -1),
		new Vec3I(0, 0, 1),
		new Vec3I(0, -1, 0)
	};
	
	protected LeverBlockMixin(int i, Material arg) {
		super(i, arg);
	}
	
	@Shadow public abstract boolean canPlaceAt(Level arg, int i, int j, int k);
	
	@Environment(EnvType.CLIENT)
	private static final TextureSample[] BHAPI_SAMPLES = new TextureSample[2];
	
	@Override
	public void appendProperties(List<StateProperty<?>> properties) {
		properties.add(LegacyProperties.META_16);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void afterClientInit() {
		if (BHAPI_SAMPLES[0] != null) return;
		BHAPI_SAMPLES[0] = Textures.getAtlas().getSample(Identifier.make("block/lever"));
		BHAPI_SAMPLES[1] = Textures.getAtlas().getSample(Identifier.make("block/cobblestone"));
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public TextureSample getTextureForIndex(BlockView view, int x, int y, int z, BlockState state, int index) {
		if (view instanceof BlockItemView) return BHAPI_SAMPLES[0];
		return BHAPI_SAMPLES[index < 6 ? 1 : 0];
	}
	
	@Override
	public void onNeighbourBlockUpdate(Level level, int x, int y, int z, BlockDirection facing, BlockState state, BlockState neighbour) {
		if (state.getMeta() == 0) return;
		BlockStateProvider provider = BlockStateProvider.cast(level);
		if (!this.canPlaceAt(level, x, y, z)) {
			BlockUtil.brokenBlock = state;
			this.drop(level, x, y, z, state.getMeta());
			provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
			return;
		}
		int wrapped = state.getMeta() & 7;
		if (wrapped > 5) wrapped = 0;
		Vec3I offset = OFFSETS[wrapped];
		if (!level.canSuffocate(x + offset.x, y + offset.y, z + offset.z)) {
			BlockUtil.brokenBlock = state;
			this.drop(level, x, y, z, state.getMeta());
			provider.setBlockState(x, y, z, BlockUtil.AIR_STATE);
		}
	}
	
	@Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
	private void bhapi_canUse(Level level, int x, int y, int z, PlayerBase player, CallbackInfoReturnable<Boolean> info) {
		if (level.isClientSide) {
			info.setReturnValue(true);
			return;
		}
		BlockStateProvider provider = BlockStateProvider.cast(level);
		BlockState state = provider.getBlockState(x, y, z);
		int meta = state.getMeta();
		int wrappedMeta = meta & 7;
		int powered = 8 - (meta & 8);
		level.setBlockMeta(x, y, z, wrappedMeta + powered);
		level.callAreaEvents(x, y, z, x, y, z);
		level.playSound((double)x + 0.5, (double)y + 0.5, (double)z + 0.5, "random.click", 0.3f, powered > 0 ? 0.6f : 0.5f);
		level.updateAdjacentBlocks(x, y, z, this.id);
		/*if (wrappedMeta == 1) {
			level.updateAdjacentBlocks(x - 1, y, z, this.id);
		} else if (wrappedMeta == 2) {
			level.updateAdjacentBlocks(x + 1, y, z, this.id);
		} else if (wrappedMeta == 3) {
			level.updateAdjacentBlocks(x, y, z - 1, this.id);
		} else if (wrappedMeta == 4) {
			level.updateAdjacentBlocks(x, y, z + 1, this.id);
		} else {
			level.updateAdjacentBlocks(x, y - 1, z, this.id);
		}*/
		/*Vec3I pos = new Vec3I();
		for (BlockDirection dir: BlockDirection.VALUES) {
			dir.move(pos.set(x, y, z));
			level.updateAdjacentBlocks(pos.x, pos.y, pos.z, this.id);
		}
		Vec3I offset = OFFSETS[state.getMeta() & 7];
		state = provider.getBlockState(pos.set(x, y, z).add(offset));
		for (BlockDirection dir: BlockDirection.VALUES) {
			dir.move(pos.set(x, y, z).add(offset));
			level.updateAdjacentBlocks(pos.x, pos.y, pos.z, state.getBlock().id);
		}*/
		int wrapped = state.getMeta() & 7;
		if (wrapped > 5) wrapped = 0;
		Vec3I offset = OFFSETS[wrapped];
		level.updateAdjacentBlocks(x + offset.x, y + offset.y, z + offset.z, state.getBlock().id);
		info.setReturnValue(true);
	}
}
