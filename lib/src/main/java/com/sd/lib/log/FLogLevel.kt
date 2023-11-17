package com.sd.lib.log

sealed class FLogLevel(
    val tag: String,
    private val level: Int,
) : Comparable<FLogLevel> {

    object All : FLogLevel("A", Int.MIN_VALUE)
    object Debug : FLogLevel("D", 100)
    object Info : FLogLevel("I", 200)
    object Waring : FLogLevel("W", 300)
    object Error : FLogLevel("E", 400)
    object Off : FLogLevel("O", Int.MAX_VALUE)

    override fun compareTo(other: FLogLevel): Int {
        return this.level.compareTo(other.level)
    }
}