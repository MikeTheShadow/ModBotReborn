package com.miketheshadow.modbotreborn.command;

import com.miketheshadow.modbotreborn.ModBotReborn;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandRegistry extends ListenerAdapter {

    private static final HashMap<String, PunishCommand> registeredCommands = new HashMap<>();

    public static final String PREFIX = "##";

    public static final ExecutorService service = Executors.newFixedThreadPool(10);

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getMessage().getAuthor().isBot())return;
        String message = event.getMessage().getContentRaw().toLowerCase();
        Member member = event.getMember();
        if(event.getMember().getRoles().stream().sequential().noneMatch(e ->
                ModBotReborn.MOD_ROLES.contains(e.getId()))
                || member == null
                || !message.startsWith(PREFIX))
            return;
        message = message.replace(PREFIX,"");
        String[] args = message.split(" ");

        if(registeredCommands.containsKey(args[0])) {
            String[] passArgs;
            if(args.length == 1) passArgs = new String[0];
            else passArgs = message.replace(args[0] + " ","").split(" ");
            service.execute(() -> registeredCommands.get(args[0]).onCommand(member,event.getMessage(),passArgs));

        }
    }

    public static void registerCommand(String name, PunishCommand command) {
        if(!registeredCommands.containsKey(name)) {
            registeredCommands.put(name,command);
        }
    }
}
