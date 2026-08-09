# ReelShelf Web (Netlify demo)

Browser demo of Shared Clips Inbox. Data stays in **localStorage** on each visitor’s device.

## Limits vs Android app

- No native share sheet from LINE/Messenger — users **paste** links
- No cloud sync / accounts
- YouTube preview metadata may fail in some browsers (CORS); saving still works

## Local run

```bash
cd web
npm install
npm run dev
```

## Deploy to Netlify

1. Push this repo (or just the `web` folder) to GitHub.
2. In Netlify: **Add new site → Import from Git**
3. Set:
   - Base directory: `web`
   - Build command: `npm run build`
   - Publish directory: `web/dist`
4. Deploy

Or from `web/` with Netlify CLI:

```bash
npm run build
npx netlify deploy --prod --dir=dist
```

`netlify.toml` in this folder already configures SPA redirects.
