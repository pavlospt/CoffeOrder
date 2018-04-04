"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const constants_1 = require("./constants");
exports.db = admin.firestore();
exports.updateOrderStatusTimestamp = functions.firestore
    .document(constants_1.ORDER_STATUS_DOCUMENT)
    .onUpdate((change, context) => {
    const previousStatus = change.before.data();
    const updatedStatus = change.after.data();
    console.log("Updating timestamp", previousStatus, updatedStatus);
    const previousOrderIsOpen = previousStatus[constants_1.ORDER_IS_OPEN_KEY];
    const updatedOrderIsOpen = updatedStatus[constants_1.ORDER_IS_OPEN_KEY];
    if (previousOrderIsOpen === updatedOrderIsOpen) {
        console.log("Previous order_is_open === Updated order_is_open", previousOrderIsOpen, updatedOrderIsOpen);
        return Promise.resolve();
    }
    if (!updatedOrderIsOpen) {
        console.log("Updated order_is_open is not true", updatedOrderIsOpen);
        return Promise.resolve();
    }
    const tokensPromise = exports.db
        .collection(constants_1.ORDER_USERS_COLLECTION)
        .get()
        .then(snap => {
        const docsWithToken = [];
        snap.forEach(doc => {
            const data = doc.data();
            if (data[constants_1.ORDER_USERS_NOTIFICATION_TOKEN]) {
                docsWithToken.push(data);
            }
        });
        return docsWithToken;
    })
        .catch(console.log);
    const updatedTimestampPromise = change.after.ref.set({
        order_status_updated_at: admin.firestore.FieldValue.serverTimestamp()
    }, {
        merge: true
    });
    return updatedTimestampPromise
        .then(() => tokensPromise)
        .then(usersWithTokens => {
        const orderUsersWithToken = usersWithTokens || [];
        console.log("Users With Tokens", orderUsersWithToken);
        if (!orderUsersWithToken || orderUsersWithToken.length === 0) {
            return Promise.reject(orderUsersWithToken);
        }
        const tokens = orderUsersWithToken.map(user => user.notification_token);
        const payload = {
            data: {
                action: constants_1.ORDER_OPEN_NOTIFICATION_ACTION
            }
        };
        return admin
            .messaging()
            .sendToDevice(tokens, payload)
            .then(messagingResponse => {
            const tokensToRemove = messagingResponse.results
                .map((result, index) => {
                const error = result.error;
                if (error) {
                    console.error("Failure sending notification to", tokens[index], error);
                    if (error.code === "messaging/invalid-registration-token" ||
                        error.code === "messaging/registration-token-not-registered") {
                        tokensToRemove.push(tokens[index]);
                    }
                }
            })
                .filter(token => token);
        });
    });
});
//# sourceMappingURL=index.js.map