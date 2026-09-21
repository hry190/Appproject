package com.jueqiao.jianghu.nav

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 后山板块跳转逻辑的回归测试。
 *
 * 「后山」= Houshan1..Houshan11 屏。路由端命名沿革见 [Routes] 顶 KDoc(早期 React Native / Expo 时代
 * 用的就是 `shilian / shilian2..11`,所以本项目里 **`Routes` 用 Shilian*,`Screen/Actions` 用 Houshan***,
 * 两套名字并存是历史有意保留,不是错位)。
 *
 * 这套测试只锁"声明层"事实,不实例化 Composable:
 *   1. 11 条后山路由字符串 (`Shilian` ~ `Shilian11`) 必须存在且值正确;
 *   2. [JianghuNavHost] 里每条 `composable(Routes.ShilianN)` 都要为该页的 onOpenHoushan{M>1}
 *      传入 `navController.navigate(Routes.ShilianM)` —— 防止"把字段接到错的路由"或"漏接";
 *   3. 每条 `onOpenHoushanM` 必须存在一对(`M` 字段 / `ShilianM` 路由),防止"字段改了名但 NavHost 没跟上"。
 *
 * 不动依赖(只 junit4,跑在 JVM),不依赖 Compose runtime。
 */
class HoushanNavigationTest {

    private val navHostSource: String by lazy {
        java.io.File(
            "src/main/java/com/jueqiao/jianghu/nav/JianghuNavHost.kt",
        ).readText(Charsets.UTF_8)
    }

    // ── 1. Routes 全集 ──────────────────────────────────────────────────────

    @Test
    fun houshanRouteConstantsHaveExpectedStringValues() {
        // 期望值对齐 Routes.kt 顶 KDoc(后山 1 页 = "shilian";2~11 页沿用 expo-router 时代命名)
        assertEquals("shilian", Routes.Shilian)
        assertEquals("shilian2", Routes.Shilian2)
        assertEquals("shilian3", Routes.Shilian3)
        assertEquals("shilian4", Routes.Shilian4)
        assertEquals("shilian5", Routes.Shilian5)
        assertEquals("shilian6", Routes.Shilian6)
        assertEquals("shilian7", Routes.Shilian7)
        assertEquals("shilian8", Routes.Shilian8)
        assertEquals("shilian9", Routes.Shilian9)
        assertEquals("shilian10", Routes.Shilian10)
        assertEquals("shilian11", Routes.Shilian11)
    }

    // ── 2. NavHost 里每条 Houshan 页都有 composable 声明 ──────────────────────

    @Test
    fun navHostRegistersComposableForEveryHoushanPage() {
        // Shilian .. Shilian11 都要在 NavHost 里 composable 注册过(否则点回去会丢页)
        // 容许多种写法:`composable(Routes.ShilianN) {` 或
        //   `composable(\n    route = Routes.ShilianN,\n    enterTransition = { ... }, ...) {`
        for (n in 1..11) {
            // 1 写 Shilian(无后缀,沿用历史命名),2..11 写 Shilian2..Shilian11
            val route = if (n == 1) "Routes.Shilian" else "Routes.Shilian$n"
            val pattern = Regex("""composable\s*\([^)]*?$route\b""", RegexOption.DOT_MATCHES_ALL)
            assertTrue(
                "NavHost 缺少 composable($route) 声明 — 后山 $n 页无法被导航到达",
                pattern.containsMatchIn(navHostSource),
            )
        }
    }

    // ── 3. 后山跳转闭包(本页 → 后山 M)都接到了正确的 ShilianM 路由 ──────────
    //
    // 后山 1 页只能向后跳到 2;2~10 页都能向后跳到 N+1、还可能向前跳 2 个邻居;
    // 11 页是终页(只有 onBack)。
    // 这里只验「字段名 N → 路由名 ShilianN」一一对应(防字段重命名/路由拼错),
    // 不强制"必须有哪些 onOpenHoushan"—— 那是 Screen 内部产品决策,改起来要人 review。
    //
    // 例:对 NavHost 文本里的 `onOpenHoushan7 = { navController.navigate(Routes.Shilian7) }`,
    //    应当 N=7。

    @Test
    fun everyOnOpenHoushanNavPointsAtMatchingShilianRoute() {
        // 抓所有 onOpenHoushanN = { navController.navigate(Routes.ShilianM) } 的 (N, M) 对
        val pattern = Regex("""onOpenHoushan(\d+)\s*=\s*\{[^}]*navigate\(Routes\.Shilian(\d+)\)""")
        val matches = pattern.findAll(navHostSource).toList()
        assertTrue(
            "NavHost 没有任何 onOpenHoushan = navigate(Routes.Shilian*) 的声明,可能正则过期或 NavHost 整体被改写",
            matches.any(),
        )
        for (m in matches) {
            val fieldN = m.groupValues[1]
            val routeM = m.groupValues[2]
            assertEquals(
                "后山跳转闭包不一致:onOpenHoushan$fieldN 跳到了 Routes.Shilian$routeM,字段序号 N 与路由序号 M 必须相等",
                fieldN,
                routeM,
            )
        }
    }

    @Test
    fun everyShilianRouteIsReachableByAtLeastOneHoushanPage() {
        // Shilian2..Shilian11 都应该至少被一条 onOpenHoushan{N} = navigate(Routes.ShilianM) 引用
        // (否则该页没有任何"上游"会跳到它,就是死页)
        // Shilian 自身入口在 Gunlun1Screen 的"后山"按钮(在另一文件),不要求本文件覆盖
        for (m in 2..11) {
            val token = "navigate(Routes.Shilian$m)"
            assertTrue(
                "Routes.Shilian$m(后山 $m 页)没有 onOpenHoushan* 跳到它,是死页",
                navHostSource.contains(token),
            )
        }
    }

    // ── 4. Actions 字段声明(只校验「必有」,不锁全部列表) ──────────────────
    //
    // 必含字段:onBack(所有页都该有,否则 BackHandler 会爆)。
    // 通过反射从编译产物取,而不是 grep 源码 —— 这样字段重命名/默认值变化也能验到。

    @Test
    fun everyHoushanActionsHasOnBackField() {
        for (n in 1..11) {
            val cls = Class.forName("com.jueqiao.jianghu.ui.screens.houshan$n.Houshan${n}Actions")
            val names = cls.declaredFields.map { it.name }
            assertTrue(
                "Houshan${n}Actions 缺少 onBack 字段(BackHandler 依赖它,缺了会爆);实际字段=$names",
                names.contains("onBack"),
            )
        }
    }
}
