package me.hsgamer.bettergui.commandoverride;

import java.util.Map;
import java.util.regex.Pattern;
import me.hsgamer.bettergui.object.addon.Addon;
import me.hsgamer.bettergui.util.CaseInsensitiveStringMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public final class Main extends Addon {

  private static final Pattern SPACE_PATTERN = Pattern.compile(" ");
  private boolean ignoreArgs = false;
  private boolean caseInsensitive = true;

  @Override
  public boolean onLoad() {
    setupConfig();
    getConfig().options().copyDefaults(true);
    getConfig().addDefault(Settings.IGNORED_ARGS, false);
    getConfig().addDefault(Settings.CASE_INSENSITIVE, true);
    saveConfig();
    return true;
  }

  @Override
  public void onEnable() {
    ignoreArgs = getConfig().getBoolean(Settings.IGNORED_ARGS, false);
    caseInsensitive = getConfig().getBoolean(Settings.CASE_INSENSITIVE, true);
    registerListener(new Listener() {
      @EventHandler(priority = EventPriority.HIGHEST)
      public void onCommand(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
          return;
        }

        String command = event.getMessage().substring(1);
        if (ignoreArgs) {
          command = SPACE_PATTERN.split(command)[0];
        }

        Map<String, BukkitCommand> menuCommand = getPlugin().getCommandManager()
            .getRegisteredMenuCommand();
        if (caseInsensitive) {
          menuCommand = new CaseInsensitiveStringMap<>(menuCommand);
        }

        if (menuCommand.containsKey(command)) {
          event.setCancelled(true);
          menuCommand.get(command).execute(event.getPlayer(), command, new String[0]);
        }
      }
    });
  }

  @Override
  public void onReload() {
    reloadConfig();
    ignoreArgs = getConfig().getBoolean(Settings.IGNORED_ARGS, false);
    caseInsensitive = getConfig().getBoolean(Settings.CASE_INSENSITIVE, true);
  }

  private static final class Settings {

    static final String IGNORED_ARGS = "ignore-args";
    static final String CASE_INSENSITIVE = "case-insensitive";
  }
}
