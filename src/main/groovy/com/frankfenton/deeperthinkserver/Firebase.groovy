package com.frankfenton.deeperthinkserver

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseCredentials
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.apache.commons.lang3.RandomStringUtils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by EchelonFour on 7/03/2017.
 */
class Firebase {

    private static final self = new Firebase()

    final FirebaseDatabase realDatabase

    private Firebase() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredential(FirebaseCredentials.applicationDefault())
                .setDatabaseUrl(System.getenv('FIREBASE_URL'))
                .build();

        FirebaseApp.initializeApp(options);
        realDatabase = FirebaseDatabase.getInstance();
    }

    Future<String> getUniqueId() {
        CompletableFuture<String> result = new CompletableFuture<>()
        def idMaybe = RandomStringUtils.randomAlphanumeric(6)
        realDatabase.getReference("phrases/${idMaybe}").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    result.complete(idMaybe)
                } else {
                    result.complete(getUniqueId().get())
                }
            }

            @Override
            void onCancelled(DatabaseError databaseError) {
                result.completeExceptionally(databaseError.toException())
            }
        })
        return result
    }

    void setCurrentPhrase(String phrase) {
        def id = this.uniqueId.get()
        realDatabase.getReference("phrases/$id").setValue([phrase: phrase])
        realDatabase.getReference("currentPhrase").setValue(id)
    }

    static getInstance() {
        return self
    }

    static getDatabase() {
        return self.realDatabase
    }
}
