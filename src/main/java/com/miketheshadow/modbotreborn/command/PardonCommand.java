package com.miketheshadow.modbotreborn.command;

import com.miketheshadow.modbotreborn.store.DBHandler;
import com.miketheshadow.modbotreborn.store.PunishedUser;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;

public class PardonCommand implements PunishCommand {

    @Override
    public void onCommand(Member moderator, Message message, String[] args) {

        if(args.length == 0) {
            message.getChannel().sendMessage("`usage: " + CommandRegistry.PREFIX + "pardon userid pid`").queue();
            return;
        }
        PunishedUser punishedUser = DBHandler.getUser(args[0]);
        Member member = message.getGuild().retrieveMemberById(args[0]).complete();

        if(punishedUser.getPunishments().size() < 1) {
            message.getChannel().sendMessage("User has no punishments: " + args[0]).queue();
            return;
        }
        try {
            int id = Integer.parseInt(args[1]);
            punishedUser.removePunishment(buildReason(args),message,moderator,member,id);
        } catch (Exception e) {
            e.printStackTrace();
            message.getChannel().sendMessage("No punishment with ID: " + args[1]).queue();
        }
    }

    String buildReason(String[] args) {
        StringBuilder builder = new StringBuilder();
        for(String s : Arrays.copyOfRange(args,2,args.length)) {
            builder.append(s).append(" ");
        }
        builder.replace(builder.length() - 1,builder.length(),"");
        return builder.toString();
    }
}
