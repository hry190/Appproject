---
name: figma-to-frontend
description: Convert a Figma design link into project frontend pages (Expo Router in this repo). Triggers when the user pastes a Figma URL, says "照这个稿子实现", "figma to page", or names a node/frame by name. Coordinates the official Figma MCP (mcp.figma.com/mcp) for read layout, the project's Expo Router 57 conventions, and visual verification.
---

# figma-to-frontend

Project: **机巧江湖** — Expo Router 57 / React 19 / RN 0.86 (web + native targets).

Goal: take a Figma frame URL, read its structure via the official Figma MCP (tool prefix `mcp__figma__*`), and emit production Expo Router pages.

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

If the user pastes a screenshot instead of a URL, ask once: "请给我这个稿子的 Figma 链接，或确认我可以用官方 Figma MCP 的 `get_screenshot` 截该文件对应 Node。" Don't guess.

### 2. Pull design data via MCP

Call the official Figma MCP via the `figma` server (registered globally in `~/.claude.json` for this project, URL `https://mcp.figma.com/mcp`, OAuth-authenticated; the access/refresh tokens are cached in `~/.claude/.credentials.json` and auto-renew). Tool prefix: `mcp__figma__*`. If those tools aren't visible, the user hasn't restarted Claude Code since adding the server entry — say so rather than guessing.

1. `mcp__figma__whoami` — sanity check that the OAuth session is still alive; if it errors with an auth problem, tell the user to re-authenticate (Claude Code will pop a browser flow on the next MCP call). If the user has a `View`-only seat (as opposed to `Editor` / `Dev`), write-side tools like `create_new_file` / `use_figma` / `generate_figma_design` will refuse — that's fine, this skill is read-only by design.
2. **`mcp__figma__get_design_context` is the primary read tool.** Before calling it, load the `figma:figma-design-to-code` skill (the tool's own description mandates this — it explains the reference-code / screenshot / metadata shape and how to adapt it to the target project). Call `get_design_context` with `{fileKey, nodeId: <chosen frame>}` — it returns reference code, a screenshot, and contextual metadata in one round trip. This is almost always the only call you need.
3. For non-trivial designs (deep nesting, many sibling frames), follow up with `mcp__figma__get_metadata` using `{fileKey, nodeId: <page or top-level frame>}` to get a cheap XML overview — node IDs, layer types, names, positions, sizes only. The tool's own description says "always prefer `get_design_context`"; use this only as a navigation map.
4. For design tokens, call `mcp__figma__get_variable_defs` with `{nodeId}` — returns bound color / spacing / typography variables for that subtree. (There is no separate `get_styles` tool — paint / text / effect styles come back inside the `get_design_context` payload.)
5. For a higher-resolution visual reference than the one `get_design_context` already includes, call `mcp__figma__get_screenshot` with `{fileKey, nodeId, maxDimension}` (PNG only — there is no SVG export path on this MCP; `maxDimension` defaults to 1024, raise it to inspect fine detail).

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
| Icons | `expo-symbols` for SF-style names; otherwise use the SVG assets you grabbed via `get_screenshot` and put them in `mobile/assets/images/`. |
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
3. Pull the Figma render via `get_screenshot` (or the user's screenshot) and compare side by side.
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

- Official Figma MCP: `https://mcp.figma.com/mcp` (HTTP, OAuth; access/refresh tokens auto-renew)
- Where it's registered: `~/.claude.json` → `projects["d:/App/Appproject"].mcpServers.figma`
- Auth cache: `~/.claude/.credentials.json` (key prefixed `figma|`)
- Personal access token (only needed if you switch to a self-hosted Figma MCP): Figma → Settings → Personal access tokens
- This repo's App Router docs: [mobile/app.json](mobile/app.json) and [mobile/AGENTS.md](mobile/AGENTS.md)
