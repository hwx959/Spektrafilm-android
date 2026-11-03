/*
 * Spektrafilm for Android — plain-language help for the opaque controls. GPLv3.
 * Film modeling powered by spektrafilm.
 *
 * The editor exposes the engine's real, physically-named knobs (DIR couplers, halation
 * scatter, density-curve gamma…). They are honest but intimidating. This is the onboarding
 * answer (USER_DRIVEN_SOLUTIONS.md §6h, pain #8): a central registry of friendly,
 * photographer-facing explanations, surfaced by the "?" help sheet on each section header.
 *
 * Relabel-only — the engine still receives the identical params, so this NEVER changes the
 * render and carries zero parity risk. Pure data (no Android/Compose types) so the content
 * coverage is JVM-unit-testable (ParamHelpTest).
 */
package com.spectrafilm.app

/**
 * One control's help: a [title], a one-line [summary] (the at-a-glance "what it does") and a
 * fuller [body] (what it does to the photo + when to reach for it), written for a photographer
 * rather than a film chemist.
 */
data class ParamHelp(
    val title: String,
    val summary: String,
    val body: String,
)

/** Central registry of plain-language help, keyed by a stable id (not the display label). */
object ParamHelpText {
    // Stable keys so relabeling a section never breaks the lookup (or its test).
    const val GRAIN = "grain"
    const val HALATION = "halation"
    const val COUPLERS = "couplers"
    const val PRINT_GAMMA = "print_gamma"
    const val PREFLASH = "preflash"
    const val GLARE = "glare"

    val sections: Map<String, ParamHelp> = mapOf(
        GRAIN to ParamHelp(
            title = "Grain",
            summary = "The fine, sparkly texture that makes film look like film.",
            body = "Real film is built from microscopic light-sensitive particles, and their random " +
                "clumping is what you see as grain. Bigger particles — a faster, higher-ISO film — give " +
                "coarser, more visible grain; the per-channel scales let one colour layer be grainier " +
                "than the others. Turn it down for a clean, modern look or up for a gritty, vintage one. " +
                "Grain is measured in microns, so it can look finer in the small on-screen preview than " +
                "in your full-resolution export.",
        ),
        HALATION to ParamHelp(
            title = "Halation",
            summary = "The soft reddish glow that blooms around bright highlights.",
            body = "On real film, strong light passes through the emulsion, reflects off the back of " +
                "the base and re-exposes the film around bright areas — street lamps, the sun through " +
                "leaves, specular highlights. \"Halation amount\" sets how strong that glow is, while " +
                "\"scatter\" adds a broader, subtler spread of light inside the emulsion; \"Boost EV\" " +
                "decides how bright a highlight must be before it blooms. Raise it for a dreamy, " +
                "cinematic look, or switch the whole section off for clinical, digital-clean highlights.",
        ),
        COUPLERS to ParamHelp(
            title = "Film colour character (couplers)",
            summary = "Film's signature colour separation, edge punch and saturation roll-off.",
            body = "As film develops, dye forming in one colour layer chemically holds back its " +
                "neighbours — the \"DIR coupler\" effect. That crosstalk is a big part of why film " +
                "colours look the way they do: crisper edges and a distinctive, non-linear saturation. " +
                "\"Effect strength\" scales the whole character, and the cross-colour controls set how " +
                "much each colour bleeds into the others. This is a character control, not a plain " +
                "saturation slider — for ordinary \"more / less colourful\", use Saturation / Vibrance " +
                "under Output.",
        ),
        PRINT_GAMMA to ParamHelp(
            title = "Film & print contrast (gamma)",
            summary = "The raw contrast baked into the negative and the print paper.",
            body = "Every film and paper has a characteristic curve, and its \"gamma\" is how steep " +
                "that curve is — the photochemical equivalent of a contrast control. A higher print " +
                "gamma gives a punchier, higher-contrast print; lower flattens it out. For everyday " +
                "contrast tweaks reach for the Contrast slider first; these are the underlying " +
                "photochemical knobs for shaping the backbone of the look.",
        ),
        PREFLASH to ParamHelp(
            title = "Preflash",
            summary = "A faint, even pre-exposure of the paper that tames harsh contrast.",
            body = "Darkroom printers sometimes flash the paper with a low, uniform light before the " +
                "real exposure. That lifts the deepest shadows and gently lowers contrast in the print " +
                "without touching the highlights. Raise \"Exposure\" for softer, more open shadows; the " +
                "Y and M filter shifts tint that pre-flash light, the same way enlarger filtration does.",
        ),
        GLARE to ParamHelp(
            title = "Glare",
            summary = "Veiling haze from light scattering across the print surface.",
            body = "This models stray light bouncing around the print and scanner, lifting the blacks a " +
                "little and laying a soft haze over the image. It is a random (stochastic) effect, so it " +
                "is left off by default and is not part of the exact, repeatable export. Raise " +
                "\"Percent\" for a hazier, lower-contrast, more nostalgic feel.",
        ),
    )

