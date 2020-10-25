package com.miketheshadow.modbotreborn.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CleanCommand implements PunishCommand {

    @Override
    public void onCommand(Member moderator, Message message, String[] args) {
        TextChannel channel = message.getTextChannel();
        if(args.length == 0) {
            channel.sendMessage("`usage: " + CommandRegistry.PREFIX + "clean amount`").queue();
            return;
        }
        int amnt = 0;
        try {
            amnt = Integer.parseInt(args[0]);
        } catch (Exception ignored) {
            message.getChannel().sendMessage("Unable to parse value: " + args[0]).queue();
            return;
        }
        List<Message> history = channel.getHistoryBefore(message.getId(),amnt).complete().getRetrievedHistory();
        List<Message> finalHistory = new ArrayList<>();
        for(Message m : history) if(!m.getTimeCreated().isBefore(OffsetDateTime.now().minusDays(14))) finalHistory.add(m);

        Consumer<Message> callback = (response) -> response.delete().queueAfter(10, TimeUnit.SECONDS);

        if(finalHistory.size() < 2) for(Message m : finalHistory)m.delete().queue();
        else channel.deleteMessages(finalHistory).queue();
        channel.deleteMessageById(message.getId()).queue();
        channel.sendMessage("Deleted " + finalHistory.size() + " message(s)!").queue(callback);
    }
}
