package team.incube.flooding.domain.homebase.entity

enum class HomebaseTable(
    val floor: Int,
    val tableNumber: Int,
    val capacity: Int
) {

    TABLE_2_1(2, 1, 6),
    TABLE_2_2(2, 2, 4),
    TABLE_2_3(2, 3, 4),

    TABLE_3_1(3, 1, 6),
    TABLE_3_2(3, 2, 6),
    TABLE_3_3(3, 3, 4),
    TABLE_3_4(3, 4, 4),
    TABLE_3_5(3, 5, 4),

    TABLE_4_1(4, 1, 6),
    TABLE_4_2(4, 2, 6),
    TABLE_4_3(4, 3, 4),
    TABLE_4_4(4, 4, 4);

    companion object {
        fun find(floor: Int, tableNumber: Int): HomebaseTable? {
            return values().find {
                it.floor == floor && it.tableNumber == tableNumber
            }
        }
    }
}