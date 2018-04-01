'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');

const orderStatusDocument = 'order-status/1';
const orderStatusUpdatedAtKey = 'order_status_updated_at';

const ORDER_USERS_COLLECTION = 'order-users';
const ORDER_USERS_NOTIFICATION_TOKEN = 'notification_token';
const ORDER_IS_OPEN_KEY = 'order_is_open';

const ORDER_OPEN_NOTIFICATION_ACTION = 'order_opened';
const ORDER_CLOSED_NOTIFICATION_ACTION = 'order_closeded';

admin.initializeApp(functions.config().firebase);

const db = admin.firestore();

exports.updateOrderStatusTimestamp = functions.firestore.document(orderStatusDocument).onUpdate((event) => {
    const previousStatus = event.data.previous.data();
    const updatedStatus = event.data.data();

    console.log('Updating timestamp', previousStatus, updatedStatus);

    const previousOrderIsOpen = previousStatus[ORDER_IS_OPEN_KEY];
    const updatedOrderIsOpen = updatedStatus[ORDER_IS_OPEN_KEY];

    if (previousOrderIsOpen != updatedOrderIsOpen) {
        const orderOpened = updatedOrderIsOpen == true;

        let collectNotificationTokensPromise;

        if (orderOpened) {
            collectNotificationTokensPromise = db
                .collection(ORDER_USERS_COLLECTION)
                .get()
                .then(snapshot => {
                    let docsWithToken = [];

                    snapshot.forEach(doc => {
                        if (doc.data()[ORDER_USERS_NOTIFICATION_TOKEN] != null) {
                            docsWithToken.push(doc.data());
                        }
                    });

                    return docsWithToken;
                })
                .catch(err => {
                    console.log("Error getting order-users", err);
                });
        }

        const updatedTimestampPromise = event.data.ref.set({
            order_status_updated_at: admin.firestore.FieldValue.serverTimestamp()
        }, {
            merge: true
        });

        return Promise.all([collectNotificationTokensPromise, updatedTimestampPromise]).then(results => {
            const orderUsersWithToken = results[0] || [];
            const updatedTimestampResponse = results[1];

            console.log("Promise all", orderUsersWithToken, updatedTimestampResponse);

            const payload = {
                data: {
                    action: ORDER_OPEN_NOTIFICATION_ACTION
                }
            };

            const tokens = orderUsersWithToken.map(user => user.notification_token);

            let pushNotificationPromise;

            if (tokens.length > 0) {
                pushNotificationPromise = admin.messaging().sendToDevice(tokens, payload);
            }

            return Promise.all([tokens, pushNotificationPromise]);
        }).then(results => {
            console.log("Send notification response", results);

            const tokens = results[0];
            const notificationsResponse = results[1] || {};
            const notificationResults = notificationsResponse.results || [];

            const tokensToRemove = [];

            notificationResults.forEach((result, index) => {
                const error = result.error;

                if (error) {
                    console.error('Failure sending notification to', tokens[index], error);
                    if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                        tokensToRemove.push(tokens[index]);
                    }
                }
            });
            return Promise.all(tokensToRemove);
        });
    }

    return Promise.resolve(updatedStatus);
});