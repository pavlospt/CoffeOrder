import kotlin.js.Promise

external fun require(module: String): dynamic
external val exports: dynamic

fun main(args: Array<String>) {
  val functions = require("firebase-functions")
  val admin = require("firebase-admin")

  val orderStatusDocument = "order-status/1"
  val orderStatusUpdatedAtKey = "order_status_updated_at"

  val ORDER_USERS_COLLECTION = "order-users"
  val ORDER_USERS_NOTIFICATION_TOKEN = "notification_token"
  val ORDER_IS_OPEN_KEY = "order_is_open"

  val ORDER_OPEN_NOTIFICATION_ACTION = "order_opened"
  val ORDER_CLOSED_NOTIFICATION_ACTION = "order_closed"

  admin.initializeApp(functions.config().firebase)

  val db = admin.firestore()

  exports.updateOrderStatusTimestamp = functions.firestore.document(orderStatusDocument).onUpdate { event ->
    val previousStatus = event.data.previous.data();
    val updatedStatus = event.data.data();

    console.log("Updating timestamp", previousStatus, updatedStatus);
  }
}
