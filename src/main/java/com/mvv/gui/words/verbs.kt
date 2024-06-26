package com.mvv.gui.words


val englishCommonVerbs: Alt<Seq<String>> = sequenceOf(
    // TODO: keep there only base/infinitive forms and create 2nd/3rd/s/ing forms dynamically
    //
    "do", "does",
    "be", "is", "are",
    "have", "has", "have no", "has no", "had", "had no",
    "get", "gets", "go", "gone", "went", "goes", "going",
    "can", "could", "may", "might", "must", "shall", "should",
    // https://enginform.com/article/contractions-in-english
    "cannot", "can't", "couldn't", "mayn't", "mightn't", "mustn't", "shouldn't", "needn’t", "oughtn’t",
    "isn't", "aren't", "wasn't", "weren't", "won't", "wouldn’t",
    "doesn't", "don't", "didn't",
    "hasn't", "haven't", "hadn't",

    "add", "allow", "answer", "appear", "ask",
    "become", "begin", "believe", "break", "bring", "build", "buy",
    "call", "carry", "cast", "catch", "change", "clean", "close",
    "come", "consider", "continue", "cook", "count", "cost", "cover", "create", "cut",
    "dance", "decide", "die", "draw", "dream", "drink", "drive",
    "eat", "expect", "explain",
    "fall", "feel", "find", "finish", "flow", "fly", "follow", "forget",
    "gain", "get", "give", "grow", "guess",
    "handle", "happen", "hang", "hear", "help", "hit", "hold",
    "include",
    "keep", "kill", "know",
    "lack", "lead", "learn", "leave", "let", "like", "live", "listen", "lose", "look", "love",
    "make", "make no", "mean", "meet", "move",
    "need",
    "offer", "open",
    "pay", "pass", "pick", "play", "provide", "pull", "put",
    "raise", "require", "relax", "remain", "remember", "report", "ride", "run", "reach", "read",
    "say", "see", "seem", "sell", "send", "serve", "set", "show", "sing", "sit", "ski",
    "sleep", "speak", "spend", "stand", "start", "stay", "stop", "swim", "suggest",
    "take", "talk", "tell", "think", "try",
    "tell", "think", "try to keep", "teach", "turn", "travel",
    "understand", "use",
    "visit",
    "wait", "walk", "want","waste", "watch", "win", "work", "write", "would",
).splitToWords()



class VerbForms (
    val base: String,
    val pastTense: List<String>,
    val pastParticiple: List<String>,
) {

    companion object {
        fun parse(items: List<String>): VerbForms {
            require(items.size == 3) { " 3 verbs are expected $items." }
            return VerbForms(
                items[0],
                items[1].split("/").toList(),
                items[2].split("/").toList(),
            )
        }
    }
}


