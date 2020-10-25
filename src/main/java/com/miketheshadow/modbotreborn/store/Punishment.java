package com.miketheshadow.modbotreborn.store;

import com.miketheshadow.modbotreborn.util.PunishmentType;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Punishment {


    public String description;
    public PunishmentType type;
    @BsonProperty(value = "admin_id")
    public String adminID;
    @BsonProperty(value = "date_given")
    public LocalDateTime dateGiven;
    @BsonProperty(value = "warn_amount")
    public int warnAmount;

    public Punishment() {

    }

    public Punishment(String description, PunishmentType type, String adminID, int warnAmount, LocalDateTime dateGiven) {
        this.description = description;
        this.type = type;
        this.adminID = adminID;
        this.dateGiven = dateGiven;
        this.warnAmount = warnAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PunishmentType getType() {
        return type;
    }

    public void setType(PunishmentType type) {
        this.type = type;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public LocalDateTime getDateGiven() {
        return dateGiven;
    }

    public void setDateGiven(LocalDateTime dateGiven) {
        this.dateGiven = dateGiven;
    }

    public int getWarnAmount() { return this.warnAmount;}

    public void setWarnAmount(int warnAmount) {this.warnAmount = warnAmount;}

}
