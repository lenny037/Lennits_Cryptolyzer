import { GoogleGenerativeAI } from "@google/generative-ai";

let genAI: GoogleGenerativeAI | null = null;

function getGenAI() {
  if (!genAI) {
    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      throw new Error("GEMINI_API_KEY is not defined in the environment.");
    }
    genAI = new GoogleGenerativeAI(apiKey);
  }
  return genAI;
}

export async function generateResponse(prompt: string, context?: string) {
  try {
    const ai = getGenAI();
    const model = ai.getGenerativeModel({ model: "gemini-1.5-flash" });
    
    const fullPrompt = context 
      ? `System Context: ${context}\n\nUser Task: ${prompt}`
      : prompt;

    const result = await model.generateContent(fullPrompt);
    const response = await result.response;
    return response.text();
  } catch (error: any) {
    console.error("[M17][GEMINI_BRIDGE] Error:", error.message);
    throw error;
  }
}

export async function generateSystemPlan(task: string) {
  const context = "You are the LENNIT_CRYPTOLYZER AI Orchestration System. Your goal is to plan financial operations (MEV, Farming, Treasury) based on user instructions and market signals.";
  return generateResponse(task, context);
}
