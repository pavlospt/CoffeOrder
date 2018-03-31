package com.workable.cofeeorder.order


object OrderRegistry {

  object FirestoreProps {
    const val COLLECTION = "order-users"
    const val ORDER_IS_OPEN = "order_is_open"
    const val ORDER_STATUS_UPDATED_AT = "order_status_updated_at"
  }

  object Document {
    const val DOCUMENT_ID = "1"
  }

}
