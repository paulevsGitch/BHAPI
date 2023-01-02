package net.bhapi.mixin.common;

import net.bhapi.BHAPI;
import net.bhapi.block.BHAirBlock;
import net.bhapi.blockstate.BlockState;
import net.bhapi.client.render.level.ClientChunks;
import net.bhapi.interfaces.NBTSerializable;
import net.bhapi.level.BlockStateProvider;
import net.bhapi.level.ChunkHeightProvider;
import net.bhapi.level.ChunkSection;
import net.bhapi.level.ChunkSectionProvider;
import net.bhapi.level.LevelHeightProvider;
import net.bhapi.storage.Vec3I;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BaseBlockEntity;
import net.minecraft.entity.BaseEntity;
import net.minecraft.level.Level;
import net.minecraft.level.LightType;
import net.minecraft.level.chunk.Chunk;
import net.minecraft.level.storage.NibbleArray;
import net.minecraft.util.io.CompoundTag;
import net.minecraft.util.io.ListTag;
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
public abstract class ChunkMixin implements NBTSerializable, LevelHeightProvider, ChunkSectionProvider, BlockStateProvider, ChunkHeightProvider {
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
	@Shadow public boolean canHaveBlockEntities;
	@Shadow public static boolean hasSkyLight;
	
	@Shadow public abstract int getBlockId(int i, int j, int k);
	@Shadow protected abstract void fillSkyLight(int i, int j);
	@Shadow protected abstract void updateSkylight(int i, int j, int k);
	@Shadow public abstract void generateHeightmap();
	
	@Environment(value=EnvType.CLIENT)
	@Shadow public abstract void updateHeightmap();
	
	@Unique private ChunkSection[] bhapi_sections;
	@Unique private final short[] bhapi_heightmap = new short[256];
	@Unique private boolean bhapi_hasSkyLight;
	@Unique private boolean bhapi_skipLight;
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;[BII)V", at = @At("TAIL"))
	private void bhapi_onChunkInit(Level level, byte[] blocks, int x, int z, CallbackInfo info) {
		bhapi_initSections();
		
		if (blocks != null && blocks.length > 0 && blocks[0] != 0) {
			bhapi_fillBlocks();
		}
		
		this.meta = null;
		this.entities = null;
		this.blockEntities = null;
		this.skyLight = null;
		this.blockLight = null;
		this.heightmap = null;
		bhapi_hasSkyLight = !level.dimension.noSkyLight;
	}
	
	@Inject(method = "<init>(Lnet/minecraft/level/Level;II)V", at = @At("TAIL"))
	private void bhapi_onChunkInit(Level level, int x, int z, CallbackInfo info) {
		bhapi_initSections();
		this.meta = null;
		this.entities = null;
		this.blockEntities = null;
		this.skyLight = null;
		this.blockLight = null;
		this.heightmap = null;
		bhapi_hasSkyLight = !level.dimension.noSkyLight;
	}
	
	@Inject(method = "setBlock(IIIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, int meta, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockUtil.getLegacyBlock(id, meta);
		if (state == null) state = BlockUtil.AIR_STATE;
		info.setReturnValue(setBlockState(x & 15, y, z & 15, state));
	}
	
	@Inject(method = "setBlock(IIII)Z", at = @At("HEAD"), cancellable = true)
	private void bhapi_setBlock(int x, int y, int z, int id, CallbackInfoReturnable<Boolean> info) {
		BlockState state = BlockUtil.getLegacyBlock(id, 0);
		if (state == null) state = BlockUtil.AIR_STATE;
		info.setReturnValue(setBlockState(x & 15, y, z & 15, state));
	}
	
	@Inject(method = "setMeta(IIII)V", at = @At("HEAD"), cancellable = true)
	private void bhapi_setMeta(int x, int y, int z, int meta, CallbackInfo info) {
		info.cancel();
		if (y >= getLevelHeight()) {
			return;
		}
		ChunkSection section = bhapi_getOrCreateSection(y, true);
		if (section != null) {
			section.setMeta(x & 15, y & 15, z & 15, meta);
			this.needUpdate = true;
			if (BHAPI.isClient()) {
				bhapi_updateClient(this.x << 4 | x, y, this.z << 4 |z);
			}
		}
	}
	
