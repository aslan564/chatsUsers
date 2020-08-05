const functions = require('firebase-functions');


const admin = require('firebase-admin');

admin.initializeApp();
exports.mesajBildirim = functions.firestore.document("sohbetKanali/{channel}/messages/{message}")
    .onCreate(async (docSnapshot, context) => {
        const message = docSnapshot.data();
        const qebuledenId = message['qebuledenId'];
        const gonderenAdi = message['gonderenAdi'];

        const userDoc = await admin.firestore().doc('users/' + qebuledenId).get();
        const registrationTokens = userDoc.get('registrationTokens');
        var bildirisBody;
        if (message['mesajinTipi'] === "IMAGE") {
            bildirisBody = "bir yeni image mesajiniz var";
        }
        else if (message['mesajinTipi'] === "AUDIO") {
            bildirisBody = "bir yeni sesli mesajiniz var";
        }
        else {
            bildirisBody = message;
        }
        const payload = {
            notfication: {
                title: gonderenAdi + " sene mesaj gonderib.",
                body: bildirisBody,
                clickAction: "ChatActivity"
            },
            data: {
                USER_NAME: gonderenAdi,
                USER_ID: message['gonderenId']
            }
        };
        const response = await admin.messaging().sendToDevice(registrationTokens, payload);
        const stillRegistrationTokens = registrationTokens;
        response.results.forEach((result, index) => {
            const error = result.error;
            if (error) {
                const failedRegistraionToken = registrationTokens[index];
                console.error('error', failedRegistraionToken.error);
                if (error.code === 'messaging/invalid-registration-token' &&
                    error.code === 'messaging/registration-token-not-registered') {
                    const failedIndex = stillRegistrationTokens.indexOf(failedRegistraionToken);
                    if (failedIndex > -1) {
                        stillRegistrationTokens.splice(failedIndex, 1);
                    }
                }
            }
        });
        return admin.firestore().doc('users' + qebuledenId).update({
            registrationTokens: stillRegistrationTokens
        });
    });

