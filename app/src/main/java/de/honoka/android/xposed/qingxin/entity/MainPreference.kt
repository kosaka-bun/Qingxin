package de.honoka.android.xposed.qingxin.entity

class MainPreference {

    /**
     * 屏蔽所有首页推广
     */
    var blockAllMainPagePublicity: Boolean? = null

    /**
     * 屏蔽所有热搜
     */
    var blockAllHotSearchWords: Boolean? = null

    /**
     * 还原所有竖屏视频
     */
    var convertAllVerticalAv: Boolean? = null

    /**
     * 是否在框架控制台输出拦截日志
     */
    var showBlockLog: Boolean? = null

    /**
     * 是否在拦截时发出气泡信息，以显示拦截条数等信息
     */
    var toastOnBlock: Boolean? = null

    /**
     * 调试模式（会在控制台输出很多调试信息）
     */
    var testMode: Boolean? = null

    /**
     * 屏蔽所有推荐话题
     */
    var blockRecommendedTopics: Boolean? = null

    /**
     * 禁用播放器长按加速
     */
    var disablePlayerLongPress: Boolean? = null

    companion object {

        @JvmStatic
        val defaultPreference: MainPreference
            get() {
                val mainPreference = MainPreference()
                mainPreference.blockAllMainPagePublicity = false
                mainPreference.blockAllHotSearchWords = false
                mainPreference.convertAllVerticalAv = false
                mainPreference.showBlockLog = false
                mainPreference.toastOnBlock = false
                mainPreference.testMode = true
                mainPreference.blockRecommendedTopics = false
                mainPreference.disablePlayerLongPress = false
                return mainPreference
            }
    }
}