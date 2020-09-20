import numpy as np 
import matplotlib.pyplot as plt
from numpy.fft import fft, ifft, fftshift, ifftshift, irfft
from scipy.io.wavfile import write

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
    print(irfft(ZCfs))
    return ZCt

def Normalize(vec):
    maxv = 0 
    for i in vec:
        tmp = abs(i)
        if tmp>maxv:
            maxv =tmp

    normalized = np.true_divide(vec,maxv) 
    return normalized

ZCt = GenerateZC()
zc = ZCt + ZCt
nzc = Normalize(zc.real)
samplerate = 48000
amplitude = np.iinfo(np.int16).max
data = nzc * amplitude
np.savetxt('zc.txt',nzc)
write("zc.wav", samplerate, data)
