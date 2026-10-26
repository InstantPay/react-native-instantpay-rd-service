import { useState, useEffect } from 'react';
import { StyleSheet, View, Text, Button, Alert } from 'react-native';
import RdService from 'react-native-instantpay-rd-service';

export default function App() {
    const [result, setResult] = useState<number | undefined>();

    useEffect(() => {
      
    }, []);

    const eyeRd = async () => {

        let pidOptions = `<?xml version="1.0"?><PidOptions ver="1.0"><Opts fCount="0" fType="0" iCount="1" iType="0" pCount="0" pType="0" format="0" pidVer="2.0" timeout="10000" posh="UNKNOWN" env="P"/><CustOpts></CustOpts></PidOptions>`;

        let output = await RdService.openEyeAuth('com.mantra.mis100v2.rdservice',pidOptions)

        console.log(output)
    }

    const aadhaarAuth = async () => {

        let output = await RdService.openAadhaarAuth('');
        
        //Alert.alert('Aadhaar Auth Result', JSON.stringify(output), [{ text: 'OK' }]);
        console.log(output)
    }

    return (
        <View style={styles.container}>
            <Button 
                title='Open Eye RD'
                onPress={eyeRd}
            />
            <Text></Text>
            <Button 
                title='Open Aadhaar Auth'
                onPress={aadhaarAuth}
            />
            <Text>Result: {result}</Text>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    box: {
        width: 60,
        height: 60,
        marginVertical: 20,
    },
});