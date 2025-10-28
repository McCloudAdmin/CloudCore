import "./chunk-TQKFIP6A.js";
import {
  createElementBlock,
  defineComponent,
  openBlock
} from "./chunk-SLKY347R.js";
import "./chunk-V4OQ3NZ2.js";

// node_modules/.pnpm/vue-turnstile@1.0.11_vue@3.5.22_typescript@5.9.3_/node_modules/vue-turnstile/dist/vue-turnstile.js
var c = "https://challenges.cloudflare.com/turnstile/v0/api.js";
var l = "cfTurnstileOnLoad";
var i = typeof window < "u" && window.turnstile !== void 0 ? "ready" : "unloaded";
var n;
var p = defineComponent({
  name: "VueTurnstile",
  emits: ["update:modelValue", "error", "unsupported", "expired", "before-interactive", "after-interactive"],
  props: {
    siteKey: {
      type: String,
      required: true
    },
    modelValue: {
      type: String,
      required: true
    },
    resetInterval: {
      type: Number,
      required: false,
      default: 295 * 1e3
    },
    size: {
      type: String,
      required: false,
      default: "normal"
    },
    theme: {
      type: String,
      required: false,
      default: "auto"
    },
    language: {
      type: String,
      required: false,
      default: "auto"
    },
    action: {
      type: String,
      required: false,
      default: ""
    },
    appearance: {
      type: String,
      required: false,
      default: "always"
    },
    renderOnMount: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      resetTimeout: void 0,
      widgetId: void 0
    };
  },
  computed: {
    turnstileOptions() {
      return {
        sitekey: this.siteKey,
        theme: this.theme,
        language: this.language,
        size: this.size,
        callback: this.callback,
        action: this.action,
        appearance: this.appearance,
        "error-callback": this.errorCallback,
        "expired-callback": this.expiredCallback,
        "unsupported-callback": this.unsupportedCallback,
        "before-interactive-callback": this.beforeInteractiveCallback,
        "after-interactive-callback": this.afterInteractivecallback
      };
    }
  },
  methods: {
    afterInteractivecallback() {
      this.$emit("after-interactive");
    },
    beforeInteractiveCallback() {
      this.$emit("before-interactive");
    },
    expiredCallback() {
      this.$emit("expired");
    },
    unsupportedCallback() {
      this.$emit("unsupported");
    },
    errorCallback(e) {
      this.$emit("error", e);
    },
    callback(e) {
      this.$emit("update:modelValue", e), this.startResetTimeout();
    },
    reset() {
      window.turnstile && (this.$emit("update:modelValue", ""), window.turnstile.reset());
    },
    remove() {
      this.widgetId && (window.turnstile.remove(this.widgetId), this.widgetId = void 0);
    },
    render() {
      this.widgetId = window.turnstile.render(this.$refs.turnstile, this.turnstileOptions);
    },
    startResetTimeout() {
      this.resetTimeout = setTimeout(() => {
        this.reset();
      }, this.resetInterval);
    }
  },
  async mounted() {
    const e = new Promise((r, t) => {
      n = { resolve: r, reject: t }, i === "ready" && r(void 0);
    });
    window[l] = () => {
      n.resolve(), i = "ready";
    }, await (() => {
      if (i === "unloaded") {
        i = "loading";
        const r = `${c}?onload=${l}&render=explicit`, t = document.createElement("script");
        t.src = r, t.async = true, t.addEventListener("error", () => {
          n.reject("Failed to load Turnstile.");
        }), document.head.appendChild(t);
      }
      return e;
    })(), this.renderOnMount && this.render();
  },
  beforeUnmount() {
    this.remove(), clearTimeout(this.resetTimeout);
  }
});
var f = (e, a) => {
  const r = e.__vccOpts || e;
  for (const [t, s] of a)
    r[t] = s;
  return r;
};
var h = { ref: "turnstile" };
function m(e, a, r, t, s, b) {
  return openBlock(), createElementBlock("div", h, null, 512);
}
var k = f(p, [["render", m]]);
export {
  k as default
};
//# sourceMappingURL=vue-turnstile.js.map
