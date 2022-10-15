package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.util.ChunkSection;
import net.bhapi.util.NBTSerializable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BaseBlock;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.BaseEntity;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.storage.NibbleArray;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
import net.minecraft.util.maths.BlockPos;
import net.minecraft.util.maths.Box;
import net.minecraft.util.maths.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mixin(Chunk.class)
public abstract class ChunkMixin implements NBTSerializable {
	@Shadow public boolean needUpdate;
	@Shadow public Map blockEntities;
	@Shadow public byte[] heightmap;
	@Shadow public NibbleArray meta;
	@Shadow public byte[] blocks;
	@Shadow @Final public int x;
	@Shadow @Final public int z;
	@Shadow public Level level;
	@Shadow public int minHeight;
	@Shadow public NibbleArray skyLight;
	@Shadow public NibbleArray blockLight;
	@Shadow public boolean decorated;
	@Shadow public boolean hasEntities;
	@Shadow public List[] entities;
	
	@Shadow public abstract int getBlockId(int i, int j, int k);
	@Shadow protected abstract void fillSkyLight(int i, int j);
	@Shadow public abstract int getMeta(int i, int j, int k);
	@Shadow protected abstract void updateSkylight(int i, int j, int k);
	@Shadow public abstract boolean setBlock(int i, int j, int k, int l, int m);
	@Shadow public abstract void generateHeightmap();
	@Shadow public abstract void afterBlockLightReset();
	@Shadow public abstract void addEntity(BaseEntity arg);
	@Shadow public abstract void addBlockEntity(BaseBlockEntity arg);
	
	@Shadow public boolean canHaveBlockEntities;
	@Unique private ChunkSection[] bhapi_sections;
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;[BII)V", at = @At("TAIL"))
	private void bhapi_onChunkInit(Level level, byte[] blocks, int x, int z, CallbackInfo info) {
		bhapi_initSections();
		
		if (blocks != null && blocks[0] != 0) {
			bhapi_fillBlocks();
		}
		
		this.meta = null;
		this.entities = null;
		this.blockEntities = null;
	}
	
	@Inject(method = "setBlock(IIIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, int meta, CallbackInfoReturnable<Boolean> info) {
		byte index = (byte) (y >> 4);
		if (index < 0 || index >= bhapi_sections.length) {
			info.setReturnValue(false);
			return;
		}
		
		ChunkSection section = bhapi_sections[index];
		if (section == null) {
			section = new ChunkSection();
			bhapi_sections[index] = section;
		}
		
		byte py = (byte) (y & 15);
		
		int height = this.heightmap[z << 4 | x] & 0xFF;
		int blockID = section.getID(x, py, z);
		if (blockID == id && section.getMeta(x, py, z) == meta) {
			info.setReturnValue(false);
			return;
		}
		
		int wx = this.x << 4 | x;
		int wz = this.z << 4 | z;
		
		section.setID(x, py, z, id);
		
		if (blockID != 0 && !this.level.isClientSide) {
			BaseBlock.BY_ID[blockID].onBlockRemoved(this.level, wx, y, wz);
		}
		
		section.setMeta(x, py, z, meta);
		
		if (!this.level.dimension.noSkyLight) {
			if (BaseBlock.LIGHT_OPACITY[id] != 0) {
				if (y >= height) {
					this.updateSkylight(x, y + 1, z);
				}
			} else if (y == height - 1) {
				this.updateSkylight(x, y, z);
			}
			this.level.updateLight(LightType.SKY, wx, y, wz, wx, y, wz);
		}
		
		this.level.updateLight(LightType.BLOCK, wx, y, wz, wx, y, wz);
		this.fillSkyLight(x, z);
		
		if (id != 0) {
			BaseBlock.BY_ID[id].onBlockPlaced(this.level, wx, y, wz);
		}
		
		this.needUpdate = true;
		info.setReturnValue(true);
	}
	
	@Inject(method = "setBlock(IIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.setBlock(x, y, z, id, 0));
	}
	
	@Inject(method = "setMeta(IIII)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_setMeta(int x, int y, int z, int meta, CallbackInfo info) {
		ChunkSection section = bhapi_getSection(y);
		if (section != null) {
			section.setMeta(x, y & 15, z, meta);
			this.needUpdate = true;
		}
		info.cancel();
	}
	
	@Inject(method = "getBlockId(III)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlockId(int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		ChunkSection section = bhapi_getSection(y);
		info.setReturnValue(section == null ? 0 : section.getID(x, y & 15, z));
	}
	
