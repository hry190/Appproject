---
name: figma-to-frontend
description: Convert a Figma design link into project frontend pages (Expo Router in this repo). Triggers when the user pastes a Figma URL, says "照这个稿子实现", "figma to page", or names a node/frame by name. Coordinates the Framelink Figma MCP (read layout), the project's Expo Router 57 conventions, and visual verification.
---

# figma-to-frontend

Project: **机巧江湖** — Expo Router 57 / React 19 / RN 0.86 (web + native targets).

Goal: take a Figma frame URL, read its structure via MCP, and emit production Expo Router pages.

## When to use

- User pastes a `figma.com/file/...` or `figma.com/design/...` URL.
- User says: "按这个稿子实现页面", "把这个 Figma 稿转成页面", "implement this design".
- User gives a screenshot of a Figma frame and asks to match it.

If no Figma URL is present and the user is asking for design help from scratch, defer to the built-in `frontend-design` skill instead.

## Workflow

### 1. Parse the URL

Extract two pieces:
- `fileKey` — the segment after `/file/` or `/design/` (also `/protoboard/`).
- `nodeId` — the `?node-id=XXXX-YYYY` param, decoded as `XXXX:YYYY` (replace `-` with `:` when calling MCP).

Example: `https://www.figma.com/design/AbCdEf12345/My-File?node-id=12-34`
→ `fileKey="AbCdEf12345"`, `nodeId="12:34"`.

If the user pastes a screenshot instead of a URL, ask once: "请给我这个稿子的 Figma 链接，或确认我可以用 Framelink MCP 的 `get_screenshot` 截该文件对应 Node。" Don't guess.

### 2. Pull design data via MCP

Call Framelink MCP tools via the `figmaframelink` server (this repo's MCP name is `figmaframelink`, NOT `figma` — the host env already has the official Figma MCP under `figma` and they collide; we use Framelink for richer parsing). Tool prefix: `mcp__figmaframelink__*`.

1. `mcp__figmaframelink__get_file` with `{fileKey}` to confirm access and pull the document root. Stop and surface a clear error if the response indicates permission denied — the user needs to be added to the file's seat.
2. `mcp__figmaframelink__get_node` with `{fileKey, nodeId}` for the chosen frame — gives layout tree, fills, strokes, text, component refs, variables.
3. If icons or images exist as fills, call `mcp__figmaframelink__get_image` for the relevant nodeIds to grab PNG/SVG assets (set `format` to `svg` for icons, `png` @2x for images).
4. Optional, but recommended: `mcp__figmaframelink__get_local_variables` and `mcp__figmaframelink__get_styles` to capture the design-token layer (colors, text styles, effect styles) for theme mapping.

Read enough to reproduce, but don't dump the whole tree into a single file. Five calls is a reasonable budget; more means the user should narrow the scope.

### 3. Map Figma elements → Expo Router file

This repo's structure:
- Pages live under [mobile/src/app/](mobile/src/app/) with file-based routing via `expo-router`.
- Tabs live under [mobile/src/app/(tabs)/](mobile/src/app/(tabs)/).
- Shared components live under [mobile/src/components/](mobile/src/components/).
- **Every new page** for this app must follow [mobile/AGENTS.md](mobile/AGENTS.md): read the versioned Expo 57 docs (`https://docs.expo.dev/versions/v57.0.0/`) before writing any code.

Routing rules:
- Top-level page → `mobile/src/app/<name>.tsx`.
- Tab page → `mobile/src/app/(tabs)/<name>.tsx`. Update [mobile/src/components/app-tabs.tsx](mobile/src/components/app-tabs.tsx) so the tab list stays in sync.
- Stack route nested in a tab → `mobile/src/app/(tabs)/<tab>/<screen>.tsx` with its own `_layout.tsx`.
- New shared component → `mobile/src/components/<name>.tsx`, with `<name>.module.css` for styles when appropriate. Don't reuse the `app-tabs.*` convention outside tabs.

### 4. Write the page

Constraints — non-negotiable for this repo:

| Concern | Rule |
|---|---|
| API levels | Pin to Expo 57 APIs. Use docs URL above as source of truth. |
| Styling | Use `StyleSheet.create` or CSS modules; do **not** pull in Tailwind. |
| Icons | `expo-symbols` for SF-style names; otherwise use the SVG assets you grabbed via `get_image` and put them in `mobile/assets/images/`. |
| Safe areas | Wrap content in `SafeAreaView` from `react-native-safe-area-context`; honour `insets.top`/`bottom`. |
| Fonts/colors | Pull from the Figma node's bound variables; if absent, fall back to the existing theme tokens used in `themed-text.tsx`/`themed-view.tsx`. |
| Layout primitive | RN flex defaults. Don't import `styled-components` or `nativewind`. |
| Type | Every exported screen gets a typed default; typed routes are enabled (`app.json:39`). |
| Web target | Prefer code that also renders under `react-native-web`. Avoid platform-only modules unless guarded with `Platform.OS`. |

While writing, also keep the `frontend-design` guidelines in mind (typography pairing, intentional spacing, restraint with color) — the page should not look like a Material/Tailwind default.

### 5. Verify visually

If the design is non-trivial (multi-section screens, custom illustrations):

1. Use the `webapp-testing` skill to boot the Expo web target (`npx expo start --web`).
2. Capture a Playwright screenshot of the new route.
3. Pull the Figma render via `get_image` (or the user's screenshot) and compare side by side.
4. Iterate on spacing, color, and typography until it matches within reason.

For trivial screens (a single button, a typo fix) skip this step.

### 6. Hand-off

When done, report:
- Files created/modified (with markdown links using repo-relative paths).
- The Figma URL it was generated from.
- Any field you couldn't read or had to invent (e.g., missing fonts → document the substitution).

## Things to refuse

- "Implement this Figma design" without a URL or screenshot — ask, don't fabricate.
- Generating code that ignores the existing folder layout (e.g., dropping a new project under [mobile/](mobile/) at the wrong path).
- Inventing tokens, copy text, or icon names that the Figma data didn't supply.

## Reference

- Figma MCP package: `figma-developer-mcp` (Framelink) — [GLips/Figma-Context-MCP](https://github.com/GLips/Figma-Context-MCP)
- Personal access token: Figma → Settings → Personal access tokens
- This repo's App Router docs: [mobile/app.json](mobile/app.json) and [mobile/AGENTS.md](mobile/AGENTS.md)
