//
//  ContentView.swift
//  zc-gesture
//
//  Created by 王国畅 on 2020/6/17.
//  Copyright © 2020 王国畅. All rights reserved.
//

import SwiftUI
var currentLabel = 0
var currentDataset = "Train"

extension Binding {
    func onChange(_ handler: @escaping (Value) -> Void) -> Binding<Value> {
        return Binding(
            get: { self.wrappedValue },
            set: { selection in
                self.wrappedValue = selection
                handler(selection)
        })
    }
}

struct ContentView: View {
    var body: some View {
        TabView {
            CollectView()
                .tabItem {
                    Image(systemName: "1.square.fill")
                    Text("Collect")
                }
            DetectView()
                .tabItem {
                    Image(systemName: "2.square.fill")
                    Text("Detect")
                }
        }
        .font(.headline)
    }
}


struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
