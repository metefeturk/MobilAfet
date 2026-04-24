const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');

// Service Account (Firebase Console -> Settings -> Service Accounts -> Generate New Private Key)
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const app = express();
app.use(cors({ origin: true }));
app.use(express.json());

// 1. REPORT GÖNDERİM ENDPOINT'İ (Bir kullanıcı afet bildirdiğinde tetiklenir)
app.post('/send-report-notification', async (req, res) => {
    try {
        const { reportId, title, description, type, city, userId } = req.body;
        
        const usersSnapshot = await db.collection('users').get();
        const tokens = [];
        
        usersSnapshot.forEach(doc => {
            const user = doc.data();
            // Bildirimi yapan kişi hariç diğer herkesin token'ını topla
            if (user.fcmToken && user.uid !== userId) {
                tokens.push(user.fcmToken);
            }
        });

        if (tokens.length === 0) {
            return res.status(200).json({ message: 'Bildirim gönderilecek geçerli FCM Token bulunamadı.' });
        }

        const payload = {
            notification: {
                title: 'Yeni Afet / Acil Durum Bildirimi',
                body: `${city} bölgesinde yeni bir ${type} raporlandı.`
            },
            data: {
                reportId: reportId || '',
                city: city || '',
                type: type || ''
            },
            tokens: tokens
        };

        const response = await admin.messaging().sendEachForMulticast(payload);
        
        if (response.failureCount > 0) {
            console.warn(`Uyarı: ${response.failureCount} adet cihaza bildirim iletilemedi.`);
        }

        res.status(200).json({ 
            success: true, 
            successCount: response.successCount, 
            failureCount: response.failureCount,
            responses: response.responses
        });
    } catch (error) {
        console.error('Report Notification Error:', error);
        res.status(500).json({ error: error.message });
    }
});

// 2. TEST ENDPOINT'İ (Manuel bildirim denemesi için)
app.post('/send-test-notification', async (req, res) => {
    try {
        const { title, message } = req.body;
        const usersSnapshot = await db.collection('users').get();
        const tokens = [];
        
        usersSnapshot.forEach(doc => {
            if (doc.data().fcmToken) tokens.push(doc.data().fcmToken);
        });

        if (tokens.length === 0) {
            return res.status(200).json({ message: 'Veritabanında FCM Token bulunamadı.' });
        }

        const payload = {
            notification: { title: title || 'Test Bildirimi', body: message || 'Bu bir test mesajıdır.' },
            data: { type: 'test_alert' },
            tokens: tokens
        };

        const response = await admin.messaging().sendEachForMulticast(payload);
        
        if (response.failureCount > 0) {
            console.warn(`Uyarı: ${response.failureCount} adet cihaza test bildirimi iletilemedi.`);
        }

        res.status(200).json({ 
            success: true, 
            successCount: response.successCount, 
            failureCount: response.failureCount,
            responses: response.responses
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`MobilAfet Backend ${PORT} portunda çalışıyor.`));