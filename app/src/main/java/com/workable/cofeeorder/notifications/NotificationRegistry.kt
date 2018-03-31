package com.workable.cofeeorder.notifications


object NotificationRegistry {

  object Data {
    const val ACTION_KEY = "action"
  }

  object Action {
    const val ORDER_OPENED = "order_opened"
    const val ORDER_CLOSED = "order_closed"
  }

  object Channel {
    const val ORDER_STATUS_CHANGE_CHANNEL = "order_status_change"
  }

  object Id {
    const val ORDER_OPENED_ID = 1
    const val ORDER_CLOSED_ID = 2
  }
}
