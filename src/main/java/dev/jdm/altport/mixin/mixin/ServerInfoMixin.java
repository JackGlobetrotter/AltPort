package dev.jdm.altport.mixin;

import dev.jdm.altport.AltPortHelper;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerInfo.class)
public class ServerInfoMixin {

    public int failoverPort = -1;

    @Inject(method = "fromNbt", at = @At(value = "RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void loadFailoverPort(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir, ServerInfo serverInfo) {
        if (root.contains("failoverPort")) {
            AltPortHelper.setFailoverPort(serverInfo, root.getInt("failoverPort"));
        }
    }

    @Inject(method = "toNbt", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void saveFailoverPort(CallbackInfoReturnable<NbtCompound> cir, NbtCompound nbtCompound) {
        if (this.failoverPort != -1) {
            nbtCompound.putInt("failoverPort", this.failoverPort);
        }

    }

    @Inject(method = "copyFrom", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void copyFromWithFailoverPort(ServerInfo serverInfo, CallbackInfo ci) {
        this.failoverPort = AltPortHelper.getFailoverPort(serverInfo);
    }
}
