package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.blockstate.BlockState;
import net.bhapi.blockstate.BlockStateContainer;
import net.bhapi.blockstate.BlockStateProvider;
import net.bhapi.registry.DefaultRegistries;
import net.bhapi.util.ChunkSection;
import net.bhapi.util.NBTSerializable;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.storage.NibbleArray;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.maths.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Chunk.class)
public class ChunkMixin implements BlockStateProvider, NBTSerializable {
	@Shadow
	public boolean needUpdate;
	@Shadow public Map blockEntities;
	@Shadow public byte[] heightmap;
	@Shadow public NibbleArray meta;
	@Shadow public byte[] blocks;
	@Shadow @Final public int x;
	@Shadow @Final public int z;
	@Shadow public Level level;
	
	@Unique private BlockState[] blockstates = new BlockState[32768];
	@Unique private ChunkSection[] sections;
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;[BII)V", at = @At("TAIL"))
	private void bhapi_onChunkInit(Level level, byte[] blocks, int x, int z, CallbackInfo info) {
		if (blocks != null) {
			for (int i = 0; i < blocks.length; i++) {
				blockstates[i] = getByID(blocks[i]);
			}
		}
	}
	
	@Inject(method = "setBlock(IIIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, int meta, CallbackInfoReturnable<Boolean> info) {
		//BHAPI.warn("Attempting to set numerical block id and meta");
		setBlockState(x, y, z, getByID(id));
		//info.cancel();
	}
	
	@Inject(method = "setBlock(IIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, CallbackInfoReturnable<Boolean> info) {
		//BHAPI.warn("Attempting to set numerical block id and meta");
		setBlockState(x, y, z, getByID(id));
		//info.cancel();
	}
	
	@Inject(method = "setMeta(IIII)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_disableMetaSet(int x, int y, int z, int id, CallbackInfo info) {
		//BHAPI.warn("Attempting to get numerical block meta");
		//info.cancel();
	}
	
	/*@Inject(method = "getBlockId(III)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_disableBlockIDGet(int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		BHAPI.warn("Attempting to get numerical block ID");
		info.setReturnValue(0);
	}
	
	@Inject(method = "getMeta(III)I", at = @At("HEAD"))
	private void bhapi_disableBlockMetaGet(int i, int j, int k, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(0);
	}*/
	
	@Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlockEntity(int x, int y, int z, CallbackInfoReturnable<BaseBlockEntity> info) {
		BlockPos pos = new BlockPos(x, y, z);
		BaseBlockEntity entity = (BaseBlockEntity) this.blockEntities.get(pos);
		if (entity == null) {
			BlockState state = getBlockState(x, y, z);
			if (state == null) {
				info.setReturnValue(null);
				return;
			}
			BlockStateContainer container = (BlockStateContainer) state.getBlock();
			if (!container.hasTileEntity(state)) {
				info.setReturnValue(null);
				return;
			}
			state.getBlock().onBlockPlaced(this.level, this.x * 16 + x, y, this.z * 16 + z);
			entity = (BaseBlockEntity) this.blockEntities.get(pos);
		}
		if (entity != null && entity.isInvalid()) {
			this.blockEntities.remove(pos);
			info.setReturnValue(null);
			return;
		}
		info.setReturnValue(entity);
	}
	
	@Shadow
	private void updateSkylight(int i, int j, int k) {}
	
	@Shadow
	private void fillSkyLight(int i, int j) {}
	
	@Unique
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state) {
		int index = getIndex(x, y, z);
		if (state == blockstates[index]) return false;
		blockstates[index] = state;
		int px = this.x << 4 | x;
		int pz = this.z << 4 | z;
		if (!this.level.isClientSide) blockstates[index].getBlock().onBlockRemoved(level, px, y, pz);
		
		int height = this.heightmap[z << 4 | x] & 0xFF;
		
		if (!this.level.dimension.noSkyLight) {
			if (state != null && state.getContainer().getLightOpacity(state) != 0) {
				if (y >= height) {
					this.updateSkylight(x, y + 1, z);
				}
			}
			else if (y == height - 1) {
				this.updateSkylight(x, y, z);
			}
			this.level.updateLight(LightType.SKY, px, y, pz, px, y, pz);
		}
		this.level.updateLight(LightType.BLOCK, px, y, pz, px, y, pz);
		this.fillSkyLight(x, z);
		state.getBlock().onBlockPlaced(this.level, px, y, pz);
		this.needUpdate = true;
		
		return true;
	}
	
	@Unique
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		return blockstates[getIndex(x, y, z)];
	}
	
	@Unique
	private int getIndex(int x, int y, int z) {
		return x << 11 | z << 7 | y;
	}
	
	@Unique
	private BlockState getByID(int id) {
		BaseBlock block = BaseBlock.BY_ID[id];
		if (block == null) block = DefaultRegistries.AIR_BLOCK;
		return BlockStateContainer.cast(block).getDefaultState();
	}
	
	@Unique
	@Override
	public void saveToNBT(CompoundTag tag) {
		byte[] data = new byte[blockstates.length];
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte) DefaultRegistries.BLOCKSTATES_MAP.getID(blockstates[i]);
		}
		tag.put("blockstates", data);
	}
	
	@Unique
	@Override
	public void loadFromNBT(CompoundTag tag) {
		byte[] data = tag.getByteArray("blockstates");
		if (data.length == blockstates.length) {
			for (int i = 0; i < data.length; i++) {
				blockstates[i] = DefaultRegistries.BLOCKSTATES_MAP.get(data[i]);
				if (blockstates[i] == null) blockstates[i] = BlockState.AIR_STATE;
			}
		}
	}
}
