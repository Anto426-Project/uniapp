/**
 * Apple Fluid Interfaces Physics Engine (WWDC 2018 translated for the web)
 *
 * Implements Apple's designer-friendly spring parameterization (damping ratio + response),
 * exponential-decay momentum projection, soft rubber-banding, pointer velocity tracking,
 * and interruptible rAF-driven spring controllers with zero velocity discontinuities.
 */

export interface AppleSpringConfig {
  /** Damping ratio: 1.0 = critically damped (no overshoot), ~0.8 = slight momentum bounce */
  damping: number;
  /** Response time in seconds (lower = snappier). Not a fixed duration. */
  response: number;
}

/** Critically damped defaults — graceful, non-distracting UI motion */
export const SPRING_UI: AppleSpringConfig = { damping: 1.0, response: 0.34 };
export const SPRING_REPOSITION: AppleSpringConfig = { damping: 1.0, response: 0.4 };
export const SPRING_PILL: AppleSpringConfig = { damping: 1.0, response: 0.28 };

/** Under-damped presets — reserved for momentum-carrying gestures (flicks, drags, sheets) */
export const SPRING_MOMENTUM: AppleSpringConfig = { damping: 0.82, response: 0.36 };
export const SPRING_SHEET: AppleSpringConfig = { damping: 0.86, response: 0.3 };
export const SPRING_ROTATION: AppleSpringConfig = { damping: 0.8, response: 0.4 };

/**
 * Apple's exact momentum projection function (Designing Fluid Interfaces, WWDC 2018).
 * decelerationRate ~ 0.998 for scroll deceleration; ~ 0.99 for snappier swipe throws.
 */
export function project(initialVelocityPxPerSec: number, decelerationRate = 0.998): number {
  return ((initialVelocityPxPerSec / 1000) * decelerationRate) / (1 - decelerationRate);
}

/**
 * Apple's soft boundary resistance (rubber-banding).
 * Progressive damping past edges so boundaries feel alive rather than frozen.
 */
export function rubberband(overshoot: number, dimension: number, constant = 0.55): number {
  if (dimension <= 0) return 0;
  return (overshoot * dimension * constant) / (dimension + constant * Math.abs(overshoot));
}

export function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

