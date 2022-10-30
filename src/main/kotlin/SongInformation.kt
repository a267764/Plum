package com.sakurawald.plum.reloaded

//用于描述一首音乐的对象
class SongInformation(
    var name: String = "",
    var id: Long = 0,
    var length: Int = 0,
    var introduction: String = "",
    var pageUrl: String = "",
    var author: String = "",
    var fileUrl: String = "",
    var mid: String = "",
    var imageUrl: String = "",
    var hash: String = "",
    // 描述这首歌来源于哪里：网易，酷狗，QQ音乐等
    var sourceType: String = "",
    // 对该音乐的总结.
    var summary: String = ""
) {

    override fun toString(): String = "SongInformation{" +
            "music_Name='$name'" +
            ", id=$id" +
            ", length=$length" +
            ", introduction='$introduction'" +
            ", pageUrl='$pageUrl'" +
            ", author='$author'" +
            ", fileUrl='$fileUrl'" +
            ", mid='$mid'" +
            ", imageUrl='$imageUrl'" +
            ", hash='$hash'" +
            ", sourceType='$sourceType'" +
            ", summary='$summary'}"


    companion object {
        fun getSongInformationsBody(sis: List<SongInformation>): String {
            val sb = StringBuffer()
            for (si in sis) {
                sb.append(
                    """
    【歌曲】${si.name}
    〖作者〗${si.author}
    〖URL〗${si.fileUrl}
    
    
    """.trimIndent()
                )
            }
            return sb.toString().trim { it <= ' ' }
        }
    }
}