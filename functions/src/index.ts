"use strict";

import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

import {
  ORDER_STATUS_DOCUMENT,
  ORDER_USERS_COLLECTION,
  ORDER_USERS_NOTIFICATION_TOKEN,
  ORDER_IS_OPEN_KEY,
  ORDER_OPEN_NOTIFICATION_ACTION
} from "./constants";

export const db = admin.firestore();

export const updateOrderStatusTimestamp = functions.firestore
  .document(ORDER_STATUS_DOCUMENT)
  .onUpdate(async (change, context) => {
    const previousStatus = change.before.data();
    const updatedStatus = change.after.data();

    console.log("Updating timestamp", previousStatus, updatedStatus);

    const previousOrderIsOpen = previousStatus[ORDER_IS_OPEN_KEY];
    const updatedOrderIsOpen = updatedStatus[ORDER_IS_OPEN_KEY];

    if (previousOrderIsOpen === updatedOrderIsOpen) {
      console.log(
        "Previous order_is_open === Updated order_is_open",
        previousOrderIsOpen,
        updatedOrderIsOpen
      );
      return null;
    }

    if (!updatedOrderIsOpen) {
      console.log("Updated order_is_open is not true", updatedOrderIsOpen);
      return null;
    }

    const snap = await db.collection(ORDER_USERS_COLLECTION).get();
    const docsWithToken = [];
    snap.forEach(doc => {
      const data = doc.data();
      if (data[ORDER_USERS_NOTIFICATION_TOKEN]) {
        docsWithToken.push(data);
      }
    });

    const updatedTimestamp = await change.after.ref.set(
      {
        order_status_updated_at: admin.firestore.FieldValue.serverTimestamp()
      },
      {
        merge: true
      }
    );

    if (!docsWithToken || docsWithToken.length === 0) {
      console.log("No user docs with token:" + docsWithToken);
      return;
    }

    const tokens = docsWithToken.map(user => user.notification_token);
    const payload = {
      data: {
        action: ORDER_OPEN_NOTIFICATION_ACTION
      }
    };

    const notificationRes = await admin
      .messaging()
      .sendToDevice(tokens, payload);
      
    const tokensToRemove = notificationRes.results
      .map((result, index) => {
        const error = result.error;
        if (error) {
          console.error(
            "Failure sending notification to",
            tokens[index],
            error
          );
          if (
            error.code === "messaging/invalid-registration-token" ||
            error.code === "messaging/registration-token-not-registered"
          ) {
            return tokens[index];
          }
        }
      })
      .filter(token => token);
  });
