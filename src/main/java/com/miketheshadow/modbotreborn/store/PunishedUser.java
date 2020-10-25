package com.miketheshadow.modbotreborn.store;


import com.miketheshadow.modbotreborn.ModBotReborn;
import com.miketheshadow.modbotreborn.util.PunishmentResult;
import com.miketheshadow.modbotreborn.util.PunishmentType;
import javafx.util.Callback;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.RestAction;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class PunishedUser {

    private ObjectId id;

    @BsonProperty(value = "user_id")
    private Long userIDNum;
    @BsonProperty(value = "warn_amount")
    public int punishmentCount;
    private List<Punishment> punishments = new ArrayList<>();
    @BsonProperty(value = "unban_day")
    private LocalDateTime unbanDay = null;

    public PunishedUser() {
        userIDNum = -1L;
    }

    public PunishedUser(long ID) {
        this.userIDNum = ID;
    }

    Consumer<Message> callback = (response) -> response.delete().queueAfter(10, TimeUnit.SECONDS);

    public void addPunishment(Punishment punishment,Message message, Member moderator, Member punished) {
        this.punishments.add(punishment);
        this.punishmentCount += punishment.warnAmount;
        if(this.punishmentCount > 2 && this.punishmentCount < 5) {
             punishment.setType(PunishmentType.MUTE);
        }
        PunishmentResult result = new PunishmentResult(this, punishment);

        if(result.getResult() == PunishmentResult.Result.SUCCESS) {
            punishUser(punishment,message,moderator,punished);
        } else {
            ModBotReborn.log("Error! Unable to update user in database!");
        }
    }

    public void removePunishment(String reason,Message message, Member moderator, Member punished,int removalIndex) {
        if(removalIndex > this.punishments.size()) {
            message.getChannel().sendMessage("Punishment doesn't exist!").queue(callback);
            return;
        }
        Punishment punishment = this.punishments.get(removalIndex);
        if(punishment == null) {
            message.getChannel().sendMessage("This punishment has already been revoked!").queue(callback);
            return;
        }
        if(punishment.getType() == PunishmentType.MUTE) {
            punished.getGuild().removeRoleFromMember(punished.getId(),ModBotReborn.MUTE_ROLE).queue();
            if(this.muteFuture != null) {
                this.muteFuture.cancel(true);
            }
            punished.getGuild().removeRoleFromMember(punished.getId(),ModBotReborn.MUTE_ROLE).queue();
            if(punished.getRoles().contains(ModBotReborn.MUTE_ROLE)) {
                System.out.println("They have the role!");
            }
            System.out.println("Role should be gone?");
        }
        MessageEmbed.Field field = new MessageEmbed.Field("Warnings","" + punishment.getWarnAmount() * -1,true);
        MessageEmbed.Field field2 = new MessageEmbed.Field("Reason",reason,true);
        MessageEmbed.Field field3 = new MessageEmbed.Field("PID","" + removalIndex,false);
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(message.getJDA().getSelfUser().getName())
                .setColor(Color.CYAN)
                .setDescription("Member: " + punished.getUser().getAsTag())
                .setFooter("Moderator: " + moderator.getUser().getAsTag())
                .setTitle("Type: " + PunishmentType.PARDON)
                .setThumbnail(punished.getUser().getAvatarUrl())
                .addField(field2)
                .addField(field)
                .addField(field3);
        this.punishmentCount -= punishment.getWarnAmount();
        this.punishments.set(removalIndex,null);
        PunishmentResult result = new PunishmentResult(this, punishment);
        if(result.getResult() == PunishmentResult.Result.SUCCESS) {
            ModBotReborn.LOG_CHANNEL.sendMessage(builder.build()).queue();

            Consumer<Message> received = m -> dmUser(punished.getUser(),message.getTextChannel(),"`You have been pardoned for reason: '" + reason + "'. We " +
                    "thank you for your patience in this matter.`");
            message.getChannel().sendMessage("Pardoned user " + punished.getUser().getAsTag() + " with reason " + reason + "").queue(received);
        } else {
            ModBotReborn.error("remove punishment error!");
        }
    }

    private void punishUser(Punishment punishment, Message message, Member moderator, Member punished) {

        if(punishment.getType() == PunishmentType.WARN) {
            PunishmentType type = punishment.getType();
            String warningString = "Warned";
            if(punishmentCount < 3) {
                sendMessage(punishment,message,moderator,punished,type,warningString);
            }
            if (punishmentCount == 3) {
                type = PunishmentType.MUTE;
                warningString = "Muted for 30m";
                sendMessage(punishment,message,moderator,punished,type,warningString);
                muteUser(punished,30);
            } else if (punishmentCount == 4) {
                type = PunishmentType.MUTE;
                warningString = "Muted for 1h";
                sendMessage(punishment,message,moderator,punished,type,warningString);
                muteUser(punished,60);
            } else if (punishmentCount == 5) {
                warningString = "Banned for 1D";
                type = PunishmentType.BAN;
                sendMessage(punishment,message,moderator,punished,type,warningString);
                banUser(punished,7);
            } else if (punishmentCount == 6) {
                type = PunishmentType.BAN;
                warningString = "Banned for 30D";
                sendMessage(punishment,message,moderator,punished,type,warningString);
                banUser(punished,30);
            } else if (punishmentCount > 6){
                warningString = "Banned Permanently";
                sendMessage(punishment,message,moderator,punished,type,warningString);
                banUser(punished,-1);
            }
        }
    }

    private void checkBanStatus(Guild guild) {
        if(this.unbanDay != null && this.unbanDay.isAfter(LocalDateTime.now())) {
            guild.unban(guild.getMemberById(this.getUserIDNum()).getUser()).queue();
        }

    }

    private void sendMessage(Punishment punishment,Message message,Member moderator,Member punished,PunishmentType type,String warningString) {
        MessageEmbed.Field field = new MessageEmbed.Field("Warnings","" + punishment.getWarnAmount(),true);
        MessageEmbed.Field field2 = new MessageEmbed.Field("Reason",punishment.getDescription(),true);
        MessageEmbed.Field field3 = new MessageEmbed.Field("PID","" + (DBHandler.getUser(punished.getId()).punishments.size() - 1),false);
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(message.getJDA().getSelfUser().getName())
                .setColor(Color.CYAN)
                .setDescription("Member: " + punished.getUser().getAsTag())
                .setFooter("Moderator: " + moderator.getUser().getAsTag())
                .setTitle("Type: " + type)
                .setThumbnail(punished.getUser().getAvatarUrl())
                .addField(field2)
                .addField(field)
                .addField(field3);
        ModBotReborn.LOG_CHANNEL.sendMessage(builder.build()).queue();
        Consumer<Message> received = m -> dmUser(punished.getUser(),message.getTextChannel(),"`You have been " + warningString + " for reason: '" + punishment.getDescription() + "'. Please " +
                "understand that continuing this behaviour will result in further punishments. For further information please see the rules channel`");
        message.getChannel().sendMessage(warningString + " user " + punished.getUser().getAsTag() + " with reason " + punishment.getDescription() + "").queue(received);
    }

    private ScheduledFuture<?> muteFuture = null;

    private void muteUser(Member member,int time) {
        member.getGuild().addRoleToMember(member.getId(),ModBotReborn.MUTE_ROLE).queue();
        muteFuture = member.getGuild().removeRoleFromMember(member.getId(),ModBotReborn.MUTE_ROLE).queueAfter(time, TimeUnit.MINUTES);

    }
    private void banUser(Member member,int time) {
        member.getGuild().ban(member,7).queue();
        if(time == -1) this.unbanDay = null;
        this.unbanDay = LocalDateTime.now().plus(Duration.ofDays(time));
    }
    private void dmUser(User member, TextChannel responseChannel,String msg) {
        member.openPrivateChannel().queue(privateChannel ->
                privateChannel.sendMessage(msg)
                        .queue(message1 -> responseChannel.sendMessage("`User notified with direct message.`")
                                .queue(),new ErrorHandler().handle(ErrorResponse.CANNOT_SEND_TO_USER,
                                (ex) -> responseChannel.sendMessage("`Unable to DM user. Their DMs are most likely turned off.`")
                                .queue())));
    }




    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public long getUserIDNum() {
        return userIDNum;
    }

    public void setUserIDNum(Long userIDNum) {this.userIDNum = userIDNum;}

    public int getPunishmentCount() {
        return punishmentCount;
    }

    public void setPunishmentCount(int punishmentCount) {
        this.punishmentCount = punishmentCount;
    }

    public List<Punishment> getPunishments() {
        return punishments;
    }

    public void setPunishments(List<Punishment> punishments) {
        this.punishments = punishments;
    }
}
