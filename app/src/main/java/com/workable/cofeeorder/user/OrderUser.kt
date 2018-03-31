package com.workable.cofeeorder.user

import android.os.Parcelable
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OrderUser(
    val id: String?,
    val email: String?,
    val notificationToken: String?
) : Parcelable {

  fun toFirestoreMap(): Map<String, Any?> = mapOf(
      UserRegistry.FirestoreProps.ID to id,
      UserRegistry.FirestoreProps.EMAIL to email,
      UserRegistry.FirestoreProps.NOTIFICATION_TOKEN to notificationToken
  )

  val documentId: String
    get() = id.toString()
}

fun GoogleSignInAccount.toOrderUser(notificationToken: String? = null) = OrderUser(
    this.id,
    this.email,
    notificationToken
)
