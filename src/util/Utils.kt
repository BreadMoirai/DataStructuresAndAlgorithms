package util

import java.time.Duration

/**
 * @author TonTL
 * @version 3/1/2018
 */
fun mst(l: Long): String {
    val d = Duration.ofNanos(l)
    return when {
        d.toNanos() < 1_000_000 -> "${d.toNanos()} ns"
        d.toMillis() < 1_000 -> "${d.toMillis()}.${d.toNanos().rem(1_000_000).div(1_000)} ms"
        d.seconds < 60 -> "${d.seconds}.${d.toMillis().rem(1_000).div(10)} s"
        else -> "${d.toMinutes()}.${d.seconds.rem(60).div(60)} m"
    }
}