// projectTypeIcons.js
import crochetHook from "./assets/icons/crochet-hook.svg";
import woolBall from "./assets/icons/noun-wool-ball-8383122.svg";
import knittingNeedles from "./assets/icons/noun-straight-5375566.svg";
import sewingNeedle from "./assets/icons/noun-sewing-needle-181392.svg";

export const PROJECT_TYPE_ICON_META = {
  crocheting: {
    src: crochetHook,
    alt: "Crochet hook icon",
    creditLabel: "crochet icon",
    author: "Ayub Irawan",
    sourceUrl: "https://thenounproject.com/browse/icons/term/crochet/",
  },
  knitting: {
    src: knittingNeedles,
    alt: "Knitting needles icon",
    creditLabel: "knitting needles icon",
    author: "hazicon",
    sourceUrl: "https://thenounproject.com/browse/icons/term/straight/",
  },
  sewing: {
    src: sewingNeedle,
    alt: "Sewing needle icon",
    creditLabel: "sewing needle icon",
    author: "Guilherme Simoes",
    sourceUrl: "https://thenounproject.com/browse/icons/term/sewing-needle/",
  },
  default: {
    src: woolBall,
    alt: "Wool ball icon",
    creditLabel: "wool ball icon",
    author: "Dutchken",
    sourceUrl: "https://thenounproject.com/browse/icons/term/wool-ball/",
  },
};

export const PROJECT_TYPE_ICONS = Object.fromEntries(
  Object.entries(PROJECT_TYPE_ICON_META).map(([key, value]) => [
    key,
    value.src,
  ]),
);

export const PROJECT_TYPE_ICON_CREDITS = Object.entries(
  PROJECT_TYPE_ICON_META,
).map(([key, value]) => ({
  type: key,
  ...value,
}));
