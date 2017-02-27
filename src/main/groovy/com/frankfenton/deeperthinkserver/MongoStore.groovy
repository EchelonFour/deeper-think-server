package com.frankfenton.deeperthinkserver

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia

/**
 * Created by EchelonFour on 6/03/2017.
 */
class MongoStore {
    static final MongoStore instance = new MongoStore()

    final Morphia morphia = new Morphia()
    final Datastore datastore
    private final mongo

    static Datastore getStore() {
        return instance.datastore
    }

    private MongoStore() {
        morphia.mapPackage("com.frankfenton.deeperthinkserver.models")
        def url = new MongoClientURI(System.getenv('MONGO_URL'))
        mongo = new MongoClient(url)
        datastore = morphia.createDatastore(mongo, url.database)
        datastore.ensureIndexes()
    }
}
