package org.valkyrienskies.core.util

import io.sentry.Breadcrumb
import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.SentryLevel.ERROR
import io.sentry.SentryLevel.FATAL
import io.sentry.SentryLevel.WARNING
import io.sentry.SpanStatus
import io.sentry.protocol.App
import io.sentry.protocol.User
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Marker
import org.valkyrienskies.core.config.VSCoreConfig

object DataCollection {

    val isCollecting = true

    init {
        Sentry.init {
            var dsn = ""

            if (dsn == "")
                dsn = VSCoreConfig.SERVER.sentryUrl

            if (dsn == "")
                dsn = VSCoreConfig.CLIENT.sentryUrl


            it.dsn = dsn
            it.tracesSampleRate = 1.0
            it.environment = "dev"// TODO get actual environment
        }

        Sentry.configureScope {
            it.level = WARNING
            it.contexts.setApp(App().apply {
                appName = "Valkyrien Skies"
                appVersion = "2.0" // TODO get actual number
            })
            it.user = User().apply {
                username = "TODO" // TODO get actual username (or shouldn't we?)
            }
        }
    }

    fun log(level: Level, marker: Marker?, message: String, t: Throwable?) {
        if (Level.ERROR.intLevel() >= level.intLevel()) {
            if (Level.ERROR.intLevel() == level.intLevel())
                Sentry.captureMessage(message, ERROR)
            else
                Sentry.captureMessage(message, FATAL)
        } else {

            if (Level.WARN.intLevel() >= level.intLevel())
                Sentry.captureMessage(message, WARNING)
            else Sentry.addBreadcrumb(Breadcrumb(message).apply {
                setData("thread", Thread.currentThread().name)
                if (marker != null) setData("marker", marker)
            })
        }

        if (t != null) {
            Sentry.captureException(t)
        }
    }

    private val _transaction = ThreadLocal<Transaction>()
    private val transaction: Transaction?
        get() = _transaction.get()

    fun <T> action(origin: String, name: String, lambda: Transaction.() -> T): T {
        val fullName = "$origin:$name"

        if (transaction == null) {
            val trans = Sentry.startTransaction("${Thread.currentThread().name}|$fullName", fullName)
            val wrap = Transaction(trans)
            _transaction.set(wrap)
            try {
                return wrap.lambda()
            } catch (e: Exception) {
                trans.throwable = e
                trans.status = SpanStatus.INTERNAL_ERROR
                throw e
            } finally {
                trans.finish()
            }
        } else {
            return transaction!!.child(fullName, lambda)
        }
    }

    fun hint(origin: String, name: String, value: Any?) {
        transaction?.hint("$origin:$name", value)
    }
}

@JvmInline
value class Transaction(internal val transaction: ISpan) {

    fun <T> child(name: String, lambda: Transaction.() -> T): T {
        val trans = transaction.startChild(name)
        try {
            return Transaction(trans).lambda()
        } catch (e: Exception) {
            trans.throwable = e
            trans.status = SpanStatus.INTERNAL_ERROR
            throw e
        } finally {
            trans.finish()
        }
    }

    fun hint(name: String, data: Any?) = transaction.setData(name, data ?: "null")
}
