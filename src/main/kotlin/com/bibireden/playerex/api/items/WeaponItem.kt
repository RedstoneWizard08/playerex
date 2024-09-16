package com.bibireden.playerex.api.items

import com.bibireden.playerex.api.attribute.PlayerEXAttributes.EXPERIENCE

interface WeaponItem {
    fun isWeapon(): Boolean

    var xp: Double
        get() {
            val self = this as ItemWithAttributes

            return self.getAttributeValue(EXPERIENCE)
        }
        set(value) {
            val self = this as ItemWithAttributes

            self.setAttributeValue(EXPERIENCE, value)
        }

    companion object {
        fun getExpReq(level: Int): Double {
            return (1.0 / 100.0) * (level * level) + 50.0
        }
    }
}
