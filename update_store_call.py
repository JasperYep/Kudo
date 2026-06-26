with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_call = """                        StorePage(
                            uiState = uiState,
                            palette = palette,"""
new_call = """                        StorePage(
                            storeItems = uiState.data.store,
                            coins = uiState.data.coins,
                            recentStoreInsertId = uiState.recentStoreInsertId,
                            palette = palette,"""

content = content.replace(old_call, new_call)

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

