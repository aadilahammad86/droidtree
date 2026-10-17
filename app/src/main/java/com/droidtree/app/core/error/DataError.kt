package com.droidtree.app.core.error

/**
 * Domain-mapped error boundary for the DroidTree data layer.
 * Implements guidelines from android-data-layer skill.
 */
sealed class DataError(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class PermissionDenied(val permission: String) : DataError("Storage access permission required: $permission")
    class StorageUnavailable(message: String) : DataError(message)
    class ScanFailed(val path: String, cause: Throwable) : DataError("Failed scanning path: $path", cause)
    class LocalIo(cause: Throwable) : DataError("Local I/O error occurred", cause)
    class Network(cause: Throwable) : DataError("Network sync failure", cause)
    class ExportError(message: String, cause: Throwable? = null) : DataError(message, cause)
}
