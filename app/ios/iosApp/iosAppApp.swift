import SwiftUI
import shared
import FirebaseCore
import Foundation
import AuthenticationServices

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()

    return true
  }
}

@main
struct TonbrettApp: App {
    @State private var loadApp: Bool = true
    @State private var token: String? = nil
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            VStack {
                ProgressView()
            }.fullScreenCover(isPresented: $loadApp, content: {
                ContentView(receivedToken: token, onAuth: onAuth)
            })
        }
    }
    
    func onAuth(token: String) async {
        loadApp = false
        self.token = token
        try? await Task.sleep(nanoseconds: 1_000_000_000)
        loadApp = true
    }
}
