package com.example.myapplication.data.model

data class Monster(
    val id: Int,
    val name: String,
    val attack: Int,
    val defense: Int,
    val maxHp: Int,
    val currentHp: Int = maxHp,
    val scoreValue: Int
) {
    val isAlive: Boolean get() = currentHp > 0

    fun takeDamage(damage: Int): Monster {
        val actualDamage = maxOf(0, damage - defense)
        return copy(currentHp = maxOf(0, currentHp - actualDamage))
    }
}

val monsterPool = listOf(
    Monster(1, "Gobelin", attack = 8, defense = 2, maxHp = 30, scoreValue = 10),
    Monster(2, "Orc", attack = 12, defense = 5, maxHp = 60, scoreValue = 25),
    Monster(3, "Troll", attack = 15, defense = 8, maxHp = 100, scoreValue = 50),
    Monster(4, "Dragon", attack = 25, defense = 12, maxHp = 150, scoreValue = 100),
    Monster(5, "Squelette", attack = 10, defense = 3, maxHp = 40, scoreValue = 15),
    Monster(6, "Loup-Garou", attack = 18, defense = 6, maxHp = 80, scoreValue = 40)
)
