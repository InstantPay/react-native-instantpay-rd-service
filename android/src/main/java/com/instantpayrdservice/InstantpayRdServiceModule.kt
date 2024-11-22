package com.instantpayrdservice


import android.app.Activity
import android.content.Intent
import android.util.Base64
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import java.security.MessageDigest
import kotlin.random.Random


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
        const val RDFACECAPTURE_CODE = 3
        const val RDEYECAPTURE_CODE = 4
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

            if(requestCode == RDINFO_CODE){
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

            if(requestCode == RDCAPTURE_CODE){

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

            if(requestCode == RDFACECAPTURE_CODE){
                /*val resultData = StringBuilder("Request Code: $requestCode, Result Code: $resultCode\n")
                data.extras?.let { bundle ->
                    for (key in bundle.keySet()) {
                        val value = bundle.get(key)
                        logPrint("Key: ${key}, Value: $value")
                        resultData.append("Key: $key, Value: $value; ")
                    }
                }*/

                val captureXML = data.getStringExtra("response")

                val sanitizeXml = parseBioMetricData(captureXML!!)

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

    @ReactMethod
    fun openFaceAuth(pidOption: String, prm: Promise){
        try {
            responsePromise = prm

            //pidOptionParam = pidOption

            val activity = currentActivity ?: return resolve("Activity doesn't exist "+"#CPDRD4")

            val CAPTURE_INTENT = "in.gov.uidai.rdservice.face.CAPTURE"

            val CAPTURE_INTENT_REQUEST = "request"

            val intent = Intent(CAPTURE_INTENT)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            //intent.putExtra(CAPTURE_INTENT_REQUEST, createFacePidOptionForAuth(getRandomNumber(), "P"))

            //intent.putExtra(CAPTURE_INTENT_REQUEST,createFacePidOptionForKUA(getRandomNumber(), "P"))

            intent.putExtra(CAPTURE_INTENT_REQUEST, pidOption)

            activity.startActivityForResult(intent, RDFACECAPTURE_CODE)
        }
        catch (e: Exception) {
            resolve("Face Auth services not available, Please install AadhaarFaceRD from play store." , FAILED, "", "", e.message.toString()+" #GFPRD2")
        }
    }

    private fun getRandomNumber(): String {

        val start = 10000000

        val end = 99999999

        val number = Random(System.nanoTime()).nextInt(end - start + 1) + start

        return number.toString()
    }

    fun createFacePidOptionForAuth(txnId: String, buildType:String): String {
        return createFacePidOptions(txnId, "auth", buildType)
    }

    private fun createFacePidOptions(txnId: String, purpose: String, buildType:String): String {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +

                "<PidOptions ver=\"1.0\" env=\"${buildType}\">\n" +

                "   <Opts fCount=\"\" fType=\"\" iCount=\"\" iType=\"\" pCount=\"\" pType=\"\" format=\"\" pidVer=\"2.0\" timeout=\"\" otp=\"\"  posh=\"\" />\n" +

                "   <CustOpts>\n" +

                "      <Param name=\"txnId\" value=\"${txnId}\"/>\n" +

                "      <Param name=\"purpose\" value=\"$purpose\"/>\n" +

                "      <Param name=\"language\" value=\"N\"/>\n" +

                "   </CustOpts>\n" +

                "</PidOptions>"

    }

    fun createFacePidOptionForKUA(txnId: String, buildType:String): String {
        return createFacePidOptionsKUA(txnId, "auth", getWADHForFace(), buildType)
    }

    fun getWADHForFace(): String {
        val VERSION = "2.5"
        val RESIDENT_AUTHENTICATION_TYPE = "P"
        val RESIDENT_CONSENT = "Y"
        var LOCAL_LANGUAGE_IR = "N"
        val DECRYPTION = "N"
        val PRINT_FORMAT="N"
        return getWADHValueForFace(VERSION + RESIDENT_AUTHENTICATION_TYPE + RESIDENT_CONSENT + LOCAL_LANGUAGE_IR + DECRYPTION + PRINT_FORMAT)
    }

    private fun getWADHValueForFace(plainWADH: String): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(plainWADH.toByteArray())
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun createFacePidOptionsKUA(txnId: String, purpose: String, wadh:String, buildType:String): String {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +

                "<PidOptions ver=\"1.0\" env=\"${buildType}\">\n" +

                "   <Opts fCount=\"\" fType=\"\" iCount=\"\" iType=\"\" pCount=\"\" pType=\"\" format=\"\" pidVer=\"2.0\" timeout=\"\" otp=\"\" wadh=\"${wadh}\" posh=\"\" />\n" +

                "   <CustOpts>\n" +

                "      <Param name=\"txnId\" value=\"${txnId}\"/>\n" +

                "      <Param name=\"purpose\" value=\"$purpose\"/>\n" +

                "      <Param name=\"language\" value=\"IR\"/>\n" +

                "   </CustOpts>\n" +

                "</PidOptions>"
    }

    @ReactMethod
    fun openEyeAuth(pidOption: String, prm: Promise){
        try {
            responsePromise = prm

            val activity = currentActivity ?: return resolve("Activity doesn't exist "+"#CPDRD5")

            //Intent intent = new Intent("in.gov.uidai.rdservice.iris.INFO");

            val CAPTURE_INTENT = "in.gov.uidai.rdservice.iris.CAPTURE"

            val CAPTURE_INTENT_REQUEST = "PID_OPTIONS"

            val intent = Intent(CAPTURE_INTENT)
            //intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

            intent.putExtra(CAPTURE_INTENT_REQUEST, pidOption)

            activity.startActivityForResult(intent, RDEYECAPTURE_CODE)
        }
        catch (e: Exception) {
            resolve("Iris Eye Auth services not available" , FAILED, "", "", e.message.toString()+" #OEARD1")
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