// see https://www.englishpage.com/irregularverbs/irregularverbs.html
private val irregularVerbs_RawList = listOf(
    "arise", "arose", "arisen",
    "awake", "awakened/awoke", "awakened/awoken",

    "backslide", "backslid", "backslidden/backslid",
    //"be/am/is/are", "was/were", "been",
    "be", "was/were", "been",
    "bear", "bore", "born/borne",
    "beat", "beat", "beaten",
    "become", "became", "become",
    "begin", "began", "begun",
    "bend", "bent", "bent",
    "bet", "bet", "bet",
    // bid (farewell) 	bid / bade 	bidden
    // bid (offer amount) 	bid 	bid
    "bid" ,"bid/bade", "bidden/bid",
    "bind", "bound", "bound",
    "bite", "bit", "bitten",
    "bleed", "bled", "bled",
    "blow", "blew", "blown",
    "break", "broke", "broken",
    "breed", "bred", "bred",
    "bring", "brought", "brought",
    "broadcast", "broadcast/broadcasted", "broadcast/broadcasted",
    "browbeat", "browbeat", "browbeaten/browbeat",
    "build", "built", "built",
    "burn", "burnt/burned", "burnt/burned",
    "burst", "burst", "burst",
    "bust", "busted/bust", "busted/bust",
    "buy", "bought", "bought",

    "cast", "cast", "cast",
    "catch", "caught", "caught",
    "choose", "chose", "chosen",
    "cling", "clung", "clung",
    "clothe", "clothed/clad", "clothed/clad",
    "come", "came", "come",
    "cost", "cost", "cost",
    "creep", "crept", "crept",
    "crossbreed", "crossbred", "crossbred",
    "cut", "cut", "cut",

    "daydream", "daydreamed/daydreamt" ,"daydreamed/daydreamt",
    "deal", "dealt", "dealt",
    "dig", "dug", "dug",
    "disprove", "disproved", "disproved/disproven",
    // dive (jump head-first) 	dove / dived 	dived
    // dive (scuba diving) 	dived / dove 	dived
    "dive", "dove/dived", "dived",
    "do", "did", "done",
    "draw", "drew", "drawn",
    "dream", "dreamt/dreamed", "dreamt/dreamed",
    "drink", "drank", "drunk",
    "drive", "drove", "driven",
    "dwell", "dwelt/dwelled", "dwelt/dwelled",

    "eat", "ate", "eaten",

    "fall", "fell", "fallen",
    "feed", "fed", "fed",
    "feel", "felt", "felt",
    "fight", "fought", "fought",
    "find", "found", "found",
    //fit (tailor, change size) 	fitted / fit [?] 	fitted / fit [?]
    //fit (be right size) 	fit / fitted [?] 	fit / fitted [?]
    "fit", "fitted/fit", "fitted/fit",
    "flee", "fled", "fled",
    "fling", "flung", "flung",
    "fly", "flew", "flown",
    "forbid", "forbade", "forbidden",
    "forecast", "forecast/forecasted", "forecast/forecasted",
    "forego/forgo", "forewent", "foregone",
    "foresee", "foresaw", "foreseen",
    "foretell", "foretold", "foretold",
    "forget", "forgot", "forgotten",
    "forgive", "forgave", "forgiven",
    "forsake", "forsook", "forsaken",
    "freeze", "froze", "frozen",
    "frostbite", "frostbit", "frostbitten",

    "get", "got", "gotten/got",
    "give", "gave", "given",
    "go", "went", "gone",
    "grind", "ground", "ground",
    "grow", "grew", "grown",

    "hand-feed", "hand-fed", "hand-fed",
    "handwrite", "handwrote", "handwritten",
    "hang", "hung", "hung",
    "have", "had", "had",
    "hear", "heard", "heard",
    "hew", "hewed", "hewn/hewed",
    "hide", "hid", "hidden",
    "hit", "hit", "hit",
    "hold", "held", "held",
    "hurt", "hurt", "hurt",

    "inbreed", "inbred", "inbred",
    "inlay", "inlaid", "inlaid",
    "input", "input/inputted", "input/inputted",
    "interbreed", "interbred", "interbred",
    "interweave", "interwove/interweaved", "interwoven/interweaved",
    "interwind", "interwound", "interwound",

    "jerry-build", "jerry-built", "jerry-built",

    "keep", "kept", "kept",
    "kneel", "knelt/kneeled", "knelt/kneeled",
    "knit", "knitted/knit", "knitted/knit",
    "know", "knew", "known",

    // others from https://www.englishpage.com/irregularverbs/irregularverbs.html#google_vignette
    "lay", "laid", "laid",
    "lead", "led", "led",
    "lean", "leant/leaned", "leant/leaned",
    "leap", "leaped/leapt", "leaped/leapt",
    "learn", "learned/learnt", "learned/learnt",
    "leave", "left", "left",
    "lend", "lent", "lent",
    "let", "let", "let",

    //lie 	lay 	lain
    //lie (not tell truth) REGULAR 	lied 	lied
    "lie", "lied/lay", "lied/lain",

    "light", "lit/lighted", "lit/lighted",
    "lip-read", "lip-read", "lip-read",
    "lose", "lost", "lost",

    "make", "made", "made",
    "mean", "meant", "meant",
    "meet", "met", "met",
    "miscast", "miscast", "miscast",
    "misdeal", "misdealt", "misdealt",
    "misdo", "misdid", "misdone",
    "mishear", "misheard", "misheard",
    "mislay", "mislaid", "mislaid",
    "mislead", "misled", "misled",
    "mislearn", "mislearned/mislearnt", "mislearned/mislearnt",
    "misread", "misread", "misread",
    "misset", "misset", "misset",
    "misspeak", "misspoke", "misspoken",
    "misspell", "misspelled/misspelt", "misspelled/misspelt",
    "misspend", "misspent", "misspent",
    "mistake", "mistook", "mistaken",
    "misteach", "mistaught", "mistaught",
    "misunderstand", "misunderstood", "misunderstood",
    "miswrite", "miswrote", "miswritten",
    "mow", "mowed", "mowed/mown",

    "offset", "offset", "offset",
    "outbid", "outbid", "outbid",
    "outbreed", "outbred", "outbred",
    "outdo", "outdid", "outdone",
    "outdraw", "outdrew", "outdrawn",
    "outdrink", "outdrank", "outdrunk",
    "outdrive", "outdrove", "outdriven",
    "outfight", "outfought", "outfought",
    "outfly", "outflew", "outflown",
    "outgrow", "outgrew", "outgrown",
    "outleap", "outleaped/outleapt", "outleaped/outleapt",
    //outlie (not tell truth) REGULAR 	outlied 	outlied
    "outlie", "outlied", "outlied",
    "outride", "outrode", "outridden",
    "outrun", "outran", "outrun",
    "outsell", "outsold", "outsold",
    "outshine", "outshined/outshone", "outshined/outshone",
    "outshoot", "outshot", "outshot",
    "outsing", "outsang", "outsung",
    "outsit", "outsat", "outsat",
    "outsleep", "outslept", "outslept",
    "outsmell", "outsmelled/outsmelt", "outsmelled/outsmelt",
    "outspeak", "outspoke", "outspoken",
    "outspeed", "outsped", "outsped",
    "outspend", "outspent", "outspent",
    "outswear", "outswore", "outsworn",
    "outswim", "outswam", "outswum",
    "outthink", "outthought", "outthought",
    "outthrow", "outthrew", "outthrown",
    "outwrite", "outwrote", "outwritten",
    "overbid", "overbid", "overbid",
    "overbreed", "overbred", "overbred",
    "overbuild", "overbuilt", "overbuilt",
    "overbuy", "overbought", "overbought",
    "overcome", "overcame", "overcome",
    "overdo", "overdid", "overdone",
    "overdraw", "overdrew", "overdrawn",
    "overdrink", "overdrank", "overdrunk",
    "overeat", "overate", "overeaten",
    "overfeed", "overfed", "overfed",
    "overhang", "overhung", "overhung",
    "overhear", "overheard", "overheard",
    "overlay", "overlaid", "overlaid",
    "overpay", "overpaid", "overpaid",
    "override", "overrode", "overridden",
    "overrun", "overran", "overrun",
    "oversee", "oversaw", "overseen",
    "oversell", "oversold", "oversold",
    "oversew", "oversewed", "oversewn/oversewed",
    "overshoot", "overshot", "overshot",
    "oversleep", "overslept", "overslept",
    "overspeak", "overspoke", "overspoken",
    "overspend", "overspent", "overspent",
    "overspill", "overspilled/overspilt", "overspilled/overspilt",
    "overtake", "overtook", "overtaken",
    "overthink", "overthought", "overthought",
    "overthrow", "overthrew", "overthrown",
    "overwind", "overwound", "overwound",
    "overwrite", "overwrote", "overwritten",

    "partake", "partook", "partaken",
    "pay", "paid", "paid",
    "plead", "pleaded/pled", "pleaded/pled",
    "prebuild", "prebuilt", "prebuilt",
    "predo", "predid", "predone",
    "premake", "premade", "premade",
    "prepay", "prepaid", "prepaid",
    "presell", "presold", "presold",
    "preset", "preset", "preset",
    "preshrink", "preshrank", "preshrunk",
    "proofread", "proofread", "proofread",
    "prove", "proved", "proven/proved",
    "put", "put", "put",

    "quick-freeze", "quick-froze", "quick-frozen",
    "quit", "quit/quitted", "quit/quitted",
    "quit", "quit", "quit",

    "read", "read", "read",
    "reawake", "reawoke", "reawaken",
    "rebid", "rebid", "rebid",
    "rebind", "rebound", "rebound",
    "rebroadcast", "rebroadcast/rebroadcasted", "rebroadcast/rebroadcasted",
    "rebuild", "rebuilt", "rebuilt",
    "recast", "recast", "recast",
    "recut", "recut", "recut",
    "redeal", "redealt", "redealt",
    "redo", "redid", "redone",
    "redraw", "redrew", "redrawn",
    //refit (replace parts) 	refit / refitted [?] 	refit / refitted [?]
    //refit (retailor) 	refitted / refit [?] 	refitted / refit [?]
    "refit", "refit/refitted", "refit/refitted",
    "regrind", "reground", "reground",
    "regrow", "regrew", "regrown",
    "rehang", "rehung", "rehung",
    "rehear", "reheard", "reheard",
    "reknit", "reknitted/reknit", "reknitted/reknit",
    //relay (for example tiles) 	relaid 	relaid
    //relay (pass along) REGULAR 	relayed relayed
    "relay", "relaid/relayed", "relaid/relayed",
    "relearn", "relearned/relearnt", "relearned/relearnt",
    "relight", "relit/relighted", "relit/relighted",
    "remake", "remade", "remade",
    "repay", "repaid", "repaid",
    "reread", "reread", "reread",
    "rerun", "reran", "rerun",
    "resell", "resold", "resold",
    "resend", "resent", "resent",
    "reset", "reset", "reset",
    "resew", "resewed", "resewn/resewed",
    "retake", "retook", "retaken",
    "reteach", "retaught", "retaught",
    "retear", "retore", "retorn",
    "retell", "retold", "retold",
    "rethink", "rethought", "rethought",
    "retread", "retread", "retread",
    "retrofit", "retrofitted/retrofit", "retrofitted/retrofit",
    "rewake", "rewoke/rewaked", "rewaken/rewaked",
    "rewear", "rewore", "reworn",
    "reweave", "rewove/reweaved", "rewoven/reweaved",
    "rewed", "rewed/rewedded", "rewed/rewedded",
    "rewet", "rewet/rewetted", "rewet/rewetted",
    "rewin", "rewon", "rewon",
    "rewind", "rewound", "rewound",
    "rewrite", "rewrote", "rewritten",
    "rid", "rid", "rid",
    "ride", "rode", "ridden",
    "ring", "rang", "rung",
    "rise", "rose", "risen",
    "roughcast", "roughcast", "roughcast",
    "run", "ran", "run",

    "sand-cast", "sand-cast", "sand-cast",
    "saw", "sawed", "sawed/sawn",
    "say", "said", "said",
    "see", "saw", "seen",
    "seek", "sought", "sought",
    "sell", "sold", "sold",
    "send", "sent", "sent",
    "set", "set", "set",
    "sew", "sewed", "sewn/sewed",
    "shake", "shook", "shaken",
    "shave", "shaved", "shaved/shaven",
    "shear", "sheared", "sheared/shorn",
    "shed", "shed", "shed",
    "shake", "shook", "shaken",
    "shave", "shaved", "shaved/shaven",
    "shear", "sheared", "sheared/shorn",
    "shine", "shone", "shone",
    "shit", "shit/shat/shitted", "shit/shat/shitted",
    "shoe", "shod", "shod",
    "shoot", "shot", "shot",
    "show", "showed", "shown",
    "shrink", "shrank", "shrunk",
    "shut", "shut", "shut",
    "sing", "sang", "sung",
    "sink", "sank", "sunk",
    "sit", "sat", "sat",
    //slay (kill) 	slew / slayed 	slain / slayed
    //slay (amuse) REGULAR 	slayed 	slayed
    "slay", "slew/slayed", "slain/slew/slayed",
    "sleep", "slept", "slept",
    "slide", "slid", "slid",
    "sling", "slung", "slung",
    "slink", "slinked/slunk", "slinked/slunk",
    "slit", "slit", "slit",
    "smell", "smelled/smelt", "smelled/smelt",
    "sneak", "sneaked/snuck", "sneaked/snuck",
    "sow", "sowed", "sown/sowed",
    "speak", "spoke", "spoken",
    "speed", "sped/speeded", "sped/speeded",
    "spell", "spelled/spelt", "spelled/spelt",
    "spend", "spent", "spent",
    "spill", "spilt/spilled", "spilt/spilled",
    "spin", "spun", "spun",
    "spit", "spit/spat", "spit/spat",
    "split", "split", "split",
    "spoil", "spoiled/spoilt", "spoiled/spoilt",
    "spoon-feed", "spoon-fed", "spoon-fed",
    "spread", "spread", "spread",
    "spring", "sprang/sprung", "sprung",
    "stand", "stood", "stood",
    "steal", "stole", "stolen",
    "stick", "stuck", "stuck",
    "sting", "stung", "stung",
    "stink", "stank", "stunk",
    "strew", "strewed", "strewn/strewed",
    "stride", "strode", "stridden",
    //strike (delete) 	struck 	stricken
    //strike (hit) 	struck 	struck / stricken
    "strike", "struck", "struck/stricken",
    "string", "strung", "strung",
    "strive", "strove/strived", "striven/strived",
    "sublet", "sublet", "sublet",
    "sunburn", "sunburned/sunburnt", "sunburned/sunburnt",
    "swear", "swore", "sworn",
    "sweat", "sweat/sweated", "sweat/sweated",
    "sweep", "swept", "swept",
    "swell", "swelled", "swollen/swelled",
    "swim", "swam", "swum",
    "swing", "swung", "swung",

    "take", "took", "taken",
    "teach", "taught", "taught",
    "tear", "tore", "torn",
    "telecast", "telecast", "telecast",
    "tell", "told", "told",
    "test-drive", "test-drove", "test-driven",
    "test-fly", "test-flew", "test-flown",
    "think", "thought", "thought",
    "throw", "threw", "thrown",
    "thrust", "thrust", "thrust",
    "tread", "trod", "trodden/trod",
    "typecast", "typecast", "typecast",
    "typeset", "typeset", "typeset",
    "typewrite", "typewrote", "typewritten",

    "unbend", "unbent", "unbent",
    "unbind", "unbound", "unbound",
    "unclothe", "unclothed/unclad", "unclothed/unclad",
    "underbid", "underbid", "underbid",
    "undercut", "undercut", "undercut",
    "underfeed", "underfed", "underfed",
    "undergo", "underwent", "undergone",
    "underlie", "underlay", "underlain",
    "undersell", "undersold", "undersold",
    "underspend", "underspent", "underspent",
    "understand", "understood", "understood",
    "undertake", "undertook", "undertaken",
    "underwrite", "underwrote", "underwritten",
    "undo", "undid", "undone",
    "unfreeze", "unfroze", "unfrozen",
    "unhang", "unhung", "unhung",
    "unhide", "unhid", "unhidden",
    "unknit", "unknitted/unknit", "unknitted/unknit",
    "unlearn", "unlearned/unlearnt", "unlearned/unlearnt",
    "unsew", "unsewed", "unsewn/unsewed",
    "unsling", "unslung", "unslung",
    "unspin", "unspun", "unspun",
    "unstick", "unstuck", "unstuck",
    "unstring", "unstrung", "unstrung",
    "unweave", "unwove/unweaved", "unwoven/unweaved",
    "unwind", "unwound", "unwound",
    "uphold", "upheld", "upheld",
    "upset", "upset", "upset",
    "understand", "understood", "understood",

    "wake", "woke", "woken",
    "waylay", "waylaid", "waylaid",
    "wear", "wore", "worn",
    "weave", "wove/weaved", "woven/weaved",
    "wed", "wed/wedded", "wed/wedded",
    "weep", "wept", "wept",
    "wet", "wet/wetted", "wet/wetted",
    //whet  REGULAR 	whetted 	whetted
    "whet", "whetted", "whetted",
    "win", "won", "won",
    "wind", "wound", "wound",
    "withdraw", "withdrew", "withdrawn",
    "withhold", "withheld", "withheld",
    "withstand", "withstood", "withstood",
    "wring", "wrung", "wrung",
    "write", "wrote", "written",
)

