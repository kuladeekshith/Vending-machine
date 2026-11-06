# ValueVend Monitor (Android / Kotlin)

A read-only Android monitoring client scaffold. It does **not** bypass QR payments,
alter payment state, unlock the vending machine, or send dispense commands.

## What it does
- Calls an operator-provided HTTPS monitoring endpoint.
- Displays connectivity, operational state, payment-service health and recent transaction status.
- Uses a bearer token supplied by the authorized operator.

## API contract expected by this sample

Configure the app with the base URL and machine ID supplied by the machine operator.
The app makes this request:

`GET https://YOUR-HOST/api/v1/machines/{machineId}/status`

Header:

`Authorization: Bearer YOUR_OPERATOR_TOKEN`

Example JSON response:

```json
{
  "machineId": "VV-001",
  "connectivity": "ONLINE",
  "operationalState": "READY",
  "paymentService": "AVAILABLE",
  "lastTransactionStatus": "PAID",
  "lastTransactionAt": "2026-11-06T06:30:00+05:30",
  "errorCode": "",
  "checkedAt": "2026-11-06T06:35:00+05:30"
}
```

This is an example contract only. Your operator/vendor must provide the actual
endpoint, authentication method, field names and permission scope. Do not assume
that ValueVend exposes this route. The token should be read-only and limited to
the specific machine(s) you are authorized to monitor.

## Build the APK

1. Install Android Studio (current stable) and Android SDK Platform 35.
2. Extract this project ZIP.
3. In Android Studio choose **Open** and select the `ValueVendMonitor` folder.
4. Allow Gradle sync to finish. Use JDK 17 (Android Studio's bundled JDK is suitable).
5. Connect an Android phone with USB debugging enabled, or create an emulator.
6. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. Android Studio reports the APK location. Usually:
   `app/build/outputs/apk/debug/app-debug.apk`

For a release APK, configure a signing key in **Build > Generate Signed Bundle / APK**.
Keep the signing key private.

## Important implementation notes
- The sample requires HTTPS and rejects plain HTTP.
- The sample is a manual-refresh proof of concept; it does not poll in the background.
- For production, store credentials using Android Keystore-backed encrypted storage,
  avoid logging tokens, use vendor-approved certificate configuration, and add
  role-based access controls and audit logging on the server.
- Do not put a privileged or shared machine-admin token in a client APK.
