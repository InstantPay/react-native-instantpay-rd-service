import InstantpayRdService from './NativeInstantpayRdService';

/* export function multiply(a: number, b: number): number {
    return InstantpayRdService.multiply(a, b);
} */

const RdService = {
    getFingerPrint: (deviceName:string,pidOption:string) => {
        return InstantpayRdService.getFingerPrint(deviceName,pidOption);
    },
    openFaceAuth: (pidOption:string,) => {
        return InstantpayRdService.openFaceAuth(pidOption);
    },
    openEyeAuth: (deviceName:string,pidOption:string) => {
        return InstantpayRdService.openEyeAuth(deviceName,pidOption);
    },
}
    
    
export default RdService;
