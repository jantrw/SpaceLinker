# SpaceLinker — Optimization Plan

## 🔍 What I Found

### 🚨 Critical Bugs

1. **ISSData — broken slash command:** onSlashCommandInteraction calls `event.getHook().sendMessage()` but never calls `event.deferReply()` first (the code was removed with a comment about "double replies"). This means /iss will throw an exception or silently fail every time.
2. **Hardcoded Windows paths everywhere:** `A:\Development\SpaceLinker\src\main\resources\config.properties` appears in 3 separate files. This won't run on any machine other than yours, and breaks if you move the project.
3. **Secrets committed to git:** config.properties contains your actual Discord bot token, NASA API key, and GeoNames username — all checked into version control. Anyone with repo access has your bot token.

### ⚠️ Bugs & Reliability Issues

4. **No error handling for null/uninitialized data:** If any API call in JSONFetcherIss fails, the fields stay null. The next fetch call that depends on them (e.g. fetchMapUrlTimeZone uses this.latitude) will NPE.
5. **Slash command registration on every startup:** `bot.updateCommands().addCommand(s...)` runs every time the bot starts, which hits Discord's rate limits. Should only update when commands change.
6. **NasaPictureOfTheDay creates its own JSONFetcherNasa in constructor** — meaning the NASA API is called as soon as the listener is instantiated, not when a user actually runs the command. Same issue with JSONFetcherIss.setUsername() throwing a raw RuntimeException if the config file is missing.
7. **Slash commands + prefix commands are uncoordinated:** /picture and /pictureinfo work via slash commands, but nasapic/nasapicinfo work via prefix. ISSData only handles slash commands (no prefix variant at all, so iss prefix command would fall through to "unknown command").

### 🏗️ Code Quality & Architecture

8. **Inconsistent JSON libraries:** Uses both com.google.gson and org.json across different files. Pick one.
9. **DiscordBot extends ListenerAdapter unnecessarily** — it never registers itself as a listener and has no event handlers.
10. **No connection pooling / resource management:** `HttpClient.newHttpClient()` is called on every single API request. Should be a shared static instance.
11. **GuildDataManager uses in-memory HashMap:** Prefixes are lost on every restart. The class itself documents this, but it means setprefix is effectively useless across restarts.
12. **No .gitignore:** .idea/, target/, and compiled classes are tracked in git.
13. **Eager API fetching in constructors:** JSONFetcherNasa fetches data in its constructor — you can't create the object without hitting the network, and you can't refresh data later.
14. **Embed description truncation risk:** NASA explanations can be very long (5000+ chars), but Discord embed descriptions are limited to 4096 characters. No truncation is done.

---

## 📋 Optimization Plan

### Phase 1: Critical Fixes

- [x] 1. Fix /iss command — add back event.deferReply() or use event.reply() instead of event.getHook().sendMessage()
- [x] 2. Externalize config path — use classpath loading (getClass().getResourceAsStream("/config.properties")) instead of hardcoded absolute paths
- [x] 3. Add .gitignore — exclude .idea/, target/, *.class, config.properties
- [ ] 4. Rotate your bot token — it's in git history, consider it compromised

### Phase 2: Reliability

- [x] 5. Centralize config loading — single Config class that loads once, shared across all classes
- [x] 6. Add null-safety to ISS fetchers — check that previous fetch steps succeeded before depending on their data
- [x] 7. Lazy-load API data — fetch on command, not in constructors
- [x] 8. Truncate embed fields to Discord's character limits

### Phase 3a: Dead Code & Consistency

- [x] 9. Pick one JSON library — drop either Gson or org.json
- [x] 12. Add a proper logging framework instead of System.out.println and e.printStackTrace()
- [x] 13. Remove DiscordBot extends ListenerAdapter since it doesn't listen to anything

### Phase 3b: Architecture & Resource Management

- [x] 10. Reuse a single HttpClient instance (static/singleton)
- [x] 11. Unify slash + prefix command handling — either add prefix support for iss, or drop prefix commands entirely and go slash-only

### Phase 3c: Performance Optimization

- [ ] 14. Conditional slash command registration — register once, only update when commands change

### Phase 4: Persistence

- [ ] 15. Persistent guild data — SQLite for prefix storage (you already noted this in the TODO)
