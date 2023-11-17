package com.sd.lib.log

sealed class FLogLevel(
    private val level: Int,
) : Comparable<FLogLevel> {

    object All : FLogLevel(Int.MIN_VALUE)
    object Debug : FLogLevel(100)
    object Info : FLogLevel(200)
    object Waring : FLogLevel(300)
    object Error : FLogLevel(400)
    object Off : FLogLevel(Int.MAX_VALUE)

    override fun compareTo(other: FLogLevel): Int {
        return this.level.compareTo(other.level)
    }
}