# Feedback gateway boundary

## Client request

The public UniApp client sends `POST` JSON to the configured public gateway:

```text
https://uniaappauthorization.antobot.info/api/v1/feedback
```

The payload follows the UniApp Server feedback contract:

```json
{
  "appId": "device-install-id",
  "userId": "optional-application-user-id",
  "fullName": "Optional display name",
  "appVersion": "1.8.9-beta",
  "title": "Feedback title",
  "description": "Feedback description",
  "source": "uniapp",
  "runtimeLogBase64": "optional-base64",
  "runtimeLogFileName": "runtime-log.txt",
  "runtimeLogMimeType": "text/plain"
}
```

`runtimeLogBase64`, `runtimeLogFileName` and `runtimeLogMimeType` are omitted
when the user does not consent to diagnostic upload. Raw log text is never put
in an extra compatibility field. Android bounds the capture file and the
uploaded tail to 700,000 bytes.

The client performs one application-level submission attempt. In particular,
it does not retry without the diagnostic attachment after an error: the report
may already have been created before a downstream attachment failure, so that
fallback could create a duplicate report.

The client does **not** send either of these private inbound headers:

- `x-uniapp-api-secret`
- `x-uniapp-discord-user-id`

`userId` in the JSON body is compatibility metadata and is never an IAM actor.

## Required gateway behavior

The gateway implementation and deployment live outside this mobile repository.
Before this route can be used in production, the gateway must:

1. authenticate the caller or resolve an existing trusted app/user binding;
2. derive the verified Discord subject server-side;
3. strip any inbound `x-uniapp-api-secret` and
   `x-uniapp-discord-user-id` values;
4. forward the canonical JSON to the private UniApp Server
   `POST /api/v1/feedback` route;
5. add the gateway-owned `x-uniapp-api-secret` and the verified
   `x-uniapp-discord-user-id` only on that internal hop;
6. return the UniApp Server response without converting authorization or
   dependency failures into success.

The expected successful response is:

```json
{
  "status": "ok",
  "data": {
    "reference": "report-id",
    "acceptedAt": "2026-08-06T12:00:00.000Z"
  },
  "requestId": "correlation-id"
}
```

The client treats a success response without `data.reference` as a failed
submission. This prevents a proxy or upstream HTML response from being shown as
a successful feedback submission.
