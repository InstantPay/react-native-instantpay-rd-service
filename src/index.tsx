import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-instantpay-rd-service' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const InstantpayRdService = NativeModules.InstantpayRdService
    ? NativeModules.InstantpayRdService
    : new Proxy(
        {},
        {
            get() {
            throw new Error(LINKING_ERROR);
            },
        }
    );

const RdService = (Platform.OS === "ios") ? null : {

    getFingerPrint: (deviceName:string,pidOption:string) => {

        return InstantpayRdService.getFingerPrint(deviceName,pidOption);
    }
}
    
    
export default RdService;
