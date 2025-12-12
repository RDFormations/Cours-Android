package com.example.myapplication.data.model

data class Character(
    val id: Int,
    val name: String,
    val attack: Int,
    val defense: Int,
    val maxHp: Int,
    val currentHp: Int = maxHp
) {
    val isAlive: Boolean get() = currentHp > 0

    fun takeDamage(damage: Int): Character {
        val actualDamage = maxOf(0, damage - defense)
        return copy(currentHp = maxOf(0, currentHp - actualDamage))
    }
}

val availableCharacters = listOf(
    Character(1, "Guerrier", attack = 15, defense = 8, maxHp = 100),
    Character(2, "Mage", attack = 20, defense = 3, maxHp = 60),
    Character(3, "Archer", attack = 12, defense = 5, maxHp = 80),
    Character(4, "Paladin", attack = 10, defense = 12, maxHp = 120),
    Character(5, "Assassin", attack = 25, defense = 2, maxHp = 50),
    Character(6, "Prêtre", attack = 8, defense = 6, maxHp = 70)
)
