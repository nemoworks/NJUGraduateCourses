//
//  Recorder.swift
//  zc-gesture
//
//  Created by 王国畅 on 2020/6/19.
//  Copyright © 2020 王国畅. All rights reserved.
//

import AVFoundation
import UIKit

var recv: [Float] = []
extension String {
    func count(of needle: Character) -> Int {
        return reduce(0) {
            $1 == needle ? $0 + 1 : $0
        }
    }
}

class AudioRecorder{
    private var audioEngine: AVAudioEngine!
    private var mic: AVAudioInputNode!
    private var micTapped = false
    
    init(){
        configureAudioSession()
        audioEngine = AVAudioEngine()
        mic = audioEngine.inputNode
    }
    
    func toggleMicTap(){
        if micTapped {
            mic.removeTap(onBus: 0)
            micTapped = false
            return
        }

        let micFormat = mic.inputFormat(forBus: 0)
        var handled = false
        mic.installTap(onBus: 0, bufferSize: 25600, format: micFormat) { (buffer, when) in
            if handled==false{
                recv = []
//                let recvPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("recv.txt")
                let sampleData = UnsafeBufferPointer(start: buffer.floatChannelData![0], count: Int(buffer.frameLength))
                for element in sampleData{
                    recv.append(element)
                }
//                try? recv.write(to: recvPath, atomically: true, encoding: String.Encoding.utf8)
                handled=true
            }
        }
        micTapped = true
        startEngine()
    }
    
    func playAudioFile() {
        let playerNode = AVAudioPlayerNode()

        let audioUrl = Bundle.main.url(forResource: "zc", withExtension: "wav")!
        let audioFile = readableAudioFileFrom(url: audioUrl)
        let audioFormat = audioFile.processingFormat
        let audioFrameCount = UInt32(audioFile.length)
        guard let audioFileBuffer = AVAudioPCMBuffer(pcmFormat: audioFormat, frameCapacity: audioFrameCount)  else{ return }
        do{
            try audioFile.read(into: audioFileBuffer)
        } catch{
            print("over")
        }
        audioEngine.attach(playerNode)
        audioEngine.connect(playerNode, to: audioEngine.outputNode, format: audioFile.processingFormat)
        startEngine()

//        playerNode.installTap(onBus: 0, bufferSize: 4096, format: playerNode.outputFormat(forBus: 0)) { (buffer, when) in
//            let _ = UnsafeBufferPointer(start: buffer.floatChannelData![0], count: Int(buffer.frameLength))
//        }
        playerNode.play()
        playerNode.scheduleBuffer(audioFileBuffer, at: nil, options:AVAudioPlayerNodeBufferOptions.loops)
    }
    
    func stopAndCollect(){
        audioEngine.stop()
        audioEngine.reset()
        audioEngine.prepare()
        guard let uploadData = try? JSONEncoder().encode(recv) else {
            return
        }
        let url = URL(string: "http://192.168.31.65:5000/"+currentDataset+"/"+String(currentLabel))!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let task = URLSession.shared.uploadTask(with: request, from: uploadData) { data, response, error in
            if let error = error {
                print ("error: \(error)")
                return
            }
            guard let response = response as? HTTPURLResponse,
                (200...299).contains(response.statusCode) else {
                print ("server error")
                return
            }
            if let mimeType = response.mimeType,
                mimeType == "application/json",
                let data = data,
                let dataString = String(data: data, encoding: .utf8) {
                print ("got data: \(dataString)")
            }
        }
        task.resume()
    }
    
    func stopAndValidate(){
        audioEngine.stop()
        audioEngine.reset()
        audioEngine.prepare()
        guard let uploadData = try? JSONEncoder().encode(recv) else {
            return
        }
        let url = URL(string: "http://192.168.31.65:5000/detect")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        let task = URLSession.shared.uploadTask(with: request, from: uploadData) { data, response, error in
            if let error = error {
                print ("error: \(error)")
                return
            }
            guard let response = response as? HTTPURLResponse,
                (200...299).contains(response.statusCode) else {
                print ("server error")
                return
            }
            if let mimeType = response.mimeType,
                mimeType == "application/json",
                let data = data,
                let dataString = String(data: data, encoding: .utf8) {
                print ("got data: \(dataString)")
                Label=dataString
            }
        }
        task.resume()
        return
    }
    
    func stop(){
        audioEngine.stop()
        audioEngine.reset()
        audioEngine.prepare()
    }
    
    
    // MARK: Internal Methods

    private func configureAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playAndRecord, options: [.mixWithOthers, .defaultToSpeaker])
            try AVAudioSession.sharedInstance().setActive(true)
        } catch { }
    }

    private func readableAudioFileFrom(url: URL) -> AVAudioFile {
        var audioFile: AVAudioFile!
        do {
            try audioFile = AVAudioFile(forReading: url)
        } catch { }
        return audioFile
    }
    
    func start(){
        playAudioFile()
        toggleMicTap()
    }

    private func startEngine() {
        guard !audioEngine.isRunning else {
            return
        }

        do {
            try audioEngine.start()
        } catch { }
    }

    private func stopAudioPlayback() {
        audioEngine.stop()
        audioEngine.reset()
    }
}
