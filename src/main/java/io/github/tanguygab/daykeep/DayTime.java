package io.github.tanguygab.daykeep;

import org.bukkit.Sound;

import java.util.List;

public record DayTime(int time, String in, String is, String title, String subtitle, Sound soundIn, Sound sound, List<String> commands) {}
