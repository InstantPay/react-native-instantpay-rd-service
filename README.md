# react-native-instantpay-rd-service

Reading finger print data using RD services, Supported only in Android.

RD service supported device :

1. Morpho
2. Mantra
3. PB510
4. SecuGen and many more...


## Installation

```sh
npm install react-native-instantpay-rd-service
```

## Usage


```js
import RdServices from "react-native-instantpay-rd-service";

// ...

let pidOption = "<?xml version='1.0'?><PidOptions ver='1.0'><Opts fCount='1' fType='1' iCount='0' pCount='0' format='0' pidVer='2.0' timeout='10000' posh='UNKNOWN' env='PP' /><CustOpts></CustOpts></PidOptions>";

const result = await RdServices.getFingerPrint('com.mantra.mfs110.rdservice', pidOption);
```

## License

MIT

---

Created By [Instantpay](https://www.instantpay.in)
