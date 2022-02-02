package me.ore.swing.ext


/**
 * Miscellaneous utility methods
 */
object OreSwingExtUtils {
    /**
     * Error handling
     *
     * Tries to handle the error by iterating over the options:
     * * error handling with [Thread.uncaughtExceptionHandler] of the current thread
     * * error handling with [Thread.defaultUncaughtExceptionHandler]
     * * error output with [Throwable.printStackTrace] ([System.err])
     *
     * After the first variant that worked, it aborts the execution
     *
     * This method is thread-safe
     *
     * @param exception Error to handle
     */
    fun handle(exception: Exception) {
        val thread = Thread.currentThread()
        var processed = false

        // region Use handler of current thread
        try {
            thread.uncaughtExceptionHandler?.let {
                it.uncaughtException(thread, exception)
                processed = true
            }
        } catch (e: Exception) {
            exception.addSuppressed(e)
        }
        // endregion
        if (processed) return

        // region Use default handler
        try {
            Thread.getDefaultUncaughtExceptionHandler()?.let {
                it.uncaughtException(thread, exception)
                processed = true
            }
        } catch (e: Exception) {
            exception.addSuppressed(e)
        }
        // endregion
        if (processed) return

        // Write to console
        exception.printStackTrace(System.err)
    }
}
