//
//  DetectView.swift
//  zc-gesture
//
//  Created by 王国畅 on 2020/6/27.
//  Copyright © 2020 王国畅. All rights reserved.
//

import SwiftUI

var Label:String = "up"

struct DetectView: View {
    var body: some View {
        VStack {
            CircleImage(image: Label)
               .frame(width: 400.0, height: 400.0)
               .offset(y: 0)
               .padding(.top, 0)
            Text("Result")
                .font(.title)
                .multilineTextAlignment(.leading)
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
                    audioRecorder.stopAndValidate()
                }) {
                    Image("pause")
                        
                }
            }.padding(50.0)
        }
    }
}

struct DetectView_Previews: PreviewProvider {
    static var previews: some View {
        DetectView()
    }
}
