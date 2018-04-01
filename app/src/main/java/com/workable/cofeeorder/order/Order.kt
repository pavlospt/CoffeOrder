package com.workable.cofeeorder.order

import com.google.firebase.firestore.DocumentSnapshot
import com.workable.cofeeorder.user.OrderUser
import java.util.Date


data class Order(
    val orderStatus: Boolean,
    val orderStatusUpdatedAt: Date?,
    val orderOwner: String?
)

fun DocumentSnapshot.toOrder() = Order(
    this.data[OrderRegistry.FirestoreProps.ORDER_IS_OPEN] as Boolean,
    this.data[OrderRegistry.FirestoreProps.ORDER_STATUS_UPDATED_AT] as Date,
    this.data[OrderRegistry.FirestoreProps.ORDER_OWNER] as? String
)

fun Order.asOpenOrder(orderUser: OrderUser): Map<String, Any?> = mapOf(
    OrderRegistry.FirestoreProps.ORDER_IS_OPEN to true,
    OrderRegistry.FirestoreProps.ORDER_OWNER to orderUser.email
)

fun Order.asClosedOrder(): Map<String, Any?> = mapOf(
    OrderRegistry.FirestoreProps.ORDER_IS_OPEN to false,
    OrderRegistry.FirestoreProps.ORDER_OWNER to null
)
