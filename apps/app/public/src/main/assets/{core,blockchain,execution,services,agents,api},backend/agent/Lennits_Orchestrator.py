import sys
import os
import asyncio

# Master Path Fix for Termux Root
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from LennitSuite.Data.Lennits_Extractor import LennitsExtractor
from LennitSuite.Data.Lennits_StreamIngestor import LennitsStreamIngestor
from LennitSuite.Passive.Lennits_FaucetCollector import LennitsFaucetCollector
from LennitSuite.Execution.Lennits_PortfolioManager import LennitsPortfolioManager
from LennitSuite.Intelligence.Lennits_RiskEngine import RiskEngine
from LennitSuite.Intelligence.Lennits_ZKBridge import LennitsZKBridge
from LennitSuite.Execution.Lennits_Betolyzer import LennitsBetolyzer

class LennitsOrchestrator:
    def __init__(self):
        self.extractor = LennitsExtractor()
        self.ingestor = LennitsStreamIngestor()
        self.collector = LennitsFaucetCollector()
        self.portfolio = LennitsPortfolioManager()
        self.risk_engine = RiskEngine()
        self.zk_bridge = LennitsZKBridge()
        self.betolyzer = LennitsBetolyzer()
        self.is_running = True

    async def autonomous_farming_cycle(self):
        print("\n--- LennitSuite: FULL CIRCUIT ENGAGED (Live Data Mode) ---")
        
        while self.is_running:
            # 1. Scout for Capital (Farming)
            leads = await self.extractor.find_high_yield_leads()
            
            # 2. Scout for Opportunity (Live Data)
            market_snapshot = await self.ingestor.get_market_snapshot()
            
            for lead in leads:
                # 3. Security Check
                proof_hash = sum(ord(c) for c in lead['source'])
                if self.zk_bridge.verify_agent(self.collector.agent_id, proof_hash):
                    
                    # 4. Harvest Passive Income
                    if self.collector.execute_claim(lead):
                        self.portfolio.update_portfolio(lead['reward'], lead['value'], lead['value'])
                        
                        # 5. Active Intelligence (Betting/Arbitrage)
                        analysis = self.betolyzer.analyze_market(market_snapshot)
                        
                        if analysis["action"] == "PLACE_BET":
                            # Only proceed if RiskEngine approves the active play
                            if self.risk_engine.approve_action(lead['value'], self.portfolio.total_value_usd):
                                print(f"Lennits_Orchestrator: Executing Active Edge -> {analysis['target']}")
                                # Simulate 2.0x win on high-confidence plays
                                self.portfolio.update_portfolio("ACTIVE_PROFIT", 0, lead['value'] * 2.0)
                        else:
                            print("Lennits_Orchestrator: No active edge found. Capital secured in Treasury.")

            print(f"\nCycle Complete. Current Treasury: ${self.portfolio.total_value_usd:.4f}")
            print("Sleeping 60s...")
            await asyncio.sleep(60)

if __name__ == "__main__":
    orchestrator = LennitsOrchestrator()
    try:
        asyncio.run(orchestrator.autonomous_farming_cycle())
    except KeyboardInterrupt:
        print("\nLennitSuite: System Safe-Stop Initialized.")
