
import UIKit
import React
import React_RCTAppDelegate

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard
            let windowScene = scene as? UIWindowScene,
            let appDelegate = UIApplication.shared.delegate as? AppDelegate,
            let factory = appDelegate.reactNativeFactory
        else {
            assertionFailure(
                "Unable to initialize the React Native scene."
            )
            return
        }

        let window = UIWindow(windowScene: windowScene)

        self.window = window
        appDelegate.window = window

        factory.startReactNative(
            withModuleName: "InstantpayRdServiceExample",
            in: window,
            launchOptions: nil
        )
    }
  
  // 2. Fixes WARM starts (Fires when the app is in the background and returns via Link)
  /*func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
          // 1. Safely extract the first URL context item
          guard let urlContext = URLContexts.first else { return }
          
          let url = urlContext.url
          
          // 2. Prepare options dictionary manually for React Native
          var options: [UIApplication.OpenURLOptionsKey: Any] = [:]
          if let sourceApplication = urlContext.options.sourceApplication {
              options[.sourceApplication] = sourceApplication
          }
          
          // 3. Pass it to the standard RCTLinkingManager application method
          RCTLinkingManager.application(
              UIApplication.shared,
              open: url,
              options: options
          )
      }*/
}