    /**
     * 简体中文帮助，键与 [sections] 一致。写法面向拍照的人，不面向胶片化学。
     * 缺某一条时 [forKey] 会回落到英文，不会空着。
     */
    val sectionsZh: Map<String, ParamHelp> = mapOf(
        GRAIN to ParamHelp(
            title = "颗粒",
            summary = "让照片看起来“像胶片”的那种细密颗粒质感。",
            body = "真实胶片由无数微小的感光颗粒组成，它们随机聚集成簇，你看到的颗粒就是这么来的。" +
                "颗粒越大——也就是感光度越高的胶片——颗粒越粗、越明显；分通道比例可以单独让某一层" +
                "颜色比其它层更粗。想要干净现代就调低，想要粗粝复古就调高。颗粒以微米计，所以屏幕上" +
                "的小预览会比全分辨率的成片看起来更细。",
        ),
        HALATION to ParamHelp(
            title = "光晕",
            summary = "高光四周泛出的柔和红光。",
            body = "在真实胶片上，强光穿过乳剂层、从片基背面反射回来，把亮部周围重新曝光一次——路灯、" +
                "透过树叶的阳光、镜面高光都会这样。“光晕强度”决定这圈光的强弱；“散射”在乳剂内部" +
                "铺一层更宽、更淡的光；“提升 EV”决定高光要亮到什么程度才开始泛。调高偏梦幻、电影感；" +
                "整组关掉，高光就是干净的数字味。",
        ),
        COUPLERS to ParamHelp(
            title = "胶片色彩特性（成色剂）",
            summary = "胶片特有的色彩分离、边缘力度和饱和度滚降。",
            body = "胶片显影时，某一层染料的生成会化学性地抑制相邻层——这就是“DIR 成色剂”效应。" +
                "这种串扰正是胶片色彩之所以像胶片的重要原因：边缘更利落，饱和度呈非线性变化。" +
                "“效果强度”缩放整套特性，交叉颜色控制决定每种颜色向其它颜色渗透多少。" +
                "这是“特性”控制，不是普通的饱和度滑杆——只想更艳或更淡，请用输出页的饱和度／自然饱和度。",
        ),
        PRINT_GAMMA to ParamHelp(
            title = "胶片与相纸反差（伽马）",
            summary = "负片和相纸本身固有的反差。",
            body = "每种胶片和相纸都有自己的特性曲线，“伽马”就是这条曲线有多陡——相当于光化学层面的" +
                "反差控制。伽马越高，照片越硬、反差越大；越低越平。日常调反差请先用“对比度”滑杆；" +
                "这里是塑造整个影调骨架的底层旋钮。",
        ),
        PREFLASH to ParamHelp(
            title = "预闪",
            summary = "给相纸做一次微弱均匀的预曝光，压一压生硬的反差。",
            body = "暗房师傅有时会在正式曝光前，用很弱且均匀的光先闪一下相纸。这样能把最深的暗部提起来，" +
                "并轻微降低整体反差，却不影响高光。提高“曝光量”，暗部更柔、更通透；Y／M 滤色偏移" +
                "会给这道预闪光染色，原理和放大机滤色一样。",
        ),
        GLARE to ParamHelp(
            title = "眩光",
            summary = "光线在照片表面散射形成的雾状灰。",
            body = "这里模拟杂散光在照片和扫描仪之间来回反射：把黑位抬起来一点，并在画面上罩一层柔雾。" +
                "它是随机效应，所以默认关闭，也不参与精确、可复现的导出。调高“百分比”会更朦胧、" +
                "反差更低、更怀旧。",
        ),
    )

    /** The help for [key], or null if none is registered. */
    fun forKey(key: String): ParamHelp? =
        if (wantsChinese()) sectionsZh[key] ?: sections[key] else sections[key]
}
