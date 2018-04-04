package com.workable.cofeeorder

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.workable.cofeeorder.order.Order
import com.workable.cofeeorder.order.OrderRegistry
import com.workable.cofeeorder.order.asClosedOrder
import com.workable.cofeeorder.order.asOpenOrder
import com.workable.cofeeorder.order.toOrder
import com.workable.cofeeorder.user.OrderUser
import com.workable.cofeeorder.user.UserRegistry
import kotlinx.android.synthetic.main.activity_order.open_close_order
import kotlinx.android.synthetic.main.activity_order.order_coffee
import kotlinx.android.synthetic.main.activity_order.order_status
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

  private lateinit var currentOrder: Order

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_order)

    saveUserInCloud(orderUser)
    checkOrderStatus()
    listenToOrderStatusChanges()
  }

  private fun setUpOrderStatusToggle() {
    open_close_order.setOnClickListener {
      toggleOrderStatus()
    }
  }

  private fun toggleOrderStatus() {
    val orderUpdate = if (currentOrder.orderStatus) {
      currentOrder.asClosedOrder()
    } else {
      currentOrder.asOpenOrder(orderUser)
    }

    orderStatusRef
        .document(OrderRegistry.Document.DOCUMENT_ID)
        .set(orderUpdate, SetOptions.merge())
  }

  private fun listenToOrderStatusChanges() {
    orderStatusRef
        .document(OrderRegistry.Document.DOCUMENT_ID)
        .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
          if (firebaseFirestoreException != null) {
            Log.d(TAG, "Snapshot failed", firebaseFirestoreException)
            return@addSnapshotListener
          }

          documentSnapshot ?: return@addSnapshotListener

          if (documentSnapshot.exists()) {
            val order = documentSnapshot.toOrder()
            updateUIForOrder(order)
          }
        }
  }

  private fun updateUIForOrder(order: Order) {
    currentOrder = order
    setUpOrderStatusToggle()

    if (order.orderStatus) {
      order_status.text = "Order is open by ${order.orderOwner}!"
      order_coffee.visibility = View.VISIBLE
    } else {
      order_status.text = "Order is closed right now!"
      order_coffee.visibility = View.GONE
    }

    if (!order.orderStatus || order.orderOwner == orderUser.email) {
      open_close_order.visibility = View.VISIBLE
    }
  }

  private fun checkOrderStatus() {
    orderStatusRef
        .document(OrderRegistry.Document.DOCUMENT_ID)
        .get()
        .addOnCompleteListener {
          if (it.isSuccessful) {
            val document: DocumentSnapshot? = it.result

            document ?: return@addOnCompleteListener

            if (document.exists()) {
              val order = document.toOrder()
              updateUIForOrder(order)
            }
          }
        }
  }

  private fun saveUserInCloud(orderUser: OrderUser?) {
    orderUser ?: return
    user_welcome.text = "Welcome ${orderUser.email}"

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
