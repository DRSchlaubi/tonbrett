import SwiftUI
import shared
import FirebaseCore

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
                ComposeView(receivedToken: token)
            }).onOpenURL(perform: { incomingUrl in
                if(incomingUrl.scheme == "tonbrett" && incomingUrl.host == "login") {
                    let token = incomingUrl.queryParameters!["token"]!
                    self.loadApp = false
                    self.token = token
                    Task {
                        try? await Task.sleep(nanoseconds: 1_000_000_000)
                        loadApp = true
                    }
                }
            })
        }
    }
}




extension URL {
    public var queryParameters: [String: String]? {
        guard
            let components = URLComponents(url: self, resolvingAgainstBaseURL: true),
            let queryItems = components.queryItems else { return nil }
        return queryItems.reduce(into: [String: String]()) { (result, item) in
            result[item.name] = item.value
        }
    }
}
