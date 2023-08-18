package dev.jdm.altport.mixin;

import dev.jdm.altport.AltPort;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin {

    boolean tryReconnect = false;

    @Inject(method = "<init>", at=@At("TAIL"))
    public void constructorDisconnectedScreen(Screen parent, Text title, Text reason, CallbackInfo ci)  {
        tryReconnect = AltPort.getInstance().canRetry();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo info) throws NoSuchFieldException, IllegalAccessException {
        if(tryReconnect){
            AltPort.getInstance().retry();
        }
    }

    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/MultilineTextWidget;<init>(Lnet/minecraft/text/Text;Lnet/minecraft/client/font/TextRenderer;)V"), index = 0)
    private Text changedReasonText(Text text) {
        if(tryReconnect)
            return Text.of("Failed to connect, retrying failover port");
        return text;
    }
}
