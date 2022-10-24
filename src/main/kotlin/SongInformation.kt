package com.sakurawald.plum.reloaded

//用于描述一首音乐的对象
class SongInformation(
    var music_Name: String = "",
    var music_ID: Long = 0,
    var music_Length: Int = 0,
    var music_introduction: String = "",
    var music_Page_URL: String = "",
    var author: String = "",
    var music_File_URL: String = "",
    var music_MID: String = "",
    var img_URL: String = "",
    var hash: String = ""
) {

    // 描述这首歌来源于哪里：网易，酷狗，QQ音乐等
    var sourceType: String = ""

    // 对该音乐的总结.
    var summary = ""

    override fun toString(): String = "SongInformation{" +
                "music_Name='$music_Name'" +
                ", music_ID=$music_ID" +
                ", music_Length=$music_Length" +
                ", music_introduction='$music_introduction'" +
                ", music_Page_URL='$music_Page_URL'" +
                ", author='$author'" +
                ", music_File_URL='$music_File_URL'" +
                ", music_MID='$music_MID'" +
                ", img_URL='$img_URL'" +
                ", hash='$hash'" +
                ", sourceType='$sourceType'" +
                ", summary='$summary'}"


    companion object {
        fun getSongInformationsBody(sis: ArrayList<SongInformation>): String {
            val sb = StringBuffer()
            for (si in sis) {
                sb.append(
                    """
    【歌曲】${si.music_Name}
    〖作者〗${si.author}
    〖URL〗${si.music_File_URL}
    
    
    """.trimIndent()
                )
            }
            return sb.toString().trim { it <= ' ' }
        }
    }
}