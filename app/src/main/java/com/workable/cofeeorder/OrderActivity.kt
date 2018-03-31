package com.workable.cofeeorder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.workable.cofeeorder.order.OrderRegistry
import com.workable.cofeeorder.user.OrderUser
import com.workable.cofeeorder.user.UserRegistry
import kotlinx.android.synthetic.main.activity_order.user_welcome

class OrderActivity : AppCompatActivity() {

  private val TAG = "OrderActivity"

  private val orderUser by lazy {
    intent.getParcelableExtra<OrderUser>(ORDER_USER_KEY)
  }

  private val db by lazy {
    FirebaseFirestore.getInstance()
  }

  private val orderUsersRef by lazy {
    db.collection(UserRegistry.FirestoreProps.COLLECTION)
  }

  private val orderStatusRef by lazy {
    db.collection(OrderRegistry.FirestoreProps.COLLECTION)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_order)

    saveUserInCloud(orderUser)
    user_welcome.text = "Welcome ${orderUser.email}"
  }

  private fun saveUserInCloud(orderUser: OrderUser?) {
    orderUser ?: return

    orderUsersRef
        .document(orderUser.documentId)
        .set(orderUser.toFirestoreMap(), SetOptions.merge())
        .addOnSuccessListener {
          Log.d(TAG, "User updated")
        }
        .addOnFailureListener {
          Log.w(TAG, "User update error", it);
        }
  }

  companion object {
    const val ORDER_USER_KEY = "order-user"

    fun startScreen(context: Context, orderUser: OrderUser) {
      val intent = Intent(context, OrderActivity::class.java)
      intent.putExtra(ORDER_USER_KEY, orderUser)
      context.startActivity(intent)
    }
  }
}
