package com.jef.justenoughfakepixel.features.uptime;

import com.jef.justenoughfakepixel.core.config.command.SimpleCommand;
import com.jef.justenoughfakepixel.init.RegisterCommand;
import com.jef.justenoughfakepixel.utils.chat.ChatUtils;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * /jeftimer <time>   – start a countdown
 * /jeftimer cancel   – stop the current timer
 * /jeftimer          – show remaining time
 *
 * Time format examples: 1h, 30m, 2d, 1h30m, 1d2h30m45s
 */
@RegisterCommand
public class JefTimerCommand extends SimpleCommand {

    // Matches any sequence of "<number><unit>" pairs
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(\\d+)([dhms])", Pattern.CASE_INSENSITIVE);

    @Override
    public String getName() { return "jeftimer"; }

    @Override
    public String getUsage() { return "/jeftimer <time|cancel>  e.g. /jeftimer 1h30m"; }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        UptimeManager mgr = UptimeManager.getInstance();

        if (args.length == 0) {
            // Show current state
            if (!mgr.isRunning()) {
                ChatUtils.sendMessage("§b[JEF Timer] §fNo timer running. Use §e/jeftimer <time>§f to start one.");
            } else {
                ChatUtils.sendMessage("§b[JEF Timer] §fRemaining: §e" + formatMs(mgr.getRemainingMs()));
            }
            return;
        }

        String input = String.join("", args).toLowerCase();

        if (input.equals("cancel") || input.equals("stop") || input.equals("clear")) {
            mgr.cancel();
            ChatUtils.sendMessage("§b[JEF Timer] §cTimer cancelled.");
            return;
        }

        long durationMs = parseTime(input);
        if (durationMs <= 0) {
            ChatUtils.sendMessage("§b[JEF Timer] §cInvalid time format. Examples: §e1h §c| §e30m §c| §e1d §c| §e1h30m §c| §e2d5h");
            return;
        }

        mgr.start(durationMs);
        ChatUtils.sendMessage("§b[JEF Timer] §aTimer started! Counting down from §e" + formatMs(durationMs) + "§a.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static long parseTime(String input) {
        Matcher m = TIME_PATTERN.matcher(input);
        long total = 0;
        boolean found = false;
        while (m.find()) {
            found = true;
            long val = Long.parseLong(m.group(1));
            switch (m.group(2).toLowerCase()) {
                case "d": total += val * 86400_000L; break;
                case "h": total += val * 3600_000L;  break;
                case "m": total += val * 60_000L;    break;
                case "s": total += val * 1_000L;     break;
            }
        }
        return found ? total : -1;
    }

    private static String formatMs(long ms) {
        long s = ms / 1000;
        long d = s / 86400; s %= 86400;
        long h = s / 3600;  s %= 3600;
        long m = s / 60;    s %= 60;
        StringBuilder sb = new StringBuilder();
        if (d > 0) sb.append(d).append("d ");
        if (h > 0) sb.append(h).append("h ");
        if (m > 0) sb.append(m).append("m ");
        if (s > 0 || sb.length() == 0) sb.append(s).append("s");
        return sb.toString().trim();
    }
}
