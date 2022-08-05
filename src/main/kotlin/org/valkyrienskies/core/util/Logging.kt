package org.valkyrienskies.core.util

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.message.Message
import org.apache.logging.log4j.spi.AbstractLogger
import kotlin.reflect.KProperty

fun logger(): DelegateLogger = DelegateLogger
fun logger(name: String): VSLogger = VSLogger(LogManager.getLogger(name))

object DelegateLogger {
    operator fun provideDelegate(thisRef: Any, property: KProperty<*>) =
        VSLogger(
            LogManager.getLogger(
                if (thisRef::class.isCompanion)
                    thisRef::class.java.declaringClass
                else
                    thisRef::class.java
            )
        )
}

class VSLogger(val std: Logger) : AbstractLogger() {

    fun visError(message: String, t: Throwable? = null) {
        std.error(message, t)
        traceData(message, t)
    }

    fun visWarn(message: String, t: Throwable? = null) {
        std.warn(message, t)
        traceData(message, t)
    }

    fun <T> action(name: String, lambda: Transaction.() -> T): T =
        DataCollection.action(name, lambda)

    fun hint(name: String, value: Any?) =
        DataCollection.hint(name, value)

    private fun traceData(message: String, t: Throwable? = null) {
        if (DataCollection.isCollecting) DataCollection.log(
            Level.TRACE,
            null,
            message,
            t
        )
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): VSLogger = this

    override fun getLevel(): Level = if (DataCollection.isCollecting) Level.ALL else std.level

    override fun isEnabled(level: Level, marker: Marker?, message: Message, t: Throwable?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: CharSequence, t: Throwable?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: Any?, t: Throwable?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String, t: Throwable?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String, vararg params: Any?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String, p0: Any?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?): Boolean =
        this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?, p5: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?, p5: Any?,
        p6: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?, p5: Any?,
        p6: Any?, p7: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?, p5: Any?,
        p6: Any?, p7: Any?, p8: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun isEnabled(
        level: Level, marker: Marker?, message: String, p0: Any?, p1: Any?, p2: Any?, p3: Any?, p4: Any?, p5: Any?,
        p6: Any?, p7: Any?, p8: Any?, p9: Any?
    ): Boolean = this.level.intLevel() >= level.intLevel()

    override fun logMessage(fqcn: String, level: Level, marker: Marker?, message: Message, t: Throwable?) {
        std.log(level, marker, message, t)

        if (DataCollection.isCollecting) {
            DataCollection.log(
                level,
                marker,
                message.formattedMessage,
                t
            )
        }
    }
}
