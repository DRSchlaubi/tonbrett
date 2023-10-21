//
//  ComposeView.swift
//  iosApp
//
//  Created by Michael Rittmeister on 21.05.23.
//

import UIKit
import SwiftUI
import shared
import AuthenticationServices
import Foundation

struct ComposeView: UIViewControllerRepresentable {
    let receivedToken: String?
    let onAuth: (String) -> Void
    
    func makeUIViewController(context: Context) -> some UIViewController {
        MainKt.MainUiViewController(receivedToken: receivedToken, onAuth: onAuth)
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}

struct ContentView: View {
    let receivedToken: String?
    let onAuth: (String) async -> Void
    @Environment(\.webAuthenticationSession) private var webAuthenticationSession
        
    var body: some View {
        ComposeView(receivedToken: receivedToken, onAuth: signIn)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .preferredColorScheme(.dark) // there is no light theme YET or ever
    }
    
    func signIn(url: String) {
        Task {
            do {
                let urlWithToken = try await webAuthenticationSession.authenticate(
                    using: URL(string: url)!,
                    callbackURLScheme: "tonbrett",
                    preferredBrowserSession: nil
                )
                
                let token = urlWithToken.queryParameters!["token"]!
                
                await onAuth(token)
            } catch {
                print("Unexpected error: \(error).")
            }
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
