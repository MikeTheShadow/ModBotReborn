package com.miketheshadow.modbotreborn;

import com.miketheshadow.modbotreborn.command.CleanCommand;
import com.miketheshadow.modbotreborn.command.CommandRegistry;
import com.miketheshadow.modbotreborn.command.PardonCommand;
import com.miketheshadow.modbotreborn.command.WarnCommand;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class ModBotReborn extends JavaPlugin {

    public static String DB_NAME;
    public static String DB_ADDRESS;
    public static List<String> MOD_ROLES;
    public static TextChannel LOG_CHANNEL;
    public static TextChannel ADMIN_LOG_CHANNEL;
    public static Role MUTE_ROLE;
    static ModBotReborn INSTANCE;

    public static FileConfiguration configuration;

    @Override
    public void onEnable() {
        INSTANCE = this;
        if(!loadConfig()) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu();
        popupMenu.getAccessibleContext().getAccessibleComponent().getBackground();
        CommandRegistry.registerCommand("clean",new CleanCommand());
        CommandRegistry.registerCommand("warn",new WarnCommand());
        CommandRegistry.registerCommand("pardon",new PardonCommand());
    }

    @Override
    public void onDisable() {

    }

    public static void log(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + msg);
    }
    public static void debug(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + msg);
    }

    public static void error(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + msg);
    }

    private static File fileConfig;

    public boolean loadConfig() {
        fileConfig = new File(getDataFolder().getAbsolutePath() + "\\config.yml");
        if(!fileConfig.exists()){

            try {
                if(!getDataFolder().mkdir() || !fileConfig.createNewFile()) {
                    Bukkit.getServer().getConsoleSender().sendMessage("Failed to create config! Stopping...");
                    this.getServer().getPluginManager().disablePlugin(this);
                    return false;
                }
                setupConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        configuration = this.getConfig();
        DB_ADDRESS = configuration.getString("dbaddress");
        DB_NAME = configuration.getString("dbname");
        MOD_ROLES = configuration.getStringList("modroles");
        String token = configuration.getString("token");
        if(token.equals("token")) {
            error("Please set token in config!");
            Bukkit.getServer().getPluginManager().disablePlugin(this);
            return false;
        } else {
            Thread thread = new Thread(new BotThread(token));
            thread.start();
        }
        return true;
    }

    public void setupConfig() throws IOException {
        FileConfiguration config = this.getConfig();
        config.set("token","token");
        config.set("dbname","ModBotReborn");
        config.set("dbaddress","localhost:27017");
        config.set("mod_log_channel_id","channelID");
        config.set("admin_log_channel_id","channelID");
        config.set("mute_role_id","muteRoleID");
        config.set("api_key","key");
        List<String> fakeRoles = new ArrayList<>();
        fakeRoles.add("role1ID");
        fakeRoles.add("role2ID");
        config.set("modroles",fakeRoles);
        config.save(fileConfig);
    }

}
