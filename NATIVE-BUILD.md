# 原生库的来源与重建方法

> **一句话**：本分支**不编译**任何 C/C++ 代码。`jniLibs` 里的 5 个 `.so` 是从上游官方
> v0.10.0 发行包中原样提取的，未做修改。本文说明它们从哪来、怎么核对、以及想自己编要怎么做。

本分支的改动全部在 Kotlin / 资源层（缩略图条、中文化、片边、恢复工具栏）。原生层一行没动，
所以直接复用了上游同版本的二进制，而不是重新编译一遍。

## 一、来源

| 项目 | 值 |
|---|---|
| 上游仓库 | https://github.com/thetechgeekko/Spektrafilm-android |
| 版本 | **v0.10.0**（tag） |
| 发行包文件名 | `strf-0.10.0.apk` |
| 发行包字节数 | 21,341,655 |
| 发行包 SHA-256 | `a09b680a16446d5f6cc51554a5b282eb711bd0152fda367552b69beff6386dff` |
| 提取路径 | APK 内 `lib/arm64-v8a/*.so` |

核对方法（无需信任本文）：

```bash
curl -LO https://github.com/thetechgeekko/Spektrafilm-android/releases/download/v0.10.0/strf-0.10.0.apk
sha256sum strf-0.10.0.apk          # 应为 a09b680a…86dff
unzip -o strf-0.10.0.apk 'lib/arm64-v8a/*.so' -d upstream-native
sha256sum upstream-native/lib/arm64-v8a/*.so   # 应与下表一致
```

## 二、打包进本分支的原生库（arm64-v8a）

| 文件 | 字节数 | SHA-256 | 所属模块 | 上游项目 / 许可 |
|---|---:|---|---|---|
| `libspektra.so` | 1,497,664 | `40073514f72fc4afb9526ded6cb6af175d1f438647319c26dda096965e544117` | `engine:spektra-core` | spektrafilm（Andrea Volpato）/ GPL-3.0 |
| `libsfraw.so` | 1,278,864 | `292f5f514027c4ca86240b3eaa0c0155553615e7844040f9ff0800eb59dd043b` | `lib:libraw` | LibRaw / LGPL-2.1-only |
| `libsftiff.so` | 74,712 | `a2b069bf7a74a7c9dc2abd9e8310737c49e9dd89d9debbf98ea548dc8c7d0f71` | `lib:tiffwriter` | libtiff 系 / 见 NOTICE.md |
| `libsfpng.so` | 70,400 | `6cf61729f0ca23d09f42983d69a43b7b159f7866f8512e5849d38f206a933995` | `lib:pngwriter` | libpng 系 / 见 NOTICE.md |
| `libc++_shared.so` | 1,253,544 | `cd61762848882a16c8244c964a6f396c0caa0b440588a210ce9cc4ab0e6d9f0c` | 随 `engine:spektra-core` | LLVM libc++ / Apache-2.0 WITH LLVM-exception |

分发位置：

```
app/src/main/jniLibs/arm64-v8a/                 ← 全部 5 个
engine/spektra-core/src/main/jniLibs/arm64-v8a/ ← libspektra.so, libc++_shared.so
lib/libraw/src/main/jniLibs/arm64-v8a/          ← libsfraw.so
lib/tiffwriter/src/main/jniLibs/arm64-v8a/      ← libsftiff.so
lib/pngwriter/src/main/jniLibs/arm64-v8a/       ← libsfpng.so
```

只保留 **arm64-v8a**：原工程支持 armeabi-v7a / x86_64，本分支按目标机型裁剪掉了另两个 ABI。

## 三、本分支为了让构建不依赖 NDK 所做的改动

四处 `build.gradle.kts`（`engine:spektra-core`、`lib:libraw`、`lib:tiffwriter`、`lib:pngwriter`）与
`app/build.gradle.kts` 中：

1. 删除各自模块的 `externalNativeBuild { cmake { path = … } }` 块 —— 不再调用 CMake。
2. 删除 `defaultConfig.ndk { abiFilters … }` 与模块级 `ndkVersion`。
3. 增加 `packaging { jniLibs { keepDebugSymbols += "**/*.so" } }` —— 预编译库禁止 AGP 调用 NDK 的
   `strip`，否则没有 NDK 时构建会失败。
4. `app/build.gradle.kts` 的 `release` 块里删除了 `ndk { debugSymbolLevel = "FULL" }`，原因同上。

**副作用（请知悉）**：`.so` 未经过 strip，包含完整符号表，APK 因此比自行编译的版本略大。
release 包当前 14 MB。

## 四、想真正从源码重建原生库

如果修改了 C/C++ 代码，或需要其他 ABI，必须走这条路径：

```bash
# 1) 安装 NDK（版本必须与上游一致，否则 16 KB 页对齐与 libc++ 版本可能不匹配）
sdkmanager "ndk;28.2.13676358"

# 2) 从上游 v0.10.0 恢复被删除的构建块
#    四个模块的 externalNativeBuild / abiFilters / ndkVersion 原样加回，
#    并从 app 模块移除 keepDebugSymbols 与 release 的 debugSymbolLevel 调整

# 3) 编译
./gradlew :app:assembleRelease
```

CMake 工程位于各模块的 `src/main/cpp/CMakeLists.txt`（未删除，仍在仓库中）。

## 五、GPL 合规提示

GPL-3.0 要求分发时提供**完整对应源码**，包括生成二进制所需的脚本。本分支的处理方式是：

- 原生库的**对应源码**即上游 v0.10.0 的完整源码（本分支未修改），仓库地址见上表；
- 本分支**新增和修改的全部代码**（Kotlin、资源、Gradle 脚本）都随本仓库提供；
- 因此"完整对应源码" = **上游 v0.10.0 源码 + 本仓库**，两者都可公开获取。

如果将来修改了 C/C++ 代码，上述等式不再成立，届时必须提供自己编译的二进制与可复现的构建脚本。
