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
    func makeUIViewController(context: Context) -> some UIViewController {
        MainKt.MainUiViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .preferredColorScheme(.dark) // there is no light theme YET or ever
    }
}
