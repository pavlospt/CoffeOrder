"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();
const constants_1 = require("./constants");
exports.db = admin.firestore();
exports.updateOrderStatusTimestamp = functions.firestore
    .document(constants_1.ORDER_STATUS_DOCUMENT)
    .onUpdate((change, context) => __awaiter(this, void 0, void 0, function* () {
    const previousStatus = change.before.data();
    const updatedStatus = change.after.data();
    console.log("Updating timestamp", previousStatus, updatedStatus);
    const previousOrderIsOpen = previousStatus[constants_1.ORDER_IS_OPEN_KEY];
    const updatedOrderIsOpen = updatedStatus[constants_1.ORDER_IS_OPEN_KEY];
    if (previousOrderIsOpen === updatedOrderIsOpen) {
        console.log("Previous order_is_open === Updated order_is_open", previousOrderIsOpen, updatedOrderIsOpen);
        return null;
    }
    if (!updatedOrderIsOpen) {
        console.log("Updated order_is_open is not true", updatedOrderIsOpen);
        return null;
    }
    const snap = yield exports.db.collection(constants_1.ORDER_USERS_COLLECTION).get();
    const docsWithToken = [];
    snap.forEach(doc => {
        const data = doc.data();
        if (data[constants_1.ORDER_USERS_NOTIFICATION_TOKEN]) {
            docsWithToken.push(data);
        }
    });
    const updatedTimestamp = yield change.after.ref.set({
        order_status_updated_at: admin.firestore.FieldValue.serverTimestamp()
    }, {
        merge: true
    });
    if (!docsWithToken || docsWithToken.length === 0) {
        console.log("No user docs with token:" + docsWithToken);
        return;
    }
    const tokens = docsWithToken.map(user => user.notification_token);
    const payload = {
        data: {
            action: constants_1.ORDER_OPEN_NOTIFICATION_ACTION
        }
    };
    const notificationRes = yield admin
        .messaging()
        .sendToDevice(tokens, payload);
    const tokensToRemove = notificationRes.results
        .map((result, index) => {
        const error = result.error;
        if (error) {
            console.error("Failure sending notification to", tokens[index], error);
            if (error.code === "messaging/invalid-registration-token" ||
                error.code === "messaging/registration-token-not-registered") {
                return tokens[index];
            }
        }
    })
        .filter(token => token);
}));
//# sourceMappingURL=index.js.map