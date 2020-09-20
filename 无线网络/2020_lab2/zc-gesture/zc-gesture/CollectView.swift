//
//  CollectView.swift
//  zc-gesture
//
//  Created by 王国畅 on 2020/6/27.
//  Copyright © 2020 王国畅. All rights reserved.
//

import SwiftUI

struct CollectView: View{
    @State var selectedLabel = 0
    @State var train:Bool = true
    func labelSelected(_ tag: Int){
        currentLabel = tag
        print(currentLabel)
    }
    
    func toggleAction(state: String) -> String {
        currentDataset = state
        print(currentDataset)
        return state
    }
    var body: some View{
        VStack {
           
            CircleImage(image: "palm")
               .frame(width: 400.0, height: 400.0)
               .offset(y: 0)
               .padding(.top, 0)
           
           VStack(alignment: .leading) {
               HStack {
                   Text("Dataset")
                       .font(.title)
                       .multilineTextAlignment(.leading)
                   Toggle(isOn: self.$train) {
                       if (self.train) {
                           Text("\(self.toggleAction(state: "Train"))").font(.title)
                       } else {
                           Text("\(self.toggleAction(state: "Val"))").font(.title)
                       }
                   }
               }
               VStack{
                   Picker("Label",selection: $selectedLabel.onChange(labelSelected)) {
                       Text("scroll up").tag(0)
                       Text("scroll down").tag(1)
                       Text("double click").tag(2)
                   }
                   .padding()
                   .frame(width: 200.0, height: 200.0)
                   
                   HStack {
                       Button(action: {
   //                        audioPlayer.play()
   //                        audioRecorder.toggleMicTap()
                           audioRecorder.start()
                       }) {
                           Image("start")
                       }
                       
                       Spacer()
                       
                       Button(action: {
   //                        audioPlayer.stop()
                           audioRecorder.stopAndCollect()
                       }) {
                           Image("pause")
                               
                       }
                   }
               }
           }
           .padding(50.0)
       }
       .frame(width: 400.0, height: 400.0)
           }
}

struct CollectView_Previews: PreviewProvider {
    static var previews: some View {
        CollectView()
    }
}
