package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Calendar
import java.util.TimeZone

/**
 * 2026-09-19 §6 闯荡江湖 —— 进程内的进度状态
 *
 * 用户指令:"先把页面做出来,先不接后端" —— 因此这里用 Compose 的 [mutableStateOf] 持有进度,
 * 进程存活期间跨页面共享(地图页 ↔ 战斗页 ↔ Boss 页),**不落库、不联网**。
 * 日后接后端时,把这个 object 换成注入的 repository 即可,页面侧的读取方式不用变。
 *
 * 规则出处(策划方案 v2):
 *   §6.1 一次正式出发:支付一枚闯荡令,从当前未通关节点开始,可连续推进到 Boss;
 *        失败 / 主动撤退 / 击败 Boss 时该次出发结束。顺利通过普通关后继续下一关**不额外扣令**。
 *   §6.2 闯荡令:持有上限 3 枚;按北京时间自然日恢复 1 枚,累积不超过上限。
 *   §2   地图需展示"闯荡令余额**和下次恢复说明**"。
 */
object ChuangdangStore {

    /** 闯荡令余额(上限 [CD_TOKENS_MAX])。 */
    var tokens by mutableStateOf(CD_TOKENS_MAX)
        private set

    /** 北京时间(GMT+8)。文档 §6.2 要求"按北京时间自然日",故不随设备时区变化。 */
    private val beijingZone: TimeZone = TimeZone.getTimeZone("GMT+8")

    /** 上次结算恢复的自然日(yyyy-MM-dd,北京时间);跨日时补发 1 枚。 */
    private var lastRestoreDay: String = beijingDayKey()

    /** 已通关的普通关序号(1..4)。 */
    var clearedStages by mutableStateOf(emptySet<Int>())
        private set

    /** Boss(第 5 关)是否已通关。 */
    var bossCleared by mutableStateOf(false)
        private set

    /** 是否正处于一次"正式出发"中:出发时扣令,失败或通关 Boss 后结束。 */
    var runActive by mutableStateOf(false)
        private set

    /**
     * 本次出发**已经进入过**的关卡(1~5)。
     *
     * 用途:从战斗页返回地图后,该关显示"继续"而不是"可挑战"(用户 2026-09-19 要求)。
     * 以下情况清空:该关通关、本次出发结束、Boss 通关、整体重置。
     */
    var startedStage by mutableStateOf<Int?>(null)
        private set

    /** 记录"已进入某关"(进入战斗页时调用)。 */
    fun markStageEntered(stage: Int) {
        startedStage = stage
    }

    /**
     * **已读过关前剧情**的关卡序号(文档 §2:「已读剧情可跳过」)。
     *
     * 只影响剧情页是否还拦在前面,不参与任何判定 —— 首次进入某关时展示 [CdStage.scene],
     * 之后重进不再挡路(但仍可从战斗页顶部「重看剧情」把面板调回来)。
     */
    var readScenes by mutableStateOf(emptySet<Int>())
        private set

    /** 标记某关的关前剧情已读。 */
    fun markSceneRead(stage: Int) {
        readScenes = readScenes + stage
    }

    /** 该关关前剧情是否已读过(读过则不再自动展示)。 */
    fun hasReadScene(stage: Int): Boolean = stage in readScenes

    /** 最近一次 Boss 评审结果(用于地图页展示与重看)。 */
    var lastBossResult by mutableStateOf<CdBossResult?>(null)

    /** 当前应当挑战的关卡:第一个未通关的普通关;四关全通后指向 Boss(5)。 */
    val currentStage: Int
        get() = (1..4).firstOrNull { it !in clearedStages } ?: CD_BOSS_INDEX

    /** 关卡 [stage] 是否已解锁(只能打到当前关,不能跳关)。 */
    fun isUnlocked(stage: Int): Boolean = stage <= currentStage

    /**
     * 开始一次出发:若已经在出发中则直接放行(连续推进不重复扣令);
     * 否则扣一枚闯荡令。返回 false 表示令已用尽,无法出发。
     */
    fun beginRun(): Boolean {
        if (runActive) return true
        if (tokens <= 0) return false
        tokens -= 1
        runActive = true
        return true
    }

    /** 结束当前出发(战败、或击败 Boss)。 */
    fun endRun() {
        runActive = false
        startedStage = null
    }

    /** 记录一个普通关通关;若正是本次进入的那一关,同时清掉"继续"标记。 */
    fun clearStage(stage: Int) {
        clearedStages = clearedStages + stage
        if (startedStage == stage) startedStage = null
    }

    /** 记录 Boss 通关。 */
    fun clearBoss() {
        bossCleared = true
        runActive = false
        startedStage = null
    }

    // ── 闯荡令恢复(文档 §6.2 的"自然恢复" + §2 的"下次恢复说明")──────────────

    /**
     * 按北京时间自然日补发闯荡令:跨过一天就 +1,累积不超过上限。
     *
     * 采用"进入页面时结算"而不是后台定时任务 —— 与文档 §10 的建议一致
     * ("每日恢复可以在用户打开页面或开始挑战时,按服务器时间和上次恢复日期补充……
     *  不必为每个用户新增定时任务")。接后端后应改为由服务端时间判定。
     */
    fun settleDailyRestore() {
        val today = beijingDayKey()
        if (today != lastRestoreDay) {
            lastRestoreDay = today
            tokens = (tokens + 1).coerceAtMost(CD_TOKENS_MAX)
        }
    }

    /** 下次恢复说明:距北京时间次日 0 点还有多久;余额已满时说明不再累积。 */
    fun nextRestoreText(): String {
        if (tokens >= CD_TOKENS_MAX) return "已满 $CD_TOKENS_MAX 枚 · 不再累积"
        val ms = millisUntilNextBeijingDay()
        val hours = ms / 3_600_000
        val minutes = (ms % 3_600_000) / 60_000
        return "下次恢复:今日 24:00(+1 枚)· 还有 %d 小时 %02d 分".format(hours, minutes)
    }

    /** 今日的北京时间日期键,如 "2026-09-19"。 */
    private fun beijingDayKey(): String {
        val c = Calendar.getInstance(beijingZone)
        return "%04d-%02d-%02d".format(
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH) + 1,
            c.get(Calendar.DAY_OF_MONTH),
        )
    }

    /** 距北京时间次日 0 点的毫秒数。 */
    private fun millisUntilNextBeijingDay(): Long {
        val c = Calendar.getInstance(beijingZone)
        val now = c.timeInMillis
        c.add(Calendar.DAY_OF_MONTH, 1)
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return (c.timeInMillis - now).coerceAtLeast(0L)
    }

    /** 全部重置(调试 / 重新开始用)。 */
    fun reset() {
        tokens = CD_TOKENS_MAX
        clearedStages = emptySet()
        bossCleared = false
        runActive = false
        lastBossResult = null
        lastRestoreDay = beijingDayKey()
        startedStage = null
        readScenes = emptySet()
    }
}
