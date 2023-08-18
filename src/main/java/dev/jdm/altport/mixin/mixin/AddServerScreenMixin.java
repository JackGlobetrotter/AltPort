package dev.jdm.altport.mixin;

import dev.jdm.altport.AltPortHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(AddServerScreen.class)
public class AddServerScreenMixin extends Screen {
    @Shadow @Final private ServerInfo server;

    private int FailoverPort = -1;
    private static final Text ENTER_FAILOVERPORT_TEXT =  Text.translatable("altport.addServer.failoverPort");

    private TextFieldWidget failoverPortField;

    protected AddServerScreenMixin(Text title) {
        super(title);
    }
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/AddServerScreen;addSelectableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;", ordinal = 1, shift = At.Shift.AFTER))
    private void addFailoverPortField(CallbackInfo ci){

        this.FailoverPort = AltPortHelper.getFailoverPort(server);

        this.failoverPortField = new TextFieldWidget(this.textRenderer, this.width / 2-100, 146, 200, 20, Text.translatable("addServer.enterFailoverPort"));
        this.failoverPortField.setMaxLength(128);
        this.failoverPortField.setText(String.valueOf(this.FailoverPort));
        this.failoverPortField.setTextPredicate( x -> {
            if(x.equals("")) {
                return true;
            }
            if(x.length() == 1 && x.charAt(0)  == '-'){
                return true;
            }
            for (int i = 0 ; i < x.length() ; i++) {
                if (!Character.isDigit(x.charAt(i))  ) {
                    if( i!=0 ||( i == 0 && x.charAt(i) != '-'))
                        return false;
                }
            }
            return Integer.parseInt(x) <= 65535 && Integer.parseInt(x) >= -1;
        });
        this.addSelectableChild(this.failoverPortField);
    }

    //Save port on Close
    @Inject(method = "addAndClose", at=@At("HEAD"))
    private void saveFailoverPort(CallbackInfo ci){
        AltPortHelper.setFailoverPort(server, Integer.parseInt(this.failoverPortField.getText()));
    }

    //Render FailoverPort Field
    @Inject(method = "render", at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", ordinal = 0),locals = LocalCapture.CAPTURE_FAILHARD)
    private void renderFailoverPortField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        context.drawTextWithShadow(this.textRenderer, ENTER_FAILOVERPORT_TEXT, this.width / 2 - 100, 135, 10526880);
        this.failoverPortField.render(context, mouseX, mouseY, delta);
    }

    // FailoverPort Field
    @Inject(method = "tick", at = @At("RETURN"))
    private void tickFailoverPortField(CallbackInfo ci){
        this.failoverPortField.tick();
    }

    //// Change default button location to make space for the port field
    //Resource Prompt Button
    @ModifyArg(method = "init" , at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/CyclingButtonWidget$Builder;build(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/CyclingButtonWidget$UpdateCallback;)Lnet/minecraft/client/gui/widget/CyclingButtonWidget;"), index = 1)
    private int changePromptButtonCords(int y) {
        return y+42;
    }

    //Done Button
    @ModifyArgs(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 0))
    private void changeDoneButtonCords(Args args) {
        int x = args.get(0);
        int y = args.get(1);
        args.set(0, x-105);
        args.set(1, y+24);
    }

    //Cancel Button
    @ModifyArg(method = "init" , at=@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;dimensions(IIII)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;", ordinal = 1), index = 0)
    private int changeCancelButtonCords(int x) {
        return x+105;
    }

}
