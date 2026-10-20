import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
    getFingerPrint(deviceName: string, pidOption: string): Promise<string>;
    openFaceAuth(pidOption: string): Promise<string>;
    openEyeAuth(deviceName: string, pidOption: string): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('InstantpayRdService');
