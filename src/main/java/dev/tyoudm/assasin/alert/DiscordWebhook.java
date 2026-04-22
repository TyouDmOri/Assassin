/*
 * ▄▀█ █▀ █▀ ▄▀█ █▀ █ █▄░█
 * █▀█ ▄█ ▄█ █▀█ ▄█ █ █░▀█
 *     ASSASIN AntiCheat v1.0.0
 *     Mitigation-First Server-Side AntiCheat
 *     Target: Paper 1.21.11 "Mounts of Mayhem"
 *     Author: TyouDm
 */

package dev.tyoudm.assasin.alert;

import dev.tyoudm.assasin.AssasinColors;
import dev.tyoudm.assasin.handler.async.AsyncProcessor;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.logging.Logger;

/**
 * Async Discord webhook sender for ASSASIN alerts.
 *
 * <p>Sends rich embed messages to a configured Discord webhook URL.
 * All HTTP calls are executed on the {@link AsyncProcessor} to avoid
 * blocking the main thread.
 *
 * <h2>Embed format</h2>
 * <ul>
 *   <li>Color: severity-based (see {@link AlertFormatter#discordColor})</li>
 *   <li>Title: {@code ⚠ PlayerName failed CheckName}</li>
 *   <li>Description: full violation details</li>
 *   <li>Footer: {@code ASSASIN AntiCheat v1.0.0 • by TyouDm}</li>
 *   <li>Timestamp: ISO-8601 UTC</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * The webhook URL is read from {@code alerts.yml} in FASE 18.
 * Until then, it is passed directly to the constructor.
 *
 * @author TyouDm
 * @version 1.0.0
 */
public final class DiscordWebhook {

    /** Discord embed footer text. */
    private static final String FOOTER_TEXT = "ASSASIN AntiCheat v1.0.0 • by TyouDm";

    /** HTTP connection timeout in ms. */
    private static final int CONNECT_TIMEOUT_MS = 5_000;

    /** HTTP read timeout in ms. */
    private static final int READ_TIMEOUT_MS = 5_000;

    // ─── State ────────────────────────────────────────────────────────────────

    private final String         webhookUrl;
    private final AsyncProcessor asyncProcessor;
    private final Logger         logger;

    /** Whether the webhook is enabled. */
    private volatile boolean enabled;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new Discord webhook sender.
     *
     * @param webhookUrl     the Discord webhook URL (may be empty to disable)
     * @param asyncProcessor the async processor for HTTP calls
     * @param logger         plugin logger
     */
    public DiscordWebhook(final String webhookUrl, final AsyncProcessor asyncProcessor,
                          final Logger logger) {
        this.webhookUrl     = webhookUrl;
        this.asyncProcessor = asyncProcessor;
        this.logger         = logger;
        this.enabled        = webhookUrl != null && !webhookUrl.isBlank();
    }

    // ─── Send ─────────────────────────────────────────────────────────────────

    /**
     * Sends an alert embed to Discord asynchronously.
     * No-op if the webhook is disabled or the URL is empty.
     *
     * @param ctx the alert context
     */
    public void send(final AlertContext ctx) {
        if (!enabled) return;

        asyncProcessor.submit(() -> {
            try {
                final String payload = buildPayload(ctx);
                post(payload);
            } catch (final Exception ex) {
                logger.warning("[ASSASIN] Discord webhook failed: " + ex.getMessage());
            }
        });
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    /**
     * Builds the JSON payload for the Discord embed.
     *
     * @param ctx the alert context
     * @return JSON string
     */
    private String buildPayload(final AlertContext ctx) {
        final int    color       = AlertFormatter.discordColor(ctx.severity());
        final String title       = escape("⚠ " + ctx.playerName() + " failed " + ctx.checkName());
        final String description = escape(AlertFormatter.buildDiscordDescription(ctx));
        final String timestamp   = Instant.ofEpochMilli(ctx.timestampMs()).toString();
        final String footer      = escape(FOOTER_TEXT);

        return "{"
            + "\"embeds\":[{"
            + "\"title\":\"" + title + "\","
            + "\"description\":\"" + description + "\","
            + "\"color\":" + color + ","
            + "\"footer\":{\"text\":\"" + footer + "\"},"
            + "\"timestamp\":\"" + timestamp + "\""
            + "}]"
            + "}";
    }

    /**
     * Posts the JSON payload to the webhook URL via HTTP POST.
     *
     * @param payload the JSON payload
     * @throws Exception if the HTTP call fails
     */
    private void post(final String payload) throws Exception {
        final HttpURLConnection conn = (HttpURLConnection)
            URI.create(webhookUrl).toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("User-Agent", "ASSASIN-AntiCheat/1.0.0");
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);
        conn.setDoOutput(true);

        final byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }

        final int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            logger.warning("[ASSASIN] Discord webhook returned HTTP " + responseCode);
        }

        conn.disconnect();
    }

    /**
     * Escapes a string for safe inclusion in a JSON value.
     *
     * @param s the string to escape
     * @return escaped string
     */
    private static String escape(final String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ─── Accessors ────────────────────────────────────────────────────────────

    /** Returns {@code true} if the webhook is enabled. */
    public boolean isEnabled()                    { return enabled; }

    /** Enables or disables the webhook at runtime. */
    public void setEnabled(final boolean enabled) { this.enabled = enabled; }
}