	@Inject(method = "getMeta(III)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getMeta(int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		ChunkSection section = bhapi_getSection(y);
		info.setReturnValue(section == null ? 0 : section.getMeta(x, y & 15, z));
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "updateHeightmap", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateHeightmap(CallbackInfo info) {
		info.cancel();
		int minHeight = 127;
		for (byte x = 0; x < 16; ++x) {
			for (byte z = 0; z < 16; ++z) {
				byte y;
				for (y = 127; y > 0 && BaseBlock.LIGHT_OPACITY[this.getBlockId(x, y - 1, z)] == 0; --y) {} // TODO change to blockstates
				this.heightmap[z << 4 | x] = y;
				if (y >= minHeight) continue;
				minHeight = y;
			}
		}
		this.minHeight = minHeight;
		this.needUpdate = true;
	}
	
	@Inject(method = "generateHeightmap", at = @At("HEAD"), cancellable = true)
	private void bhapi_generateHeightmap(CallbackInfo info) {
		info.cancel();
		bhapi_fillBlocks();
		
		byte x, z;
		int minHeight = 127;
		for (x = 0; x < 16; ++x) {
			for (z = 0; z < 16; ++z) {
				byte y;
				for (y = 127; y > 0 && BaseBlock.LIGHT_OPACITY[this.getBlockId(x, y - 1, z)] == 0; --y) {} // TODO change to blockstates
				this.heightmap[z << 4 | x] = y;
				if (y < minHeight) {
					minHeight = y;
				}
				if (this.level.dimension.noSkyLight) continue;
				int n6 = 15;
				for (byte h = 127; h >= 0; --h) {
					if ((n6 -= BaseBlock.LIGHT_OPACITY[this.getBlockId(x, h, z)]) <= 0) continue; // TODO change to blockstates
					this.skyLight.setValue(x, h, z, n6);
				}
			}
		}
		
		this.minHeight = minHeight;
		for (x = 0; x < 16; ++x) {
			for (z = 0; z < 16; ++z) {
				this.fillSkyLight(x, z);
			}
		}
		
		this.needUpdate = true;
	}
	
	// TODO optimise method, rework cycles
	@Inject(method = "updateSkylight", at = @At("HEAD"), cancellable = true)
	private void bhapi_updateSkylight(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		
		int n, n2, n3, h2;
		int h1 = h2 = this.heightmap[z << 4 | x] & 0xFF;
		
		if (y > h2) {
			h1 = y;
		}
		
		while (h1 > 0 && BaseBlock.LIGHT_OPACITY[getBlockId(x, h1 - 1, z) & 0xFF] == 0) { // TODO change to blockstates
			--h1;
		}
		
		if (h1 == h2) {
			return;
		}
		
		this.level.callColumnAreaEvent(x, z, h1, h2);
		this.heightmap[z << 4 | x] = (byte)h1;
		
		if (h1 < this.minHeight) {
			this.minHeight = h1;
		}
		else {
			n3 = 127;
			for (n2 = 0; n2 < 16; ++n2) {
				for (n = 0; n < 16; ++n) {
					if ((this.heightmap[n << 4 | n2] & 0xFF) >= n3) continue;
					n3 = this.heightmap[n << 4 | n2] & 0xFF;
				}
			}
			this.minHeight = n3;
		}
		
		n3 = this.x * 16 + x;
		n2 = this.z * 16 + z;
		
		if (h1 < h2) {
			for (n = h1; n < h2; ++n) {
				this.skyLight.setValue(x, n, z, 15);
			}
		}
		else {
			this.level.updateLight(LightType.SKY, n3, h2, n2, n3, h1, n2);
			for (n = h2; n < h1; ++n) {
				this.skyLight.setValue(x, n, z, 0);
			}
		}
		
		n = 15;
		int n7 = h1;
		while (h1 > 0 && n > 0) {
			int n8;
			if ((n8 = BaseBlock.LIGHT_OPACITY[this.getBlockId(x, --h1, z)]) == 0) {
				n8 = 1;
			}
			if ((n -= n8) < 0) {
				n = 0;
			}
			this.skyLight.setValue(x, h1, z, n);
		}
		
		while (h1 > 0 && BaseBlock.LIGHT_OPACITY[this.getBlockId(x, h1 - 1, z)] == 0) {
			--h1;
		}
		
		if (h1 != n7) {
			this.level.updateLight(LightType.SKY, n3 - 1, h1, n2 - 1, n3 + 1, n7, n2 + 1);
		}
		
		this.needUpdate = true;
	}
	
	@Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_addEntity(BaseEntity entity, CallbackInfo info) {
		info.cancel();
		
		int chunkX = MathHelper.floor(entity.x / 16.0);
		int chunkZ = MathHelper.floor(entity.z / 16.0);
		
		if (chunkX != this.x || chunkZ != this.z) {
			BHAPI.log("Wrong location! " + entity);
			Thread.dumpStack();
			return;
		}
		
		byte sectionY = (byte) MathHelper.floor(entity.y / 16.0);
		if (sectionY < 0 || sectionY >= bhapi_sections.length) return;
		ChunkSection section = bhapi_sections[sectionY];
		if (section == null) return;
		
		this.hasEntities = true;
		
		entity.placedInWorld = true;
		entity.chunkX = this.x;
		entity.chunkIndex = sectionY;
		entity.chunkZ = this.z;
		section.entities.add(entity);
	}
	
	@Inject(method = "removeEntity(Lnet/minecraft/entity/BaseEntity;I)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_removeEntity(BaseEntity entity, int sectionY, CallbackInfo info) {
		ChunkSection section = sectionY < 0 || sectionY >= bhapi_sections.length ? null : bhapi_sections[sectionY];
		if (section != null) section.entities.remove(entity);
		info.cancel();
	}
	
	@Inject(method = "addEntitiesToLevel", at = @At("HEAD"), cancellable = true)
	private void bhapi_addEntitiesToLevel(CallbackInfo info) {
		info.cancel();
		this.canHaveBlockEntities = true;
		Arrays.stream(bhapi_sections).forEach(section -> {
			this.level.addEntities(section.entities);
			this.level.addBlockEntities(section.getBlockEntities());
		});
	}
	
	@Inject(method = "removeEntitiesFromLevel", at = @At("HEAD"), cancellable = true)
	private void bhapi_removeEntitiesFromLevel(CallbackInfo info) {
		info.cancel();
		this.canHaveBlockEntities = false;
		Arrays.stream(bhapi_sections).forEach(section -> {
			section.getBlockEntities().forEach(BaseBlockEntity::invalidate);
			level.removeEntities(section.entities);
		});
	}
	
	@SuppressWarnings("unchecked")
	@Inject(method = "getEntities(Lnet/minecraft/entity/BaseEntity;Lnet/minecraft/util/maths/Box;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_getEntities(BaseEntity entity, Box area, List list, CallbackInfo info) {
		info.cancel();
		int minY = MathHelper.floor((area.minY - 2.0) / 16.0);
		int maxY = MathHelper.floor((area.maxY + 2.0) / 16.0);
		if (minY < 0) minY = 0;
		if (maxY >= bhapi_sections.length) maxY = bhapi_sections.length - 1;
		for (int sectionY = minY; sectionY <= maxY; ++sectionY) {
			ChunkSection section = bhapi_sections[sectionY];
			if (section == null) continue;
			list.addAll(section.entities.stream().filter(
				e -> e != entity && e.boundingBox.boxIntersects(area)
			).toList());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Inject(method = "getEntities(Ljava/lang/Class;Lnet/minecraft/util/maths/Box;Ljava/util/List;)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_getEntities(Class entityClass, Box area, List list, CallbackInfo info) {
		info.cancel();
		int minY = MathHelper.floor((area.minY - 2.0) / 16.0);
		int maxY = MathHelper.floor((area.maxY + 2.0) / 16.0);
		if (minY < 0) minY = 0;
		if (maxY >= bhapi_sections.length) maxY = bhapi_sections.length - 1;
		for (int sectionY = minY; sectionY <= maxY; ++sectionY) {
			ChunkSection section = bhapi_sections[sectionY];
			if (section == null) continue;
			list.addAll(section.entities.stream().filter(
				e -> entityClass.isAssignableFrom(e.getClass()) && e.boundingBox.boxIntersects(area)
			).toList());
		}
	}
	
	@Inject(method = "getBlockEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlockEntity(int x, int y, int z, CallbackInfoReturnable<BaseBlockEntity> info) {
		ChunkSection section = bhapi_getSection(y);
		if (section == null) {
			info.setReturnValue(null);
			return;
		}
		
		BlockPos pos = new BlockPos(x, y & 15, z);
		BaseBlockEntity entity = section.getBlockEntity(pos);
		if (entity == null) {
			int blockID = this.getBlockId(x, y, z);
			if (!BaseBlock.HAS_TILE_ENTITY[blockID]) {
				info.setReturnValue(null);
				return;
			}
			BlockWithEntity blockWithEntity = (BlockWithEntity) BaseBlock.BY_ID[blockID];
			blockWithEntity.onBlockPlaced(this.level, this.x << 4 | x, y, this.z << 4 | z);
			entity = section.getBlockEntity(pos);
		}
		
		if (entity != null && entity.isInvalid()) {
			section.removeBlockEntity(pos);
			info.setReturnValue(null);
			return;
		}
		
		info.setReturnValue(entity);
	}
	
	@Inject(method = "setBlockEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlockEntity(int x, int y, int z, BaseBlockEntity entity, CallbackInfo info) {
		info.cancel();
		
		ChunkSection section = bhapi_getSection(y);
		if (section == null) {
			return;
		}
		
		BlockPos pos = new BlockPos(x, y & 15, z);
		entity.level = this.level;
		entity.x = this.x << 4 | x;
		entity.y = y;
		entity.z = this.z << 4 | z;
		if (this.getBlockId(x, y, z) == 0 || !(BaseBlock.BY_ID[this.getBlockId(x, y, z)] instanceof BlockWithEntity)) {
			BHAPI.log("Attempted to place a tile entity where there was no entity tile!");
			return;
		}
		entity.validate();
		section.setBlockEntity(pos, entity);
	}
	
	@Inject(method = "removeBlockEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_removeBlockEntity(int x, int y, int z, CallbackInfo info) {
		info.cancel();
		
		ChunkSection section = bhapi_getSection(y);
		if (section == null || !this.canHaveBlockEntities) {
			return;
		}
		
		BlockPos pos = new BlockPos(x, y & 15, z);
		BaseBlockEntity entity = section.getBlockEntity(pos);
		if (entity != null) entity.invalidate();
		section.removeBlockEntity(pos);
	}
	
	@Unique
	private ChunkSection bhapi_getSection(int y) {
		byte sectionY = (byte) (y >> 4);
		return sectionY < 0 || sectionY >= bhapi_sections.length ? null : bhapi_sections[sectionY];
	}
	
	@Unique
	private void bhapi_initSections() {
		if (bhapi_sections == null) {
			bhapi_sections = new ChunkSection[8];
			for (byte i = 0; i < bhapi_sections.length; i++) {
				bhapi_sections[i] = new ChunkSection();
			}
		}
	}
	
	@Unique
	private void bhapi_fillBlocks() {
		if (this.blocks != null) {
			for (int i = 0; i < this.blocks.length; i++) {
				int px = (i >> 11) & 15;
				int pz = (i >> 7) & 15;
				int py = i & 127;
				bhapi_sections[py >> 4].setID(px, py & 15, pz, this.blocks[i]);
			}
			this.blocks = null;
		}
	}
	
	@Unique
	private void bhapi_updateHasEntities() {
		this.hasEntities = Arrays.stream(bhapi_sections).anyMatch(section -> !section.entities.isEmpty());
	}
	
	@Unique
	@Override
	public void saveToNBT(CompoundTag tag) {
		tag.put("x", this.x);
		tag.put("z", this.z);
		tag.put("skyLight", this.skyLight.data);
		tag.put("blockLight", this.blockLight.data);
		tag.put("heightmap", this.heightmap);
		tag.put("populated", this.decorated);
		
		ListTag sectionList = new ListTag();
		tag.put("sections", sectionList);
		for (byte i = 0; i < bhapi_sections.length; i++) {
			CompoundTag sectionTag = new CompoundTag();
			sectionTag.put("y", i);
			bhapi_sections[i].saveToNBT(sectionTag);
			sectionList.add(sectionTag);
		}
		
		bhapi_updateHasEntities();
	}
	
	@Unique
	@Override
	public void loadFromNBT(CompoundTag tag) {
		bhapi_initSections();
		
		this.skyLight = new NibbleArray(tag.getByteArray("skyLight"));
		this.blockLight = new NibbleArray(tag.getByteArray("blockLight"));
		this.heightmap = tag.getByteArray("heightmap");
		this.decorated = tag.getBoolean("populated");
		
		if (this.heightmap == null || !this.skyLight.isValid()) {
			this.heightmap = new byte[256];
			this.skyLight = new NibbleArray(32768);
			this.generateHeightmap();
		}
		
		if (!this.blockLight.isValid()) {
			this.blockLight = new NibbleArray(32768);
			this.afterBlockLightReset();
		}
		
		ListTag sectionList = tag.getListTag("sections");
		final int size = sectionList.size();
		for (byte i = 0; i < size; i++) {
			CompoundTag sectionTag = (CompoundTag) sectionList.get(i);
			byte y = sectionTag.getByte("y");
			bhapi_sections[y].loadFromNBT(sectionTag);
			bhapi_sections[y].loadEntities(sectionTag, this.level, this.x, y, this.z, this.canHaveBlockEntities);
		}
		
		bhapi_updateHasEntities();
	}
}
