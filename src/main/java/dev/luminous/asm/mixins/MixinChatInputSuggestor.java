package dev.luminous.asm.mixins;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.luminous.Alien;
import dev.luminous.mod.commands.Command;
import dev.luminous.mod.modules.impl.client.ClientSetting;
import dev.luminous.api.utils.render.Render2DUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor {
	@Final
	@Shadow
	TextFieldWidget textField;
	@Shadow
	private CompletableFuture<Suggestions> pendingSuggestions;
	@Final
	@Shadow
	private List<OrderedText> messages;

	@Shadow
	public abstract void show(boolean narrateFirstSuggestion);
	@Unique
	private boolean showOutline = false;

	@Inject(at = {@At(value = "HEAD")}, method = "render")
	private void onRender(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
		if (showOutline) {

			int x = textField.getX() - 3;
			int y = textField.getY() - 3;
			//Up
			Render2DUtil.drawRect(context.getMatrices(), x, y, textField.getWidth() + 1, 1, ClientSetting.INSTANCE.color.getValue().getRGB());

			//Down
			Render2DUtil.drawRect(context.getMatrices(), x, y + textField.getHeight() + 1, textField.getWidth() + 1, 1, ClientSetting.INSTANCE.color.getValue().getRGB());

			//Left
			Render2DUtil.drawRect(context.getMatrices(), x, y, 1, textField.getHeight() + 1, ClientSetting.INSTANCE.color.getValue().getRGB());

			//Right
			Render2DUtil.drawRect(context.getMatrices(), x + textField.getWidth() + 1, y, 1, textField.getHeight() + 2, ClientSetting.INSTANCE.color.getValue().getRGB());
		}
	}

	@Inject(at = {
			@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;getCursor()I", ordinal = 0)}, method = "refresh()V")
	private void onRefresh(CallbackInfo ci) {
		String prefix = Alien.PREFIX;
		String string = this.textField.getText();

		showOutline = string.startsWith(prefix);

		if (!string.isEmpty()) {
			int cursorPos = this.textField.getCursor();
			String string2 = string.substring(0, cursorPos);

			if (prefix.startsWith(string2) || string2.startsWith(prefix)) {
				int j = 0;
				Matcher matcher = Pattern.compile("(\\s+)").matcher(string2);
				while (matcher.find()) {
					j = matcher.end();
				}

				SuggestionsBuilder builder = new SuggestionsBuilder(string2, j);
				if (string2.length() < prefix.length()) {
					if (prefix.startsWith(string2)) {
						builder.suggest(prefix);
					} else {
						return;
					}
				} else if (string2.startsWith(prefix)) {
					int count = StringUtils.countMatches(string2, " ");
					List<String> seperated = Arrays.asList(string2.split(" "));
					if (count == 0) {
						for (Object strObj : Alien.COMMAND.getCommands().keySet().toArray()) {
							String str = (String) strObj;
							builder.suggest(Alien.PREFIX + str + " ");
						}
					} else {
						if (seperated.isEmpty()) return;
						Command c = Alien.COMMAND.getCommandBySyntax(seperated.get(0).substring(prefix.length()));
						if (c == null) {
							messages.add(Text.of("§cno commands found: §e" + seperated.get(0).substring(prefix.length())).asOrderedText());
							return;
						}

						String[] suggestions = c.getAutocorrect(count, seperated);

						if (suggestions == null || suggestions.length == 0) return;
						for (String str : suggestions) {
							builder.suggest(str + " ");
						}
					}
				} else {
					return;
				}

				this.pendingSuggestions = builder.buildFuture();
				this.show(false);
			}
		}
	}
}
