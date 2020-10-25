package com.miketheshadow.modbotreborn.command;

import com.miketheshadow.modbotreborn.store.DBHandler;
import com.miketheshadow.modbotreborn.store.Punishment;
import com.miketheshadow.modbotreborn.util.PunishmentType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarnCommand implements PunishCommand {

    @Override
    public void onCommand(Member moderator, Message message, String[] args) {

        TextChannel currentChannel = message.getTextChannel();

        if(args.length == 0) {
            currentChannel.sendMessage("`usage: " + CommandRegistry.PREFIX + "warn user_id reason`").queue();
            return;
        }
        int warnCount = 1;
        long punisheeID = 0;
        Member member;
        if(message.getMentionedMembers().size() > 0) {
            member = message.getMentionedMembers().get(0);
            try {
                warnCount = Integer.parseInt(args[0]);
            } catch (Exception ignored){}
        } else {
            try {
                punisheeID = Long.parseLong(args[0]);
                punisheeID = Long.parseLong(args[1]);
                warnCount = Integer.parseInt(args[0]);

            } catch (Exception ignored) {}
            if(punisheeID == 0) {
                currentChannel.sendMessage("Unknown user ID " + args[0]).queue();
                return;
            }
            member = currentChannel.getGuild().retrieveMemberById(punisheeID).complete();
        }
        if(warnCount > 1) {
            args = Arrays.copyOfRange(args,2,args.length);
        } else args = Arrays.copyOfRange(args,1,args.length);
        Punishment punishment = new Punishment(arrayToString(args), PunishmentType.WARN, moderator.getId(),warnCount, LocalDateTime.now());

        if(member == null) {
            if(punisheeID != 0)currentChannel.sendMessage("Unknown user ID " + punisheeID).queue();
            else currentChannel.sendMessage("Unknown user " + message.getMentionedMembers().get(0)).queue();
            return;
        }
        DBHandler.getUser(member.getId()).addPunishment(punishment,message,moderator,member);
    }

    String arrayToString(String[] array) {
        StringBuilder builder = new StringBuilder();
        for(String s : array) {
            builder.append(s).append(" ");
        }
        builder.replace(builder.length() - 1,builder.length(),"");
        return builder.toString();
    }

    String[] removeItem(String[] original) {
        List<String> items = new ArrayList<>(Arrays.asList(original).subList(2, original.length));
        return (String[]) items.toArray();
    }
}
