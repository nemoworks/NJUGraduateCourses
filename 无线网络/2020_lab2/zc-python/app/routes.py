from app import app
from numpy.fft import fft, ifft, fftshift, ifftshift, irfft
import os
from flask import Flask, flash, request, redirect, url_for, Response, jsonify, make_response
from werkzeug.utils import secure_filename
from io import StringIO
from scipy.io.wavfile import write
import numpy as np
import torch
import torch.nn as nn
import torchaudio
from functools import partial
import time

from torchvision.datasets import DatasetFolder
use_gpu = torch.cuda.is_available()

if use_gpu:
    device = torch.device("cuda:0")

def audio_loader(path, max_length_in_seconds, pad_and_truncate):
    audio_tensor, sample_rate = torchaudio.load(path, normalization=True)
    max_length = sample_rate * max_length_in_seconds
    audio_size = audio_tensor.size()

    if pad_and_truncate:
        if audio_size[1] < max_length:
            difference = max_length - audio_size[1]
            padding = torch.zeros(audio_size[0], difference)
            padded_audio = torch.cat([audio_tensor, padding], 1)
            return padded_audio

        if audio_size[1] > max_length:
            random_idx = random.randint(0, audio_size[1] - max_length)
            return audio_tensor.narrow(1, random_idx, max_length)

    return audio_tensor

def get_audio_dataset(datafolder, max_length_in_seconds=1, pad_and_truncate=False):
    loader_func = partial(
        audio_loader,
        max_length_in_seconds=max_length_in_seconds,
        pad_and_truncate=pad_and_truncate,
    )
    dataset = DatasetFolder(datafolder, loader_func, ".wav")

    return dataset


class AudioCNN(nn.Module):
    def __init__(self, num_classes):
        super(AudioCNN, self).__init__()

        self.main = nn.Sequential(
            nn.Conv1d(1, 64, 80, 4, 2),
            nn.BatchNorm1d(64),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(64, 128, 80, 4, 2),
            nn.BatchNorm1d(128),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(128, 256, 80, 4, 2),
            nn.BatchNorm1d(256),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(256, 512, 80, 4, 2),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(0.2, inplace=True),
            nn.Conv1d(512, 512, 40, 4, 2),
            nn.BatchNorm1d(512),
            nn.LeakyReLU(0.2, inplace=True),
        )
        self.classifier = nn.Sequential(nn.Linear(14848, num_classes), nn.Softmax())

    def forward(self, tensor):
        batch_size = tensor.size(0)
        hidden = self.main(tensor)
        # print(hidden.size())
        hidden = hidden.view(batch_size, -1)
        # print(hidden.size())
        return self.classifier(hidden)

audio_cnn = AudioCNN(3)
audio_cnn.load_state_dict(torch.load('model.pth'))
audio_cnn.eval()

def GenerateZC(Nzc=127, u=1, cf=0, q=0, fs=44100, f0=18750, length=1024):
    # get the zc sequence in time-domain
    n = np.arange(0,Nzc)
    zc = np.exp(1j*(np.pi*u*n*(n+cf+2*q)/Nzc))
    # get the fft(zc) and use fftshift to place zero-frequency point in the middle
    ZC = fft(zc)
    ZCs = fftshift(ZC)

    # move ZCs to the f0, 0-frequency point is on ZCs[int(Nzc/2)], 
    # we should move the int(Nzc/2) to k = 1920 * f0 / fs = 640
    ZCf = np.zeros(length, dtype=complex)
    ZCf[int(length*f0/fs-(Nzc-1)/2):int(length*f0/fs+(Nzc+1)/2)] = ZCs[0:Nzc]
    # the ZCf is not processed by fftshift here

    # conjugate processing of the ZCf to make the ifft signal is a real signal
    ZCfs = fftshift(ZCf)
    for i in range(int(length/2)+1, length):
        ZCfs[length - i] = np.conjugate(ZCfs[i])

    # ifft
    ZCfs = fftshift(ZCfs)
    ZCt = ifft(ZCfs)
    return ZCt

