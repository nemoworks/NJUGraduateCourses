//
//  CircleImage.swift
//  zc-gesture
//
//  Created by 王国畅 on 2020/6/17.
//  Copyright © 2020 王国畅. All rights reserved.
//

import SwiftUI

struct CircleImage: View {
    let image:String
    var body: some View {
        Image(image).resizable()
            .padding()
            .clipShape(Circle())
            .overlay(
                Circle().stroke(Color.white, lineWidth: 4))
            .shadow(radius: 10)
    }
}

struct CircleImage_Previews: PreviewProvider {
    static var previews: some View {
        CircleImage(image: "palm")
    }
}
