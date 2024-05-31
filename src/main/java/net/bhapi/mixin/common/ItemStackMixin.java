package net.bhapi.mixin.common;

import net.bhapi.blockstate.BlockState;
import net.bhapi.item.BHBlockItem;
import net.bhapi.item.ItemProvider;
import net.bhapi.registry.CommonRegistries;
import net.bhapi.util.BlockUtil;
import net.bhapi.util.Identifier;
import net.bhapi.util.ItemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.LivingEntity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.level.Level;
import net.minecraft.stat.Stats;
import net.minecraft.util.io.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements ItemProvider {
	@Shadow public int count;
	@Shadow private int damage;
	@Shadow public int itemId;
	
	@Shadow public abstract Item getType();
	@Shadow public abstract boolean hasDurability();
	@Shadow public abstract int getDurability();
	
	@Shadow public int cooldown;
	@Unique private Item bhapi_item;
	
	@Inject(method = "<init>(Lnet/minecraft/block/Block;)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit1(Block block, CallbackInfo info) {
		bhapi_processBlockItem(block);
	}
	
	@Inject(method = "<init>(Lnet/minecraft/block/Block;I)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit2(Block block, int count, CallbackInfo info) {
		bhapi_processBlockItem(block);
	}
	
	@Inject(method = "<init>(Lnet/minecraft/block/Block;II)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit3(Block block, int count, int damage, CallbackInfo info) {
		if (this.bhapi_item != null) return;
		if (!ItemUtil.isFrozen()) {
			ItemUtil.addStackForPostProcessing(BlockState.getDefaultState(block), ItemStack.class.cast(this));
			return;
		}
		BlockState state = block.id != BlockUtil.MOD_BLOCK_ID ? BlockUtil.getLegacyBlock(block.id, damage) : BlockState.getDefaultState(block);
		this.bhapi_item = BHBlockItem.get(state);
	}
	
	@Inject(method = "<init>(Lnet/minecraft/item/Item;)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit4(Item item, CallbackInfo info) {
		this.bhapi_item = item;
	}
	
	@Inject(method = "<init>(Lnet/minecraft/item/Item;I)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit5(Item item, int count, CallbackInfo info) {
		this.bhapi_item = item;
	}
	
	@Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit6(Item item, int count, int damage, CallbackInfo info) {
		this.bhapi_item = item;
	}
	
	@Inject(method = "<init>(III)V", at = @At("TAIL"))
	private void bhapi_onItemStackInit7(int id, int count, int damage, CallbackInfo info) {
		if (this.bhapi_item != null) return;
		if (this.itemId < 2002) {
			if (id < 256) {
				if (id != BlockUtil.MOD_BLOCK_ID) {
					BlockState state = BlockUtil.getLegacyBlock(id, damage);
					if (!ItemUtil.isFrozen()) {
						ItemUtil.addStackForPostProcessing(state, ItemStack.class.cast(this));
						return;
					}
					this.bhapi_item = BHBlockItem.get(state);
					if (this.bhapi_item == null) {
						ItemUtil.addStackForPostProcessing(state, ItemStack.class.cast(this));
						return;
					}
				}
				else return;
			}
			else this.bhapi_item = Item.byId[id];
			if (this.bhapi_item == null) {
				this.itemId = 256;
				this.count = 0;
			}
		}
	}
	
	@Inject(method = "split", at = @At("HEAD"), cancellable = true)
	private void bhapi_split(int amount, CallbackInfoReturnable<ItemStack> info) {
		this.count -= amount;
		info.setReturnValue(new ItemStack(getType(), amount, this.damage));
	}
	
	@Inject(method = "getType", at = @At("HEAD"), cancellable = true)
	private void bhapi_getType(CallbackInfoReturnable<Item> info) {
		if (this.bhapi_item == null) {
			info.setReturnValue(Item.ironShovel);
		}
		else info.setReturnValue(this.bhapi_item);
	}
	
	@Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
	private void bhapi_useOnBlock(PlayerEntity arg, Level level, int x, int y, int z, int facing, CallbackInfoReturnable<Boolean> info) {
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		boolean result = type.useOnBlock(stack, arg, level, x, y, z, facing);
		if (result) {
			arg.increaseStat(Stats.useItem[type.id], 1);
		}
		info.setReturnValue(result);
	}
	
	@Inject(method = "toTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_toTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info) {
		Item item = getType();
		Identifier id = CommonRegistries.ITEM_REGISTRY.getID(item);
		if (id == null) {
			if (item instanceof BHBlockItem) {
				id = CommonRegistries.BLOCK_REGISTRY.getID(BHBlockItem.cast(item).getState().getBlock());
				item = CommonRegistries.ITEM_REGISTRY.get(id);
				bhapi_setItem(item);
			}
			if (id == null) count = 0;
		}
		if (id != null) tag.put("item", id.toString());
		tag.put("count", (byte) this.count);
		tag.put("damage", (short) this.damage);
		info.setReturnValue(tag);
	}
	
	@Inject(method = "fromTag", at = @At("HEAD"), cancellable = true)
	private void bhapi_fromTag(CompoundTag arg, CallbackInfo info) {
		info.cancel();
		this.bhapi_item = CommonRegistries.ITEM_REGISTRY.get(Identifier.make(arg.getString("item")));
		this.count = arg.getByte("count");
		this.damage = arg.getShort("damage");
		if (this.bhapi_item == null) count = 0;
	}
	
	@Inject(method = "hasDurability", at = @At("HEAD"), cancellable = true)
	private void bhapi_hasDurability(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.getType().getDurability() > 0);
	}
	
	@Inject(method = "usesMeta", at = @At("HEAD"), cancellable = true)
	private void bhapi_usesMeta(CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.getType().usesMeta());
	}
	
	@Inject(method = "getDurability", at = @At("HEAD"), cancellable = true)
	private void bhapi_getDurability(CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(this.getType().getDurability());
	}
	
	@Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
	private void applyDamage(int damage, Entity entity, CallbackInfo info) {
		info.cancel();
		if (!this.hasDurability()) return;
		this.damage += damage;
		if (this.damage > this.getDurability()) {
			if (entity instanceof PlayerEntity) {
				Item type = this.getType();
				((PlayerEntity)entity).increaseStat(Stats.breakItem[type.id], 1);
			}
			--this.count;
			if (this.count < 0) this.count = 0;
			this.damage = 0;
		}
	}
	
	@Inject(method = "postHit", at = @At("HEAD"), cancellable = true)
	private void bhapi_postHit(LivingEntity entity, PlayerEntity arg2, CallbackInfo info) {
		info.cancel();
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		if (type.postHit(stack, entity, arg2)) {
			arg2.increaseStat(Stats.useItem[type.id], 1);
		}
	}
	
	@Inject(method = "postMine", at = @At("HEAD"), cancellable = true)
	private void bhapi_postMine(int i, int j, int k, int l, PlayerEntity arg, CallbackInfo info) {
		info.cancel();
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		if (type.postMine(stack, i, j, k, l, arg)) {
			arg.increaseStat(Stats.useItem[type.id], 1);
		}
	}
	
	@Inject(method = "getAttack", at = @At("HEAD"), cancellable = true)
	private void bhapi_getAttack(Entity entity, CallbackInfoReturnable<Integer> info) {
		info.setReturnValue(this.getType().getAttackDamage(entity));
	}
	
	@Inject(method = "isEffectiveOn", at = @At("HEAD"), cancellable = true)
	private void bhapi_isEffectiveOn(Block block, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.getType().isEffectiveOn(block));
	}
	
	@Inject(method = "interactWithEntity", at = @At("HEAD"), cancellable = true)
	private void bhapi_interactWithEntity(LivingEntity entity, CallbackInfo info) {
		info.cancel();
		ItemStack stack = ItemStack.class.cast(this);
		this.getType().interactWithEntity(stack, entity);
	}
	
	@Inject(method = "copy", at = @At("HEAD"), cancellable = true)
	private void bhapi_copy(CallbackInfoReturnable<ItemStack> info) {
		info.setReturnValue(new ItemStack(this.getType(), this.count, this.damage));
	}
	
	@Inject(method = "isStackIdentical", at = @At("HEAD"), cancellable = true)
	private void bhapi_isStackIdentical(ItemStack arg, CallbackInfoReturnable<Boolean> info) {
		if (this.count != arg.count) {
			info.setReturnValue(false);
			return;
		}
		if (this.damage != arg.getDamage()) {
			info.setReturnValue(false);
			return;
		}
		info.setReturnValue(this.getType() == arg.getType());
	}
	
	@Inject(method = "isDamageAndIDIdentical", at = @At("HEAD"), cancellable = true)
	private void bhapi_isDamageAndIDIdentical(ItemStack arg, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.damage == arg.getDamage() && this.getType() == arg.getType());
	}
	
	@Environment(value= EnvType.CLIENT)
	@Inject(method = "getTranslationKey", at = @At("HEAD"), cancellable = true)
	private void bhapi_getTranslationKey(CallbackInfoReturnable<String> info) {
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		info.setReturnValue(type.getTranslationKey(stack));
	}
	
	@Inject(method = "toString", at = @At("HEAD"), cancellable = true)
	private void bhapi_toString(CallbackInfoReturnable<String> info) {
		info.setReturnValue(this.count + "x" + this.getType().getTranslationKey() + "@" + this.damage);
	}
	
	@Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
	private void bhapi_inventoryTick(Level level, Entity entity, int i, boolean bl, CallbackInfo info) {
		info.cancel();
		if (this.cooldown > 0) --this.cooldown;
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		type.inventoryTick(stack, level, entity, i, bl);
	}
	
	@Inject(method = "onCrafted", at = @At("HEAD"), cancellable = true)
	private void bhapi_onCrafted(Level level, PlayerEntity player, CallbackInfo info) {
		info.cancel();
		ItemStack stack = ItemStack.class.cast(this);
		Item type = this.getType();
		player.increaseStat(Stats.timesCrafted[type.id], this.count);
		type.onCreation(stack, level, player);
	}
	
	@Inject(method = "isStackIdentical2", at = @At("HEAD"), cancellable = true)
	private void bhapi_isStackIdentical2(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
		info.setReturnValue(this.count == stack.count && this.damage == stack.getDamage() && this.getType() == stack.getType());
	}
	
	@Unique
	private void bhapi_processBlockItem(Block block) {
		if (this.bhapi_item != null) return;
		if (!ItemUtil.isFrozen()) {
			ItemUtil.addStackForPostProcessing(BlockState.getDefaultState(block), ItemStack.class.cast(this));
			return;
		}
		BlockState state = BlockState.getDefaultState(block);
		this.bhapi_item = BHBlockItem.get(state);
	}
	
	@Unique
	@Override
	public Item bhapi_getItem() {
		return this.bhapi_item;
	}
	
	@Unique
	@Override
	public void bhapi_setItem(Item item) {
		this.bhapi_item = item;
	}
}
