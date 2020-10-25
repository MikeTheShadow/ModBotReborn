package com.miketheshadow.modbotreborn.listener;

import com.miketheshadow.modbotreborn.ModBotReborn;
import com.miketheshadow.modbotreborn.store.DBHandler;
import com.miketheshadow.modbotreborn.store.PunishedUser;
import com.miketheshadow.modbotreborn.store.Punishment;
import com.miketheshadow.modbotreborn.util.PerspectiveQueue;
import com.miketheshadow.modbotreborn.util.PunishmentType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.miketheshadow.modbotreborn.util.PerspectiveQueue.TOXICICITY_BAR;

public class GuildMessageEvent extends ListenerAdapter {

    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getMessage().getContentDisplay().length() < 1) return;
        if(event.getMember() == null
                || event.getMember().getRoles().stream()
                .anyMatch(e -> ModBotReborn.MOD_ROLES.contains(e.getId()))
                || event.getMember().getUser().isBot())return;
        executorService.submit(() -> {
            double response = PerspectiveQueue.awaitQueue(event.getMessage().getContentStripped(),event.getAuthor().getId());
            if(response > TOXICICITY_BAR) {
                if(event.getMember().getUser().isBot())return;
                punishMember(event.getMember(),event.getMessage());
            }
        });
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if(event.getMessage().getContentDisplay().length() < 1) return;
        if(event.getMember() == null
                || event.getMember().getRoles().stream()
                .anyMatch(e -> ModBotReborn.MOD_ROLES.contains(e.getId()))
                || event.getMember().getUser().isBot())return;
        executorService.submit(() -> {
            double response = PerspectiveQueue.awaitQueue(event.getMessage().getContentStripped(),event.getAuthor().getId());
            if(response > TOXICICITY_BAR) {
                if(event.getMember().getUser().isBot())return;
                punishMember(event.getMember(),event.getMessage());
            }
        });
    }
    private static void punishMember(Member member, Message message) {
        PunishedUser punishedUser = DBHandler.getUser(member.getId());
        message.getChannel().deleteMessageById(message.getId()).queue();
        Member self = message.getGuild().getMember(member.getJDA().getSelfUser());
        if(self == null) {
            ModBotReborn.error("Cannot get JDA self user!");
            return;
        }
        ModBotReborn.ADMIN_LOG_CHANNEL.sendMessage("User: " + member.getUser().getAsTag() + " posted possible toxic comment `" + message.getContentDisplay() +"`").queue();
        Punishment punishment = new Punishment("Explicit message in chat", PunishmentType.WARN , self.getId(),1, LocalDateTime.now());
        punishedUser.addPunishment(punishment,message,self,member);
    }

}
