package com.iluha168.mc4d.mixin.net.minecraft.world.level.gameevent.vibrations;

import com.iluha168.mc4d.api.net.minecraft.server.level.ServerLevel4;
import com.iluha168.mc4d.api.net.minecraft.world.level.ChunkPos4;
import com.iluha168.mc4d.api.net.minecraft.world.level.chunk.ChunkSource4;
import com.iluha168.mc4d.api.net.minecraft.world.phys.Vec4;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationSystem.class)
interface VibrationSystemMixin {
	@Mixin(VibrationSystem.Listener.class)
	class ListenerMixin {
		@Redirect(method = "isOccluded", at = @At(
			value = "NEW",
			target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
			ordinal = 0
		))
		private static Vec3 isOccluded_from(double x, double y, double z, @Local(name = "origin", argsOnly = true) Vec3 origin) {
			return new Vec4(x, y, z, Mth.floor(((Vec4) origin).w) + 0.5);
		}
		@Redirect(method = "isOccluded", at = @At(
			value = "NEW",
			target = "(DDD)Lnet/minecraft/world/phys/Vec3;",
			ordinal = 1
		))
		private static Vec3 isOccluded_to(double x, double y, double z, @Local(name = "dest", argsOnly = true) Vec3 dest) {
			return new Vec4(x, y, z, Mth.floor(((Vec4) dest).w) + 0.5);
		}
	}

	@Mixin(VibrationSystem.Ticker.class)
	interface TickerMixin {
		@Redirect(method = "lambda$trySelectAndScheduleVibration$0", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
		))
		private static <T extends ParticleOptions> int trySelectAndScheduleVibration(
			ServerLevel instance, T particle, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed,
			@Local(name = "origin") Vec3 origin
		) {
			return ((ServerLevel4) instance).sendParticles(particle, x, y, z, ((Vec4) origin).w, count, xDist, yDist, zDist, zDist, speed);
		}

		@Redirect(method = "tryReloadVibrationParticle", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"
		))
		private static <T extends ParticleOptions> int tryReloadVibrationParticle(
			ServerLevel instance, T particle, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed,
			@Local(name = "origin") Vec3 origin,
			@Local(name = "destination") Vec3 destination,
			@Local(name = "alpha") double alpha
		) {
			final double newInitialW = Mth.lerp(alpha, ((Vec4) origin).w, ((Vec4) destination).w);
			return ((ServerLevel4) instance).sendParticles(particle, x, y, z, newInitialW, count, xDist, yDist, zDist, zDist, speed);
		}

		@Definition(id = "x", local = @Local(type = int.class, name = "x"))
		@Expression("x = @(? - 1)")
		@Inject(method = "areAdjacentChunksTicking", at = @At("MIXINEXTRAS:EXPRESSION"))
		private static void areAdjacentChunksTicking_w(
			Level level, BlockPos listenerPos, CallbackInfoReturnable<Boolean> cir,
			@Local(name = "listenerChunkPos") ChunkPos listenerChunkPos,
			@Share("w") LocalIntRef w
		) {
			w.set(ChunkPos4.as(listenerChunkPos).w() - 1);
		}
		// This does apply properly, IDE is lying.
		@Definition(id = "x", local = @Local(type = int.class, name = "x"))
		@Expression("x = x + @(1)")
		@ModifyExpressionValue(method = "areAdjacentChunksTicking", at = @At("MIXINEXTRAS:EXPRESSION"))
		private static int areAdjacentChunksTicking_incrementW(
			int one,
			@Local(name = "listenerChunkPos") ChunkPos listenerChunkPos,
			@Share("w") LocalIntRef w
		) {
			w.set(w.get() + 1);
			final int listenerChunkPosW = ChunkPos4.as(listenerChunkPos).w();
			if (w.get() <= listenerChunkPosW + 1) return 0;
			w.set(listenerChunkPosW - 1);
			return 1;
		}
		@Redirect(method = "areAdjacentChunksTicking", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/ChunkPos;pack(II)J"
		))
		private static long areAdjacentChunksTicking_pack(int x, int z, @Share("w") LocalIntRef w) {
			return ChunkPos4.pack(x, z, w.get());
		}
		@Redirect(method = "areAdjacentChunksTicking", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/chunk/ChunkSource;getChunkNow(II)Lnet/minecraft/world/level/chunk/LevelChunk;"
		))
		private static LevelChunk areAdjacentChunksTicking_getChunkNow(ChunkSource instance, int x, int z, @Share("w") LocalIntRef w) {
			return ((ChunkSource4) instance).getChunkNow(x, z, w.get());
		}
	}
}