val irregularVerbs: List<VerbForms> = irregularVerbs_RawList.chunked(3)
    //.filterNot { it[0] == "be" || it[0].startsWith("be/") }
    .map { VerbForms.parse(it) }


/*
private val irregularVerbsMap: Map<String, VerbForms> = irregularVerbs.associateBy { it.base }


fun getProbably2ndVerbForm(infinitive: String): List<String> {

    var pastParticiple = irregularVerbsMap[infinitive]?.pastParticiple
    if (pastParticiple != null) return pastParticiple

    // In a word with 1 syllable, double the final consonant ONLY if the word ends in 1 vowel + 1 consonant.
    if (infinitive.isWordWithOneSyllable && infinitive.wordEndsInOneVowelAndOneConsonant) {
        return listOf(infinitive + infinitive.lastChar + "ed")
    }

    // In a word with 2 or more syllables, double the final consonant ONLY if the word ends in 1 vowel + 1 consonant AND the final syllable is stressed.
    if (infinitive.isWordWith2orMoreSyllables && infinitive.wordEndsInOneVowelAndOneConsonantAndTheFinalSyllableIsStressed) {

    }


    // 1) try to get from irregular ones
    // 2) add depending on suffix
    //    'y' - 'ied', 'e' - 'ed', 'ue' - 'ued', 'y' - 'yied' (hurried), 'y' - 'yed' (played)
    //    stop → stopped
    //    plan → planned

    TODO("Impl")
}
*/

