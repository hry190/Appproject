package com.jueqiao.jianghu.ui.screens.chuangdang

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

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
 *   §6.2 闯荡令:持有上限 3 枚。
 */
object ChuangdangStore {

    /** 闯荡令余额(上限 [CD_TOKENS_MAX])。 */
    var tokens by mutableStateOf(CD_TOKENS_MAX)
        private set

    /** 已通关的普通关序号(1..4)。 */
    var clearedStages by mutableStateOf(emptySet<Int>())
        private set

    /** Boss(第 5 关)是否已通关。 */
    var bossCleared by mutableStateOf(false)
        private set

    /** 是否正处于一次"正式出发"中:出发时扣令,失败或通关 Boss 后结束。 */
    var runActive by mutableStateOf(false)
        private set

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

    /** 结束当前出发(战败、主动撤退,或击败 Boss)。 */
    fun endRun() {
        runActive = false
    }

    /** 记录一个普通关通关。 */
    fun clearStage(stage: Int) {
        clearedStages = clearedStages + stage
    }

    /** 记录 Boss 通关。 */
    fun clearBoss() {
        bossCleared = true
        runActive = false
    }

    /** 全部重置(调试 / 重新开始用)。 */
    fun reset() {
        tokens = CD_TOKENS_MAX
        clearedStages = emptySet()
        bossCleared = false
        runActive = false
        lastBossResult = null
    }
}
