package me.hsgamer.bettergui.commandoverride;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.hsgamer.bettergui.object.addon.Addon;
import me.hsgamer.bettergui.util.CaseInsensitiveStringMap;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class Main extends Addon {

  private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
  private final List<String> ignoredCommands = new ArrayList<>();
  private boolean caseInsensitive = true;

  @Override
  public boolean onLoad() {
    setupConfig();
    getConfig().options().copyDefaults(true);
    getConfig().addDefault(Settings.IGNORED_COMMANDS, Collections.singletonList("warp test"));
    getConfig().addDefault(Settings.CASE_INSENSITIVE, true);
    saveConfig();
    return true;
  }

  @Override
  public void onEnable() {
    ignoredCommands.addAll(getConfig().getStringList(Settings.IGNORED_COMMANDS));
    caseInsensitive = getConfig().getBoolean(Settings.CASE_INSENSITIVE, true);
    registerListener(new Listener() {
      @EventHandler(priority = EventPriority.HIGHEST)
      public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
          return;
        }

        String rawCommand = event.getMessage().substring(1);
        if (ignoredCommands.stream().anyMatch(s -> s.equalsIgnoreCase(rawCommand))) {
          return;
        }

        String[] split = SPACE_PATTERN.split(rawCommand);
        String command = split[0];
        String[] args = new String[0];
        if (split.length > 1) {
          args = Arrays.copyOfRange(split, 1, split.length);
        }

        Map<String, Command> menuCommand = getPlugin().getCommandManager()
            .getRegisteredMenuCommand();
        if (caseInsensitive) {
          menuCommand = new CaseInsensitiveStringMap<>(menuCommand);
        }

        if (menuCommand.containsKey(command)) {
          event.setCancelled(true);
          menuCommand.get(command).execute(event.getPlayer(), command, args);
        }
      }
    });
  }

  @Override
  public void onReload() {
    reloadConfig();
    ignoredCommands.clear();
    ignoredCommands.addAll(getConfig().getStringList(Settings.IGNORED_COMMANDS));
    caseInsensitive = getConfig().getBoolean(Settings.CASE_INSENSITIVE, true);
  }

  private static final class Settings {

    static final String IGNORED_COMMANDS = "ignore-commands";
    static final String CASE_INSENSITIVE = "case-insensitive";
  }
}
