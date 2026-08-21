import { initializeApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore, doc, getDocFromServer } from 'firebase/firestore';
import firebaseConfig from '../../firebase-applet-config.json';

const app = initializeApp(firebaseConfig);
export const db = getFirestore(app, firebaseConfig.firestoreDatabaseId);
export const auth = getAuth();

const isServer = typeof window === 'undefined';

/**
 * Validate Connection to Firestore (Critical Constraint)
 */
async function testConnection() {
  if (isServer) return; // Skip on server
  try {
    await getDocFromServer(doc(db, 'test', 'connection'));
    console.log("[FIREBASE] Connection validated.");
  } catch (error) {
    if (error instanceof Error && error.message.includes('offline')) {
      console.error("[FIREBASE] Client is offline. Please check your configuration.");
    } else {
      console.warn("[FIREBASE] Connection test skipped or failed:", error);
    }
  }
}

testConnection();
