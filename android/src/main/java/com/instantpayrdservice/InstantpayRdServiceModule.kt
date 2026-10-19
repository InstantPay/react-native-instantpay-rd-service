package com.instantpayrdservice

import com.facebook.react.bridge.ReactApplicationContext

class InstantpayRdServiceModule(reactContext: ReactApplicationContext) :
  NativeInstantpayRdServiceSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeInstantpayRdServiceSpec.NAME
  }
}