export function prefersReducedMotionNow(): boolean {
  if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') return false;
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/** Subtle causal haptic pulse on touch devices that support Vibration API. */
export function hapticSnap(durationMs = 6): void {
  if (typeof navigator !== 'undefined' && typeof navigator.vibrate === 'function') {
    try {
      navigator.vibrate(durationMs);
    } catch {
      // Ignore browser permission restrictions.
    }
  }
}

interface VelocitySample {
  x: number;
  y: number;
  t: number;
}

/**
 * Tracks a short rolling window (~95ms) of pointer coordinates to compute
 * clean release velocity in px/s for seamless drag-to-spring velocity handoff.
 */
export class VelocityTracker {
  private samples: VelocitySample[] = [];
  private readonly windowMs: number;

  constructor(windowMs = 95) {
    this.windowMs = windowMs;
  }

  reset(x: number, y: number, t = performance.now()): void {
    this.samples = [{ x, y, t }];
  }

  add(x: number, y: number, t = performance.now()): void {
    this.samples.push({ x, y, t });
    const cutoff = t - this.windowMs;
    while (this.samples.length > 2 && this.samples[0].t < cutoff) {
      this.samples.shift();
    }
  }

  getVelocity(now = performance.now()): { vx: number; vy: number } {
    const cutoff = now - this.windowMs;
    while (this.samples.length > 2 && this.samples[0].t < cutoff) {
      this.samples.shift();
    }
    if (this.samples.length < 2) return { vx: 0, vy: 0 };

    const last = this.samples[this.samples.length - 1];
    // If pointer rested for > 65ms before release, user intended to stop, not flick.
    if (now - last.t > 65) return { vx: 0, vy: 0 };

    const first = this.samples[0];
    const dt = (last.t - first.t) / 1000;
    if (dt <= 0.001) return { vx: 0, vy: 0 };

    return {
      vx: clamp((last.x - first.x) / dt, -4800, 4800),
      vy: clamp((last.y - first.y) / dt, -4800, 4800),
    };
  }
}

export interface SpringStepOptions {
  velocity?: number;
  config?: AppleSpringConfig;
  immediate?: boolean;
  onRest?: () => void;
}

/**
 * Interruptible 1D spring driver synced to requestAnimationFrame.
 * Always animates from the live presentation value and carries velocity through re-targets.
 */
export class SpringDriver {
  value: number;
  velocity = 0;
  target: number;

  private config: AppleSpringConfig;
  private rafId: number | null = null;
  private lastTime = 0;
  private onUpdate: (value: number, velocity: number) => void;
  private onRestCallback?: () => void;
  private readonly precision: number;

  constructor(
    initialValue: number,
    onUpdate: (value: number, velocity: number) => void,
    config: AppleSpringConfig = SPRING_UI,
    precision = 0.05
  ) {
    this.value = initialValue;
    this.target = initialValue;
    this.onUpdate = onUpdate;
    this.config = config;
    this.precision = precision;
  }

  get isAnimating(): boolean {
    return this.rafId !== null;
  }

  /** Interrupt mid-flight and lock directly to a pointer-driven position. */
  setImmediate(value: number): void {
    this.stop();
    this.value = value;
    this.target = value;
    this.velocity = 0;
    this.onUpdate(this.value, 0);
  }

  /** Stop the animation in-place, preserving the current live presentation value. */
  stop(): void {
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId);
      this.rafId = null;
    }
    this.onRestCallback = undefined;
  }

  /**
   * Animate to target from current live value.
   * When velocity is omitted, existing in-flight velocity is preserved (additive feel).
   */
  animateTo(target: number, options: SpringStepOptions = {}): void {
    this.target = target;
    if (options.config) this.config = options.config;
    if (typeof options.velocity === 'number') {
      this.velocity = options.velocity;
    }
    this.onRestCallback = options.onRest;

    if (options.immediate || prefersReducedMotionNow()) {
      this.stop();
      this.value = target;
      this.velocity = 0;
      this.onUpdate(this.value, 0);
      options.onRest?.();
      return;
    }

    if (
      Math.abs(this.target - this.value) < this.precision &&
      Math.abs(this.velocity) < this.precision * 10
    ) {
      this.stop();
      this.value = target;
      this.velocity = 0;
      this.onUpdate(this.value, 0);
      options.onRest?.();
      return;
    }

    if (this.rafId === null) {
      this.lastTime = performance.now();
      this.rafId = requestAnimationFrame(this.tick);
    }
  }

  private tick = (now: number): void => {
    const rawDt = Math.min((now - this.lastTime) / 1000, 0.032);
    this.lastTime = now;

    // Convert Apple (damping, response) into physical spring constants (mass = 1)
    const response = Math.max(0.06, this.config.response);
    const omega = (2 * Math.PI) / response;
    const stiffness = omega * omega;
    const damping = 2 * this.config.damping * omega;

    // Sub-step integration (<= 4ms per step) for stability across 60Hz–240Hz displays
    const subSteps = Math.max(1, Math.ceil(rawDt / 0.004));
    const dt = rawDt / subSteps;

    for (let i = 0; i < subSteps; i++) {
      const displacement = this.value - this.target;
      const acceleration = -stiffness * displacement - damping * this.velocity;
      this.velocity += acceleration * dt;
      this.value += this.velocity * dt;
    }

    const settled =
      Math.abs(this.target - this.value) < this.precision &&
      Math.abs(this.velocity) < this.precision * 12;

    if (settled) {
      this.value = this.target;
      this.velocity = 0;
      this.rafId = null;
      this.onUpdate(this.value, 0);
      const cb = this.onRestCallback;
      this.onRestCallback = undefined;
      cb?.();
      return;
    }

    this.onUpdate(this.value, this.velocity);
    this.rafId = requestAnimationFrame(this.tick);
  };

  destroy(): void {
    this.stop();
  }
}

/**
 * Independent X and Y springs for 2D motion (§3: Decompose 2D motion into independent axes).
 */
export class Spring2D {
  readonly x: SpringDriver;
  readonly y: SpringDriver;
  private onUpdate: (x: number, y: number, vx: number, vy: number) => void;

  constructor(
    initialX: number,
    initialY: number,
    onUpdate: (x: number, y: number, vx: number, vy: number) => void,
    config: AppleSpringConfig = SPRING_REPOSITION,
    precision = 0.05
  ) {
    this.onUpdate = onUpdate;
    this.x = new SpringDriver(initialX, () => this.emit(), config, precision);
    this.y = new SpringDriver(initialY, () => this.emit(), config, precision);
  }

  private emit(): void {
    this.onUpdate(this.x.value, this.y.value, this.x.velocity, this.y.velocity);
  }

  setImmediate(x: number, y: number): void {
    this.x.stop();
    this.y.stop();
    this.x.value = x;
    this.x.target = x;
    this.x.velocity = 0;
    this.y.value = y;
    this.y.target = y;
    this.y.velocity = 0;
    this.emit();
  }

  animateTo(
    targetX: number,
    targetY: number,
    options: {
      vx?: number;
      vy?: number;
      config?: AppleSpringConfig;
      immediate?: boolean;
      onRest?: () => void;
    } = {}
  ): void {
    let remaining = 2;
    const handleRest = () => {
      remaining -= 1;
      if (remaining === 0) options.onRest?.();
    };
    this.x.animateTo(targetX, {
      velocity: options.vx,
      config: options.config,
      immediate: options.immediate,
      onRest: handleRest,
    });
    this.y.animateTo(targetY, {
      velocity: options.vy,
      config: options.config,
      immediate: options.immediate,
      onRest: handleRest,
    });
  }

  stop(): void {
    this.x.stop();
    this.y.stop();
  }

  destroy(): void {
    this.x.destroy();
    this.y.destroy();
  }
}
