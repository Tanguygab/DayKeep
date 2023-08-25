package io.github.tanguygab.daykeep;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class DayKeep extends JavaPlugin {

    private final Map<World,Integer> worldTimes = new HashMap<>();
    private final Map<Integer,DayTime> times = new HashMap<>();

    private int fadeIn;
    private int stay;
    private int fadeOut;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reload();
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> getServer().getWorlds().forEach(world->{
            if (world.getEnvironment() != World.Environment.NORMAL) return;
            long worldTime = world.getTime();
            int previousWorldTime = worldTimes.getOrDefault(world,-1);
            for (int time : times.keySet()) {
                DayTime daytime = times.get(time);
                if (times.get(previousWorldTime) == daytime) return;
                long i = worldTime+100;
                if (i > time || (previousWorldTime != -1 && i < previousWorldTime)) {
                    setTime(world,daytime);
                    return;
                }
            }
        }), 5L, 5L);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        worldTimes.clear();
        times.clear();
    }

    public void setTime(World world, DayTime time) {
        worldTimes.put(world,time.time());
        for (int i = 5; i >= 0; i--) {
            boolean is = i == 0;
            String msg = (is ? time.is() : time.in()).formatted(i);
            getServer().getScheduler().runTaskLaterAsynchronously(this,()->{
                world.getPlayers().forEach(player->{
                    player.sendMessage(msg);
                    player.playSound(player.getLocation(), is ? time.sound() : time.soundIn(), 3.0f, 0.5f);
                    if (is) player.sendTitle(time.title(), time.subtitle(), fadeIn, stay, fadeOut);
                });
                if (is) getServer().getScheduler().runTask(this,()->time.commands().forEach(command-> getServer().dispatchCommand(getServer().getConsoleSender(), command)));
            },100-i*20L);
        }
    }

    public void reload() {
        reloadConfig();
        FileConfiguration config = getConfig();

        fadeIn = config.getInt("fadeIn",5);
        stay = config.getInt("stay",20);
        fadeOut = config.getInt("fadeOut",5);

        ConfigurationSection times = config.getConfigurationSection("times");
        assert times != null;
        times.getValues(false).keySet().forEach(time->{
            ConfigurationSection cfg = times.getConfigurationSection(time);
            assert cfg != null;
            String in = get(cfg,"in");
            String is = get(cfg,"is");
            String title = get(cfg,"title");
            String subtitle = get(cfg,"subtitle");
            Sound soundIn = getSound(cfg,"soundIn");
            Sound sound = getSound(cfg,"sound");
            List<String> commands = cfg.getStringList("commands");
            commands.forEach(command->commands.set(commands.indexOf(command),color(command)));
            int t = Integer.parseInt(time);
            this.times.put(t,new DayTime(t,in,is,title,subtitle,soundIn,sound,commands));
        });
    }

    private String get(ConfigurationSection section, String path) {
        return color(section.getString(path,""));
    }
    private Sound getSound(ConfigurationSection section, String path) {
        try {return Sound.valueOf(section.getString(path, Sound.BLOCK_NOTE_BLOCK_BELL.toString()));}
        catch (Exception e) {return Sound.BLOCK_NOTE_BLOCK_BELL;}
    }

    private String color(String str) {
        return ChatColor.translateAlternateColorCodes('&',str);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reload();
        sender.sendMessage(ChatColor.GREEN + "Reload Complete!");
        return true;
    }

}
