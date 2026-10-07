package com.example.minuteman.memory

import android.content.Context
import android.content.SharedPreferences
import com.example.minuteman.model.GitSyncConfig
import com.example.minuteman.model.MemorySlot
import com.example.minuteman.model.SaveSnapshot
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MemoryCardSystem(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("minuteman_memory_card_prefs", Context.MODE_PRIVATE)
    private val slots = mutableMapOf<String, MemorySlot>()
    private var gitConfig = GitSyncConfig()

    init {
        loadFromStorage()
        loadGitConfig()
    }

    private fun loadFromStorage() {
        val raw = prefs.getString("slots_json", null) ?: return
        try {
            val json = JSONObject(raw)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = json.getJSONObject(key)
                val snapshotsList = mutableListOf<SaveSnapshot>()
                if (obj.has("snapshots")) {
                    val arr = obj.getJSONArray("snapshots")
                    for (i in 0 until arr.length()) {
                        val sObj = arr.getJSONObject(i)
                        snapshotsList.add(
                            SaveSnapshot(
                                id = sObj.optString("id", UUID.randomUUID().toString()),
                                timestamp = sObj.optString("timestamp", ""),
                                sizeBytes = sObj.optLong("sizeBytes", 0),
                                dataJson = sObj.optString("dataJson", "{}"),
                                description = sObj.optString("description", "Checkpoint")
                            )
                        )
                    }
                }
                slots[key] = MemorySlot(
                    cartId = obj.optString("cartId", key),
                    cartName = obj.optString("cartName", key),
                    updatedAt = obj.optString("updatedAt", ""),
                    sizeBytes = obj.optLong("sizeBytes", 0),
                    dataJson = obj.optString("dataJson", "{}"),
                    dirty = obj.optBoolean("dirty", false),
                    snapshots = snapshotsList
                )
            }
        } catch (_: Exception) {}
    }

    private fun persistToStorage() {
        try {
            val root = JSONObject()
            slots.forEach { (key, slot) ->
                val obj = JSONObject().apply {
                    put("cartId", slot.cartId)
                    put("cartName", slot.cartName)
                    put("updatedAt", slot.updatedAt)
                    put("sizeBytes", slot.sizeBytes)
                    put("dataJson", slot.dataJson)
                    put("dirty", slot.dirty)
                    val arr = JSONArray()
                    slot.snapshots.forEach { snap ->
                        arr.put(JSONObject().apply {
                            put("id", snap.id)
                            put("timestamp", snap.timestamp)
                            put("sizeBytes", snap.sizeBytes)
                            put("dataJson", snap.dataJson)
                            put("description", snap.description)
                        })
                    }
                    put("snapshots", arr)
                }
                root.put(key, obj)
            }
            prefs.edit().putString("slots_json", root.toString()).apply()
        } catch (_: Exception) {}
    }

    private fun loadGitConfig() {
        val raw = prefs.getString("git_config", null) ?: return
        try {
            val obj = JSONObject(raw)
            gitConfig = GitSyncConfig(
                repoOwner = obj.optString("repoOwner", ""),
                repoName = obj.optString("repoName", ""),
                branch = obj.optString("branch", "main"),
                token = obj.optString("token", ""),
                lastSynced = obj.optString("lastSynced", "")
            )
        } catch (_: Exception) {}
    }

    fun getGitConfig(): GitSyncConfig = gitConfig

    fun setGitConfig(config: GitSyncConfig) {
        gitConfig = config
        try {
            val obj = JSONObject().apply {
                put("repoOwner", config.repoOwner)
                put("repoName", config.repoName)
                put("branch", config.branch)
                put("token", config.token)
                put("lastSynced", config.lastSynced)
            }
            prefs.edit().putString("git_config", obj.toString()).apply()
        } catch (_: Exception) {}
    }

    fun saveCartridgeData(cartId: String, cartName: String, dataJson: String, description: String? = null): Boolean {
        if (cartId.isBlank()) return false
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val sizeBytes = dataJson.toByteArray().size.toLong()

        val existing = slots[cartId]
        val snapshots = existing?.snapshots?.toMutableList() ?: mutableListOf()

        if (existing != null && existing.dataJson != dataJson) {
            snapshots.add(
                0,
                SaveSnapshot(
                    id = "snap_${UUID.randomUUID().toString().take(7)}",
                    timestamp = existing.updatedAt,
                    sizeBytes = existing.sizeBytes,
                    dataJson = existing.dataJson,
                    description = description ?: "Auto-save checkpoint"
                )
            )
            if (snapshots.size > 10) {
                snapshots.removeAt(snapshots.size - 1)
            }
        }

        slots[cartId] = MemorySlot(
            cartId = cartId,
            cartName = cartName,
            updatedAt = now,
            sizeBytes = sizeBytes,
            dataJson = dataJson,
            dirty = true,
            snapshots = snapshots
        )

        persistToStorage()
        return true
    }

    fun loadCartridgeData(cartId: String): String? = slots[cartId]?.dataJson

    fun getAllSlots(): List<MemorySlot> = slots.values.toList()

    fun getSlot(cartId: String): MemorySlot? = slots[cartId]

    fun deleteSlot(cartId: String): Boolean {
        return if (slots.remove(cartId) != null) {
            persistToStorage()
            true
        } else false
    }

    fun formatMemoryCard() {
        slots.clear()
        persistToStorage()
    }

    fun revertToSnapshot(cartId: String, snapshotId: String): Boolean {
        val slot = slots[cartId] ?: return false
        val snap = slot.snapshots.find { it.id == snapshotId } ?: return false

        val remainingSnaps = slot.snapshots.filter { it.id != snapshotId }.toMutableList()
        remainingSnaps.add(
            0,
            SaveSnapshot(
                id = "snap_${UUID.randomUUID().toString().take(7)}",
                timestamp = slot.updatedAt,
                sizeBytes = slot.sizeBytes,
                dataJson = slot.dataJson,
                description = "Pre-revert state"
            )
        )

        slots[cartId] = slot.copy(
            updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
            dataJson = snap.dataJson,
            sizeBytes = snap.sizeBytes,
            dirty = true,
            snapshots = remainingSnaps.take(10)
        )

        persistToStorage()
        return true
    }

    fun exportBackup(): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("system", "minuteman")
        root.put("exportedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        val slotsObj = JSONObject()
        slots.forEach { (k, v) ->
            slotsObj.put(k, JSONObject().apply {
                put("cartId", v.cartId)
                put("cartName", v.cartName)
                put("updatedAt", v.updatedAt)
                put("sizeBytes", v.sizeBytes)
                put("dataJson", v.dataJson)
            })
        }
        root.put("slots", slotsObj)
        return root.toString(2)
    }

    fun importBackup(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val slotsObj = root.getJSONObject("slots")
            val keys = slotsObj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val o = slotsObj.getJSONObject(k)
                slots[k] = MemorySlot(
                    cartId = o.optString("cartId", k),
                    cartName = o.optString("cartName", k),
                    updatedAt = o.optString("updatedAt", ""),
                    sizeBytes = o.optLong("sizeBytes", 0),
                    dataJson = o.optString("dataJson", "{}"),
                    dirty = false
                )
            }
            persistToStorage()
            true
        } catch (_: Exception) {
            false
        }
    }
}
