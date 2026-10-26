import InstantpayRdService from './NativeInstantpayRdService';

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
    openAadhaarAuth: (token:string) => {
        return InstantpayRdService.openAadhaarAuth(token);
    }
}
    
    
export default RdService;
