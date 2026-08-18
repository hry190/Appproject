// Ambient module declarations for non-TS assets that the template imports.
// Required because the project keeps strict TS but references CSS files.

declare module '*.css';

// CSS modules used in src/components/animated-icon.module.css etc.
// They export an object mapping local class names to globally-scoped names.
declare module '*.module.css' {
  const classes: Record<string, string>;
  export default classes;
}

declare module '*.svg' {
  const src: number;
  export default src;
}
