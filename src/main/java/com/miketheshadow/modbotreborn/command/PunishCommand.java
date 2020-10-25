package com.miketheshadow.modbotreborn.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public interface PunishCommand {

    void onCommand(Member moderator, Message message, String[] args);


}
