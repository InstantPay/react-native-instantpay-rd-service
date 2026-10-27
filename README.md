# react-native-instantpay-rd-service

This module supports RD (Registered Device) services for biometric authentication, including fingerprint capture and face capture. These features comply with UIDAI (Unique Identification Authority of India) standards, enabling secure and reliable biometric authentication, Supported only in Android.

### Features ###

* Fingerprint Capture: Integrates with certified RD fingerprint devices to capture biometric data.
* Face Capture: Utilizes UIDAI-certified RD face authentication apps for capturing face biometrics.
* Iris Capture: Integrates with certified RD Iris devices to capture biometric data.
* OVSE Auth: Integrates with Aadhaar App to validate Aadhaar details.

### Installation ###

Ensure that the required RD service apps are installed on the device:

    1. Fingerprint Devices: Install the respective RD service app for your device (e.g., Mantra, Morpho).
    2. Face Authentication: Install the UIDAI Face RD service app [AadhaarFaceRD](https://play.google.com/store/apps/details?id=in.gov.uidai.facerd&hl=en_IN)
        * Permissions: Ensure that the app has the necessary permissions to interact with the RD service.
            * Camera permissions must be granted.
    3. Iris Devices: Install the respective RD service app for your device (e.g., Mantra, Morpho).
    4. Aadhaar App: Ensure that the Aadhaar app is installed for OVSE authentication.


```sh
npm install react-native-instantpay-rd-service
```

## Usage

```js
import RdServices from "react-native-instantpay-rd-service";

// ...

// For Fingerprint Capture
let fingerPidOption = "<?xml version='1.0'?><PidOptions ver='1.0'><Opts fCount='1' fType='1' iCount='0' pCount='0' format='0' pidVer='2.0' timeout='10000' posh='UNKNOWN' env='PP' /><CustOpts></CustOpts></PidOptions>";

const result = await RdServices.getFingerPrint('<Package Name>', fingerPidOption); //Package Name: com.mantra.mfs110.rdservice

| Field     | Finger Device Value | Explanation                       |
| --------- | ------------------- | --------------------------------- |
| `fCount`  | `1` or `2`          | Number of fingerprints to capture |
| `fType`   | `0`'(0:FMR,1:FIR)'  | Fingerprint type (0 = default)    |
| `iCount`  | `0`                 | Iris not used                     |
| `iType`   | `0`                 | Iris type (ignored)               |
| `pCount`  | `0`                 | Face auth not used                |
| `pType`   | `0`                 | Face type                         |
| `format`  | `0`                 | XML PID block                     |
| `pidVer`  | `2.0`               | UIDAI mandated PID version        |
| `timeout` | `20000`             | Capture timeout in ms             |
| `env`     | `P / PP`            | Production / Pre-Prod             |
| `posh`    | `UNKNOWN`           | Position hint                     |


// For Face Auth Capture
let facePidOptions = "<?xmlversion='1.0' encoding='UTF-8'?><PidOptions ver='1.0' env='P'><Opts fCount='' fType='' iCount='' iType='' pCount='' pType='' format='' pidVer='2.0' timeout='' otp='' wadh='' posh='' /><CustOpts><Param name='txnId' value=''/><Param name='purpose' value='auth'/><Param name='language' value='en'/></CustOpts></PidOptions>"

const result = await RdServices.openFaceAuth(facePidOptions);

// For Iris Capture
let irisPidOption = '<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="0" fType="0" iCount="1" iType="0" pCount="0" pType="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P"/><CustOpts></CustOpts></PidOptions>';

const result = await RdServices.openEyeAuth('<Package Name>', irisPidOption); //Package Name : com.mantra.mis100v2.rdservice

| Attribute | Value     | Meaning                                   |
| --------- | --------- | ----------------------------------------- |
| `iCount`  | `1`       | Number of iris captures (1 = single iris) |
| `iType`   | `0`       | Iris type (0 = default)                   |
| `fCount`  | `0`       | No fingerprint ( eg. MIS100V2 is iris)    |
| `pCount`  | `0`       | No face authentication                    |
| `format`  | `0`       | XML PID block                             |
| `pidVer`  | `2.0`     | UIDAI PID version                         |
| `timeout` | `20000`   | 20 seconds capture timeout                |
| `env`     | `P`       | Production (`P`), use `PP` for pre-prod   |
| `posh`    | `UNKNOWN` | Position hint                             |


// For OVSE Auth Capture
let output = await RdService.openAadhaarAuth(<OVSE_JWT_TOKEN>);


```

## License

MIT

---

Created By [Instantpay](https://www.instantpay.in)