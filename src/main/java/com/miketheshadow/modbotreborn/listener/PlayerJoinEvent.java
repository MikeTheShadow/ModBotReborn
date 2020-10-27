package com.miketheshadow.modbotreborn.listener;

import com.miketheshadow.modbotreborn.ModBotReborn;
import com.miketheshadow.modbotreborn.util.PerspectiveQueue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinEvent extends ListenerAdapter {

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        System.out.println("Player joined!" + event.getMember().getUser().getName());
        User member = event.getMember().getUser();
        String username  = member.getName();
        double x = PerspectiveQueue.awaitQueue(username,username);
        System.out.println("X" + x);
        if(x > .90) {
            ModBotReborn.ADMIN_LOG_CHANNEL.sendMessage("A user attempted to join with the name: ***" + username + "***").queue();
            dmUser(event.getMember(),ModBotReborn.ADMIN_LOG_CHANNEL,"`Your discord username`*** " + username + "***` is not appropriate for our server" +
                    ". Please consider changing it and joining again.Thank you!" +
                    "\nPlease note if this was an error please open a ticket to discuss this further!`");
        }
    }

    private void dmUser(Member member, TextChannel adminChannel, String msg) {
        member.getUser().openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage(msg)
                        .queue(message1 -> {
                            adminChannel.sendMessage("`User notified with direct message.`").queue();
                            member.getGuild().kick(member,"Inappropriate username!").queue();
                        },new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
                                (ex) -> {
                            adminChannel.sendMessage("`Unable to DM user. Their DMs are most likely turned off.`").queue();
                            member.getGuild().kick(member,"Inappropriate username!").queue();
                        })));
    }
}
