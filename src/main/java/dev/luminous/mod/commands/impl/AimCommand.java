package dev.luminous.mod.commands.impl;

import dev.luminous.Alien;
import dev.luminous.core.impl.CommandManager;
import dev.luminous.mod.commands.Command;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.List;

public class AimCommand extends Command {

	public AimCommand() {
		super("aim", "[x] [y] [z]");
	}

	@Override
	public void runCommand(String[] parameters) {
		if (parameters.length != 3)
		{
			sendUsage();
			return;
		}
		double x;
		double y;
		double z;
		if (isNumeric(parameters[0])) {
			x = Double.parseDouble(parameters[0]);
		} else if (parameters[0].startsWith("~")) {
			if (isNumeric(parameters[0].replace("~", ""))) {
				x = mc.player.getX() + Double.parseDouble(parameters[0].replace("~", ""));
			} else if (parameters[0].replace("~", "").equals("")) {
				x = mc.player.getX();
			} else {
				sendUsage();
				return;
			}
		} else {
			sendUsage();
			return;
		}

		if (isNumeric(parameters[1])) {
			y = Double.parseDouble(parameters[1]);
		} else if (parameters[1].startsWith("~")) {
			if (isNumeric(parameters[1].replace("~", ""))) {
				y = mc.player.getY() + Double.parseDouble(parameters[1].replace("~", ""));
			} else if (parameters[1].replace("~", "").equals("")) {
				y = mc.player.getY();
			} else {
				sendUsage();
				return;
			}
		} else {
			sendUsage();
			return;
		}

		if (isNumeric(parameters[2])) {
			z = Double.parseDouble(parameters[2]);
		} else if (parameters[2].startsWith("~")) {
			if (isNumeric(parameters[2].replace("~", ""))) {
				z = mc.player.getZ() + Double.parseDouble(parameters[2].replace("~", ""));
			} else if (parameters[2].replace("~", "").equals("")) {
				z = mc.player.getZ();
			} else {
				sendUsage();
				return;
			}
		} else {
			sendUsage();
			return;
		}
		float[] angle = Alien.ROTATION.getRotation(new Vec3d(x, y, z));
		mc.player.setYaw(angle[0]);
		mc.player.setPitch(angle[1]);
		DecimalFormat df = new DecimalFormat("0.0");
		CommandManager.sendChatMessage("§fAim to §eX:" + df.format(x) + " Y:" +  df.format(y) + " Z:" + df.format(z));
	}

	private boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	@Override
	public String[] getAutocorrect(int count, List<String> seperated) {
		return new String[] {"~ "};
	}
}
