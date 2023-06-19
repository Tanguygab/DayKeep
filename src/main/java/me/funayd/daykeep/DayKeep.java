package me.funayd.daykeep;

import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class DayKeep extends JavaPlugin {

    public final List<World> isDay = new ArrayList<>();

    private String dayIn;
    private String day;
    private String dayTitle;
    private String daySubTitle;
    private Sound daySoundIn;
    private Sound daySound;

    private String nightIn;
    private String night;
    private String nightTitle;
    private String nightSubTitle;
    private Sound nightSoundIn;
    private Sound nightSound;

    private int fadeIn;
    private int stay;
    private int fadeOut;

    public void onEnable() {
        saveDefaultConfig();
        reload();
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> getServer().getWorlds().forEach(world->{
            if (world.getEnvironment() != World.Environment.NORMAL) return;
            long t = world.getTime();
            if ((t + 100L > 24000L || t + 100L < 13000L) && !isDay.contains(world))
                setDay(world);

            else if (t + 100L > 13000L && t + 100L < 24000L && isDay.contains(world))
                setNight(world);
        }), 5L, 5L);
    }

    public void setDay(World world) {
        isDay.add(world);
        for (int i = 5; i >= 0; i--)
            task(world,i == 0 ? day : dayIn,dayTitle,daySubTitle,i == 0 ? daySound : daySoundIn,true,i);
    }

    public void setNight(World world) {
        isDay.remove(world);
        for (int i = 5; i >= 0; i--)
            task(world,i == 0 ? night : nightIn,nightTitle,nightSubTitle,i == 0 ? nightSound : nightSoundIn,false,i);
    }

    private void task(World world, String message, String title, String subtitle, Sound sound, boolean keepInventory, int i) {
        String msg = message.formatted(i);
        getServer().getScheduler().scheduleSyncDelayedTask(this,()->{
            world.getPlayers().forEach(player->{
                player.sendMessage(msg);
                player.playSound(player.getLocation(), sound, 3.0f, 0.5f);
                if (i == 0) player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            });
            if (i == 0) world.setGameRule(GameRule.KEEP_INVENTORY, keepInventory);
        },100-i*20L);
    }

    public void reload() {
        reloadConfig();
        FileConfiguration config = getConfig();
        ConfigurationSection dayCfg = config.getConfigurationSection("day");
        assert dayCfg != null;
        dayIn = get(dayCfg,"in","&aTrời sáng trong &6@s &agiây");
        day = get(dayCfg,"is","&aTrời đã sáng, chết sẽ không mất đồ!");
        dayTitle = get(dayCfg,"title","&6Trời đã sáng");
        daySubTitle = get(dayCfg,"subtitle","&achết sẽ không mất đồ!");
        daySoundIn = get(dayCfg,"soundIn",Sound.BLOCK_NOTE_BLOCK_BELL);
        daySound = get(dayCfg,"sound",Sound.BLOCK_NOTE_BLOCK_BELL);

        ConfigurationSection nightCfg = config.getConfigurationSection("night");
        assert nightCfg != null;
        nightIn = get(nightCfg,"in","&cTrời tối trong &6@s &cgiây");
        night = get(nightCfg,"is","&cTrời đã tối, chết sẽ mất đồ!");
        nightTitle = get(nightCfg,"title","&cTrời đã tối");
        nightSubTitle = get(nightCfg,"subtitle","&cchết sẽ mất đồ!");
        nightSoundIn = get(nightCfg,"soundIn",Sound.AMBIENT_CAVE);
        nightSound = get(nightCfg,"sound",Sound.AMBIENT_CAVE);

        fadeIn = config.getInt("fadeIn",5);
        stay = config.getInt("stay",20);
        fadeOut = config.getInt("fadeOut",5);
    }

    private String get(ConfigurationSection section, String path, String def) {
        return ChatColor.translateAlternateColorCodes('&',section.getString(path,def));
    }
    private Sound get(ConfigurationSection section, String path, Sound def) {
        try {return Sound.valueOf(section.getString(path, def.toString()));}
        catch (Exception e) {return def;}
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        reload();
        sender.sendMessage(ChatColor.GREEN + "Reload Complete!");
        return true;
    }

}
