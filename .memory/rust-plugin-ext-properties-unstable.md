---
name: Rust 插件内部扩展属性不能直接调用（RsFunction.block 案例）
description: Why calling org.rust.lang.core.psi.ext.* extension properties compiles but crashes at runtime with NoSuchMethodError
type: gotcha
---

调用 Rust 插件的顶层扩展属性（`org.rust.lang.core.psi.ext.block` 等）编译期不报错、运行期抛
`NoSuchMethodError`，因为 JetBrains 会把它们在不同文件间搬来搬去，而 Kotlin 按名字解析扩展、
import 后硬编码成 `RsFunctionKt.getBlock(...)` 的静态调用。

**Why** — `RsFunction.block` 在 RustRover 2026.2 从 `org.rust.lang.core.psi.ext.RsFunctionKt`
移到了 `org.rust.lang.core.psi.ext.RsFunctionImplMixinKt`（后者是 mixin 内部类）。签名没变，
只换了宿主类，所以编译通过、运行崩。报错形如
`'org.rust.lang.core.psi.RsBlock ...RsFunctionKt.getBlock(RsFunction)'` —— 外层单引号是
`NoSuchMethodError` 的标志。本仓库对应 issue #63。

**How to apply** — 遇到 `org.rust.lang.core.psi.ext` 前缀的调用，优先换成纯平台 API。
`RsFunction.block` 的等价写法（`javap -c` 反编译确认过，行为一致）：

```kotlin
PsiTreeUtil.getChildOfType(function, RsBlock::class.java)
```

排查方法：先 `javap -p -classpath <jar> <类名>` 确认目标方法还在不在、在哪个类；
再 `javap -p -c` 看新实现是否与旧行为等价。
Rust 插件代码位置：`~/.gradle/caches/<gradle 版本>/transforms/.../RustRover-<版本>-aarch64/plugins/intellij-rust/lib/`，
`intellij.rustrover.common.jar` 放 PSI 接口，`modules/intellij.rustrover.core.jar` 放实现。

**Related** — [[gradle-setupdependencies-removed]]
