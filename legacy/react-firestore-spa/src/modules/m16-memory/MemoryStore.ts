import { eventBus, SystemEvent } from "../../lib/eventBus";
import { db } from "../../lib/firebase";
import { collection, doc, setDoc, onSnapshot, query, limit, getDocs } from "firebase/firestore";
import { handleFirestoreError, OperationType } from "../../lib/firestoreUtils";

export interface SystemMemory {
  id: string;
  agentId?: string;
  type: 'strategy' | 'transaction' | 'user_pref' | 'lesson' | 'log';
  content: string;
  embedding?: number[]; // Vector for semantic search
  importance: number; // 0-1
  tags: string[];
  createdAt: string;
}

const isServer = typeof window === 'undefined';

export class MemoryStore {
  private readonly COLLECTION_PATH = "memories";
  private buffer: SystemMemory[] = [];

  constructor() {
    this.setupListeners();
    if (isServer) {
        this.syncFromFirestoreAdmin();
    }
  }

  private async syncFromFirestoreAdmin() {
    try {
        const { adminDb } = await import("../../lib/firebaseAdmin");
        adminDb.collection(this.COLLECTION_PATH).orderBy('createdAt', 'desc').onSnapshot((snapshot) => {
            snapshot.docChanges().forEach((change) => {
                if (change.type === "added") {
                    this.buffer.unshift(change.doc.data() as SystemMemory);
                }
            });
        }, (error) => {
            console.error("[M16][ADMIN] Sync error:", error);
        });
    } catch (err) {
        console.error("[M16][ADMIN] Failed to initialize admin sync:", err);
    }
  }

  private setupListeners() {
    eventBus.subscribe(SystemEvent.MEMORY_STORED, (event) => {
      this.store(event.payload);
    });
  }

  async store(memory: Partial<SystemMemory>) {
    const id = crypto.randomUUID();
    const fullMemory: SystemMemory = {
      id,
      type: 'lesson',
      content: '',
      importance: 0.5,
      tags: [],
      createdAt: new Date().toISOString(),
      ...memory
    };

    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          await adminDb.collection(this.COLLECTION_PATH).doc(id).set(fullMemory);
      } else {
          const memoryRef = doc(db, this.COLLECTION_PATH, id);
          await setDoc(memoryRef, fullMemory);
      }
      return fullMemory;
    } catch (error) {
      handleFirestoreError(error, OperationType.WRITE, `${this.COLLECTION_PATH}/${id}`);
      return null;
    }
  }

  async getLogs(agentId: string, limitCount: number = 50) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          const querySnapshot = await adminDb.collection(this.COLLECTION_PATH)
            .where('agentId', '==', agentId)
            .where('type', '==', 'log')
            .orderBy('createdAt', 'desc')
            .limit(limitCount)
            .get();
          return querySnapshot.docs.map(doc => doc.data() as SystemMemory);
      } else {
        // Fallback or client-side retrieval if needed
        return this.buffer.filter(m => m.agentId === agentId && m.type === 'log').slice(0, limitCount);
      }
    } catch (error) {
       console.error("[M16] getLogs error:", error);
       return [];
    }
  }

  async search(queryStr: string, limitCount: number = 5) {
    try {
      if (isServer) {
          const { adminDb } = await import("../../lib/firebaseAdmin");
          const querySnapshot = await adminDb.collection(this.COLLECTION_PATH).limit(limitCount).get();
          return querySnapshot.docs.map(doc => doc.data() as SystemMemory);
      } else {
          const memoriesRef = collection(db, this.COLLECTION_PATH);
          const q = query(memoriesRef, limit(limitCount));
          const querySnapshot = await getDocs(q);
          return querySnapshot.docs.map(doc => doc.data() as SystemMemory);
      }
    } catch (error) {
      handleFirestoreError(error, OperationType.GET, this.COLLECTION_PATH);
      return [];
    }
  }
}

export const memoryStore = new MemoryStore();
