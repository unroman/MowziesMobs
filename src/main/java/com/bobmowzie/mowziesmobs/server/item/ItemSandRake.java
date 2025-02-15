package com.bobmowzie.mowziesmobs.server.item;

import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.block.RakedSandBlock;
import com.bobmowzie.mowziesmobs.server.sound.MMSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;

public class ItemSandRake extends Item {
    public ItemSandRake(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isValidRepairItem(ItemStack thisStack, ItemStack ingredientStack) {
        return ingredientStack.is(ItemTags.PLANKS) || super.isValidRepairItem(thisStack, ingredientStack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        } else {
            Player player = context.getPlayer();
            if (player != null) {
                BlockPlaceContext blockPlaceContext = new BlockPlaceContext(player, context.getHand(), context.getItemInHand(), new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside()));
                RakedSandBlock origBlock = null;
                if (blockstate.is(Tags.Blocks.SAND_COLORLESS)) {
                    origBlock = BlockHandler.RAKED_SAND.get();
                }
                else if (blockstate.is(Tags.Blocks.SAND_RED)) {
                    origBlock = BlockHandler.RED_RAKED_SAND.get();
                }

                if (origBlock != null) {
                    BlockState blockState = origBlock.getStateForPlacement(blockPlaceContext);
                    if (blockState != null) {
                        level.playSound(player, blockpos, MMSounds.BLOCK_RAKE_SAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!level.isClientSide) {
                            level.setBlock(blockpos, blockState, 11);
                            origBlock.onPlace(blockState, level, blockpos, blockstate, false);
                            origBlock.updateState(blockState, level, blockpos, false);
                            context.getItemInHand().hurtAndBreak(1, player, (p_43122_) -> {
                                p_43122_.broadcastBreakEvent(context.getHand());
                            });
                        }
                    }

                    return InteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(new TranslatableComponent(getDescriptionId() + ".text.0").setStyle(ItemHandler.TOOLTIP_STYLE));
    }
}
