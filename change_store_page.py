import re

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

old_func_sig = """private fun StorePage(
    uiState: KudoUiState,
    palette: KudoPalette,"""

new_func_sig = """private fun StorePage(
    storeItems: List<KudoStoreItem>,
    coins: Int,
    recentStoreInsertId: Long?,
    palette: KudoPalette,"""

content = content.replace(old_func_sig, new_func_sig)

start_idx = content.find(new_func_sig) + len(new_func_sig)
end_idx = content.find("private fun ", start_idx)
if end_idx == -1: end_idx = len(content)

store_body = content[start_idx:end_idx]

store_body = store_body.replace("uiState.data.store", "storeItems")
store_body = store_body.replace("uiState.data.coins", "coins")
store_body = store_body.replace("uiState.recentStoreInsertId", "recentStoreInsertId")

content = content[:start_idx] + store_body + content[end_idx:]

with open('./app/src/main/java/com/kudo/app/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)

