# react-native-instantpay-rd-service

This module supports RD (Registered Device) services for biometric authentication, including fingerprint capture and face capture. These features comply with UIDAI (Unique Identification Authority of India) standards, enabling secure and reliable biometric authentication, Supported only in Android.

### Features ###

* Fingerprint Capture: Integrates with certified RD fingerprint devices to capture biometric data.
* Face Capture: Utilizes UIDAI-certified RD face authentication apps for capturing face biometrics.

### Installation ###

Ensure that the required RD service apps are installed on the device:

    1. Fingerprint Devices: Install the respective RD service app for your device (e.g., Mantra, Morpho).
    2. Face Authentication: Install the UIDAI Face RD service app [AadhaarFaceRD](https://play.google.com/store/apps/details?id=in.gov.uidai.facerd&hl=en_IN)
        * Permissions: Ensure that the app has the necessary permissions to interact with the RD service.
            * Camera permissions must be granted.

```sh
npm install react-native-instantpay-rd-service
```

## Usage

```js
import RdServices from "react-native-instantpay-rd-service";

// ...

// For Fingerprint Capture
let fingerPidOption = "<?xml version='1.0'?><PidOptions ver='1.0'><Opts fCount='1' fType='1' iCount='0' pCount='0' format='0' pidVer='2.0' timeout='10000' posh='UNKNOWN' env='PP' /><CustOpts></CustOpts></PidOptions>";

const result = await RdServices.getFingerPrint('com.mantra.mfs110.rdservice', fingerPidOption);

// For Face Auth Capture
let facePidOptions = "<?xmlversion='1.0' encoding='UTF-8'?><PidOptions ver='1.0' env='P'><Opts fCount='' fType='' iCount='' iType='' pCount='' pType='' format='' pidVer='2.0' timeout='' otp='' wadh='' posh='' /><CustOpts><Param name='txnId' value=''/><Param name='purpose' value='auth'/><Param name='language' value='en'/></CustOpts></PidOptions>"

const result = await RdServices.openFaceAuth(facePidOptions);

```

## License

MIT

---

Created By [Instantpay](https://www.instantpay.in)
