import SwiftUI
import shared
import SafariServices
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
    @State private var isShowingLoginView = AuthenticationKt.getTokenOrNull() == nil
    @State private var renderApp = AuthenticationKt.getTokenOrNull() != nil
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            VStack {
                Button("Retry") {
                    isShowingLoginView = false
                    isShowingLoginView = true
                }
            }
            .fullScreenCover(isPresented: $isShowingLoginView) {
                SafariView(url: URL(string: AuthenticationKt.getAuthUrl())!)
                    .ignoresSafeArea()
            }
            .fullScreenCover(isPresented: $renderApp, content: {
                ComposeView()
            })
            .onOpenURL { incomingUrl in
                if(incomingUrl.scheme == "tonbrett" && incomingUrl.host == "login") {
                    let token = incomingUrl.queryParameters!["token"]!
                    AuthenticationKt.setToken(token: token)
                    isShowingLoginView = false
                    renderApp = true
                }
            }
        }
    }
    
    func navigateBackToAuthorizationView() {
        renderApp = false
        isShowingLoginView = true
    }
}

struct SafariView: UIViewControllerRepresentable {
    let url: URL
    
    func makeUIViewController(context: Context) -> SFSafariViewController {
        let controller = SFSafariViewController(url: url)
        controller.modalPresentationCapturesStatusBarAppearance = true
        return controller
    }
    
    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {
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