def ZCProcess(recv):
    Nzc = 127
    length = 1024
    f0 = 18750
    fs = 44100
    s1 = GenerateZC(f0=f0,length=length)
    roww = int(len(recv)/length)
    footprint = np.zeros((roww+1,length),dtype=complex)
    for i in range(0,roww):
        a = recv[length*i:length*(i+1)]
        af = fft(a)
        ag = af[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        s1f = fft(s1)
        s1g = s1f[int(length/(fs/f0)-(Nzc-1)/2):int(length/(fs/f0)+(Nzc+1)/2)]
        as1g = ag * np.conjugate(s1g)
        as1gf = np.zeros(length,dtype=complex)
        as1gf[int(length/2-(Nzc-1)/2):int(length/2+(Nzc+1)/2)] = as1g
        as1gf = fftshift(as1gf)
        as1gft = ifft(as1gf)
        footprint[i] = as1gft
    footprint[roww] = footprint[roww - 1]
    for i in range(0,roww):
        footprint[i] = footprint[i] - footprint[i+1]
    footprint = footprint[0:roww]
    return footprint.flatten()

def Normalize(vec):
    maxv = 0 
    for i in vec:
        tmp = abs(i)
        if tmp>maxv:
            maxv =tmp

    normalized = np.true_divide(vec,maxv) 
    return normalized

@app.route('/')
@app.route('/index')
def index():
    return "Hello, World!"

@app.route('/Train/<label>', methods=['POST'])
def upload_train_file(label):
    print("processing...")
    content = request.json
    raw = np.array(content)
    # try:
    #     raw = np.loadtxt(content,delimiter='|')
    # except Exception:
    #     print(Exception)
    #     print("Collection Failed")
    #     return Response(status=500)
    data = np.zeros(int(len(raw)/2))
    for i in range(0,len(data)):
        data[i] = raw[2*i]
    processed = ZCProcess(data)
    samplerate = 44100
    amplitude = np.iinfo(np.int16).max
    data = Normalize(processed.real) * amplitude
    setdir = os.path.join(app.config['UPLOAD_FOLDER'], 'Train')
    labeldir = os.path.join(setdir,label)
    idx = len(os.listdir(labeldir))
    filepath = os.path.join(labeldir,str(idx)+".wav")
    write(filepath, samplerate, data)
    print("Collection succeeded, current samples: "+str(idx))
    print("Data length: "+str(len(data)))
    return Response(status=200)

@app.route('/Val/<label>', methods=['POST'])
def upload_val_file(label):
    print("processing...")
    content = request.json
    raw = np.array(content)
    # try:
    #     raw = np.loadtxt(content,delimiter='|')
    # except Exception:
    #     print(Exception)
    #     print("Collection Failed")
    #     return Response(status=500)
    data = np.zeros(int(len(raw)/2))
    for i in range(0,len(data)):
        data[i] = raw[2*i]
    processed = ZCProcess(data)
    samplerate = 44100
    amplitude = np.iinfo(np.int16).max
    data = Normalize(processed.real) * amplitude
    setdir = os.path.join(app.config['UPLOAD_FOLDER'], 'Val')
    labeldir = os.path.join(setdir,label)
    idx = len(os.listdir(labeldir))
    filepath = os.path.join(labeldir,str(idx)+".wav")
    write(filepath, samplerate, data)
    print("Collection succeeded, current samples: "+str(idx))
    print("Data length: "+str(len(data)))
    return Response(status=200)


@app.route('/detect',methods=['POST'])
def detect_audio():
    since = time.time()
    content = request.json
    raw = np.array(content)
    data = np.zeros(int(len(raw)/2))
    for i in range(0,len(data)):
        data[i] = raw[2*i]
    processed = ZCProcess(data)
    samplerate = 44100
    amplitude = np.iinfo(np.int16).max
    data = Normalize(processed.real) * amplitude
    write('fakeset/0/temp.wav', samplerate, data)
    valset = get_audio_dataset('fakeset',max_length_in_seconds=1,pad_and_truncate=True)
    val_dataloader = torch.utils.data.DataLoader(valset, batch_size=5, num_workers=4)
    with torch.no_grad():
        for sample_idx, (audio, target) in enumerate(val_dataloader):
            if use_gpu:
                audio, target = audio.to(device), target.to(device)

            output = audio_cnn(audio)

            _, predicted = torch.max(output.data, 1)
    result = predicted[0].item()
    time_elapsed = time.time() - since
    print(time_elapsed)
    if result==0:
        return make_response(jsonify("up"),200)
    elif result==1:
        return make_response(jsonify("down"),200)
    else:
        return make_response(jsonify("fist"),200)