	@Inject(method = "getBlockId(III)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getBlockId(int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		ChunkSection section = bhapi_getSection(y);
		if (section == null) {
			info.setReturnValue(0);
			return;
		}
		int id = section.getBlockState(x, y & 15, z).getBlock().id;
		if (id == BlockUtil.MOD_BLOCK_ID) id = 0;
		info.setReturnValue(id);
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
		short maxY = getLevelHeight();
		while (bhapi_getSection(maxY) == null && maxY > 0) maxY -= 16;
		this.minHeight = maxY;
		for (byte x = 0; x < 16; ++x) {
			for (byte z = 0; z < 16; ++z) {
				short y;
				for (y = maxY; y > 0; y--) {
					if (getBlockState(x, y, z).getLightOpacity() == 0) break;
				}
				bhapi_setHeight(x, z, y);
				if (y < minHeight) this.minHeight = y;
			}
		}
		this.needUpdate = true;
	}
	
	@Inject(method = "generateHeightmap", at = @At("HEAD"), cancellable = true)
	private void bhapi_generateHeightmap(CallbackInfo info) {
		info.cancel();
		bhapi_fillBlocks();
		
		byte x, z;
		short maxY = (short) (getLevelHeight() - 1);
		while (bhapi_getSection(maxY) == null && maxY > 0) maxY -= 16;
		this.minHeight = maxY;
		for (x = 0; x < 16; ++x) {
			for (z = 0; z < 16; ++z) {
				short y;
				for (y = maxY; y > 0; y--) {
					if (getBlockState(x, y - 1, z).getLightOpacity() > 0) break;
				}
				bhapi_setHeight(x, z, y);
				if (y < minHeight) this.minHeight = y;
				if (this.level.dimension.noSkyLight) continue;
				short light = 15;
				for (short h = maxY; h > y; --h) {
					light -= (short) getBlockState(x, h, z).getLightOpacity();
					if (light < 1) break;
					ChunkSection section = bhapi_getSection(h);
					if (section != null) section.setLight(LightType.SKY, x, h & 15, z, light);
				}
			}
		}
		
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
		
		int h, wz, wx;
		short h1, h2;
		h1 = h2 = (short) Math.max(getHeightmapData(x, z), getLevelHeight() - 1);
		
		if (y > h2) h1 = (short) y;
		
		while (h1 > 0 && getBlockState(x, h1 - 1, z).getLightOpacity() == 0) {
			--h1;
		}
		
		if (h1 == h2) return;
		
		this.level.callColumnAreaEvent(x, z, h1, h2);
		bhapi_setHeight(x, z, h1);
		
		if (h1 < this.minHeight) {
			this.minHeight = h1;
		}
		else {
			int minHeight = getLevelHeight();
			for (wz = 0; wz < 16; ++wz) {
				for (h = 0; h < 16; ++h) {
					short mh = getHeightmapData(wz, h << 4);
					if (mh >= minHeight) continue;
					minHeight = mh;
				}
			}
			this.minHeight = minHeight;
		}
		
		wx = this.x << 4 | x;
		wz = this.z << 4 | z;
		
		if (h1 < h2) {
			for (h = h1; h < h2; ++h) {
				ChunkSection section = bhapi_getSection(h);
				if (section != null) section.setLight(LightType.SKY, x, h & 15, z, 15);
			}
		}
		else {
			this.level.updateLight(LightType.SKY, wx, h2, wz, wx, h1, wz);
			/*for (h = h2; h < h1; ++h) {
				ChunkSection section = bhapi_getSection(h);
				if (section != null) section.setLight(LightType.SKY, x, h & 15, z, 0);
			}*/
		}
		
		h = 15;
		h2 = h1;
		while (h1 > 0 && h > 0) {
			int n8;
			if ((n8 = getBlockState(x, --h1, z).getLightOpacity()) == 0) {
				n8 = 1;
			}
			if ((h -= n8) < 0) {
				h = 0;
			}
			ChunkSection section = bhapi_getSection(h1);
			if (section != null) section.setLight(LightType.SKY, x, h1 & 15, z, h);
		}
		
		while (h1 > 0 && getBlockState(x, h1 - 1, z).getLightOpacity() == 0) {
			--h1;
		}
		
		if (h1 != h2) {
			this.level.updateLight(LightType.SKY, wx - 1, h1, wz - 1, wx + 1, h2, wz + 1);
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
		
		short sectionY = (byte) MathHelper.floor(entity.y / 16.0);
		sectionY = (short) MathUtil.clamp(sectionY, 0, bhapi_sections.length - 1);
		ChunkSection section = bhapi_sections[sectionY];
		if (section == null) {
			section = bhapi_getOrCreateSection(sectionY << 4, true);
			if (section == null) return;
		}
		
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
		Arrays.stream(bhapi_sections).filter(s -> s != null).forEach(section -> {
			this.level.addEntities(section.entities);
			this.level.addBlockEntities(section.getBlockEntities());
		});
	}
	
	@Inject(method = "removeEntitiesFromLevel", at = @At("HEAD"), cancellable = true)
	private void bhapi_removeEntitiesFromLevel(CallbackInfo info) {
		info.cancel();
		this.canHaveBlockEntities = false;
		Arrays.stream(bhapi_sections).filter(s -> s != null).forEach(section -> {
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
		
		Vec3I pos = new Vec3I(x, y & 15, z);
		BaseBlockEntity entity = section.getBlockEntity(pos);
		if (entity == null) {
			BlockState state = getBlockState(x, y, z);
			if (!state.hasBlockEntity() || !(state.getBlock() instanceof BlockWithEntity block)) {
				info.setReturnValue(null);
				return;
			}
			block.onBlockPlaced(this.level, this.x << 4 | x, y, this.z << 4 | z);
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
		
		Vec3I pos = new Vec3I(x, y & 15, z);
		entity.level = this.level;
		entity.x = this.x << 4 | x;
		entity.y = y;
		entity.z = this.z << 4 | z;
		
		BlockState state = getBlockState(x, y, z);
		
		if (!state.hasBlockEntity() || !(state.getBlock() instanceof BlockWithEntity)) {
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
		
		Vec3I pos = new Vec3I(x, y & 15, z);
		BaseBlockEntity entity = section.getBlockEntity(pos);
		if (entity != null) entity.invalidate();
		section.removeBlockEntity(pos);
	}
	
	@Inject(method = "getLight(Lnet/minecraft/level/LightType;III)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getLight(LightType type, int x, int y, int z, CallbackInfoReturnable<Integer> info) {
		if (type == LightType.SKY && y >= getHeightmapData(x, z)) {
			info.setReturnValue(bhapi_hasSkyLight ? 15 : 0);
		}
		else {
			ChunkSection section = bhapi_getSection(y);
			if (section == null) {
				info.setReturnValue(type == LightType.SKY ? (bhapi_hasSkyLight ? 15 : 0) : 0);
			}
			else {
				info.setReturnValue(section.getLight(type, x, y & 15, z));
			}
		}
	}
	
	@Inject(method = "setLight", at = @At("HEAD"), cancellable = true)
	private void bhapi_setLight(LightType type, int x, int y, int z, int value, CallbackInfo info) {
		info.cancel();
		ChunkSection section = bhapi_getOrCreateSection(y, true);
		if (section != null) {
			section.setLight(type, x, y & 15, z, value);
			this.needUpdate = true;
		}
	}
	
	@Inject(method = "getLight(IIII)I", at = @At("HEAD"), cancellable = true)
	private void bhapi_getLight(int x, int y, int z, int value, CallbackInfoReturnable<Integer> info) {
		ChunkSection section = bhapi_getSection(y);
		
		int light = 0;
		if (bhapi_hasSkyLight) {
			if (y >= getHeightmapData(x, z)) {
				light = 15;
			}
			else {
				light = section == null ? 15 : section.getLight(LightType.SKY, x, y & 15, z);
				if (light > 0) hasSkyLight = true;
			}
		}
		
		int blockLight = section == null ? 0 : section.getLight(LightType.BLOCK, x, y & 15, z);
		if (blockLight > (light -= value)) {
			light = blockLight;
		}
		
		info.setReturnValue(light);
	}
	
	@Inject(method = "isAboveGround", at = @At("HEAD"), cancellable = true)
	private void isAboveGround(int x, int y, int z, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(y >= getHeightmapData(x, z));
	}
	
	@Inject(method = "getHeight(II)I", at = @At("HEAD"), cancellable = true)
	private void getHeightmapData(int x, int z, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue((int) getHeightmapData(x, z));
	}
	
	@Environment(value=EnvType.CLIENT)
	@Inject(method = "setClientBlockData", at = @At("HEAD"), cancellable = true)
	private void bhapi_setClientBlockData(byte[] data, int x1, int y1, int z1, int x2, int y2, int z2, int index, CallbackInfoReturnable<Integer> info) {
		for (int y = y1; y < y2; y++) {
			ChunkSection section = bhapi_getOrCreateSection(y, false);
			for (int x = x1; x < x2; x++) {
				for (int z = z1; z < z2; z++) {
					index = section.readData(data, x, y & 15, z, index);
				}
			}
		}
		this.updateHeightmap();
		info.setReturnValue(index);
	}
	
	@Environment(value=EnvType.SERVER)
	@Inject(method = "getServerBlockData", at = @At("HEAD"), cancellable = true)
	private void bhapi_getServerBlockData(byte[] data, int x1, int y1, int z1, int x2, int y2, int z2, int index, CallbackInfoReturnable<Integer> info) {
		for (int y = y1; y < y2; y++) {
			ChunkSection section = bhapi_sections[y >> 4];
			if (section == null) section = ChunkSection.EMPTY;
			for (int x = x1; x < x2; x++) {
				for (int z = z1; z < z2; z++) {
					index = section.writeData(data, x, y & 15, z, index);
				}
			}
		}
		info.setReturnValue(index);
	}
	
	@Environment(EnvType.CLIENT)
	@Inject(method = "markToUpdate", at = @At("HEAD"))
	private void bhapi_markToUpdate(CallbackInfo info) {
		if (bhapi_skipLight) return;
		if (decorated) {
			bhapi_skipLight = true;
			int wx = this.x << 4;
			int wz = this.z << 4;
			for (short i = 0; i < getSectionsCount(); i++) {
				ChunkSection section = getChunkSection(i);
				if (section != null) {
					int wy = i << 4;
					level.updateLight(LightType.BLOCK, wx, wy, wz, wx | 15, wy | 15, wz | 15);
				}
			}
		}
	}
	
	@Unique
	@Override
	public short getHeightmapData(int x, int z) {
		return bhapi_heightmap[x << 4 | z];
	}
	
	@Unique
	private void bhapi_setHeight(int x, int z, short height) {
		bhapi_heightmap[x << 4 | z] = height;
	}
	
	@Unique
	private ChunkSection bhapi_getSection(int y) {
		short sectionY = (short) (y >> 4);
		return sectionY < 0 || sectionY >= bhapi_sections.length ? null : bhapi_sections[sectionY];
	}
	
	@Unique
	private ChunkSection bhapi_getOrCreateSection(int y, boolean fillSkyLight) {
		byte sectionY = (byte) (y >> 4);
		if (sectionY < 0 || sectionY >= bhapi_sections.length) return null;
		ChunkSection section = bhapi_sections[sectionY];
		if (section == null) {
			section = new ChunkSection();
			bhapi_sections[sectionY] = section;
			if (this.bhapi_hasSkyLight && fillSkyLight) {
				short posY = (short) (sectionY << 4);
				for (short i = 0; i < 256; i++) {
					byte x = (byte) (i >> 4);
					byte z = (byte) (i & 15);
					short height = bhapi_heightmap[i];
					short minY = (short) (height - posY);
					minY = (short) MathUtil.clamp(minY, 0, 16);
					for (byte py = (byte) minY; py < 16; py++) {
						section.setLight(LightType.SKY, x, py, z, 15);
					}
				}
			}
		}
		return section;
	}
	
	@Unique
	private void bhapi_initSections() {
		if (bhapi_sections == null) {
			short count = LevelHeightProvider.cast(this.level.dimension).getSectionsCount();
			bhapi_sections = new ChunkSection[count];
		}
	}
	
	@Unique
	private void bhapi_fillBlocks() {
		if (this.blocks != null && this.blocks.length == 32768) {
			for (int i = 0; i < this.blocks.length; i++) {
				if (this.blocks[i] == 0) continue;
				
				int py = i & 127;
				if (py >= getLevelHeight()) continue;
				
				int px = (i >> 11) & 15;
				int pz = (i >> 7) & 15;
				
				short sectionY = (short) (py >> 4);
				ChunkSection section = bhapi_sections[sectionY];
				if (section == null) {
					section = new ChunkSection();
					bhapi_sections[sectionY] = section;
				}
				
				BlockState state = BlockUtil.getLegacyBlock(this.blocks[i], 0);
				if (state == null) continue;
				section.setBlockState(px, py & 15, pz, state);
				
				short sectionY2 = (short) (sectionY + 1);
				if (sectionY2 < bhapi_sections.length) {
					section = bhapi_sections[sectionY2];
					if (section == null) {
						section = new ChunkSection();
						bhapi_sections[sectionY2] = section;
					}
				}
			}
			this.blocks = null;
		}
	}
	
	@Unique
	private void bhapi_updateHasEntities() {
		this.hasEntities = Arrays.stream(bhapi_sections).anyMatch(section -> section != null && !section.entities.isEmpty());
	}
	
	@Unique
	private void bhapi_loadHeightmap(byte[] data) {
		for (int i = 0; i < 256; i++) {
			int i2 = i << 1;
			bhapi_heightmap[i] = (short) ((data[i2 | 1] & 255) << 8 | (data[i2] & 255));
		}
		this.minHeight = bhapi_heightmap[0];
		for (short i = 1; i < 256; i++) {
			if (bhapi_heightmap[i] < this.minHeight) this.minHeight = bhapi_heightmap[i];
		}
	}
	
	@Unique
	private byte[] bhapi_saveHeightmap() {
		byte[] data = new byte[512];
		for (int i = 0; i < 512; i++) {
			int i2 = i >> 1;
			data[i] = (byte) ((i & 1) == 0 ? (bhapi_heightmap[i2] & 255) : ((bhapi_heightmap[i2] >> 8) & 255));
		}
		return data;
	}
	
	@Unique
	@Override
	public void saveToNBT(CompoundTag tag) {
		tag.put("x", this.x);
		tag.put("z", this.z);
		tag.put("heightmap", bhapi_saveHeightmap());
		tag.put("populated", this.decorated);
		
		ListTag sectionList = new ListTag();
		tag.put("sections", sectionList);
		
		for (short i = 0; i < bhapi_sections.length; i++) {
			if (bhapi_sections[i] == null) continue;
			CompoundTag sectionTag = new CompoundTag();
			bhapi_sections[i].saveToNBT(sectionTag);
			if (sectionTag.containsKey("states")) {
				sectionTag.put("y", i);
				sectionList.add(sectionTag);
			}
		}
		
		bhapi_updateHasEntities();
	}
	
	@Unique
	@Override
	public void loadFromNBT(CompoundTag tag) {
		this.decorated = tag.getBoolean("populated");
		
		byte[] heightmap = tag.getByteArray("heightmap");
		if (heightmap == null || heightmap.length != 512) {
			this.generateHeightmap();
		}
		else {
			this.bhapi_loadHeightmap(heightmap);
		}
		
		bhapi_initSections();
		ListTag sectionList = tag.getListTag("sections");
		final int size = sectionList.size();
		for (byte i = 0; i < size; i++) {
			CompoundTag sectionTag = (CompoundTag) sectionList.get(i);
			short y = sectionTag.getShort("y");
			ChunkSection section = new ChunkSection();
			bhapi_sections[y] = section;
			section.loadFromNBT(sectionTag);
			section.loadEntities(sectionTag, this.level, this.x, y, this.z, this.canHaveBlockEntities);
		}
		
		bhapi_updateHasEntities();
	}
	
	@Unique
	@Override
	public short getLevelHeight() {
		return LevelHeightProvider.cast(this.level.dimension).getLevelHeight();
	}
	
	@Unique
	@Override
	public short getSectionsCount() {
		return (short) this.bhapi_sections.length;
	}
	
	@Unique
	@Override
	public ChunkSection[] getChunkSections() {
		return bhapi_sections;
	}
	
	@Environment(EnvType.CLIENT)
	private void bhapi_updateClient(int x, int y, int z) {
		int cx1 = (x - 1) >> 4;
		int cy1 = (y - 1) >> 4;
		int cz1 = (z - 1) >> 4;
		int cx2 = (x + 1) >> 4;
		int cy2 = (y + 1) >> 4;
		int cz2 = (z + 1) >> 4;
		
		short count = (short) (bhapi_sections.length - 1);
		cy1 = MathUtil.clamp(cy1, 0, count);
		cy2 = MathUtil.clamp(cy2, 0, count);
		
		Vec3I pos = new Vec3I();
		for (pos.y = cy1; pos.y <= cy2; pos.y++) {
			for (pos.x = cx1; pos.x <= cx2; pos.x++) {
				for (pos.z = cz1; pos.z <= cz2; pos.z++) {
					ClientChunks.update(pos);
				}
			}
		}
	}
	
	@Unique
	@Override
	public boolean setBlockState(int x, int y, int z, BlockState state, boolean update) {
		if (y < 0 || y >= getLevelHeight()) return false;
		
		ChunkSection section = bhapi_getOrCreateSection(y, true);
		if (section == null) return false;
		
		byte py = (byte) (y & 15);
		
		short height = getHeightmapData(x, z);
		BlockState oldState = section.getBlockState(x, py, z);
		if (oldState == state) return false;
		
		int wx = this.x << 4 | x;
		int wz = this.z << 4 | z;
		
		section.setBlockState(x, py, z, state);
		
		if (BHAPI.isClient()) {
			bhapi_updateClient(wx, y, wz);
		}
		
		if (!update) {
			this.needUpdate = true;
			return true;
		}
		
		if (!(oldState.getBlock() instanceof BHAirBlock) && !this.level.isClientSide) {
			oldState.onBlockRemoved(this.level, wx, y, wz, state);
		}
		
		if (!this.level.dimension.noSkyLight) {
			if (state.getLightOpacity() != 0) {
				if (y >= height) {
					this.updateSkylight(x, y + 1, z);
				}
			}
			else if (y == height - 1) {
				this.updateSkylight(x, y, z);
			}
			this.level.updateLight(LightType.SKY, wx, y, wz, wx, y, wz);
		}
		
		if (this.decorated) {
			int light = this.level.getLight(LightType.BLOCK, wx, y, wz);
			if (light != state.getEmittance()) {
				if (this.decorated) {
					this.level.updateLight(LightType.BLOCK, wx, y, wz, wx, y, wz);
				}
				else {
					int x1 = (wx >> 4) << 4;
					int y1 = (y >> 4) << 4;
					int z1 = (wz >> 4) << 4;
					this.level.updateLight(LightType.BLOCK, x1, y1, z1, x1 + 15, y1 + 15, z1 + 15);
				}
			}
		}
		this.fillSkyLight(x, z);
		
		if (!state.isAir()) {
			state.onBlockPlaced(this.level, wx, y, wz);
		}
		
		this.needUpdate = true;
		return true;
	}
	
	@Unique
	@Override
	public BlockState getBlockState(int x, int y, int z) {
		ChunkSection section = bhapi_getSection(y);
		if (section == null) return BlockUtil.AIR_STATE;
		return section.getBlockState(x, y & 15, z);
	}
	
	@Unique
	@Override
	public ChunkSection getChunkSection(int index) {
		return bhapi_sections[index];
	}
}
