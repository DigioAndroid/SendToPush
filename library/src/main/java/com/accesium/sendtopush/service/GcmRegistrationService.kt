package com.accesium.sendtopush.service

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Fran Gilberte on 19/01/2016.
 */
class GcmRegistrationService : FirebaseInstanceIdService() {

    companion object {
        var subject = BehaviorSubject.create<String>()
        var token = ""
        var tokenReceived = false

        fun register(context: Context): BehaviorSubject<String> {
            FirebaseApp.initializeApp(context)
            token = FirebaseInstanceId.getInstance().token ?: ""
            if(token.isNotBlank()) {
                subject.onNext(token)
                tokenReceived = true
            } else {
                tokenReceived = false
            }
            return subject
        }

        fun unregister() {
            Thread{ FirebaseInstanceId.getInstance().deleteInstanceId() }.start()
        }
    }

    override fun onTokenRefresh() {
        // Get updated InstanceID token.
        FirebaseInstanceId.getInstance().token?.let {
            token = it
            if(!tokenReceived) {
                subject.onNext(it)
                tokenReceived = true
            }
        }
    }


}
