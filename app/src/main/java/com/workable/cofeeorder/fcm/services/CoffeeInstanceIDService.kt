package com.workable.cofeeorder.fcm.services

import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.workable.cofeeorder.user.UserRegistry
import com.workable.cofeeorder.user.toOrderUser

class CoffeeInstanceIDService : FirebaseInstanceIdService() {

  private val TAG = "CoffeeInstanceIDService"

  private val lastSignedInAccount by lazy {
    GoogleSignIn.getLastSignedInAccount(applicationContext)
  }

  private val db by lazy {
    FirebaseFirestore.getInstance()
  }

  private val orderUsersRef by lazy {
    db.collection(UserRegistry.FirestoreProps.COLLECTION)
  }

  override fun onTokenRefresh() {
    super.onTokenRefresh()

    lastSignedInAccount ?: return

    val orderUser = lastSignedInAccount?.toOrderUser()

    orderUser ?: return

    val freshToken = FirebaseInstanceId.getInstance().token

    val orderUserWithToken = orderUser.copy(notificationToken = freshToken)

    orderUsersRef
        .document(orderUserWithToken.documentId)
        .set(orderUserWithToken.toFirestoreMap(), SetOptions.merge())
        .addOnSuccessListener {
          Log.d(TAG, "User updated")
        }
        .addOnFailureListener {
          Log.w(TAG, "User update error", it);
        }
  }
}
