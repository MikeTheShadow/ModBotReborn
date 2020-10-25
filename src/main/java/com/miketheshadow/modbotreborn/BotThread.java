package com.miketheshadow.modbotreborn;

import com.miketheshadow.modbotreborn.command.CommandRegistry;
import com.miketheshadow.modbotreborn.listener.GuildMessageEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.bukkit.ChatColor;

import java.util.Objects;


public class BotThread implements Runnable {

    private final String token;

    public BotThread(String token) {
        this.token = token;
    }

    @Override
    public void run() {
        JDABuilder builder = JDABuilder.createDefault(token);

        // Disable parts of the cache
        builder.setBulkDeleteSplittingEnabled(false)
                .setActivity(Activity.watching("The World Burn"))
                .addEventListeners(
                        new CommandRegistry(),
                        new GuildMessageEvent());
        try {
            JDA jda = builder.build().awaitReady();
            String modChannelID = ModBotReborn.configuration.getString("mod_log_channel_id");
            String adminChannelID = ModBotReborn.configuration.getString("admin_log_channel_id");
            assert modChannelID != null && adminChannelID != null;
            ModBotReborn.ADMIN_LOG_CHANNEL = jda.getTextChannelById(adminChannelID);
            ModBotReborn.LOG_CHANNEL = jda.getTextChannelById(modChannelID);
            ModBotReborn.MUTE_ROLE = jda.getRoleById(Objects.requireNonNull(ModBotReborn.configuration.getString("mute_role_id")));
            if(ModBotReborn.LOG_CHANNEL == null) {
                throw new IllegalArgumentException("Config missing values!");
            }

        } catch (Exception ignored) {

            ModBotReborn.error(ChatColor.DARK_PURPLE + "Loading error! This plugin is now disabled.");
            ignored.printStackTrace();
            ModBotReborn.INSTANCE.getServer().getPluginManager().disablePlugin(ModBotReborn.INSTANCE);
        }

    }

}
