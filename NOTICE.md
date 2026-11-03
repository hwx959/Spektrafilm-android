# Attribution & Third-Party Notices

Spektrafilm for Android is licensed under the **GNU General Public License v3.0** (see `LICENSE`).

> **Dedicated to the [pixls.us](https://pixls.us) community** — for keeping open photography and
> open color science alive. Thank you. 💛

Authored and directed by **Akshay** —
[Instagram @akshay.pool](https://www.instagram.com/akshay.pool/) ·
[YouTube @Akshayishere](https://www.youtube.com/@Akshayishere/videos).

## Required attribution (GPLv3 section 7(b) additional term)

This project is distributed under the GNU General Public License v3.0. As permitted by
**GPLv3 section 7(b)** — *"requiring preservation of specified reasonable legal notices or
author attributions in that material or in the Appropriate Legal Notices displayed by works
containing it"* — the following attribution is a required notice.

**If you convey this software, or any work based on it, you must preserve this attribution and
display it in your Appropriate Legal Notices** (an about screen, a credits screen, a licences
screen, a store listing's description, or the documentation — somewhere a user can actually
find it):

> **Spektrafilm for Android** by **Akshay Sharma** —
> https://github.com/thetechgeekko/Spektrafilm-android
>
> Film modeling powered by **spektrafilm** (Andrea Volpato) —
> https://github.com/andreavolpato/spektrafilm

Where the medium allows a hyperlink, both URLs must be clickable. Where it does not, the URLs
must appear as text. This applies to every form of conveyance, including a Google Play or other
app-store listing of a derivative work.

**Both lines are required, and the second is not optional courtesy**: the film-simulation engine
here is a port of Andrea Volpato's spektrafilm, whose own attribution request this project has
always honoured. Crediting the Android port while dropping the upstream engine would misrepresent
the work — and removing either notice removes your licence to convey the software at all
(GPLv3 section 8).

This term adds **no restriction beyond attribution**. It does not limit commercial use,
modification, or redistribution, and it is deliberately confined to what section 7(b) permits so
that this project stays GPL-compatible and can still be combined with other GPLv3 software. If
you receive this work with this term attached and prefer not to carry it, section 7 lets you
remove *additional* terms from material you convey — but the GPLv3 obligations themselves, and
the upstream spektrafilm attribution required by the engine's own licence, remain.

## Film modeling powered by spektrafilm

The film-simulation engine in this project is a port of **spektrafilm** by Andrea Volpato.

- Upstream: https://github.com/andreavolpato/spektrafilm
- License: GPLv3
- Citation: see `CITATION.cff` upstream / the project's Zenodo DOI.

Per the upstream request: *film modeling powered by `spektrafilm`*. Any academic use should
cite the upstream repository.

## Vulkan orchestration reference: spektrafilm OFX

Selected Vulkan host and resource-lifetime orchestration is adapted from
**spektrafilm OFX** by Aedan Oskar Otto Diez.

- Upstream: https://github.com/chaert-s/spektrafilm-ofx
- Pinned source: `86476afc5b077de77e2278e3658d1ba9309892a1`
- License: GPL-3.0-only
- Adaptation record: `docs/research/spektrafilm-ofx-port.md`
- Shader code adapted (modified copy, GPL-3.0-only, both copyright lines kept):
  `shaders/vulkan/SpektraHalation.comp` -> `engine/spektra-core/src/main/cpp/gpu/halation_scatter.comp`

This Android project is independent and is not endorsed by the upstream author. No official
binary-only resources or spektrafilm OFX-exported LUTs are included.

## Color science: colour-science

Color-matching functions, illuminants, chromatic adaptation, and color-space transforms follow
**colour-science** — the reference used by spektrafilm and the ground truth for this port.

- Upstream: https://www.colour-science.org / https://github.com/colour-science/colour
- License: BSD-3-Clause

## Android architecture reference: Image Toolbox

The Android app design is informed by **Image Toolbox** by T8RIN (Malik Mukhametzyanov) — its
modular Compose/Hilt architecture guided this project, and it remains the intended richer host
for future versions.

- Upstream: https://github.com/T8RIN/ImageToolbox
- License: Apache License 2.0 (one-way compatible with GPLv3)

## RAW decoding: LibRaw

<!-- libraw-license-route: LGPL-2.1-only -->

LibRaw Android distribution route: LGPL-2.1-only.

Full-resolution RAW/DNG decoding uses **LibRaw**.

- Upstream: https://www.libraw.org / https://github.com/LibRaw/LibRaw
- License: LibRaw is offered under a choice of LGPL-2.1-only or CDDL-1.0. This
  application's static integration is distributed under **LGPL-2.1-only**; both
  upstream license texts remain included for provenance. CDDL-1.0 is not
  generally treated as GPL-compatible and was declined. The reproducible
  source/relink package described in `docs/LICENSING.md` ships with each release.
  The route marker cannot be changed alone: the canonical decision record also
  identifies the human approval and confirms rights to license the local patch
  contributions under the elected route.

## Film & paper profiles

The 28 film/paper JSON profiles and the spectral-upsampling LUT binaries originate from
the spektrafilm project and are redistributed under GPLv3. See `docs/ASSETS.md`.

## ICC color profiles

The bundled ICC profiles under `engine/spektra-core/src/main/assets/spektra/icc/` are
redistributed from two upstream sets (carried over from spektrafilm's asset bundle) so the
export writers can embed a correct color interpretation. They are kept under their original
upstream filenames for traceability. See `engine/spektra-core/src/main/assets/spektra/icc/README.md`.

- **`icc/ellelstone/` — Elle Stone's well-behaved ICC profiles.** Copyright 2016, Elle Stone
  (http://ninedegreesbelow.com/). Licensed under **Creative Commons Attribution-ShareAlike 3.0
  Unported (CC BY-SA 3.0)**; attribution required. Upstream:
  https://github.com/ellelstone/elles_icc_profiles — license text:
  `icc/ellelstone/LICENSE-CC-BY-SA-3.0`. The profiles remain under their own
  license; no GPL compatibility or relicensing claim is made here.
- **`icc/saucecontrol/` — Compact ICC Profiles** by Clinton Ingram. Licensed under the **MIT
  License**. Upstream: https://github.com/saucecontrol/Compact-ICC-Profiles — license text:
  `icc/saucecontrol/LICENSE-MIT`.

---

# 本分支（Spektra胶片模拟）的改动与新增归属

本分支基于 **Akshay Sharma 的 Spektrafilm for Android v0.10.0**（GPL-3.0）修改，
**非官方版本**，与 SpektraLab 及其作者无关。上游的 GPLv3 §7(b) 署名要求照原样保留，未做删改。

## 新增功能（本分支原创实现）

- 胶片缩略图条（`FilmThumbnailStrip.kt`）与配套预渲染调度
- 隐藏工具栏后的「恢复工具栏」入口（`RestoreChromeHint.kt`）
- 片边合成（`FilmEdge.kt`）：135 / 120 / 极简白边三种画幅，画布预览与导出共用同一实现
- 中文参数帮助文案（`ParamHelp.kt` 中的 `sectionsZh`）

以上代码为本分支独立编写，以 GPL-3.0 发布。

## 中文界面译文

`app/src/main/res/values-zh-rCN/` 下的界面译文，以及
`engine/spektra-core/src/main/assets/spektra/catalog.json` 中的 `titleZh` / `summaryZh` 字段，
**取自 SpektraLab-CN 0.10.0-cn9（com.spektralab.cn）** 的 `(zh-rCN)` 资源与 `assets` 数据，
按资源名与 JSON 键对齐移植，共 914 条字符串、28 条胶片简介、5 个分组标题。

译文的著作权归该版本译者（Hanze Qiu / 邱瀚泽，GitHub: JamesQiu2005）。
该版本以 GPL-3.0 分发，本分支据此在同样许可下使用这些译文。
若译者认为此处署名有误或希望调整，请联系本仓库维护者，将立即更正。

## 片边配色

片边三种画幅的配色（片基、边缘字、齿孔、条码带）参照 SpektraLab-CN 的 `FilmEdgeCompositor`
的输出效果对齐，绘制代码为本分支独立编写，未复制其实现。

## 原生库来源

本分支不编译 C/C++ 代码，`jniLibs` 下的原生库取自上游 v0.10.0 发行包，
来源、哈希与重建方法见 `NATIVE-BUILD.md`。
