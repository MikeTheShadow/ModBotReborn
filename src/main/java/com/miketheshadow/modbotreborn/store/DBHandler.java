package com.miketheshadow.modbotreborn.store;

import com.miketheshadow.modbotreborn.ModBotReborn;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static com.mongodb.client.model.Filters.eq;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class DBHandler {

    public static final MongoCollection<PunishedUser> collection = init();

    public static PunishedUser getUser(String id) {
        Long lngID = Long.parseLong(id);
        PunishedUser user = collection.find(eq("user_id",lngID)).first();
        if(user == null) {
            user = new PunishedUser(Long.parseLong(id));
            collection.insertOne(user);
        }
        return user;
    }

    public static PunishedUser updateUser(PunishedUser user) {
        FindOneAndReplaceOptions returnDocAfterReplace = new FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER);
        return collection.findOneAndReplace(new Document("_id", user.getId()), user, returnDocAfterReplace);
    }

    public static MongoCollection<PunishedUser> init() {
        if(collection == null) {
            ConnectionString connectionString = new ConnectionString("mongodb://" + ModBotReborn.DB_ADDRESS);
            CodecRegistry pojoCodecRegistry = fromProviders(PojoCodecProvider.builder()
                    .register(PunishedUser.class)
                    .register(Punishment.class)
                    .automatic(true)
                    .build());
            CodecRegistry codecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
            MongoClientSettings clientSettings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .codecRegistry(codecRegistry)
                    .build();
            MongoClient mongoClient = MongoClients.create(clientSettings);
            MongoDatabase db = mongoClient.getDatabase(ModBotReborn.DB_NAME);
            return db.getCollection("Users", PunishedUser.class);
        }
        return collection;
    }

}
