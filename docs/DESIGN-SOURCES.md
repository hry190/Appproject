# 设计稿来源清单(DESIGN-SOURCES)

> **创建于 2026-09-20**。起因:`D:\图\`(设计稿源目录,本地、不在 git)**计划可能被删除**,
> 删掉之后注释里的文件名就「无人可证」了,目录里没被引用的文件更是连名字都不剩 ——
> 所以这里把「代码用过哪些设计稿、对应仓库里哪个资源」固化成一份可查的清单。

## 怎么用 / 怎么维护

- **查来源**:想知道 `img_xxx.png` 从哪张设计稿来 → 在下表搜资源名,或搜设计稿名。
- **加素材**:导入新图时,顺手在表里补一行(`设计稿名 → img_资源名`);**代码注释里不要再写盘符路径**(规则见 [CONTRIBUTING.md](../CONTRIBUTING.md) §9)。
- **复现**:`python scripts/design-sources-inventory.py --write`(从代码注释 + 目录实时重算)。

## 统计(导出时快照)

| 项 | 值 |
|---|---|
| 导出时间 | 2026-09-20 11:06 |
| 代码注释里的引用行 | **579** 行 |
| 设计稿源目录 | `D:\图` —— **312 个文件 / 332.2 MB** |
| 被代码引用到的设计稿 | **295** 个 |
| 其中同行已写项目内资源名 | 295 个 |
| 目录里**内容已在仓库**(逐字节一致)| **294** / 312 个 |
| 注释里**查不到出处**的目录文件 | **17** 个 → 其中 0 个内容已在仓库、**17 个真正找不到对应**

## ① 代码引用过的设计稿 → 项目内资源

| 设计稿文件名 | 项目内资源名 | 引用处(最多 3 个)|
|---|---|---|
| `98.png` | `img_volume1part8_98` | Volume1Part8Screen.kt:42 |
| `Group 196.png` | `img_learning_group_196` | Learning2Screen.kt:42 |
| `Group 255.png` | `img_volume1_group_255` | Volume1Screen.kt:39 · Volume10Part1Screen.kt:47 · Volume10Part10Screen.kt:49 等 92 处 |
| `Group 256.png` | `img_volume1part10_group_256`, `img_volume1part2_group_256` | Volume10Part11Screen.kt:47 · Volume10Part13Screen.kt:47 · Volume10Part2Screen.kt:47 等 50 处 |
| `Group 281.png` | `img_learning_group_281` | LearningScreen.kt:31 |
| `Mask group.png` | `img_volume1part6_mask_group` | Volume1Part6Screen.kt:41 |
| `Rectangle 24.png` | `img_chuangzuodangan6_rect24` | Chuangzuodangan6Screen.kt:206 |
| `Rectangle 86.png` | `img_gunlun1_rect86` | Gunlun1Screen.kt:76 |
| `group.png` | `img_volume1part7_group` | Volume1Part7Screen.kt:42 |
| `image 0.png` | `img_volume4part14_image_0` | Volume4Part14Screen.kt:46 |
| `image 01.png` | `img_volume5part10_image_01` | Volume5Part10Screen.kt:46 |
| `image 101.png` | `img_volume5part12_image_101` | Volume5Part12Screen.kt:45 |
| `image 129.png` | `img_volume1_bg` | Volume1Screen.kt:38 · Volume10Part1Screen.kt:46 · Volume10Part10Screen.kt:48 等 142 处 |
| `image 130.png` | `img_volume6part12_image_130` | Volume6Part12Screen.kt:47 |
| `image 174.png` | `img_learning_image_174` | Learning2Screen.kt:41 |
| `image 2.png` | `img_volume6part13_image_2`, `img_volume6part14_image_2` | Volume6Part13Screen.kt:46 · Volume6Part14Screen.kt:45 |
| `image 21.png` | `img_volume6part8_image_21` | Volume6Part8Screen.kt:46 |
| `image 230.png` | `img_volume1_image_230` | Volume1Screen.kt:41 |
| `image 231.png` | `img_volume1part3_image_231` | Volume1Part3Screen.kt:40 |
| `image 232.png` | `img_volume1part3_image_232` | Volume1Part3Screen.kt:41 |
| `image 233.png` | `img_volume1_image_233` | Volume1Screen.kt:40 |
| `image 234.png` | `img_volume1part2_image_234` | Volume1Part2Screen.kt:46 |
| `image 236.png` | `img_volume1part4_image_236` | Volume1Part4Screen.kt:41 |
| `image 237.png` | `img_volume1part4_image_237` | Volume1Part4Screen.kt:40 |
| `image 238.png` | `img_volume1part5_image_238` | Volume1Part5Screen.kt:42 |
| `image 239.png` | `img_volume1part5_image_239` | Volume1Part5Screen.kt:43 |
| `image 240.png` | `img_volume1part6_image_240` | Volume1Part6Screen.kt:40 |
| `image 243.png` | `img_volume1part7_image_243` | Volume1Part7Screen.kt:41 |
| `image 245.png` | `img_volume1part7_image_245` | Volume1Part7Screen.kt:43 |
| `image 246.png` | `img_volume1part8_image_246` | Volume1Part8Screen.kt:41 |
| `image 250.png` | `img_volume1part9_image_250` | Volume1Part9Screen.kt:40 |
| `image 253.png` | `img_volume1part10_image_253` | Volume1Part10Screen.kt:40 |
| `image 257.png` | `img_volume1part11_image_257` | Volume1Part11Screen.kt:40 |
| `image 259.png` | `img_volume1part11_image_259` | Volume1Part11Screen.kt:41 |
| `image 265.png` | `img_volume1part12_image_265` | Volume1Part12Screen.kt:41 |
| `image 267.png` | `img_volume1part13_image_267` | Volume1Part13Screen.kt:40 |
| `image 268.png` | `img_volume1part13_image_268` | Volume1Part13Screen.kt:41 |
| `image 269.png` | `img_volume1part14_image_269` | Volume1Part14Screen.kt:39 |
| `image 270.png` | `img_volume1part12_image_270` | Volume1Part12Screen.kt:40 |
| `image 272.png` | `img_volume2part1_image_272` | Volume2Part1Screen.kt:40 |
| `image 273.png` | `img_volume2part1_image_273` | Volume2Part1Screen.kt:41 |
| `image 274.png` | `img_volume2part2_image_274` | Volume2Part2Screen.kt:40 |
| `image 275.png` | `img_volume2part2_image_275` | Volume2Part2Screen.kt:41 |
| `image 276.png` | `img_volume2part3_image_276` | Volume2Part3Screen.kt:40 |
| `image 277.png` | `img_volume2part3_image_277` | Volume2Part3Screen.kt:41 |
| `image 279.png` | `img_volume2part4_image_279` | Volume2Part4Screen.kt:40 |
| `image 281.png` | `img_volume2part4_image_281` | Volume2Part5Screen.kt:41 |
| `image 282.png` | `img_volume2part4_image_282` | Volume2Part4Screen.kt:41 |
| `image 283.png` | `img_volume2part4_image_283` | Volume2Part5Screen.kt:42 |
| `image 284.png` | `img_volume2part4_image_284` | Volume2Part5Screen.kt:43 |
| `image 285.png` | `img_volume2part6_image_285` | Volume2Part6Screen.kt:39 |
| `image 289.png` | `img_volume2part7_image_289` | Volume2Part8Screen.kt:41 |
| `image 290.png` | `img_volume2part8_image_290` | Volume2Part8Screen.kt:40 |
| `image 291.png` | `img_volume2part7_image_291` | Volume2Part7Screen.kt:40 |
| `image 292.png` | `img_volume2part7_image_292` | Volume2Part7Screen.kt:41 |
| `image 293.png` | `img_volume2part9_image_293` | Volume2Part9Screen.kt:39 |
| `image 294.png` | `img_volume2part10_image_294` | Volume2Part10Screen.kt:40 |
| `image 295.png` | `img_volume2part10_image_295` | Volume2Part10Screen.kt:41 |
| `image 296.png` | `img_volume2part11_image_296` | Volume2Part11Screen.kt:40 |
| `image 297.png` | `img_volume2part11_image_297` | Volume2Part11Screen.kt:41 |
| `image 299.png` | `img_volume2part12_image_299` | Volume2Part12Screen.kt:40 |
| `image 30.png` | `img_volume1part2_image_30` | Volume1Part2Screen.kt:45 |
| `image 300.png` | `img_volume2part12_image_300` | Volume2Part12Screen.kt:41 |
| `image 301.png` | `img_volume2part13_image_301` | Volume2Part13Screen.kt:40 |
| `image 302.png` | `img_volume2part13_image_302` | Volume2Part13Screen.kt:41 |
| `image 303.png` | `img_volume2part14_image_303` | Volume2Part14Screen.kt:40 |
| `image 304.png` | `img_volume2part14_image_304` | Volume2Part14Screen.kt:41 |
| `image 305.png` | `img_volume2part15_image_305` | Volume2Part15Screen.kt:40 |
| `image 306.png` | `img_volume2part15_image_306` | Volume2Part15Screen.kt:41 |
| `image 316.png` | `img_volume3part1_image_316` | Volume3Part1Screen.kt:40 |
| `image 319.png` | `img_volume3part1_image_319` | Volume3Part1Screen.kt:41 |
| `image 320.png` | `img_volume3part2_image_320` | Volume3Part2Screen.kt:46 |
| `image 321.png` | `img_volume3part2_image_321` | Volume3Part2Screen.kt:47 |
| `image 323.png` | `img_volume3part3_image_323` | Volume3Part3Screen.kt:45 |
| `image 324.png` | `img_volume3part4_image_324` | Volume3Part4Screen.kt:47 |
| `image 325.png` | `img_volume3part4_image_325` | Volume3Part4Screen.kt:48 |
| `image 326.png` | `img_volume3part4_image_326` | Volume3Part4Screen.kt:49 |
| `image 327.png` | `img_volume3part5_image_327` | Volume3Part5Screen.kt:48 |
| `image 328.png` | `img_volume3part5_image_328` | Volume3Part5Screen.kt:50 |
| `image 329.png` | `img_volume3part5_image_329` | Volume3Part5Screen.kt:49 |
| `image 330.png` | `img_volume3part6_image_330` | Volume3Part6Screen.kt:48 |
| `image 331.png` | `img_volume3part6_image_331` | Volume3Part6Screen.kt:49 |
| `image 333.png` | `img_volume3part7_image_333` | Volume3Part7Screen.kt:49 |
| `image 334.png` | `img_volume3part7_image_334` | Volume3Part7Screen.kt:50 |
| `image 335.png` | `img_volume3part8_image_335` | Volume3Part8Screen.kt:44 |
| `image 336.png` | `img_volume3part9_image_336` | Volume3Part9Screen.kt:47 |
| `image 337.png` | `img_volume3part9_image_337` | Volume3Part9Screen.kt:48 |
| `image 338.png` | `img_volume3part10_image_338` | Volume3Part10Screen.kt:47 |
| `image 339.png` | `img_volume3part10_image_339` | Volume3Part10Screen.kt:48 |
| `image 340.png` | `img_volume3part11_image_340` | Volume3Part11Screen.kt:46 |
| `image 341.png` | `img_volume3part12_image_341` | Volume3Part12Screen.kt:49 |
| `image 342.png` | `img_volume3part12_image_342` | Volume3Part12Screen.kt:50 |
| `image 343.png` | `img_volume3part13_image_343` | Volume3Part13Screen.kt:48 |
| `image 345.png` | `img_volume3part13_image_345` | Volume3Part13Screen.kt:47 |
| `image 346.png` | `img_volume3part14_image_346` | Volume3Part14Screen.kt:46 |
| `image 348.png` | `img_volume4part1_image_348` | Volume4Part1Screen.kt:48 |
| `image 349.png` | `img_volume4part1_image_349` | Volume4Part1Screen.kt:49 |
| `image 352.png` | `img_volume4part2_image_352` | Volume4Part2Screen.kt:47 |
| `image 353.png` | `img_volume4part2_image_353` | Volume4Part2Screen.kt:46 |
| `image 354.png` | `img_volume4part1_image_354` | Volume4Part1Screen.kt:50 |
| `image 355.png` | `img_volume4part3_image_355` | Volume4Part3Screen.kt:46 |
| `image 356.png` | `img_volume4part3_image_356` | Volume4Part3Screen.kt:47 |
| `image 357.png` | `img_volume4part4_image_357` | Volume4Part4Screen.kt:46 |
| `image 358.png` | `img_volume4part4_image_358` | Volume4Part4Screen.kt:47 |
| `image 359.png` | `img_volume4part5_image_359` | Volume4Part5Screen.kt:46 |
| `image 36.png` | `img_volume3part14_image_36` | Volume3Part14Screen.kt:47 |
| `image 360.png` | `img_volume4part5_image_360` | Volume4Part5Screen.kt:47 |
| `image 361.png` | `img_volume4part6_image_361` | Volume4Part6Screen.kt:46 |
| `image 362.png` | `img_volume4part6_image_362` | Volume4Part6Screen.kt:47 |
| `image 364.png` | `img_volume4part7_image_364` | Volume4Part7Screen.kt:46 |
| `image 366.png` | `img_volume4part7_image_366` | Volume4Part7Screen.kt:47 |
| `image 367.png` | `img_volume4part8_image_367` | Volume4Part8Screen.kt:42 |
| `image 368.png` | `img_volume4part9_image_368` | Volume4Part9Screen.kt:46 |
| `image 370.png` | `img_volume4part9_image_370` | Volume4Part9Screen.kt:47 |
| `image 371.png` | `img_volume4part10_image_371` | Volume4Part10Screen.kt:46 |
| `image 372.png` | `img_volume4part10_image_372` | Volume4Part10Screen.kt:47 |
| `image 373.png` | `img_volume4part11_image_373` | Volume4Part11Screen.kt:46 |
| `image 374.png` | `img_volume4part11_image_374` | Volume4Part11Screen.kt:47 |
| `image 375.png` | `img_volume4part12_image_375` | Volume4Part12Screen.kt:46 |
| `image 376.png` | `img_volume4part12_image_376` | Volume4Part12Screen.kt:47 |
| `image 377.png` | `img_volume4part13_image_377` | Volume4Part13Screen.kt:48 |
| `image 378.png` | `img_volume4part13_image_378` | Volume4Part13Screen.kt:49 |
| `image 379.png` | `img_volume4part14_image_379` | Volume4Part14Screen.kt:47 |
| `image 380.png` | `img_volume3part11_image_380` | Volume3Part11Screen.kt:47 |
| `image 382.png` | `img_volume5part1_image_382` | Volume5Part1Screen.kt:45 |
| `image 383.png` | `img_volume5part1_image_383` | Volume5Part1Screen.kt:46 |
| `image 384.png` | `img_volume5part2_image_384` | Volume5Part2Screen.kt:45 |
| `image 386.png` | `img_volume5part2_image_386` | Volume5Part2Screen.kt:46 |
| `image 387.png` | `img_volume5part3_image_387` | Volume5Part3Screen.kt:45 |
| `image 388.png` | `img_volume5part3_image_388` | Volume5Part3Screen.kt:46 |
| `image 389.png` | `img_volume5part4_image_389` | Volume5Part4Screen.kt:45 |
| `image 390.png` | `img_volume5part4_image_390` | Volume5Part4Screen.kt:46 |
| `image 391.png` | `img_volume5part5_image_391` | Volume5Part5Screen.kt:45 |
| `image 393.png` | `img_volume5part6_image_393` | Volume5Part6Screen.kt:45 |
| `image 394.png` | `img_volume5part6_image_394` | Volume5Part6Screen.kt:46 |
| `image 395.png` | `img_volume5part5_image_395` | Volume5Part5Screen.kt:46 |
| `image 396.png` | `img_volume5part7_image_396` | Volume5Part7Screen.kt:45 |
| `image 397.png` | `img_volume5part7_image_397` | Volume5Part7Screen.kt:46 |
| `image 398.png` | `img_volume5part8_image_398` | Volume5Part8Screen.kt:45 |
| `image 399.png` | `img_volume5part8_image_399` | Volume5Part8Screen.kt:46 |
| `image 4.png` | `img_volume5part11_image_4` | Volume5Part11Screen.kt:45 |
| `image 40.png` | `img_volume10part10_image_40` | Volume10Part10Screen.kt:51 |
| `image 400.png` | `img_volume5part9_image_400` | Volume5Part9Screen.kt:44 |
| `image 4001.png` | `img_volume5part11_image_4001` | Volume5Part11Screen.kt:46 |
| `image 401.png` | `img_volume5part10_image_401` | Volume5Part10Screen.kt:45 |
| `image 402.png` | `img_volume5part13_image_402` | Volume5Part13Screen.kt:45 |
| `image 403.png` | `img_volume5part13_image_403` | Volume5Part13Screen.kt:46 |
| `image 405.png` | `img_volume5part14_image_405` | Volume5Part14Screen.kt:45 |
| `image 406.png` | `img_volume5part14_image_406` | Volume5Part14Screen.kt:46 |
| `image 407.png` | `img_volume5part15_image_407` | Volume5Part15Screen.kt:45 |
| `image 409.png` | `img_volume6part1_image_409` | Volume6Part1Screen.kt:45 |
| `image 41.png` | `img_volume10part2_image_41` | Volume10Part2Screen.kt:49 |
| `image 410.png` | `img_volume6part1_image_410` | Volume6Part1Screen.kt:46 |
| `image 411.png` | `img_volume6part2_image_411` | Volume6Part2Screen.kt:45 |
| `image 412.png` | `img_volume6part2_image_412` | Volume6Part2Screen.kt:46 |
| `image 413.png` | `img_volume6part3_image_413` | Volume6Part3Screen.kt:45 |
| `image 414.png` | `img_volume6part3_image_414` | Volume6Part3Screen.kt:46 |
| `image 415.png` | `img_volume6part4_image_415` | Volume6Part4Screen.kt:45 |
| `image 416.png` | `img_volume6part4_image_416` | Volume6Part4Screen.kt:46 |
| `image 417.png` | `img_volume6part5_image_417` | Volume6Part5Screen.kt:45 |
| `image 418.png` | `img_volume6part5_image_418` | Volume6Part5Screen.kt:46 |
| `image 419.png` | `img_volume6part6_image_419` | Volume6Part6Screen.kt:45 |
| `image 42.png` | `img_volume6part7_image_42` | Volume6Part7Screen.kt:46 |
| `image 420.png` | `img_volume6part7_image_420` | Volume6Part7Screen.kt:45 |
| `image 421.png` | `img_volume6part8_image_421` | Volume6Part8Screen.kt:45 |
| `image 422.png` | `img_volume6part9_image_422` | Volume6Part9Screen.kt:45 |
| `image 423.png` | `img_volume6part9_image_423` | Volume6Part9Screen.kt:46 |
| `image 425.png` | `img_volume6part10_image_425` | Volume6Part10Screen.kt:45 |
| `image 426.png` | `img_volume6part10_image_426` | Volume6Part10Screen.kt:46 |
| `image 427.png` | `img_volume6part11_image_427` | Volume6Part11Screen.kt:45 |
| `image 428.png` | `img_volume6part11_image_428` | Volume6Part11Screen.kt:46 |
| `image 43.png` | `img_volume10part4_image_43` | Volume10Part4Screen.kt:49 |
| `image 432.png` | `img_volume6part13_image_432` | Volume6Part13Screen.kt:45 |
| `image 434.png` | `img_volume6part14_image_434` | Volume6Part14Screen.kt:46 |
| `image 435.png` | `img_volume6part15_image_435` | Volume6Part15Screen.kt:45 |
| `image 436.png` | `img_volume7part1_image_436` | Volume7Part1Screen.kt:45 |
| `image 437.png` | `img_volume7part1_image_437` | Volume7Part1Screen.kt:46 |
| `image 438.png` | `img_volume7part2_image_438` | Volume7Part2Screen.kt:45 |
| `image 439.png` | `img_volume7part2_image_439` | Volume7Part2Screen.kt:46 |
| `image 440.png` | `img_volume7part3_image_440` | Volume7Part3Screen.kt:45 |
| `image 441.png` | `img_volume7part3_image_441` | Volume7Part3Screen.kt:46 |
| `image 442.png` | `img_volume7part4_image_442` | Volume7Part4Screen.kt:47 |
| `image 443.png` | `img_volume7part4_image_443` | Volume7Part4Screen.kt:48 |
| `image 444.png` | `img_volume7part4_image_444` | Volume7Part4Screen.kt:49 |
| `image 445.png` | `img_volume7part5_image_445` | Volume7Part5Screen.kt:47 |
| `image 446.png` | `img_volume7part5_image_446` | Volume7Part5Screen.kt:48 |
| `image 447.png` | `img_volume7part5_image_447` | Volume7Part5Screen.kt:49 |
| `image 448.png` | `img_volume7part6_image_448` | Volume7Part6Screen.kt:45 |
| `image 449.png` | `img_volume7part6_image_449` | Volume7Part6Screen.kt:46 |
| `image 45.png` | `img_volume10part6_image_45` | Volume10Part6Screen.kt:49 |
| `image 450.png` | `img_volume7part7_image_450` | Volume7Part7Screen.kt:45 |
| `image 451.png` | `img_volume7part7_image_451` | Volume7Part7Screen.kt:46 |
| `image 452.png` | `img_volume7part8_image_452` | Volume7Part8Screen.kt:45 |
| `image 453.png` | `img_volume7part8_image_453` | Volume7Part8Screen.kt:46 |
| `image 454.png` | `img_volume7part9_image_454` | Volume7Part9Screen.kt:45 |
| `image 455.png` | `img_volume7part9_image_455` | Volume7Part9Screen.kt:46 |
| `image 456.png` | `img_volume7part10_image_456` | Volume7Part10Screen.kt:45 |
| `image 457.png` | `img_volume7part10_image_457` | Volume7Part10Screen.kt:46 |
| `image 458.png` | `img_volume7part11_image_458` | Volume7Part11Screen.kt:49 |
| `image 459.png` | `img_volume7part11_image_459` | Volume7Part11Screen.kt:50 |
| `image 46.png` | `img_volume10part7_image_46` | Volume10Part7Screen.kt:49 |
| `image 460.png` | `img_volume7part11_image_460` | Volume7Part11Screen.kt:51 |
| `image 461.png` | `img_volume7part11_image_461` | Volume7Part11Screen.kt:52 |
| `image 462.png` | `img_volume7part12_image_462` | Volume7Part12Screen.kt:45 |
| `image 463.png` | `img_volume7part12_image_463` | Volume7Part12Screen.kt:46 |
| `image 464.png` | `img_volume8part1_image_464` | Volume8Part1Screen.kt:48 |
| `image 465.png` | `img_volume8part1_image_465` | Volume8Part1Screen.kt:49 |
| `image 466.png` | `img_volume8part2_image_466` | Volume8Part2Screen.kt:48 |
| `image 467.png` | `img_volume8part2_image_467` | Volume8Part2Screen.kt:49 |
| `image 468.png` | `img_volume8part3_image_468` | Volume8Part3Screen.kt:48 |
| `image 469.png` | `img_volume8part3_image_469` | Volume8Part3Screen.kt:49 |
| `image 47.png` | `img_volume10part6_image_47` | Volume10Part6Screen.kt:48 |
| `image 470.png` | `img_volume10part1_image_470` | Volume10Part1Screen.kt:48 |
| `image 471.png` | `img_volume10part2_image_471` | Volume10Part2Screen.kt:48 |
| `image 472.png` | `img_volume10part3_image_472` | Volume10Part3Screen.kt:48 |
| `image 473.png` | `img_volume10part4_image_473` | Volume10Part4Screen.kt:48 |
| `image 474.png` | `img_volume10part5_image_474` | Volume10Part5Screen.kt:49 |
| `image 475.png` | `img_volume10part5_image_475` | Volume10Part5Screen.kt:48 |
| `image 476.png` | `img_volume10part7_image_476` | Volume10Part7Screen.kt:48 |
| `image 477.png` | `img_volume10part8_image_477` | Volume10Part8Screen.kt:48 |
| `image 478.png` | `img_volume10part8_image_478` | Volume10Part8Screen.kt:49 |
| `image 479.png` | `img_volume10part9_image_479` | Volume10Part9Screen.kt:48 |
| `image 480.png` | `img_volume10part10_image_480` | Volume10Part10Screen.kt:50 |
| `image 482.png` | `img_volume10part10_image_482` | Volume10Part10Screen.kt:52 |
| `image 483.png` | `img_volume10part11_image_483` | Volume10Part11Screen.kt:48 |
| `image 484.png` | `img_volume10part11_image_484` | Volume10Part11Screen.kt:49 |
| `image 485.png` | `img_volume10part12_image_485` | Volume10Part12Screen.kt:48 |
| `image 486.png` | `img_volume10part13_image_486` | Volume10Part13Screen.kt:48 |
| `image 487.png` | `img_volume10part13_image_487` | Volume10Part13Screen.kt:49 |
| `image 488.png` | `img_volume10part14_image_488` | Volume10Part14Screen.kt:48 |
| `image 49.png` | `img_volume9part4_image_49` | Volume9Part4Screen.kt:49 |
| `image 491.png` | `img_volume9part1_image_491` | Volume9Part1Screen.kt:48 |
| `image 492.png` | `img_volume9part1_image_492` | Volume9Part1Screen.kt:49 |
| `image 493.png` | `img_volume9part2_image_493` | Volume9Part2Screen.kt:48 |
| `image 494.png` | `img_volume9part2_image_494` | Volume9Part2Screen.kt:49 |
| `image 495.png` | `img_volume9part3_image_495` | Volume9Part3Screen.kt:48 |
| `image 496.png` | `img_volume9part3_image_496` | Volume9Part3Screen.kt:49 |
| `image 497.png` | `img_volume9part4_image_497` | Volume9Part4Screen.kt:48 |
| `image 498.png` | `img_volume9part5_image_498` | Volume9Part5Screen.kt:48 |
| `image 499.png` | `img_volume9part5_image_499` | Volume9Part5Screen.kt:49 |
| `image 500.png` | `img_volume9part6_image_500` | Volume9Part6Screen.kt:48 |
| `image 502.png` | `img_volume9part6_image_502` | Volume9Part6Screen.kt:49 |
| `image 503.png` | `img_volume9part7_image_503` | Volume9Part7Screen.kt:49 |
| `image 504.png` | `img_volume9part7_image_504` | Volume9Part7Screen.kt:50 |
| `image 505.png` | `img_volume9part8_image_505` | Volume9Part8Screen.kt:48 |
| `image 506.png` | `img_volume9part8_image_506` | Volume9Part8Screen.kt:49 |
| `image 507.png` | `img_volume9part9_image_507` | Volume9Part9Screen.kt:48 |
| `image 508.png` | `img_volume9part9_image_508` | Volume9Part9Screen.kt:49 |
| `image 509.png` | `img_volume9part10_image_509` | Volume9Part10Screen.kt:48 |
| `image 510.png` | `img_volume9part10_image_510` | Volume9Part10Screen.kt:49 |
| `image 511.png` | `img_volume9part11_image_511` | Volume9Part11Screen.kt:48 |
| `image 512.png` | `img_volume9part11_image_512` | Volume9Part11Screen.kt:49 |
| `image 513.png` | `img_volume9part12_image_513` | Volume9Part12Screen.kt:48 |
| `image 514.png` | `img_volume9part12_image_514` | Volume9Part12Screen.kt:49 |
| `image 515.png` | `img_volume9part13_image_515` | Volume9Part13Screen.kt:49 |
| `image 516.png` | `img_volume9part13_image_516` | Volume9Part13Screen.kt:50 |
| `image 517.png` | `img_volume9part14_image_517` | Volume9Part14Screen.kt:48 |
| `image 518.png` | `img_volume9part14_image_518` | Volume9Part14Screen.kt:49 |
| `image 519.png` | `img_volume9part15_image_519` | Volume9Part15Screen.kt:48 |
| `image 520.png` | `img_volume8part4_image_520` | Volume8Part4Screen.kt:48 |
| `image 521.png` | `img_volume8part4_image_521` | Volume8Part4Screen.kt:49 |
| `image 522.png` | `img_volume8part5_image_522` | Volume8Part5Screen.kt:49 |
| `image 523.png` | `img_volume8part6_image_523` | Volume8Part6Screen.kt:48 |
| `image 524.png` | `img_volume8part5_image_524` | Volume8Part5Screen.kt:48 |
| `image 525.png` | `img_volume8part6_image_525` | Volume8Part6Screen.kt:49 |
| `image 526.png` | `img_volume8part7_image_526` | Volume8Part7Screen.kt:48 |
| `image 527.png` | `img_volume8part7_image_527` | Volume8Part7Screen.kt:49 |
| `image 528.png` | `img_volume8part8_image_528` | Volume8Part8Screen.kt:48 |
| `image 529.png` | `img_volume8part8_image_529` | Volume8Part8Screen.kt:49 |
| `image 530.png` | `img_volume8part9_image_530` | Volume8Part9Screen.kt:48 |
| `image 531.png` | `img_volume8part10_image_531` | Volume8Part10Screen.kt:50 |
| `image 532.png` | `img_volume8part10_image_532` | Volume8Part10Screen.kt:51 |
| `image 533.png` | `img_volume8part10_image_533` | Volume8Part10Screen.kt:52 |
| `image 534.png` | `img_volume8part11_image_534` | Volume8Part11Screen.kt:48 |
| `image 535.png` | `img_volume8part12_image_535` | Volume8Part12Screen.kt:48 |
| `image 536.png` | `img_volume8part12_image_536` | Volume8Part12Screen.kt:49 |
| `image 537.png` | `img_volume8part13_image_537` | Volume8Part13Screen.kt:48 |
| `image 538.png` | `img_volume8part13_image_538` | Volume8Part13Screen.kt:49 |
| `image 539.png` | `img_volume8part14_image_539` | Volume8Part14Screen.kt:48 |
| `image 54.png` | `img_chuangzuodangan6_image54` | Chuangzuodangan6Screen.kt:186 |
| `image 57.png` | `img_chuangzuodangan6_image57` | Chuangzuodangan6Screen.kt:187 |
| `image 64.png` | `img_chuangzuodangan3_image64` | Chuangzuodangan3Screen.kt:51 |
| `image 7.png` | `img_volume6part12_image_7` | Volume6Part12Screen.kt:48 |
| `image 70.png` | `img_volume10part1_image_70` | Volume10Part1Screen.kt:49 |
| `image 72.png` | `img_volume10part3_image_72` | Volume10Part3Screen.kt:49 |
| `image 85.png` | `img_volume10part12_image_85` | Volume10Part12Screen.kt:49 |
| `image 9.png` | `img_volume10part9_image_9` | Volume10Part9Screen.kt:49 |
| `image 901.png` | `img_volume5part12_image_901` | Volume5Part12Screen.kt:46 |
| `p.png` | `img_volume1part9_p` | Volume1Part9Screen.kt:41 |
| `roup.png` | `img_volume1part8_roup` | Volume1Part8Screen.kt:43 |
| `up.png` | `img_volume1part10_up` | Volume1Part10Screen.kt:41 |
| `创作档案.png` | `img_chuangzuodangan_bg` | Chuangzuodangan2Screen.kt:76 |
| `创作档案3.png` | `img_chuangzuodangan3_bg` | Chuangzuodangan3Screen.kt:50 · Chuangzuodangan3Screen.kt:77 · Chuangzuodangan4Screen.kt:75 |
| `创作档案5.png` | `img_chuangzuodangan5_bg` | Chuangzuodangan5Screen.kt:73 |
| `创作档案6.png` | `img_chuangzuodangan6_bg` | Chuangzuodangan6Screen.kt:61 |

## ②-a 注释查不到出处、**内容也找不到对应** —— 删除前值得过一眼(17 个)

> ⚠️ 别把这一节读成「没用的文件」:它只说明**仓库里没有逐字节相同的副本**。
> 已知的两类正常情况:
>   · **被处理过的素材** —— 例如三个敌人原图(棋冠石像 / 百声纸鹤 / 百面机枢)在 §20~§22 按用户要求
>     **裁掉水印**后才入库,内容自然对不上;删目录会失去**未裁的原始版本**;
>   · 过度导出/压缩过的图,或确实没进过项目。

- `image 44.png` —— 2.64 MB
- `演武场视频首页.png` —— 1.96 MB
- `棋冠石像.png` —— 1.38 MB
- `百面机枢.png` —— 1.11 MB
- `百声纸鹤.png` —— 0.47 MB
- `未 完 待 续.png` —— 0.06 MB
- `Ellipse 20.png` —— 0.05 MB
- `Group 273.png` —— 0.03 MB
- `image 51.png` —— 0.01 MB
- `AI教练辅助记录.png` —— 0.01 MB
- `Group 291.png` —— 0.01 MB
- `修改版本记录.png` —— 0.01 MB
- `原创 记录.png` —— 0.01 MB
- `选择作品查看.png` —— 0.00 MB
- `Group 258.png` —— 0.00 MB
- `Group 213.png` —— 0.00 MB
- `Vector.png` —— 0.00 MB

## ③ 已从设计稿目录删除的文件(98 个)

> 判据:它们的**内容逐字节存在于 `res/`**,且那份副本是 **git 跟踪**的文件 ——
> 所以删除是**可逆**的(随时能从仓库复制回来),删的只是「设计稿目录里的那一份」。
> 记录本身留在这里,是为了以后有人问「这张图当初是不是有原图」时能查到。

| 设计稿文件名 | 体积 | 仓库内副本 |
|---|---|---|
| `86.png` | 0.00 MB | `img_learning4_bubble_86.png` |
| `Android Compact - 109.png` | 1.65 MB | `img_houshan_bg.png` |
| `Android Compact - 124.png` | 0.23 MB | `img_unfinished_compact124.png` |
| `Ellipse 5.png` | 0.76 MB | `img_houshan1_cloud_57.png` |
| `Ellipse 56.png` | 1.54 MB | `img_houshan1_cloud_56.png` |
| `Ellipse 58.png` | 0.29 MB | `img_shilian3_cloud_58.png` |
| `Ellipse 60.png` | 0.09 MB | `img_houshan1_cloud_60.png` |
| `Ellipse 61.png` | 0.02 MB | `img_houshan1_cloud_61.png` |
| `Ellipse 62.png` | 0.12 MB | `img_houshan3_cloud_62.png` |
| `Group 165.png` | 0.01 MB | `group_258.png` |
| `Group 212.png` | 1.07 MB | `img_shengtu_group212.png` |
| `Group 23.png` | 0.00 MB | `group_213.png` |
| `Group 253.png` | 0.00 MB | `img_shengtu_group253.png` |
| `Group 280.png` | 0.31 MB | `img_home1_group280.png` |
| `Group 709.png` | 0.01 MB | `img_chuangzuodangan2_group709.png` |
| `Group280.png` | 0.04 MB | `img_learning_group_280.png` |
| `Rectangle 16.png` | 0.01 MB | `img_chuangzuodangan3_rect16.png` |
| `Rectangle 18.png` | 0.00 MB | `img_xiulian_rectangle_18.png` |
| `Rectangle 186.png` | 0.01 MB | `img_chuangzuodangan_rect186.png` |
| `Rectangle 220.png` | 0.00 MB | `img_chatresult_rect220.png` |
| `Rectangle 221.png` | 0.00 MB | `img_chatresult_rect221.png` |
| `Rectangle 227.png` | 0.00 MB | `img_shengtu_rect227.png` |
| `Rectangle 228.png` | 0.39 MB | `img_shengtu_rect228.png` |
| `Rectangle 231.png` | 0.00 MB | `img_shengtu_rect231.png` |
| `Rectangle 245.png` | 0.32 MB | `img_chuangzuodangan3_rect245.png` |
| `Rectangle 25.png` | 0.26 MB | `img_chuangzuodangan5_rect25.png` |
| `Rectangle 251.png` | 0.00 MB | `img_gunlun7_rect251.png` |
| `Rectangle 6.png` | 0.00 MB | `img_gunlun1_rect86.png` |
| `Rectangle156.png` | 0.00 MB | `img_shilian_rect156.png` |
| `Rectangle16.png` | 0.00 MB | `img_gunlun5_rect16.png` |
| `Return (返回).png` | 0.00 MB | `img_gongfang_return.png` |
| `Return(返回).png` | 0.00 MB | `img_shilian_return.png` |
| `Vector 579.png` | 0.01 MB | `img_xiulian_vector_579.png` |
| `Vector 611.png` | 0.01 MB | `img_picture_vector611.png` |
| `image 134.png` | 1.50 MB | `img_unfinished_image134.png` |
| `image 217.png` | 0.19 MB | `img_learning_image_217.png` |
| `image 307.png` | 0.38 MB | `img_unfinished_image307.png` |
| `image 38.png` | 0.24 MB | `img_picture_image38.png` |
| `image 430.png` | 0.96 MB | `img_volume6part12_image_130.png` |
| `image 431.png` | 0.80 MB | `img_volume6part12_image_7.png` |
| `image 52.png` | 0.20 MB | `img_chuangzuodangan3_image52.png` |
| `image 540.png` | 0.28 MB | `img_learning4_image_540.png` |
| `image 59.png` | 0.07 MB | `img_chuangzuodangan5_image59.png` |
| `image 61.png` | 0.13 MB | `img_chuangzuodangan3_image61.png` |
| `image 62.png` | 0.23 MB | `img_chuangzuodangan4_image62.png` |
| `image 75.png` | 0.16 MB | `img_shilian_panda.png` |
| `创作.png` | 2.59 MB | `img_shengtu_bg.png` |
| `加载 1.png` | 0.00 MB | `img_shengtu_loading1.png` |
| `待解锁.png` | 0.05 MB | `img_pendingunlock_text.png` |
| `断目机关蝠.png` | 1.52 MB | `img_chuangdang_duanmujiguanfu.png` |
| `未标题-1 41.png` | 0.08 MB | `img_shengtu_untitled41.png` |
| `未标题-1 50.png` | 0.01 MB | `img_gunlun1_untitled_1_50.png` |
| `未标题-1 51.png` | 0.01 MB | `img_dahui_o.png` |
| `未标题-1 72.png` | 0.28 MB | `img_chuangzuodangan_untitled172.png` |
| `未标题-1-恢复的 5.png` | 0.52 MB | `img_gunlun1_untitled_1_recovered_5.png` |
| `未标题-1-恢复的 8.png` | 0.04 MB | `img_shilian2_recovered_8.png` |
| `未标题-1-恢复的-恢复的 4.png` | 0.03 MB | `img_shilian_recovered_4.png` |
| `未标题-150.png` | 0.01 MB | `img_gunlun1_untitled_150.png` |
| `未标题-151.png` | 0.03 MB | `img_home1_btn5.png` |
| `未标题-2 2.png` | 0.03 MB | `img_gunlun11_untitled_2_2.png` |
| `未标题-2 23.png` | 0.01 MB | `img_pendingunlock_button.png` |
| `未标题-2 24.png` | 0.01 MB | `img_gunlun10_untitled_2_24.png` |
| `未标题-2 26.png` | 0.02 MB | `img_gunlun10_untitled_2_26.png` |
| `未标题-2 28.png` | 0.02 MB | `img_gunlun10_untitled_2_28.png` |
| `未标题-2 30.png` | 0.06 MB | `img_gunlun10_untitled_2_30.png` |
| `未标题-2 31.png` | 0.02 MB | `img_gunlun10_untitled_2_31.png` |
| `未标题-2 32.png` | 0.03 MB | `img_gunlun10_untitled_2_32.png` |
| `未标题-2 33.png` | 0.13 MB | `img_gunlun10_untitled_2_33.png` |
| `未标题-2 38.png` | 0.02 MB | `img_gunlun4_untitled_2_38.png` |
| `未标题-2 41.png` | 0.05 MB | `img_gunlun8_untitled_2_41.png` |
| `未标题-2 42.png` | 0.06 MB | `img_gunlun15_untitled_2_42.png` |
| `未标题-2 44.png` | 0.02 MB | `img_gunlun12_untitled_2_44.png` |
| `未标题-2 46.png` | 0.02 MB | `img_gunlun11_untitled_2_46.png` |
| `未标题-2 47.png` | 0.03 MB | `img_gunlun12_untitled_2_47.png` |
| `未标题-2 50.png` | 0.07 MB | `img_gunlun11_untitled_2_50.png` |
| `未标题-2 56.png` | 0.08 MB | `img_gunlun13_untitled_2_56.png` |
| `未标题-2-恢复的 1.png` | 0.49 MB | `img_learning4_untitled_2_recovered_1.png` |
| `未标题-2-恢复的 10.png` | 0.17 MB | `img_learning_untitled_2_recovered_10.png` |
| `未标题-2-恢复的 14.png` | 0.02 MB | `img_gunlun11_untitled_2_recovered_14.png` |
| `未标题-2-恢复的 16.png` | 0.02 MB | `img_gunlun10_untitled_2_recovered_16.png` |
| `未标题-2-恢复的 17.png` | 0.05 MB | `img_gunlun9_untitled_2_recovered_17.png` |
| `未标题-2-恢复的 18.png` | 0.02 MB | `img_gunlun12_untitled_2_recovered_18.png` |
| `未标题-2.png` | 0.01 MB | `img_gunlun2_untitled_2.png` |
| `未标题-232.png` | 0.02 MB | `img_gunlun10_untitled_232.png` |
| `未标题-241.png` | 0.12 MB | `img_gunlun11_untitled_241.png` |
| `未标题-3.png` | 0.01 MB | `img_gunlun2_untitled_3.png` |
| `未标题-4.png` | 0.01 MB | `img_gunlun2_untitled_4.png` |
| `未标题-5.png` | 0.01 MB | `img_gunlun2_untitled_5.png` |
| `未标题-6.png` | 0.01 MB | `img_gunlun2_untitled_6.png` |
| `未标题-7.png` | 0.01 MB | `img_gunlun2_untitled_7.png` |
| `未标题-8.png` | 0.01 MB | `img_gunlun2_untitled_8.png` |
| `未标题-9.png` | 0.02 MB | `img_gunlun2_untitled_9.png` |
| `未标题1.png` | 0.02 MB | `img_gunlun2_untitled_1.png` |
| `滚轮.png` | 2.16 MB | `img_gunlun1_bg.png` |
| `背景.png` | 2.30 MB | `img_auth_bg.png` |
| `试炼.png` | 3.40 MB | `img_shilian_bg.png` |
| `试炼转换.png` | 3.29 MB | `img_shilian2_bg.png` |
| `铜齿门卫.png` | 1.74 MB | `img_chuangdang_tongchimenwei.png` |

