package com.iluha168.mc4d.mixin.net.minecraft.world.level.block;

import com.iluha168.mc4d.api.net.minecraft.core.BlockPos4;
import com.iluha168.mc4d.api.net.minecraft.core.Direction4;
import com.iluha168.mc4d.api.net.minecraft.world.level.block.state.properties.BlockStateProperties4;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TripWireBlock.class)
abstract class TripWireBlockMixin {
	@Shadow
	public abstract boolean shouldConnectTo(BlockState blockState, Direction direction);

	@ModifyArg(method = "<init>", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/world/level/block/TripWireBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"
	))
	BlockState registerDefaultState(BlockState state) {
		return state
			.setValue(BlockStateProperties4.ANA, false)
			.setValue(BlockStateProperties4.KATA, false);
	}

	@ModifyReturnValue(method = "getStateForPlacement", at = @At("RETURN"))
	BlockState getStateForPlacement(
		BlockState state,
		@Local(name = "level") BlockGetter level,
		@Local(name = "pos") BlockPos pos
	) {
		final BlockPos4 pos4 = (BlockPos4) pos;
		return state
			.setValue(BlockStateProperties4.ANA, this.shouldConnectTo(level.getBlockState(pos4.ana()), Direction4.ANA))
			.setValue(BlockStateProperties4.KATA, this.shouldConnectTo(level.getBlockState(pos4.kata()), Direction4.KATA));
	}

	@Definition(id = "Direction", type = Direction.class)
	@Expression("new Direction[]{?, ?}")
	@ModifyExpressionValue(method = "updateSource", at = @At("MIXINEXTRAS:EXPRESSION"))
	Direction[] updateSource(Direction[] scanned) {
		return ArrayUtils.add(scanned, Direction4.ANA);
	}

	// TODO rotate
	// TODO mirror

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
		builder.add(BlockStateProperties4.ANA, BlockStateProperties4.KATA);
	}
}
