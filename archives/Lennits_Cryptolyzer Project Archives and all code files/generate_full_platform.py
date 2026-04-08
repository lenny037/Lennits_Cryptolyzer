import os

BASE_DIR = os.getcwd()

structure = {

"android/cryptolyzer-app/src/main/java/com/lennit/cryptolyzer/screens": [
"DashboardScreen.kt",
"AgentsScreen.kt",
"VaultScreen.kt",
"StrategiesScreen.kt",
"NotificationsScreen.kt",
"SafeScreen.kt",
"SettingsScreen.kt"
],

"android/cryptolyzer-app/src/main/java/com/lennit/cryptolyzer/viewmodel": [
"DashboardViewModel.kt",
"AgentsViewModel.kt",
"VaultViewModel.kt",
"StrategiesViewModel.kt",
"NotificationsViewModel.kt",
"SafeControlViewModel.kt",
"SettingsViewModel.kt"
],

"android/lennit-suite-mcp/app/src/main/java/com/lennit/mcp/screens":[
"DashboardScreen.kt",
"AgentsScreen.kt",
"TreasuryScreen.kt",
"StrategiesScreen.kt",
"NotificationsScreen.kt",
"SettingsScreen.kt",
"BrochureWebViewScreen.kt"
],

"android/lennit-suite-mcp/app/src/main/java/com/lennit/mcp/viewmodels":[
"DashboardViewModel.kt",
"AgentsViewModel.kt",
"TreasuryViewModel.kt",
"StrategiesViewModel.kt",
"NotificationsViewModel.kt",
"SettingsViewModel.kt"
],

"backend":[
"main.py",
"models.py",
"auth.py",
"ratelimiter.py",
"requirements.txt"
],

"backend/strategy-engine":[
"engine.py",
"runner.py",
"simulator.py",
"__init__.py"
],

"backend/strategy-engine/strategies":[
"mev_arbitrage.py",
"liquidity_farming.py",
"airdrop_hunter.py",
"__init__.py"
],

"backend/agents":[
"agent_manager.py",
"agent_runner.py"
],

"web/dashboard":[
"index.html",
"styles.css",
"script.js"
],

"cloud/firebase":[],
"cloud/cloud-run":[],
"docker/backend":[],
"docker/redis":[],
"docker/workers":[]
}


def create_structure(base, tree):

    for folder, files in tree.items():

        folder_path = os.path.join(base, folder)

        os.makedirs(folder_path, exist_ok=True)

        for file in files:

            file_path = os.path.join(folder_path, file)

            if not os.path.exists(file_path):

                with open(file_path,"w") as f:

                    f.write(f"# Auto-generated file: {file}\n")

    print("Platform structure created successfully.")


if __name__ == "__main__":

    create_structure(BASE_DIR, structure)
