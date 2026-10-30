# Quran Radio — isolated homepage UI prototype

This directory is an isolated, static visual prototype based on the supplied target screenshot.

## Safety boundary
- Prototype work lives on branch `ui/homepage-design-prototype`.
- No existing Android application source, app entry point, Gradle configuration, radio playback logic, or `main` branch files were changed.
- The prototype does not connect to the real radio stream, prayer-time provider, notifications, or app navigation. Playback and navigation controls are visual demonstrations only.
- Do not copy this prototype over production files. Integration should happen only after the owner reviews and explicitly requests transfer to `main`.

## Preview
Open `index.html` in a browser. For a local server, run:

```bash
python -m http.server 8080
```

Then open `http://localhost:8080/design-prototype/` if serving from the repository root, or `http://localhost:8080/` if serving from this directory.

The GitHub Pages preview is deployed automatically when files under `design-prototype/` are updated on the prototype branch. No manual workflow selection is needed for normal updates. The workflow file is kept on this branch to preserve isolation from `main`.

## Files
- `index.html` — isolated semantic layout
- `styles.css` — responsive visual system, blue/sky-blue palette, card styles and responsive behavior
- `app.js` — custom inline SVG icons, category data, search filtering, and demo-only controls

## Review checklist
- [x] Use the uploaded Cairo/Nile photograph in the hero card and tune responsive crops.
- [x] Add the supplied app emblem, geometric pattern, and prayer-card silhouette assets.
- [x] Preserve 12 categories, five shortcuts, and five bottom-navigation items.
- [x] Add visible keyboard-focus states and reduced-motion handling.
- [ ] Compare against the original reference on matching desktop and Android screen sizes.
- [ ] Verify Arabic typography and RTL on a real Android viewport.
- [ ] Replace demo controls with real app callbacks only during a separately approved integration.
