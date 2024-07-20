package com.instantpayrdservice


import android.app.Activity
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap


class InstantpayRdServiceModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    val SUCCESS: String = "SUCCESS"
    val FAILED: String = "FAILED"
    lateinit var DATA: String
    private var responsePromise: Promise? = null
    private var packageNameParam = ""
    private var pidOptionParam = ""

    override fun getName(): String {
        return NAME
    }

    companion object {
        const val NAME = "InstantpayRdService"
        const val RDINFO_CODE = 1
        const val RDCAPTURE_CODE = 2
    }

    private val activityEventListener = object : BaseActivityEventListener(){
        override fun onActivityResult(
            activity: Activity?,
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) {
            super.onActivityResult(activity, requestCode, resultCode, data)

            if(data == null){
                return resolve("No action taken")
            }

            if(resultCode == RDINFO_CODE){
                val requiredValue = data.getStringExtra("RD_SERVICE_INFO")

                if(requiredValue == null){
                    return resolve("Device not ready")
                }

                if(requiredValue.length <= 10){
                    return resolve("Device not ready" , FAILED, "", "", requiredValue+" #RDRES1")
                }

                if(requiredValue.lowercase().contains("notready" , true)){
                    return resolve("Device not ready" , FAILED, "", "", requiredValue+" #RDRES3")
                }

                captureData()
                return
            }

            if(resultCode == RDCAPTURE_CODE){

                val captureXML = data.getStringExtra("PID_DATA")

                if (captureXML == null || captureXML.length <= 10) {
                    return resolve("Device not ready" , FAILED, "", "", captureXML+" #RDRES4")
                }

                if (captureXML.lowercase().contains("device not ready", true)) {
                    return resolve("Device not ready" , FAILED, "", "", captureXML+" #RDRES5")
                }

                val sanitizeXml = parseBioMetricData(captureXML)

                return resolve("Successfully Captured the data",SUCCESS, sanitizeXml)
            }
        }
    }

    init {
        reactContext.addActivityEventListener(activityEventListener)
    }

    private fun deviceInfo(){
        try {

            val activity = currentActivity ?: return resolve("Activity doesn't exist "+"#DVIRD3")

            val intent =  Intent()
            intent.setAction("in.gov.uidai.rdservice.fp.INFO")
            activity.startActivityForResult(intent, RDINFO_CODE)
        }
        catch (e: Exception){
            resolve("RD services not available" , FAILED, "", "", e.message.toString()+" #DVIRD1")
        }
    }

    private fun captureData(){
        try {

            val activity = currentActivity ?: return resolve("Activity doesn't exist "+"#CPDRD3")

            var pidOption  = "<?xml version=\"1.0\"?><PidOptions ver=\"1.0\"><Opts fCount=\"1\" fType=\"2\" iCount=\"0\" pCount=\"0\" format=\"0\" pidVer=\"2.0\" timeout=\"10000\" posh=\"UNKNOWN\" env=\"P\" /><CustOpts></CustOpts></PidOptions>"

            if (pidOptionParam.length >= 10 ) {
                pidOption = pidOptionParam
            }

            val intent = Intent()

            intent.setAction("in.gov.uidai.rdservice.fp.CAPTURE")
            intent.putExtra("PID_OPTIONS", pidOption)
            intent.setPackage(packageNameParam)

            activity.startActivityForResult(intent, RDCAPTURE_CODE)
        }
        catch (e:Exception){
            resolve("Selected device not found" , FAILED, "", "", e.message.toString()+" #CPDRD1")
        }
    }

    private fun parseBioMetricData(xmlData: String): String {

        var bioxml = xmlData

        bioxml = bioxml.replace("\\n   ", " ")
        bioxml = bioxml.replace("\\n   ", " ")
        bioxml = bioxml.replace("\\n ", " ")

        return bioxml
    }

    @ReactMethod
    fun getFingerPrint(deviceName: String, pidOption: String, prm: Promise) {
        try {
            responsePromise = prm
            packageNameParam = deviceName
            pidOptionParam = pidOption
            deviceInfo()
        }
        catch (e: Exception) {
            resolve("RD services not available" , FAILED, "", "", e.message.toString()+" #GFPRD1")
        }
    }

    private fun resolve(message: String, status: String = FAILED ,data: String = "", actCode: String = "", exceptionMessage: String = "" ){

        if(responsePromise == null){
            return
        }

        val map: WritableMap = Arguments.createMap()
        map.putString("status",status)
        map.putString("message",message)
        map.putString("data",data)
        map.putString("actCode",actCode)

        if(exceptionMessage.isNotEmpty()){
            map.putString("exceptionMessage",exceptionMessage)
        }

        responsePromise!!.resolve(map)
        responsePromise = null
    }

    private fun logPrint(value: String?) {
        if (value == null) {
            return
        }
        Log.i("InstantpayRdService*", value)
    }
}
