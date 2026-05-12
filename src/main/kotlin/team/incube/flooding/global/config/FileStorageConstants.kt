package team.incube.flooding.global.config

object FileStorageConstants {
    const val IMAGE_URL_PREFIX = "/images"
    const val CLUB_IMAGE_SUB_DIR = "clubs"
    val IMAGE_RESOURCE_SUB_DIRS = setOf(CLUB_IMAGE_SUB_DIR)
    val IMAGE_RESOURCE_PATTERNS = IMAGE_RESOURCE_SUB_DIRS.map { "$IMAGE_URL_PREFIX/$it/**" }.toTypedArray()
}
