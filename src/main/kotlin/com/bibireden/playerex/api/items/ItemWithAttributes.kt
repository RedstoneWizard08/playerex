package com.bibireden.playerex.api.items

import net.minecraft.world.entity.ai.attributes.RangedAttribute

interface ItemWithAttributes {
    fun getAttributeValue(attr: RangedAttribute): Double
    fun setAttributeValue(attr: RangedAttribute, value: Double)
}
