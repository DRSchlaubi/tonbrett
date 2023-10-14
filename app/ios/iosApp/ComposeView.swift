//
//  ComposeView.swift
//  iosApp
//
//  Created by Michael Rittmeister on 21.05.23.
//

import UIKit
import SwiftUI
import shared

struct ComposeView: UIViewControllerRepresentable {
    let receivedToken: String?
    
    func makeUIViewController(context: Context) -> some UIViewController {
        MainKt.MainUiViewController(receivedToken: receivedToken)
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}

struct ContentView: View {
    let receivedToken: String?
    var body: some View {
        ComposeView(receivedToken: receivedToken)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .preferredColorScheme(.dark) // there is no light theme YET or ever
    }
}
