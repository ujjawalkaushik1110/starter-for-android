package io.appwrite.starterkit.constants

/**
 * Appwrite integration constants.
 *
 * This object holds values related to the Appwrite server setup,
 * including version, project details, and API endpoint.
 */
object AppwriteConfig {
    /**
     * Appwrite Server version.
     */
    const val APPWRITE_VERSION = "1.6.0"

    /**
     * Appwrite project id.
     */
    const val APPWRITE_PROJECT_ID = "692d916e003a4b7dbd36"

    /**
     * Appwrite project name.
     */
    const val APPWRITE_PROJECT_NAME = "Testing"

    /**
     * Appwrite server endpoint url.
     */
    const val APPWRITE_PUBLIC_ENDPOINT = "https://nyc.cloud.appwrite.io/v1"

    /**
     * Database and collection identifiers used for syncing the scheduler data.
     * Replace the placeholder values with your actual Appwrite IDs.
     */
    const val APPWRITE_DATABASE_ID = "schedulewise"
    const val APPWRITE_CLASSES_COLLECTION_ID = "classes"
    const val APPWRITE_TASKS_COLLECTION_ID = "tasks"
}
