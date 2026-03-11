# Release Signing

This repository is public and the signing key must never be committed. Keep the keystore local and store it in GitHub repository secrets as a Base64 string.

## 1. Generate a release keystore

Run this once on your own machine:

```bash
keytool -genkeypair \
  -v \
  -keystore kudo-release.keystore \
  -alias kudo \
  -keyalg RSA \
  -keysize 4096 \
  -validity 3650
```

You will be prompted for:

- keystore password
- key password
- owner information

Keep these values. You will need them for GitHub Secrets.

## 2. Convert the keystore to Base64

Linux:

```bash
base64 -w 0 kudo-release.keystore > kudo-release.keystore.base64
```

macOS:

```bash
base64 < kudo-release.keystore | tr -d '\n' > kudo-release.keystore.base64
```

The resulting file contains a single-line Base64 string.

## 3. Required GitHub repository secrets

Add these secrets in:

`GitHub repository -> Settings -> Secrets and variables -> Actions`

Required secrets:

- `ANDROID_KEYSTORE_BASE64` — Base64 content of `kudo-release.keystore`
- `ANDROID_KEYSTORE_PASSWORD` — keystore password
- `ANDROID_KEY_ALIAS` — key alias, for example `kudo`
- `ANDROID_KEY_PASSWORD` — key password

## 4. How releases work

When you push a Git tag, GitHub Actions will:

1. decode the keystore from secrets
2. build the Android `release` APK
3. sign the APK with the release keystore
4. create/update the GitHub Release for that tag
5. attach a universal APK named `Kudo-<tag>.apk`

Example:

```bash
git tag v1.0.1
git push origin v1.0.1
```
