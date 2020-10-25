package com.miketheshadow.modbotreborn.util;

import com.miketheshadow.modbotreborn.store.DBHandler;
import com.miketheshadow.modbotreborn.store.PunishedUser;
import com.miketheshadow.modbotreborn.store.Punishment;

public class PunishmentResult {

    Result result;
    PunishedUser punishedUser;

    public PunishmentResult(PunishedUser user, Punishment punishment) {
        try {
            PunishedUser updatedPlayer = DBHandler.updateUser(user);
            result = Result.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.FAILURE;
        }
    }

    public PunishedUser getPunishedUser() {
        return this.punishedUser;
    }

    public Result getResult() {

        return result;
    }

    public enum Result {
        SUCCESS,FAILURE
    }

}
