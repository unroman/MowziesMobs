package com.bobmowzie.mowziesmobs.server.world.feature.structure;

import com.bobmowzie.mowziesmobs.MowziesMobs;
import com.bobmowzie.mowziesmobs.server.block.BlockHandler;
import com.bobmowzie.mowziesmobs.server.entity.EntityHandler;
import com.bobmowzie.mowziesmobs.server.entity.barakoa.EntityBarako;
import com.bobmowzie.mowziesmobs.server.entity.barakoa.EntityBarakoaVillager;
import com.bobmowzie.mowziesmobs.server.entity.barakoa.MaskType;
import com.bobmowzie.mowziesmobs.server.item.ItemBarakoaMask;
import com.bobmowzie.mowziesmobs.server.item.ItemHandler;
import com.bobmowzie.mowziesmobs.server.loot.LootTableHandler;
import com.bobmowzie.mowziesmobs.server.world.feature.FeatureHandler;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec2;

import java.util.*;

public class BarakoaVillagePieces {
    private static final Set<Block> BLOCKS_NEEDING_POSTPROCESSING = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).add(Blocks.SKELETON_SKULL).build();

    public static final ResourceLocation HOUSE = new ResourceLocation(MowziesMobs.MODID, "barakoa_house");
    public static final ResourceLocation ROOF = new ResourceLocation(MowziesMobs.MODID, "barakoa_house_roof");
    public static final ResourceLocation HOUSE_SIDE = new ResourceLocation(MowziesMobs.MODID, "barakoa_house_side");
    public static final ResourceLocation THRONE = new ResourceLocation(MowziesMobs.MODID, "barako_throne");

    private static final Map<ResourceLocation, BlockPos> OFFSET = ImmutableMap.of(
        HOUSE, new BlockPos(-3, 1, -3),
        ROOF, new BlockPos(-3, 5, -3),
        HOUSE_SIDE, new BlockPos(2, 1, -2),
        THRONE, new BlockPos(-3, 0, 0)
    );

    public static StructurePiece addPiece(ResourceLocation resourceLocation, StructureManager manager, BlockPos pos, Rotation rot, StructurePieceAccessor pieces, WorldgenRandom rand) {
        StructurePiece newPiece = new BarakoaVillagePieces.Piece(manager, resourceLocation, rot, pos);
        pieces.addPiece(newPiece);
        return newPiece;
    }

    public static StructurePiece addPieceCheckBounds(ResourceLocation resourceLocation, StructureManager manager, BlockPos pos, Rotation rot, StructurePieceAccessor pieces, WorldgenRandom rand, List<StructurePiece> ignore) {
        BarakoaVillagePieces.Piece newPiece = new BarakoaVillagePieces.Piece(manager, resourceLocation, rot, pos);
        StructurePiece collisionPiece = pieces.findCollisionPiece(newPiece.getBoundingBox());
        if (!ignore.contains(collisionPiece)) return null;
        pieces.addPiece(newPiece);
        return newPiece;
    }

    public static StructurePiece addHouse(StructureManager manager, BlockPos pos, Rotation rot, StructurePiecesBuilder builder, WorldgenRandom rand) {
        BarakoaVillagePieces.HousePiece newPiece = new BarakoaVillagePieces.HousePiece(manager, HOUSE, rot, pos);
        if (builder.findCollisionPiece(newPiece.getBoundingBox()) != null) return null;
        builder.addPiece(newPiece);
        newPiece.tableCorner = rand.nextInt(6);
        newPiece.tableContent = rand.nextInt(4);
        newPiece.bedCorner = rand.nextInt(6);
        newPiece.bedDirection = rand.nextInt(2);
        newPiece.chestCorner = rand.nextInt(6);
        newPiece.chestDirection = rand.nextInt(2);
        return newPiece;
    }

    public static StructurePiece addPieceCheckBounds(ResourceLocation resourceLocation, StructureManager manager, BlockPos pos, Rotation rot, StructurePiecesBuilder pieces, WorldgenRandom rand) {
       return addPieceCheckBounds(resourceLocation, manager, pos, rot, pieces, rand, Collections.emptyList());
    }

    public static class Piece extends TemplateStructurePiece {
        protected ResourceLocation resourceLocation;

        public Piece(StructurePieceType pieceType, StructureManager manager, ResourceLocation resourceLocationIn, Rotation rotation, BlockPos pos) {
            super(pieceType, 0, manager, resourceLocationIn, resourceLocationIn.toString(), makeSettings(rotation, resourceLocationIn), makePosition(resourceLocationIn, pos, rotation));
            this.resourceLocation = resourceLocationIn;
        }

        public Piece(StructurePieceType pieceType, StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super(pieceType, tagCompound, context.structureManager(), (resourceLocation) -> makeSettings(Rotation.valueOf(tagCompound.getString("Rot")), resourceLocation));
        }

        public Piece(StructureManager manager, ResourceLocation resourceLocationIn, Rotation rotation, BlockPos pos) {
            this(FeatureHandler.BARAKOA_VILLAGE_PIECE, manager, resourceLocationIn, rotation, pos);
        }

        public Piece(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            this(FeatureHandler.BARAKOA_VILLAGE_PIECE, context, tagCompound);
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation, ResourceLocation resourceLocation) {
            return (new StructurePlaceSettings()).setRotation(rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        private static BlockPos makePosition(ResourceLocation resourceLocation, BlockPos pos, Rotation rotation) {
            return pos.offset(BarakoaVillagePieces.OFFSET.get(resourceLocation).rotate(rotation));
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super.addAdditionalSaveData(context, tagCompound);
            tagCompound.putString("Rot", this.placeSettings.getRotation().name());
        }

        /*
         * If you added any data marker structure blocks to your structure, you can access and modify them here. In this case,
         * our structure has a data maker with the string "chest" put into it. So we check to see if the incoming function is
         * "chest" and if it is, we now have that exact position.
         *
         * So what is done here is we replace the structure block with a chest and we can then set the loottable for it.
         *
         * You can set other data markers to do other behaviors such as spawn a random mob in a certain spot, randomize what
         * rare block spawns under the floor, or what item an Item Frame will have.
         */
        @Override
        protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand, BoundingBox sbb) {
            Rotation rotation = this.placeSettings.getRotation();
            if (function.equals("support")) {
                worldIn.setBlock(pos, Blocks.OAK_FENCE.defaultBlockState(), 3);
                fillAirLiquidDown(worldIn, Blocks.OAK_FENCE.defaultBlockState(), pos.below());
            }
            else if (function.equals("leg")) {
                worldIn.setBlock(pos, Blocks.ACACIA_LOG.defaultBlockState(), 3);
                fillAirLiquidDown(worldIn, Blocks.ACACIA_LOG.defaultBlockState(), pos.below());
            }
            else if (function.equals("stairs")) {
                Direction stairDirection = Direction.EAST;
                stairDirection = rotation.rotate(stairDirection);
                setBlockState(worldIn, pos.relative(Direction.UP, 1), Blocks.AIR.defaultBlockState());
                setBlockState(worldIn, pos.relative(Direction.UP, 2), Blocks.AIR.defaultBlockState());
                setBlockState(worldIn, pos, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, stairDirection.getOpposite()));
                pos = pos.relative(Direction.DOWN);
                setBlockState(worldIn, pos, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, stairDirection).setValue(StairBlock.HALF, Half.TOP));
                for (int i = 1; i < 20; i++) {
                    pos = pos.relative(stairDirection);
                    if (!Block.canSupportRigidBlock(worldIn, pos)) {
                        setBlockState(worldIn, pos, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, stairDirection.getOpposite()));
                        pos = pos.relative(Direction.DOWN);
                        setBlockState(worldIn, pos, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, stairDirection).setValue(StairBlock.HALF, Half.TOP));
                    } else {
                        break;
                    }
                }
            }
            else if (function.equals("barako")) {
                setBlockState(worldIn, pos, Blocks.AIR.defaultBlockState());
                EntityBarako barako = new EntityBarako(EntityHandler.BARAKO.get(), worldIn.getLevel());
                barako.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                int i = rotation.rotate(3, 4);
                barako.setDirection(i);
                barako.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(barako.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                BlockPos offset = new BlockPos(0, 0, -18);
                offset = offset.rotate(rotation);
                BlockPos firePitPos = pos.offset(offset);
                firePitPos = worldIn.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, firePitPos);
                barako.restrictTo(firePitPos, -1);
                worldIn.addFreshEntity(barako);
            }
            else {
                worldIn.removeBlock(pos, false);
            }
        }

        protected void setBlockState(LevelAccessor worldIn, BlockPos pos, BlockState state) {
            FluidState ifluidstate = worldIn.getFluidState(pos);
            if (!ifluidstate.isEmpty()) {
                worldIn.getFluidTicks().willTickThisTick(pos, ifluidstate.getType());
                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            worldIn.setBlock(pos, state, 2);
            if (BLOCKS_NEEDING_POSTPROCESSING.contains(state.getBlock())) {
                worldIn.getChunk(pos).markPosForPostprocessing(pos);
            }
        }

        public void fillAirLiquidDown(LevelAccessor worldIn, BlockState state, BlockPos startPos) {
            int i = startPos.getX();
            int j = startPos.getY();
            int k = startPos.getZ();
            while(!Block.canSupportRigidBlock(worldIn, new BlockPos(i, j, k)) && j > 1) {
                BlockPos pos = new BlockPos(i, j, k);
                setBlockState(worldIn, pos, state);
                --j;
            }
        }
    }

    public static class HousePiece extends Piece {
        private int tableCorner;
        private int tableContent;
        private int bedCorner;
        private int bedDirection;
        private int chestCorner;
        private int chestDirection;

        public HousePiece(StructureManager manager, ResourceLocation resourceLocationIn, Rotation rotation, BlockPos pos) {
            super(FeatureHandler.BARAKOA_VILLAGE_HOUSE, manager, resourceLocationIn, rotation, pos);
        }

        public HousePiece(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super(FeatureHandler.BARAKOA_VILLAGE_HOUSE, context, tagCompound);
            tableCorner = tagCompound.getInt("TableCorner");
            tableContent = tagCompound.getInt("TableContent");
            bedCorner = tagCompound.getInt("BedCorner");
            bedDirection = tagCompound.getInt("bedDirection");
            chestCorner = tagCompound.getInt("ChestCorner");
            chestDirection = tagCompound.getInt("ChestDirection");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super.addAdditionalSaveData(context, tagCompound);
            tagCompound.putInt("TableCorner", tableCorner);
            tagCompound.putInt("TableContent", tableContent);
            tagCompound.putInt("BedCorner", bedCorner);
            tagCompound.putInt("bedDirection", bedDirection);
            tagCompound.putInt("ChestCorner", chestCorner);
            tagCompound.putInt("ChestDirection", chestDirection);
        }

        @Override
        protected void handleDataMarker(String function, BlockPos pos, ServerLevelAccessor worldIn, Random rand, BoundingBox sbb) {
            Rotation rotation = this.placeSettings.getRotation();
            if ("corner".equals(function.substring(0, function.length() - 1))) {
                worldIn.removeBlock(pos, false);
                pos = pos.below();
                int whichCorner = Integer.parseInt(function.substring(function.length() - 1));
                Rotation cornerRotation = Rotation.values()[4 - whichCorner];
                if (whichCorner == tableCorner) {
                    setBlockState(worldIn, pos, Blocks.ACACIA_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                    if (tableContent <= 1) {
                        setBlockState(worldIn, pos.above(), Blocks.TORCH.defaultBlockState());
                    }
                    else if (tableContent == 2) {
                        int skullRot = cornerRotation.rotate(rotation.rotate(2, 16), 16);
                        setBlockState(worldIn, pos.above(), Blocks.SKELETON_SKULL.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, skullRot));
                    }
                }
                else if (whichCorner == bedCorner) {
                    setBlockState(worldIn, pos, Blocks.YELLOW_CARPET.defaultBlockState());
                    BlockPos offset = new BlockPos(1, 0, 0);
                    if (bedDirection == 1) offset = new BlockPos(0, 0, -1);
                    offset = offset.rotate(rotation);
                    offset = offset.rotate(cornerRotation);
                    setBlockState(worldIn, pos.offset(offset), Blocks.YELLOW_CARPET.defaultBlockState());
                }
                else if (whichCorner == chestCorner) {
                    Direction facing = Direction.NORTH;
                    if (chestDirection == 1) facing = Direction.EAST;
                    facing = rotation.rotate(facing);
                    facing = cornerRotation.rotate(facing);
                    createChest(worldIn, sbb, rand, pos, LootTableHandler.BARAKOA_VILLAGE_HOUSE, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
                }
                else worldIn.removeBlock(pos, false);
            }
            else if ("mask".equals(function)) {
                worldIn.removeBlock(pos, false);
                ItemFrame itemFrame = new ItemFrame(worldIn.getLevel(), pos, rotation.rotate(Direction.EAST));
                int i = rand.nextInt(MaskType.values().length);
                MaskType type = MaskType.values()[i];
                ItemBarakoaMask mask = ItemHandler.BARAKOA_MASK_FURY;
                switch (type) {
                    case BLISS:
                        mask = ItemHandler.BARAKOA_MASK_BLISS;
                        break;
                    case FEAR:
                        mask = ItemHandler.BARAKOA_MASK_FEAR;
                        break;
                    case FURY:
                        mask = ItemHandler.BARAKOA_MASK_FURY;
                        break;
                    case MISERY:
                        mask = ItemHandler.BARAKOA_MASK_MISERY;
                        break;
                    case RAGE:
                        mask = ItemHandler.BARAKOA_MASK_RAGE;
                        break;
                    case FAITH:
                        mask = ItemHandler.BARAKOA_MASK_FAITH;
                        break;
                }
                ItemStack stack = new ItemStack(mask);
                itemFrame.setItem(stack, false);
                worldIn.addFreshEntity(itemFrame);
            }
            else {
                super.handleDataMarker(function, pos, worldIn, rand, sbb);
            }
        }
    }

    public abstract static class NonTemplatePiece extends ScatteredFeaturePiece {

        protected NonTemplatePiece(StructurePieceType structurePieceTypeIn, int xIn, int yIn, int zIn, int widthIn, int heightIn, int depthIn, Direction direction) {
            super(structurePieceTypeIn, xIn, yIn, zIn, widthIn, heightIn, depthIn, direction);
        }

        protected NonTemplatePiece(StructurePieceType structurePieceTypeIn, CompoundTag nbt) {
            super(structurePieceTypeIn, nbt);
        }

        public BlockPos findGround(LevelAccessor worldIn, int x, int z) {
            int i = this.getWorldX(x, z);
            int k = this.getWorldZ(x, z);
            int j = worldIn.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, i, k);
            return new BlockPos(i, j, k);
        }

        public void fillAirLiquidBelowHeightmap(LevelAccessor worldIn, BlockState state, int x, int z) {
            int i = this.getWorldX(x, z);
            int k = this.getWorldZ(x, z);
            int j = worldIn.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, i, k) - 2;
            while(!Block.canSupportRigidBlock(worldIn, new BlockPos(i, j, k)) && j > 1) {
                BlockPos pos = new BlockPos(i, j, k);
                FluidState ifluidstate = worldIn.getFluidState(pos);
                BlockState toPut = state;
                if (!ifluidstate.isEmpty()) {
                    worldIn.getFluidTicks().willTickThisTick(pos, ifluidstate.getType());
                    if (toPut.hasProperty(BlockStateProperties.WATERLOGGED)) toPut = toPut.setValue(BlockStateProperties.WATERLOGGED, true);
                }
                worldIn.setBlock(pos, toPut, 2);
                if (BLOCKS_NEEDING_POSTPROCESSING.contains(state.getBlock())) {
                    worldIn.getChunk(pos).markPosForPostprocessing(pos);
                }
                --j;
            }
        }

        protected void setBlockState(LevelAccessor worldIn, BlockPos pos, BlockState state) {
            FluidState ifluidstate = worldIn.getFluidState(pos);
            if (!ifluidstate.isEmpty()) {
                worldIn.getFluidTicks().willTickThisTick(pos, ifluidstate.getType());
                if (state.hasProperty(BlockStateProperties.WATERLOGGED)) state = state.setValue(BlockStateProperties.WATERLOGGED, true);
            }
            worldIn.setBlock(pos, state, 2);
            if (BLOCKS_NEEDING_POSTPROCESSING.contains(state.getBlock())) {
                worldIn.getChunk(pos).markPosForPostprocessing(pos);
            }
        }
    }

    public static class FirepitPiece extends NonTemplatePiece {

        public FirepitPiece(Random random, int x, int z) {
            super(FeatureHandler.BARAKOA_VILLAGE_FIREPIT, x, 64, z, 9, 3, 9, Direction.NORTH);
        }

        public FirepitPiece(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super(FeatureHandler.BARAKOA_VILLAGE_FIREPIT, tagCompound);
        }

        @Override
        public void postProcess(WorldGenLevel worldIn, StructureFeatureManager structureManager, ChunkGenerator chunkGenerator, Random randomIn, BoundingBox p_230383_5_, ChunkPos p_230383_6_, BlockPos p_230383_7_) {
            BlockPos centerPos = findGround(worldIn, 4, 4);
            worldIn.setBlock(centerPos, Blocks.CAMPFIRE.defaultBlockState(), 2);
            fillAirLiquidBelowHeightmap(worldIn, Blocks.ACACIA_LOG.defaultBlockState(), 4, 4);
            Vec2[] positions = new Vec2[] {
                    new Vec2(0, 3),
                    new Vec2(0, 5),
                    new Vec2(8, 3),
                    new Vec2(8, 5),
                    new Vec2(3, 0),
                    new Vec2(5, 0),
                    new Vec2(3, 8),
                    new Vec2(5, 8),
                    new Vec2(1, 1),
                    new Vec2(1, 7),
                    new Vec2(7, 1),
                    new Vec2(7, 7),
            };
            for (Vec2 pos : positions) {
                worldIn.setBlock(findGround(worldIn, (int) pos.x, (int) pos.y), Blocks.ACACIA_LOG.defaultBlockState(), 2);
                fillAirLiquidBelowHeightmap(worldIn, Blocks.ACACIA_LOG.defaultBlockState(), (int) pos.x, (int) pos.y);
            }

            // Spawn Barakoa
            int numBarakoa = randomIn.nextInt(5) + 5;
            for (int i = 1; i <= numBarakoa; i++) {
                int distance;
                int angle;
                EntityBarakoaVillager barakoa = new EntityBarakoaVillager(EntityHandler.BARAKOA_VILLAGER.get(), worldIn.getLevel());
                for (int j = 1; j <= 20; j++) {
                    distance = randomIn.nextInt(10) + 2;
                    angle = randomIn.nextInt(360);
                    int x = (int) (distance * Math.sin(Math.toRadians(angle))) + 4;
                    int z = (int) (distance * Math.cos(Math.toRadians(angle))) + 4;
                    BlockPos bPos = findGround(worldIn, x, z);
                    barakoa.setPos(bPos.getX(), bPos.getY(), bPos.getZ());
                    if (bPos.getY() > 0 && barakoa.checkSpawnRules(worldIn, MobSpawnType.STRUCTURE) && worldIn.noCollision(barakoa.getBoundingBox())) {
                        barakoa.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(barakoa.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                        barakoa.restrictTo(centerPos, 25);
                        worldIn.addFreshEntity(barakoa);
                        break;
                    }
                }
            }
        }
    }

    public static class StakePiece extends NonTemplatePiece {
        private final boolean skull;
        private final int skullDir;

        public StakePiece(Random random, int x, int y, int z) {
            super(FeatureHandler.BARAKOA_VILLAGE_STAKE, x, y, z, 1, 3, 1, Direction.NORTH);
            skull = random.nextBoolean();
            skullDir = random.nextInt(16);
        }

        public StakePiece(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super(FeatureHandler.BARAKOA_VILLAGE_STAKE, tagCompound);
            skull = tagCompound.getBoolean("Skull");
            skullDir = tagCompound.getInt("SkullDir");
        }

        /**
         * (abstract) Helper method to read subclass data from NBT
         */
        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super.addAdditionalSaveData(context, tagCompound);
            tagCompound.putBoolean("Skull", skull);
            tagCompound.putInt("SkullDir", skullDir);
        }

        @Override
        public void postProcess(WorldGenLevel worldIn, StructureFeatureManager p_230383_2_, ChunkGenerator p_230383_3_, Random p_230383_4_, BoundingBox mutableBoundingBoxIn, ChunkPos p_230383_6_, BlockPos p_230383_7_) {
            placeBlock(worldIn, Blocks.OAK_FENCE.defaultBlockState(), 0, 1, 0, mutableBoundingBoxIn);
            if (skull) {
                placeBlock(worldIn, Blocks.SKELETON_SKULL.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, skullDir), 0, 2, 0, mutableBoundingBoxIn);
            }
            else {
                placeBlock(worldIn, Blocks.TORCH.defaultBlockState(), 0, 2, 0, mutableBoundingBoxIn);
            }
            fillAirLiquidBelowHeightmap(worldIn, Blocks.OAK_FENCE.defaultBlockState(), 0, 0);
        }
    }

    public static class AltarPiece extends NonTemplatePiece {
        public AltarPiece(int x, int y, int z, Direction direction) {
            super(FeatureHandler.BARAKOA_VILLAGE_ALTAR, x - 2, y - 1, z - 2, 5, 4, 5, direction);
        }

        public AltarPiece(StructurePieceSerializationContext context, CompoundTag tagCompound) {
            super(FeatureHandler.BARAKOA_VILLAGE_STAKE, tagCompound);
        }

        @Override
        public void postProcess(WorldGenLevel worldIn, StructureFeatureManager p_230383_2_, ChunkGenerator p_230383_3_, Random randomIn, BoundingBox p_230383_5_, ChunkPos p_230383_6_, BlockPos p_230383_7_) {
            Vec2[] thatchPositions = new Vec2[] {
                    new Vec2(0, 1),
                    new Vec2(0, 2),
                    new Vec2(0, 3),
                    new Vec2(1, 0),
                    new Vec2(1, 1),
                    new Vec2(1, 2),
                    new Vec2(1, 3),
                    new Vec2(1, 4),
                    new Vec2(2, 0),
                    new Vec2(2, 1),
                    new Vec2(2, 2),
                    new Vec2(2, 3),
                    new Vec2(2, 4),
                    new Vec2(3, 0),
                    new Vec2(3, 1),
                    new Vec2(3, 2),
                    new Vec2(3, 3),
                    new Vec2(3, 4),
                    new Vec2(4, 1),
                    new Vec2(4, 2),
                    new Vec2(4, 3),
            };
            for (Vec2 pos : thatchPositions) {
                BlockPos placePos = findGround(worldIn, (int) pos.x, (int) pos.y).below();
                worldIn.setBlock(placePos, BlockHandler.THATCH.get().defaultBlockState(), 2);
            }
            Vec2[] groundSkullPositions = new Vec2[] {
                    new Vec2(0, 1),
                    new Vec2(0, 3),
                    new Vec2(2, 4),
                    new Vec2(3, 3),
                    new Vec2(4, 2),
            };
            for (Vec2 pos : groundSkullPositions) {
                setBlockState(worldIn, findGround(worldIn, (int) pos.x, (int) pos.y), Blocks.SKELETON_SKULL.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, randomIn.nextInt(16)));
            }
            Vec2[] fenceSkullPositions = new Vec2[] {
                    new Vec2(0, 2),
                    new Vec2(1, 4),
                    new Vec2(3, 4),
                    new Vec2(4, 1),
                    new Vec2(4, 3),
            };
            for (Vec2 pos : fenceSkullPositions) {
                BlockPos groundPos = findGround(worldIn, (int) pos.x, (int) pos.y);
                setBlockState(worldIn, groundPos, Blocks.OAK_FENCE.defaultBlockState());
                setBlockState(worldIn, groundPos.above(), Blocks.SKELETON_SKULL.defaultBlockState().setValue(BlockStateProperties.ROTATION_16, randomIn.nextInt(16)));
            }
        }

        public BlockPos findGround(LevelAccessor worldIn, int x, int z) {
            int i = this.getWorldX(x, z);
            int k = this.getWorldZ(x, z);
            int j = worldIn.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, i, k);
            return new BlockPos(i, j, k);
        }
    }
}
