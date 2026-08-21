import admin from 'firebase-admin';
import firebaseConfig from '../../firebase-applet-config.json';

import { getFirestore } from 'firebase-admin/firestore';

if (!admin.apps.length) {
  admin.initializeApp({
    projectId: firebaseConfig.projectId,
  });
}

const app = admin.apps[0]!;
export const adminDb = getFirestore(app, firebaseConfig.firestoreDatabaseId || '(default)');
