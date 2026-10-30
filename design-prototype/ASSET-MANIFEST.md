# Asset manifest

| Asset | Current implementation | Final asset needed |
|---|---|---|
| App emblem | `assets/app-emblem.svg` | Confirm official brand artwork and licensing before production integration |
| Cairo radio hero | `assets/cairo-nile-hero.jpg` | Confirm image ownership/licensing and final crop before production integration |
| Category icons (12) | Inline SVG paths in `app.js` | Review icon family against the original reference |
| Prayer card artwork | `assets/prayer-mosque-silhouette.svg` | Confirm visual fit and production approval |
| Khatma cards | Gradient cards with geometric motif | Approved Quran/book artwork if required by final reference |
| Shortcut icons (5) | Inline SVG paths in `app.js` | Review icon family against the original reference |
| Bottom navigation (5) | Inline SVG paths in `app.js` | Review icon family against the original reference |

## Asset constraints
- Prefer SVG for interface icons; use transparent PNG/WebP for decorative art.
- Keep icon viewboxes consistent (24 × 24 or 32 × 32).
- Avoid emoji and Unicode glyphs as final icon artwork.
- Keep source/licensing information for every photograph.