/*

https://learnenglishteens.britishcouncil.org/grammar/a1-a2-grammar/past-simple-regular-verbs

Yes, but there are some spelling rules. If a verb ends in -e, you add -d.
agree → agreed
like → liked
escape → escaped

If a verb ends in a vowel and a consonant, the consonant is usually doubled before -ed.
stop → stopped
plan → planned

See detailed rule at https://www.britannica.com/dictionary/eb/qa/Doubling-the-final-consonant-before-adding-ed-or-ing



If a verb ends in consonant and -y, you take off the y and add -ied.
try → tried
carry → carried

But if the word ends in a vowel and -y, you add -ed.
play → played
enjoy → enjoyed


Vowels are:
a, e, i, o, u.

Consonants are the rest of the letters in the alphabet:
b, c, d, f, g, h, j, k, l, m, n, p, q, r, s, t, v, w, x, y and z.

The letter y is a bit different, because sometimes it acts as a consonant and sometimes it acts as a vowel.

*/


// When a verb ends in a consonant
//val String.verbEndsInConsonant: Boolean get() {
//}


/*
Doubling the final consonant before adding –ed or –ing
Question
When do you double the consonant at the end of a verb, before adding –ed or –ing?  — Pasam G, Tanzania
Answer

SPELLING RULES FOR VERBS WITH -ING AND -ED ENDINGS

When a verb ends in a consonant, sometimes the consonant is doubled before adding the –ed or –ing ending, like this:

stop --> stopped, stopping

    Lucy stopped the car.
    Why was Lucy stopping the car?

And sometimes the final consonant is not doubled, like this:

shift --> shifted, shifting

    Sandy shifted the gears.
    Sandy was shifting the gears too much.

To know when to double the final consonant, follow the rules below.

RULES

    In a word with 1 syllable, double the final consonant ONLY if the word ends in 1 vowel + 1 consonant.
    In a word with 2 or more syllables, double the final consonant ONLY if the word ends in 1 vowel + 1 consonant AND the final syllable is stressed.
    At the end of a word, don’t count w, x, or y as a consonant.

APPLYING THE RULES

These verbs get a doubled final consonant:

    tip / He tipped the waiter. /He isn't tipping the waiter.
    cram / The students crammed for the test. /The students were cramming for the test.
    regret / Carl regretted the things he had said. /Carl was regretting the things he had said.

These verbs do not get a doubled final consonant:

    vote --> voted, voting (vote ends in a vowel)
    instruct --> instructed, instructing (instruct ends in 2 consonants)
    listen --> listened, listening (listen has 2 syllables and the final syllable is not stressed)

*/
