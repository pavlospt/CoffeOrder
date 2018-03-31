package com.workable.cofeeorder

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.order_status
import kotlinx.android.synthetic.main.activity_main.toggle_order_status
import java.util.Date

class MainActivity : AppCompatActivity() {

  private val TAG = "OrderStatus"

  private val orderStatusCollection = "order-status"

  private val db by lazy {
    FirebaseFirestore.getInstance()
  }

  private val orderStatusRef by lazy {
    db.collection(orderStatusCollection).document("1")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    fetchOrderStatus()
    setUpClickListeners()
    setUpOrderStatusSnapshotListener()
  }

  private fun setUpOrderStatusSnapshotListener() {
    orderStatusRef.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
      if (firebaseFirestoreException != null) {
        Log.d(TAG, "Snapshot failed", firebaseFirestoreException)
        return@addSnapshotListener
      }

      documentSnapshot ?: return@addSnapshotListener

      if (documentSnapshot.exists()) {
        val orderStatus = documentSnapshot.data["order_is_open"] as Boolean
        val orderStatusText = if (orderStatus) "Open" else "Closed"
        Toast.makeText(this@MainActivity, "Order is: $orderStatusText", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun setUpClickListeners() {
    toggle_order_status.setOnClickListener {
      updateOrderStatus()
    }
  }

  private fun updateOrderStatus() {
    orderStatusRef
        .set(mapOf("order_is_open" to true))
        .addOnSuccessListener {
          order_status.text = true.toString()
          Log.d(TAG, "Order Updated")
        }
        .addOnFailureListener {
          Log.w(TAG, "Error updating order status", it);
        }
  }

  private fun fetchOrderStatus() {
    orderStatusRef.get().addOnCompleteListener {
      if (it.isSuccessful) {
        val document: DocumentSnapshot? = it.result

        document ?: return@addOnCompleteListener

        if (document.exists()) {
          val orderStatus = document.data["order_is_open"] as Boolean
          val orderStatusUpdatedAt = document.data["order_status_updated_at"] as? Date

          Log.d(TAG, orderStatus.toString())
          Log.d(TAG, orderStatusUpdatedAt.toString())

          order_status.text = orderStatus.toString()
        }
      }
    }
  }
}
