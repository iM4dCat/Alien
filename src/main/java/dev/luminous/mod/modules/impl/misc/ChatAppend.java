package dev.luminous.mod.modules.impl.misc;

import dev.luminous.Alien;
import dev.luminous.mod.modules.settings.impl.StringSetting;
import dev.luminous.api.events.eventbus.EventHandler;
import dev.luminous.api.events.impl.SendMessageEvent;
import dev.luminous.mod.modules.Module;

public class ChatAppend extends Module {
	public static ChatAppend INSTANCE;
	private final StringSetting message = add(new StringSetting("append", Alien.NAME));
	public ChatAppend() {
		super("ChatAppend", Category.Misc);
		setChinese("消息后缀");
		INSTANCE = this;
	}

	@EventHandler
	public void onSendMessage(SendMessageEvent event) {
		if (nullCheck() || event.isCancelled() || AutoQueue.inQueue) return;
		String message = event.message;

		if (message.startsWith("/") || message.startsWith("!") || message.endsWith(this.message.getValue())) {
			return;
		}
		String suffix = this.message.getValue();
		message = message + " " + suffix;
		event.message = message;
	}
}